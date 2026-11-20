package dev.elkuku.kmybanners.github

import dev.elkuku.kmybanners.httpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GitHubTest {
    private val requests = mutableListOf<Pair<String, String>>() // "METHOD url" to body

    private fun client(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        httpClient(MockEngine { req ->
            val body = (req.body as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString() ?: ""
            requests += "${req.method.value} ${req.url}" to body
            handler(req)
        })

    private fun MockRequestHandleScope.json(s: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(s, status, headersOf(HttpHeaders.ContentType, "application/json"))

    @Test
    fun deviceFlowWaitsUntilApproved() = runTest {
        var polls = 0
        val auth = GitHubAuth(client { req ->
            when (req.url.encodedPath) {
                "/login/device/code" -> json("""{"device_code":"dc","user_code":"ABCD-1234",
                    "verification_uri":"https://github.com/login/device","expires_in":900,"interval":5}""")
                else -> if (++polls < 3) json("""{"error":"authorization_pending"}""")
                else json("""{"access_token":"gho_1","refresh_token":"ghr_1","expires_in":28800}""")
            }
        }, "client1", now = { testScheduler.currentTime })

        val code = auth.start()
        assertEquals("ABCD-1234", code.userCode)
        val t = auth.await(code)!!
        assertEquals("gho_1", t.access)
        assertEquals("ghr_1", t.refresh)
        assertEquals(3, polls)
        val (_, body) = requests.last()
        assertEquals(true, body.contains("grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Adevice_code"))
        assertEquals(true, requests.first().second.contains("scope=public_repo"))
    }

    @Test
    fun networkErrorWhileWaitingIsRetried() = runTest {
        var polls = 0
        val auth = GitHubAuth(client {
            if (++polls == 2) throw java.net.UnknownHostException("github.com")
            if (polls < 3) json("""{"error":"authorization_pending"}""")
            else json("""{"access_token":"gho_1","expires_in":28800}""")
        }, "c", now = { testScheduler.currentTime })
        assertEquals("gho_1", auth.await(DeviceCode("dc", "X", "u", 900, 5))?.access)
        assertEquals(3, polls)
    }

    @Test
    fun deniedSignInGivesNull() = runTest {
        val auth = GitHubAuth(client { json("""{"error":"access_denied"}""") }, "c", now = { testScheduler.currentTime })
        assertNull(auth.await(DeviceCode("dc", "X", "u", 900, 5)))
    }

    @Test
    fun refreshUsesTheRefreshToken() = runTest {
        val auth = GitHubAuth(client { json("""{"access_token":"gho_2","refresh_token":"ghr_2","expires_in":28800}""") }, "c")
        val t = auth.refresh(Tokens("gho_1", "ghr_1", 0))!!
        assertEquals("gho_2", t.access)
        assertEquals(true, requests.single().second.contains("grant_type=refresh_token"))
        assertNull(auth.refresh(Tokens("gho_1", null, 0)))
    }

    @Test
    fun commitPutsAllFilesInOneCommit() = runTest {
        var blobs = 0
        val api = GitHubApi(client { req ->
            val p = req.url.encodedPath
            when {
                p.endsWith("/git/ref/heads/main") -> json("""{"object":{"sha":"c0"}}""")
                p.endsWith("/git/commits/c0") -> json("""{"sha":"c0","tree":{"sha":"t0"}}""")
                p.endsWith("/git/blobs") -> json("""{"sha":"b${blobs++}"}""", HttpStatusCode.Created)
                p.endsWith("/git/trees") -> json("""{"sha":"t1"}""", HttpStatusCode.Created)
                p.endsWith("/git/commits") -> json("""{"sha":"c1"}""", HttpStatusCode.Created)
                p.endsWith("/git/refs/heads/main") -> json("""{"object":{"sha":"c1"}}""")
                else -> json("{}", HttpStatusCode.NotFound)
            }
        }) { "gho_1" }

        val progress = mutableListOf<Int>()
        val sha = api.commit("agent", "ingress-banners",
            listOf(RepoFile("inbox/1/00.jpg", byteArrayOf(1, 2)), RepoFile("inbox/1/01.jpg", byteArrayOf(3))),
            "Add 2 screenshots") { progress += it }

        assertEquals("c1", sha)
        assertEquals(listOf(1, 2), progress)
        val tree = requests.single { it.first.endsWith("/git/trees") }.second
        assertEquals(true, tree.contains(""""base_tree":"t0""""))
        assertEquals(true, tree.contains(""""path":"inbox/1/01.jpg","mode":"100644","type":"blob""""))
        assertEquals(true, requests.single { it.first.endsWith("/git/commits") }.second.contains(""""parents":["c0"]"""))
        assertEquals("PATCH https://api.github.com/repos/agent/ingress-banners/git/refs/heads/main", requests.last().first)
    }

    @Test
    fun repoKnowsItsTemplate() = runTest {
        val api = GitHubApi(client { req ->
            if (req.url.encodedPath.endsWith("/ingress-banners")) json("""{"full_name":"a/ingress-banners","html_url":"h",
                "template_repository":{"full_name":"elkuku/KMyBanners-template"}}""")
            else json("""{"full_name":"a/other","html_url":"h"}""")
        }) { "t" }
        assertEquals(true, api.repo("a", "ingress-banners")!!.fromTemplate)
        assertEquals(false, api.repo("a", "other")!!.fromTemplate)
    }

    @Test
    fun missingRepoIsNull() = runTest {
        val api = GitHubApi(client { json("""{"message":"Not Found"}""", HttpStatusCode.NotFound) }) { "t" }
        assertNull(api.repo("agent", "ingress-banners"))
    }

    @Test
    fun ownReposWithTopicsOverSeveralPages() = runTest {
        val api = GitHubApi(client { req ->
            val page = req.url.parameters["page"]!!.toInt()
            val n = if (page == 1) 100 else 3
            json((1..n).joinToString(",", "[", "]") { k ->
                """{"full_name":"a/r$page-$k","html_url":"h","topics":${if (page == 2 && k == 2) "[\"kmybanners\"]" else "[]"}}"""
            })
        }) { "t" }
        val repos = api.ownRepos()
        assertEquals(103, repos.size)
        assertEquals(listOf("r2-2"), repos.filter { "kmybanners" in it.topics }.map { it.name })
        assertEquals(2, requests.size)
        assertEquals(true, requests[0].first.contains("/user/repos?type=owner&per_page=100&page=1"))
    }

    @Test
    fun renameAndTopics() = runTest {
        val api = GitHubApi(client { req ->
            if (req.method.value == "PATCH") json("""{"full_name":"a/my-banners","html_url":"h"}""")
            else json("""{"names":["kmybanners"]}""")
        }) { "t" }
        val r = api.rename("a", "ingress-banners", "my-banners")
        assertEquals("my-banners", r.name)
        assertEquals("PATCH https://api.github.com/repos/a/ingress-banners" to """{"name":"my-banners"}""", requests[0])
        api.setTopics("a", "my-banners", listOf("kmybanners"))
        assertEquals("PUT https://api.github.com/repos/a/my-banners/topics" to """{"names":["kmybanners"]}""", requests[1])
    }

    @Test(expected = GitHubException::class)
    fun renameToATakenNameFails() = runTest {
        val api = GitHubApi(client { json("""{"message":"Repository creation failed."}""", HttpStatusCode.UnprocessableEntity) }) { "t" }
        api.rename("a", "ingress-banners", "taken")
    }
}
