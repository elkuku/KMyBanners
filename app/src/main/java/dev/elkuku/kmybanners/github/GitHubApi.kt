package dev.elkuku.kmybanners.github

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Base64

@Serializable
data class User(val login: String)

@Serializable
data class Repo(
    @SerialName("full_name") val fullName: String,
    @SerialName("html_url") val htmlUrl: String,
    /** The template it was made from (GitHub keeps this, also after a rename). */
    @SerialName("template_repository") val template: TemplateRef? = null,
) {
    val fromTemplate: Boolean get() = template?.fullName == GitHubApi.TEMPLATE
}

@Serializable
data class TemplateRef(@SerialName("full_name") val fullName: String)

@Serializable
data class Pages(@SerialName("html_url") val htmlUrl: String? = null)

@Serializable
data class WorkflowRun(
    val id: Long,
    val status: String, // queued, in_progress, completed
    val conclusion: String? = null, // success, failure, …
    @SerialName("html_url") val htmlUrl: String,
)

/** A file for [GitHubApi.commit]: its path in the repo and its bytes. */
class RepoFile(val path: String, val bytes: ByteArray)

class GitHubException(val status: HttpStatusCode, message: String) : Exception(message)

/** The GitHub calls of the app (REST API, with the user's token). */
class GitHubApi(private val http: HttpClient, private val token: suspend () -> String) {

    suspend fun user(): User = send(HttpMethod.Get, "/user").ok().body()

    /** The repo, or null when it doesn't exist (or isn't visible). */
    suspend fun repo(owner: String, name: String): Repo? =
        send(HttpMethod.Get, "/repos/$owner/$name").orNull()?.body()

    /** A new public repo from the template; waits until its main branch exists (the copy takes a moment). */
    suspend fun createFromTemplate(owner: String, name: String, description: String): Repo {
        val repo: Repo = send(HttpMethod.Post, "/repos/$TEMPLATE/generate", Generate(owner, name, description)).ok().body()
        repeat(30) {
            if (send(HttpMethod.Get, "/repos/$owner/$name/git/ref/heads/main").status.isSuccess()) return repo
            delay(2000)
        }
        throw GitHubException(HttpStatusCode.GatewayTimeout, "The new repo has no main branch yet")
    }

    /** GitHub Pages built by Actions; fine when it is on already. */
    suspend fun enablePages(owner: String, name: String) {
        val r = send(HttpMethod.Post, "/repos/$owner/$name/pages", PagesSource("workflow"))
        if (r.status != HttpStatusCode.Conflict) r.ok()
    }

    suspend fun pages(owner: String, name: String): Pages? =
        send(HttpMethod.Get, "/repos/$owner/$name/pages").orNull()?.body()

    /**
     * The files in one commit on main (Git Data API: blobs, tree, commit, move the branch).
     * [progress] gets the number of files uploaded so far. Returns the new commit's sha.
     */
    suspend fun commit(owner: String, name: String, files: List<RepoFile>, message: String,
                       progress: (Int) -> Unit = {}): String {
        val repo = "/repos/$owner/$name/git"
        val head: Ref = send(HttpMethod.Get, "$repo/ref/heads/main").ok().body()
        val parent: Commit = send(HttpMethod.Get, "$repo/commits/${head.`object`.sha}").ok().body()
        val entries = files.mapIndexed { n, f ->
            val blob: Sha = send(HttpMethod.Post, "$repo/blobs",
                Blob(Base64.getEncoder().encodeToString(f.bytes), "base64")).ok().body()
            progress(n + 1)
            TreeEntry(f.path, sha = blob.sha)
        }
        val tree: Sha = send(HttpMethod.Post, "$repo/trees", NewTree(parent.tree.sha, entries)).ok().body()
        val commit: Sha = send(HttpMethod.Post, "$repo/commits",
            NewCommit(message, tree.sha, listOf(parent.sha))).ok().body()
        send(HttpMethod.Patch, "$repo/refs/heads/main", Sha(commit.sha)).ok()
        return commit.sha
    }

