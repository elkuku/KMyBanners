package com.elkuku.ingressbanners.ui

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elkuku.ingressbanners.Account
import com.elkuku.ingressbanners.NotSignedInException
import com.elkuku.ingressbanners.Shot
import com.elkuku.ingressbanners.bannergress.BannergressApi
import com.elkuku.ingressbanners.bannergress.BannergressAuth
import com.elkuku.ingressbanners.bannergress.BannergressLogin
import com.elkuku.ingressbanners.bannergress.BannergressSignedOutException
import com.elkuku.ingressbanners.github.DeviceCode
import com.elkuku.ingressbanners.github.GitHubApi
import com.elkuku.ingressbanners.github.GitHubAuth
import com.elkuku.ingressbanners.github.RepoFile
import com.elkuku.ingressbanners.json
import com.elkuku.ingressbanners.shotJpeg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** A build of the page on GitHub Actions after an upload. */
data class Build(val status: String, val conclusion: String? = null, val url: String? = null) {
    val done get() = status == "completed"
}

data class UiState(
    val loading: Boolean = true,
    val signedIn: Boolean = false,
    val code: DeviceCode? = null,          // shown while the user approves the app on github.com
    val login: String? = null,
    val repo: String? = null,              // owner/name once the page repo exists
    val pageUrl: String? = null,
    val shots: List<Shot> = emptyList(),   // shared, not uploaded yet
    val uploaded: Int? = null,             // upload progress
    val build: Build? = null,
    val busy: Boolean = false,
    val error: String? = null,
    val bannergress: Boolean = false,            // signed in to Bannergress
    val bannergressLogin: BannergressLogin? = null, // the WebView sign-in while it is open
    val doneUploaded: Int? = null,               // banners in the done list just uploaded
)

