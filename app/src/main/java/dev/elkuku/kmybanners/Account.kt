package dev.elkuku.kmybanners

import android.content.SharedPreferences
import androidx.core.content.edit
import dev.elkuku.kmybanners.github.GitHubAuth
import dev.elkuku.kmybanners.github.Tokens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Small string settings (SharedPreferences in the app, a map in tests). */
interface Store {
    fun get(key: String): String?
    fun put(key: String, value: String?)
}

class PrefsStore(private val prefs: SharedPreferences) : Store {
    override fun get(key: String): String? = prefs.getString(key, null)
    override fun put(key: String, value: String?) = prefs.edit { if (value == null) remove(key) else putString(key, value) }
}

class NotSignedInException : Exception("Not signed in to GitHub")

/** The GitHub sign-in, kept in the app's private settings and refreshed when it expires. */
class Account(private val store: Store, private val auth: GitHubAuth, private val now: () -> Long = System::currentTimeMillis) {
    private val lock = Mutex()

    val signedIn: Boolean get() = store.get(ACCESS) != null

    /** The GitHub login. */
    var login: String?
        get() = store.get(LOGIN)
        set(v) = store.put(LOGIN, v)

    /** The page repo's name, once found or made (it can be renamed). */
    var repo: String?
        get() = store.get(REPO)
        set(v) = store.put(REPO, v)

    fun save(t: Tokens) {
        store.put(ACCESS, t.access)
        store.put(REFRESH, t.refresh)
        store.put(EXPIRES, t.expiresAt?.toString())
    }

    /** A valid access token, refreshed first when it expires soon. */
    suspend fun accessToken(): String = lock.withLock {
        val access = store.get(ACCESS) ?: throw NotSignedInException()
        val t = Tokens(access, store.get(REFRESH), store.get(EXPIRES)?.toLongOrNull())
        if (!t.expiresSoon(now())) return access
        val fresh = auth.refresh(t) ?: run { signOut(); throw NotSignedInException() }
        save(fresh)
        fresh.access
    }

    fun signOut() {
        listOf(ACCESS, REFRESH, EXPIRES, LOGIN, REPO).forEach { store.put(it, null) }
    }

    private companion object {
        const val ACCESS = "github_access"
        const val REFRESH = "github_refresh"
        const val EXPIRES = "github_expires"
        const val LOGIN = "github_login"
        const val REPO = "page_repo"
    }
}