    /** A file's bytes on main, or null when there is no such file. */
    suspend fun file(owner: String, name: String, path: String): ByteArray? =
        send(HttpMethod.Get, "/repos/$owner/$name/contents/$path").orNull()?.body<Contents>()
            ?.let { Base64.getMimeDecoder().decode(it.content) }

    /** Starts the page workflow on main (as "Run workflow" on GitHub). Returns the newest such run before it. */
    suspend fun dispatch(owner: String, name: String, workflow: String): Long? {
        val before = latestDispatch(owner, name, workflow)?.id
        send(HttpMethod.Post, "/repos/$owner/$name/actions/workflows/$workflow/dispatches", Dispatch("main")).ok()
        return before
    }

    /** The newest run of [workflow] started by hand ("Run workflow" or [dispatch]). */
    suspend fun latestDispatch(owner: String, name: String, workflow: String): WorkflowRun? =
        send(HttpMethod.Get, "/repos/$owner/$name/actions/workflows/$workflow/runs?event=workflow_dispatch&per_page=1").ok()
            .body<Runs>().workflowRuns.firstOrNull()

    /** The newest run of [workflow], whatever started it (an upload, "Build again"). */
    suspend fun latestRun(owner: String, name: String, workflow: String): WorkflowRun? =
        send(HttpMethod.Get, "/repos/$owner/$name/actions/workflows/$workflow/runs?per_page=1").ok()
            .body<Runs>().workflowRuns.firstOrNull()

    suspend fun runById(owner: String, name: String, id: Long): WorkflowRun =
        send(HttpMethod.Get, "/repos/$owner/$name/actions/runs/$id").ok().body()

    /** The workflow run for a commit (null until GitHub has started it). */
    suspend fun run(owner: String, name: String, sha: String): WorkflowRun? =
        send(HttpMethod.Get, "/repos/$owner/$name/actions/runs?head_sha=$sha").ok()
            .body<Runs>().workflowRuns.firstOrNull()

    // --- plumbing ---

    private suspend fun send(method: HttpMethod, path: String): HttpResponse = http.request(API + path) {
        this.method = method
        bearerAuth(token())
        header("Accept", "application/vnd.github+json")
        header("X-GitHub-Api-Version", "2022-11-28")
    }

    private suspend inline fun <reified T : Any> send(method: HttpMethod, path: String, body: T): HttpResponse {
        val t = token()
        return http.request(API + path) {
            this.method = method
            bearerAuth(t)
            header("Accept", "application/vnd.github+json")
            header("X-GitHub-Api-Version", "2022-11-28")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    private suspend fun HttpResponse.ok(): HttpResponse {
        if (!status.isSuccess()) throw GitHubException(status, "GitHub ${status.value}: ${bodyAsText().take(300)}")
        return this
    }

    private suspend fun HttpResponse.orNull(): HttpResponse? = if (status == HttpStatusCode.NotFound) null else ok()

    @Serializable internal data class Contents(val content: String)
    @Serializable internal data class Dispatch(val ref: String)
    @Serializable internal data class Runs(@SerialName("workflow_runs") val workflowRuns: List<WorkflowRun> = emptyList())
    @Serializable internal data class Ref(val `object`: Sha)
    @Serializable internal data class Sha(val sha: String)
    @Serializable internal data class Commit(val sha: String, val tree: Sha)
    @Serializable internal data class TreeEntry(val path: String, val mode: String = "100644",
                                                val type: String = "blob", val sha: String)
    @Serializable internal data class Generate(val owner: String, val name: String, val description: String,
                                               val private: Boolean = false)
    @Serializable internal data class PagesSource(@SerialName("build_type") val buildType: String)
    @Serializable internal data class Blob(val content: String, val encoding: String)
    @Serializable internal data class NewTree(@SerialName("base_tree") val baseTree: String, val tree: List<TreeEntry>)
    @Serializable internal data class NewCommit(val message: String, val tree: String, val parents: List<String>)

    companion object {
        const val API = "https://api.github.com"
        const val TEMPLATE = "elkuku/KMyBanners-template"
    }
}
