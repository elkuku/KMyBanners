package com.elkuku.ingressbanners.bannergress

import com.elkuku.ingressbanners.Store
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.parameters
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** A started Bannergress sign-in: open [url] in a WebView and wait for the redirect to [BannergressAuth.REDIRECT_URI]. */
data class BannergressLogin(val url: String, val verifier: String, val state: String) {
    /** The authorization code of a redirect for this sign-in, else null (cancelled, refused, another sign-in). */
    fun code(redirect: String): String? {
        if (!redirect.startsWith(BannergressAuth.REDIRECT_URI)) return null
        val p = Url(redirect).parameters
        return p["code"]?.takeIf { p["state"] == state }
    }
}

@Serializable
internal data class KeycloakTokens(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long = 0,
)

class BannergressSignedOutException : Exception("Please sign in to Bannergress again.")

/**
 * The Bannergress sign-in as AgentTools and kBannerGuider do it: Keycloak's authorization code flow with PKCE
 * for the Bannergress website's client, whose only redirect is bannergress.com, so the app catches it in a
 * WebView. The refresh token is kept in the app's private settings, the access token only in memory.
 * (Later: a client of our own and a Custom Tab.)
 */
class BannergressAuth(
    private val http: HttpClient,
    private val store: Store,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private var access: String? = null
    private var expiresAt = 0L
    private val lock = Mutex()

    val signedIn: Boolean get() = store.get(REFRESH) != null

    fun start(): BannergressLogin {
        val verifier = random(32)
        val state = random(16)
        val url = URLBuilder("$KEYCLOAK/auth").apply {
            parameters.append("client_id", CLIENT_ID)
            parameters.append("redirect_uri", REDIRECT_URI)
            parameters.append("response_type", "code")
            parameters.append("scope", "openid profile email")
            parameters.append("state", state)
            parameters.append("code_challenge", base64Url(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray())))
            parameters.append("code_challenge_method", "S256")
        }.buildString()
        return BannergressLogin(url, verifier, state)
    }

    /** Exchanges the redirect's code for tokens. False when Bannergress refused it. */
    suspend fun finish(login: BannergressLogin, code: String): Boolean = token(parameters {
        append("grant_type", "authorization_code")
        append("client_id", CLIENT_ID)
        append("redirect_uri", REDIRECT_URI)
        append("code", code)
        append("code_verifier", login.verifier)
    })

    /** A valid access token, refreshed when it (nearly) expired. */
    suspend fun accessToken(): String = lock.withLock {
        access?.takeIf { now() < expiresAt - 60_000 }?.let { return it }
        val refresh = store.get(REFRESH) ?: throw BannergressSignedOutException()
        if (!token(parameters {
                append("grant_type", "refresh_token")
                append("client_id", CLIENT_ID)
                append("refresh_token", refresh)
            })) throw BannergressSignedOutException()
        access!!
    }

    fun signOut() {
        store.put(REFRESH, null)
        access = null
    }

    /** Asks the token endpoint; signs out when Keycloak refuses (an ended session). */
    private suspend fun token(form: Parameters): Boolean {
        val r = http.submitForm("$KEYCLOAK/token", form)
        val t = if (r.status == HttpStatusCode.OK) r.body<KeycloakTokens>() else null
        if (t?.accessToken == null) {
            if (r.status.value in 400..499) signOut()
            return false
        }
        access = t.accessToken
        expiresAt = now() + t.expiresIn * 1000
        t.refreshToken?.let { store.put(REFRESH, it) }
        return true
    }

    private fun random(bytes: Int) = base64Url(ByteArray(bytes).also(SecureRandom()::nextBytes))

    private fun base64Url(b: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(b)

    companion object {
        const val REDIRECT_URI = "https://bannergress.com/"
        private const val KEYCLOAK = "https://login.bannergress.com/auth/realms/bannergress/protocol/openid-connect"
        private const val CLIENT_ID = "bannergress-website"
        private const val REFRESH = "bannergress_refresh"
    }
}

/** A banner of the done list, with what the page build needs (done.json, Bannergress's field names). */
@Serializable
data class DoneBanner(
    val id: String,
    val title: String = "",
    val numberOfMissions: Int = 0,
    val picture: String? = null,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val eventStartDate: String? = null,
)

/** Bannergress's `GET /bnrs` with the user's token. */
class BannergressApi(private val http: HttpClient, private val token: suspend () -> String) {

    /** The whole done list, the newest first, page by page (Bannergress returns at most 100). */
    suspend fun doneList(): List<DoneBanner> {
        val all = mutableListOf<DoneBanner>()
        while (true) {
            val r = http.get("$API/bnrs") {
                bearerAuth(token())
                parameter("listTypes", "done")
                parameter("orderBy", "listAdded")
                parameter("orderDirection", "DESC")
                parameter("offset", all.size)
                parameter("limit", PAGE)
                ATTRIBUTES.forEach { parameter("attributes", it) }
            }
            when (r.status) {
                HttpStatusCode.OK -> Unit
                HttpStatusCode.Unauthorized -> throw BannergressSignedOutException()
                else -> throw Exception("Bannergress: ${r.status.value}")
            }
            val page: List<DoneBanner> = r.body()
            all += page
            if (page.size < PAGE) return all
        }
    }

    companion object {
        const val API = "https://api.bannergress.com"
        private const val PAGE = 100
        private val ATTRIBUTES = listOf("id", "title", "numberOfMissions", "picture", "startLatitude", "startLongitude",
            "eventStartDate")
    }
}
