package dev.elkuku.kmybanners.ui

import android.content.ContentResolver
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.elkuku.kmybanners.Account
import dev.elkuku.kmybanners.BannerOption
import dev.elkuku.kmybanners.Choice
import dev.elkuku.kmybanners.Corrections
import dev.elkuku.kmybanners.NotSignedInException
import dev.elkuku.kmybanners.PageBanner
import dev.elkuku.kmybanners.PageRepo
import dev.elkuku.kmybanners.R
import dev.elkuku.kmybanners.Shot
import dev.elkuku.kmybanners.bannerId
import dev.elkuku.kmybanners.bannergress.BannergressApi
import dev.elkuku.kmybanners.bannergress.BannergressAuth
import dev.elkuku.kmybanners.bannergress.BannergressLogin
import dev.elkuku.kmybanners.bannergress.BannergressSignedOutException
import dev.elkuku.kmybanners.github.DeviceCode
import dev.elkuku.kmybanners.github.GitHubApi
import dev.elkuku.kmybanners.github.GitHubAuth
import dev.elkuku.kmybanners.github.GitHubException
import dev.elkuku.kmybanners.github.Repo
import dev.elkuku.kmybanners.github.RepoFile
import dev.elkuku.kmybanners.json
import dev.elkuku.kmybanners.shotJpeg
import io.ktor.http.HttpStatusCode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** A message for the user: a string resource, or a plural one for [count], and its arguments. */
data class UiError(
    @StringRes val res: Int = 0,
    val args: List<Any> = emptyList(),
    @PluralsRes val plural: Int = 0,
    val count: Int = 0,
)

/** Ends a step with a message for the user. */
class AppException(val error: UiError) : Exception()

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
    val error: UiError? = null,
    val bannergress: Boolean = false,            // signed in to Bannergress
    val bannergressLogin: BannergressLogin? = null, // the WebView sign-in while it is open
    val doneUploaded: Int? = null,               // banners in the done list (on the page, or just uploaded)
    val doneSyncedAt: String? = null,            // when the done list was uploaded (config.json)
    val banners: List<PageBanner> = emptyList(),   // the page's medal row groups (banners.json)
    val reviewList: List<PageBanner> = emptyList(),// what the review screen goes through
    val reviewing: Int? = null,                    // the entry on the review screen (index into reviewList)
    val decisions: Map<String, Pair<PageBanner, Choice>> = emptyMap(), // by first fingerprint, not saved yet
    val lookup: Lookup? = null,                    // a Bannergress link being checked
    val myBanners: Boolean = false,                // the "My banners" list is open
) {
    /** The medal rows the build could not match for sure. */
    val toCheck: List<PageBanner> get() = banners.filter { it.status == "review" }
}

/** A Bannergress link the agent entered: being checked, found, or not found. */
data class Lookup(val id: String, val banner: BannerOption? = null, val notFound: Boolean = false)