class MainViewModel(
    private val account: Account,
    private val auth: GitHubAuth,
    private val api: GitHubApi,
    private val resolver: ContentResolver,
    private val bgAuth: BannergressAuth,
    private val bgApi: BannergressApi,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state
    private var signIn: Job? = null

    init {
        refresh()
    }

    /** Who is signed in and whether the page repo exists. */
    fun refresh() = work {
        if (!account.signedIn) {
            _state.update { it.copy(loading = false, signedIn = false) }
            return@work
        }
        val login = account.login ?: api.user().login.also { account.login = it }
        val name = account.repo ?: REPO
        // Set up when the repo exists and config.json has the agent name (else "Create my page" finishes it)
        val repo = api.repo(login, name)?.takeIf { agentName(login, name).isNotBlank() }
        account.repo = repo?.let { name }
        val pages = repo?.let { api.pages(login, name) }
        _state.update {
            it.copy(loading = false, signedIn = true, login = login, repo = repo?.fullName, pageUrl = pages?.htmlUrl,
                bannergress = bgAuth.signedIn)
        }
    }

    private suspend fun agentName(owner: String, name: String): String =
        api.file(owner, name, "config.json")?.decodeToString()
            ?.let { runCatching { json.parseToJsonElement(it).jsonObject["agent"]?.jsonPrimitive?.content }.getOrNull() }
            .orEmpty()

    fun signIn() {
        signIn?.cancel()
        signIn = work {
            val code = auth.start()
            _state.update { it.copy(code = code) }
            val tokens = auth.await(code)
            _state.update { it.copy(code = null) }
            if (tokens == null) {
                _state.update { it.copy(error = "The sign-in code expired or was declined. Try again.") }
                return@work
            }
            account.save(tokens)
            refresh()
        }
    }

    fun cancelSignIn() {
        signIn?.cancel()
        _state.update { it.copy(code = null, busy = false) }
    }

    /** The page repo from the template, with Pages on and the agent name in config.json. */
    fun createPage(agent: String) = work {
        val login = _state.value.login ?: return@work
        // Again after an error: the repo may exist already
        val repo = api.repo(login, REPO)
            ?: api.createFromTemplate(login, REPO, "My Ingress mission banners, made with the Ingress Banners app")
        api.enablePages(login, REPO)
        val config = "{\n  \"agent\": ${jsonString(agent.trim())}\n}\n"
        api.commit(login, REPO, listOf(RepoFile("config.json", config.toByteArray())), "Set the agent name")
        account.repo = REPO
        _state.update { it.copy(repo = repo.fullName, pageUrl = api.pages(login, REPO)?.htmlUrl) }
    }

    fun shared(shots: List<Shot>) = _state.update { it.copy(shots = shots, build = null, error = null) }

    fun clearShots() = _state.update { it.copy(shots = emptyList()) }

    /** The shared screenshots in one commit to inbox/<time>/, then follow the build. */
    fun upload() = work {
        val login = _state.value.login ?: return@work
        val shots = _state.value.shots
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date())
        _state.update { it.copy(uploaded = 0) }
        val files = withContext(Dispatchers.IO) {
            shots.mapIndexedNotNull { n, s ->
                shotJpeg(s, resolver)?.let { RepoFile("inbox/$stamp/%02d.jpg".format(n), it) }
            }
        }
        if (files.size < shots.size) {
            _state.update { it.copy(uploaded = null, error = "${shots.size - files.size} screenshots could not be read.") }
            return@work
        }
        val sha = api.commit(login, REPO, files, "Add ${files.size} screenshots") { n ->
            _state.update { it.copy(uploaded = n) }
        }
        _state.update { it.copy(shots = emptyList(), uploaded = null, build = Build("queued")) }
        follow(login, sha)
    }

    /** Polls the workflow run of the commit until it is done (the page is deployed then). */
    private suspend fun follow(login: String, sha: String) {
        repeat(120) {
            delay(5000)
            val run = api.run(login, REPO, sha)
            if (run != null) {
                _state.update { it.copy(build = Build(run.status, run.conclusion, run.htmlUrl)) }
                if (run.status == "completed") {
                    if (run.conclusion == "success") {
                        _state.update { it.copy(pageUrl = api.pages(login, REPO)?.htmlUrl ?: it.pageUrl) }
                    }
                    return
                }
            }
        }
    }

    // --- Bannergress: the done list the page matches the medals with ---

    fun bannergressSignIn() = _state.update { it.copy(bannergressLogin = bgAuth.start(), error = null) }

    fun bannergressCancel() = _state.update { it.copy(bannergressLogin = null) }

    fun bannergressCode(code: String) {
        val login = _state.value.bannergressLogin ?: return
        _state.update { it.copy(bannergressLogin = null) }
        work {
            if (!bgAuth.finish(login, code)) throw Exception("Bannergress refused the sign-in. Try again.")
            _state.update { it.copy(bannergress = true) }
        }
    }

    /** The whole done list to done.json and the sync time to config.json, in one commit; then follow the build. */
    fun uploadDoneList() = work {
        val login = _state.value.login ?: return@work
        val done = try {
            bgApi.doneList()
        } catch (e: BannergressSignedOutException) {
            bgAuth.signOut()
            _state.update { it.copy(bannergress = false) }
            throw e
        }
        val synced = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())
        val config = api.file(login, REPO, "config.json")?.decodeToString()
            ?.let { runCatching { json.parseToJsonElement(it).jsonObject }.getOrNull() } ?: JsonObject(emptyMap())
        val newConfig = JsonObject(config + ("doneSyncedAt" to JsonPrimitive(synced)))
        val sha = api.commit(login, REPO, listOf(
            RepoFile("done.json", json.encodeToString(done).toByteArray()),
            RepoFile("config.json", (prettyJson.encodeToString(JsonObject.serializer(), newConfig) + "\n").toByteArray()),
        ), "Update the done list (${done.size} banners)")
        _state.update { it.copy(doneUploaded = done.size, build = Build("queued")) }
        follow(login, sha)
    }

    fun signOut() {
        bgAuth.signOut()
        account.signOut()
        _state.value = UiState(loading = false)
    }

    fun dismissError() = _state.update { it.copy(error = null) }

    private fun work(block: suspend () -> Unit): Job = viewModelScope.launch {
        _state.update { it.copy(busy = true, error = null) }
        try {
            block()
        } catch (e: NotSignedInException) {
            _state.value = UiState(loading = false, error = "Please sign in to GitHub again.")
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update { it.copy(loading = false, uploaded = null, error = e.message ?: e.toString()) }
        } finally {
            _state.update { it.copy(busy = false) }
        }
    }

    companion object {
        /** The page repo in the user's account. */
        const val REPO = "ingress-banners-page"

        private val prettyJson = Json { prettyPrint = true }

        fun jsonString(s: String): String =
            "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
    }
}
