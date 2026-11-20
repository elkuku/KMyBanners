package dev.elkuku.kmybanners.bannergress

import dev.elkuku.kmybanners.Store
import dev.elkuku.kmybanners.httpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BannergressTest {
    private class MapStore : Store {
        val m = mutableMapOf<String, String>()
        override fun get(key: String) = m[key]
        override fun put(key: String, value: String?) { if (value == null) m.remove(key) else m[key] = value }
    }

    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun redirectGivesTheCodeOnlyForThisSignIn() {
        val login = BannergressLogin("u", "verifier", "s1")
        assertEquals("c1", login.code("https://bannergress.com/?state=s1&session_state=x&code=c1"))
        assertNull(login.code("https://bannergress.com/?state=other&code=c1"))
        assertNull(login.code("https://bannergress.com/?error=access_denied&state=s1"))
        assertNull(login.code("https://login.bannergress.com/?state=s1&code=c1"))
    }

    @Test
    fun finishKeepsTheRefreshTokenAndRefusalSignsOut() = runTest {
        val forms = mutableListOf<String>()
        var refuse = false
        val store = MapStore()
        val auth = BannergressAuth(httpClient(MockEngine { req ->
            forms += (req.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
            if (refuse) respond("""{"error":"invalid_grant"}""", HttpStatusCode.BadRequest, json)
            else respond("""{"access_token":"a1","refresh_token":"r1","expires_in":300}""", HttpStatusCode.OK, json)
        }), store, now = { 0 })

        val login = auth.start()
        assertTrue(login.url.contains("code_challenge_method=S256"))
        assertTrue(auth.finish(login, "c1"))
        assertTrue(forms.last().contains("code_verifier=${login.verifier}"))
        assertEquals("r1", store.get("bannergress_refresh"))
        assertEquals("a1", auth.accessToken()) // still valid: no refresh

        refuse = true
        assertFalse(auth.finish(login, "c2"))
        assertFalse(auth.signedIn)
    }

    @Test
    fun doneListReadsAllPages() = runTest {
        val offsets = mutableListOf<String?>()
        val api = BannergressApi(httpClient(MockEngine { req ->
            offsets += req.url.parameters["offset"]
            assertEquals("done", req.url.parameters["listTypes"])
            assertEquals("listAdded", req.url.parameters["orderBy"])
            val n = if (req.url.parameters["offset"] == "0") 100 else 3
            val page = (0 until n).joinToString(",", "[", "]") { """{"id":"b$it","title":"T","numberOfMissions":6}""" }
            respond(page, HttpStatusCode.OK, json)
        })) { "token" }

        val done = api.doneList()
        assertEquals(103, done.size)
        assertEquals(listOf("0", "100"), offsets)
        assertEquals(6, done.first().numberOfMissions)
    }
}