class MainViewModel(
    private val account: Account,
    private val auth: GitHubAuth,
    private val api: GitHubApi,
    private val resolver: ContentResolver,
    private val bgAuth: BannergressAuth,
    private val bgApi: BannergressApi,
    private val fetchBanners: suspend (pageUrl: String) -> List<PageBanner>?,
    private val lookUp: suspend (id: String) -> BannerOption?,
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
        // Set up when the repo exists, was made from the template and config.json has the agent name
        // (else "Create my page" finishes it)
        val found = findPage(login)
        val config = found?.let { readConfig(login, it.name) } ?: JsonObject(emptyMap())
        val agent = config["agent"]?.jsonPrimitive?.content.orEmpty()
        val repo = found?.takeIf { agent.isNotBlank() }
        val pages = repo?.let { api.pages(login, it.name) }
        // The done list on the page: how many banners, uploaded when (for the front page)
        val doneCount = repo?.let {
            api.file(login, it.name, "done.json")?.decodeToString()
                ?.let { runCatching { json.parseToJsonElement(it).jsonArray.size }.getOrNull() }
        }
        _state.update {
            it.copy(loading = false, signedIn = true, login = login, repo = repo?.fullName, pageUrl = pages?.htmlUrl,
                bannergress = bgAuth.signedIn, doneUploaded = doneCount,
                doneSyncedAt = config["doneSyncedAt"]?.jsonPrimitive?.content)
        }
        loadToCheck()
        checkBuild()
    }

    /**
     * The page repo: under the name the app knows (GitHub's API follows a rename made on the website), else by
     * its topic (another phone, a rename), else under the default name (pages made before the name could be
     * chosen, without the topic). Only a repo made from the template counts.
     */
    private suspend fun findPage(login: String): Repo? {
        account.repo?.let { api.repo(login, it) }?.takeIf { it.fromTemplate }?.let { return remember(login, it) }
        api.ownRepos().filter { PageRepo.TOPIC in it.topics }
            .firstNotNullOfOrNull { r -> api.repo(login, r.name)?.takeIf { it.fromTemplate } }
            ?.let { return remember(login, it) }
        api.repo(login, PageRepo.DEFAULT)?.takeIf { it.fromTemplate }?.let { return remember(login, it) }
        return null
    }

    /** The page repo's name for later, and its topic, so the app finds it again whatever its name. */
    private suspend fun remember(login: String, repo: Repo): Repo {
        account.repo = repo.name
        if (PageRepo.TOPIC !in repo.topics) api.setTopics(login, repo.name, repo.topics + PageRepo.TOPIC)
        return repo
    }

    /** The page repo's name (once it is set up). */
    private val name: String get() = _state.value.repo?.substringAfter('/') ?: account.repo ?: PageRepo.DEFAULT

    /** The page repo's config.json (empty when there is none yet). */
    private suspend fun readConfig(owner: String, repo: String): JsonObject =
        api.file(owner, repo, "config.json")?.decodeToString()
            ?.let { runCatching { json.parseToJsonElement(it).jsonObject }.getOrNull() } ?: JsonObject(emptyMap())

    fun signIn() {
        signIn?.cancel()
        signIn = work {
            val code = auth.start()
            _state.update { it.copy(code = code) }
            val tokens = try {
                auth.await(code)
            } catch (e: Throwable) {
                _state.update { it.copy(code = null) } // no code card left waiting
                throw e
            }
            if (tokens == null) {
                _state.update { it.copy(code = null, error = UiError(R.string.error_code_expired)) }
                return@work
            }
            // The spinner until refresh() has the page, not the sign-in intro in between
            _state.update { it.copy(code = null, loading = true) }
            account.save(tokens)
            refresh()
        }
    }

    fun cancelSignIn() {
        signIn?.cancel()
        _state.update { it.copy(code = null, busy = false) }
    }

    /** The page repo [name] from the template, with its topic, Pages on and the agent name in config.json. */
    fun createPage(agent: String, name: String) = work {
        val login = _state.value.login ?: return@work
        val repoName = name.trim()
        if (!PageRepo.valid(repoName)) throw AppException(UiError(R.string.error_repo_name))
        // Again after an error: the repo may exist already (not one GitHub only redirects to, after a rename)
        val existing = api.repo(login, repoName)
        if (existing != null && (!existing.fromTemplate || !existing.name.equals(repoName, ignoreCase = true))) {
            throw AppException(UiError(R.string.error_repo_taken, listOf(repoName)))
        }
        val repo = existing
            ?: api.createFromTemplate(login, repoName, "My Ingress mission banners, made with the KMyBanners app")
        remember(login, repo)
        api.enablePages(login, repo.name)
        val config = "{\n  \"agent\": ${jsonString(agent.trim())}\n}\n"
        api.commit(login, repo.name, listOf(RepoFile("config.json", config.toByteArray())), "Set the agent name")
        _state.update { it.copy(repo = repo.fullName, pageUrl = api.pages(login, repo.name)?.htmlUrl) }
    }

    /**
     * The page repo under a new name, then the page built again: GitHub Pages doesn't redirect, the page is at
     * its new address once built (the old address stops working).
     */
    fun renamePage(newName: String) = work {
        val login = _state.value.login ?: return@work
        val to = newName.trim()
        if (!PageRepo.valid(to)) throw AppException(UiError(R.string.error_repo_name))
        if (to == name) return@work
        val repo = try {
            api.rename(login, name, to)
        } catch (e: GitHubException) {
            if (e.status == HttpStatusCode.UnprocessableEntity) throw AppException(UiError(R.string.error_repo_taken, listOf(to)))
            throw e
        }
        account.repo = repo.name
        _state.update { it.copy(repo = repo.fullName, pageUrl = PageRepo.pageUrl(login, repo.name)) }
        val before = api.dispatch(login, repo.name, WORKFLOW)
        _state.update { it.copy(build = Build("queued")) }
        follow(login) { api.latestDispatch(login, repo.name, WORKFLOW)?.takeIf { it.id != before } }
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
            val n = shots.size - files.size
            _state.update { it.copy(uploaded = null, error = UiError(args = listOf(n), plural = R.plurals.error_shots_unreadable, count = n)) }
            return@work
        }
        val sha = api.commit(login, name, files, "Add ${files.size} screenshots") { n ->
            _state.update { it.copy(uploaded = n) }
        }
        _state.update { it.copy(shots = emptyList(), uploaded = null, build = Build("queued")) }
        follow(login, sha)
    }

    /** A new build of the page (after a failed one, or to pick up a new version of the tool). */
    fun buildAgain() = work {
        val login = _state.value.login ?: return@work
        val before = api.dispatch(login, name, WORKFLOW)
        _state.update { it.copy(build = Build("queued")) }
        follow(login) { api.latestDispatch(login, name, WORKFLOW)?.takeIf { it.id != before } }
    }

    /** Polls the workflow run of the commit until it is done (the page is deployed then). */
    private suspend fun follow(login: String, sha: String) = follow(login) { api.run(login, name, sha) }

    /**
     * Polls the run until it is done: every 5 s for 2 minutes, then every 15 s, for up to an hour (the first
     * build with a whole list downloads many Bannergress pictures and takes over 10 minutes). If the app goes
     * away meanwhile, [checkBuild] picks the run up again.
     */
    @Volatile private var following = false

    private suspend fun follow(login: String, find: suspend () -> dev.elkuku.kmybanners.github.WorkflowRun?) {
        following = true
        try {
            followRun(login, find)
        } finally {
            following = false
        }
    }

    private suspend fun followRun(login: String, find: suspend () -> dev.elkuku.kmybanners.github.WorkflowRun?) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < 60 * 60_000L) {
            delay(if (System.currentTimeMillis() - start < 2 * 60_000L) 5_000 else 15_000)
            val run = find()
            if (run != null) {
                _state.update { it.copy(build = Build(run.status, run.conclusion, run.htmlUrl)) }
                if (run.status == "completed") {
                    if (run.conclusion == "success") {
                        _state.update { it.copy(pageUrl = api.pages(login, name)?.htmlUrl ?: it.pageUrl) }
                        delay(10_000) // Pages needs a moment to serve the new files
                        loadToCheck()
                    }
                    return
                }
            }
        }
    }

    private var checking: Job? = null

    /**
     * The newest build of the page, when the app starts or comes back: a running one is followed (also when
     * the app was closed meanwhile), a finished one shown with its result.
     */
    fun checkBuild() {
        val login = _state.value.login ?: return
        // following: an upload or "Build again" is following its build already
        if (_state.value.repo == null || following || checking?.isActive == true) return
        checking = viewModelScope.launch {
            try {
                val run = api.latestRun(login, name, WORKFLOW) ?: return@launch
                val shown = _state.value.build
                if (run.status != "completed") {
                    _state.update { it.copy(build = Build(run.status, null, run.htmlUrl)) }
                    follow(login) { api.runById(login, name, run.id) }
                } else if (shown != null && !shown.done) {
                    // A build the app was waiting for finished while it wasn't looking
                    _state.update { it.copy(build = Build(run.status, run.conclusion, run.htmlUrl)) }
                    if (run.conclusion == "success") loadToCheck()
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                // offline or GitHub down: the next resume tries again
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
            if (!bgAuth.finish(login, code)) throw AppException(UiError(R.string.error_bannergress_refused))
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
        val config = api.file(login, name, "config.json")?.decodeToString()
            ?.let { runCatching { json.parseToJsonElement(it).jsonObject }.getOrNull() } ?: JsonObject(emptyMap())
        val newConfig = JsonObject(config + ("doneSyncedAt" to JsonPrimitive(synced)))
        val sha = api.commit(login, name, listOf(
            RepoFile("done.json", json.encodeToString(done).toByteArray()),
            RepoFile("config.json", (prettyJson.encodeToString(JsonObject.serializer(), newConfig) + "\n").toByteArray()),
        ), "Update the done list (${done.size} banners)")
        _state.update { it.copy(doneUploaded = done.size, doneSyncedAt = synced, build = Build("queued")) }
        follow(login, sha)
    }

    // --- Checking the rows the build could not match for sure, and changing any entry later ---

    private suspend fun loadToCheck() {
        val url = _state.value.pageUrl ?: return
        fetchBanners(url)?.let { rows -> _state.update { it.copy(banners = rows) } }
    }

    /** The rows to check, starting at the first one without a decision. */
    fun startReview() = _state.update { s ->
        val i = s.toCheck.indexOfFirst { it.fp !in s.decisions }.takeIf { it >= 0 } ?: 0
        s.copy(reviewList = s.toCheck, reviewing = i, lookup = null)
    }

    fun openMyBanners() = _state.update { it.copy(myBanners = true) }

    fun closeMyBanners() = _state.update { it.copy(myBanners = false) }

    /** One entry of "My banners" on the review screen, to change it. */
    fun edit(entry: PageBanner) = _state.update { it.copy(reviewList = listOf(entry), reviewing = 0, lookup = null) }

    fun closeReview() = _state.update { it.copy(reviewing = null, lookup = null) }

    /** The agent's choice for the entry on the screen (null: undecided again); then the next one. */
    fun decide(choice: Choice?) = _state.update { s ->
        val i = s.reviewing ?: return@update s
        val entry = s.reviewList[i]
        val decisions = if (choice == null) s.decisions - entry.fp else s.decisions + (entry.fp to (entry to choice))
        s.copy(decisions = decisions, reviewing = if (i + 1 < s.reviewList.size) i + 1 else null, lookup = null)
    }

    fun previousRow() = _state.update { s ->
        s.copy(reviewing = s.reviewing?.let { (it - 1).coerceAtLeast(0) }, lookup = null)
    }

    /** Checks a Bannergress link (or id) the agent entered: does the banner exist? */
    fun checkLink(text: String) {
        val id = bannerId(text) ?: run { _state.update { it.copy(lookup = Lookup(text.trim(), notFound = true)) }; return }
        _state.update { it.copy(lookup = Lookup(id)) }
        work {
            val found = lookUp(id)
            _state.update { it.copy(lookup = Lookup(id, found, notFound = found == null)) }
        }
    }

    /** The decisions to corrections.json (one per row, added to the earlier ones) in one commit; then the build. */
    fun saveDecisions() = work {
        val login = _state.value.login ?: return@work
        val chosen = _state.value.decisions.values
        if (chosen.isEmpty()) return@work
        val new = chosen.flatMap { (entry, choice) -> choice.corrections(entry) }
        val old = api.file(login, name, "corrections.json")?.decodeToString()
            ?.let { runCatching { json.decodeFromString<Corrections>(it) }.getOrNull() } ?: Corrections()
        val file = prettyJson.encodeToString(Corrections.serializer(), old.plus(new)) + "\n"
        val sha = api.commit(login, name, listOf(RepoFile("corrections.json", file.toByteArray())),
            "Check ${chosen.size} medal rows")
        _state.update { it.copy(decisions = emptyMap(), reviewing = null, build = Build("queued")) }
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
            _state.value = UiState(loading = false, error = UiError(R.string.error_github_signed_out))
        } catch (e: BannergressSignedOutException) {
            _state.update { it.copy(loading = false, uploaded = null, error = UiError(R.string.error_bannergress_signed_out)) }
        } catch (e: AppException) {
            _state.update { it.copy(loading = false, uploaded = null, error = e.error) }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update { it.copy(loading = false, uploaded = null,
                error = UiError(R.string.error_generic, listOf(e.message ?: e.toString()))) }
        } finally {
            _state.update { it.copy(busy = false) }
        }
    }

    companion object {
        /** The page repo's workflow (from the template). */
        const val WORKFLOW = "banner-page.yml"

        private val prettyJson = Json { prettyPrint = true }

        fun jsonString(s: String): String =
            "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
    }
}
