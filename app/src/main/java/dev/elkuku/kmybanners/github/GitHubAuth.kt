package dev.elkuku.kmybanners.github

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.forms.submitForm
import io.ktor.http.ContentType
import io.ktor.http.parameters
import kotlinx.coroutines.delay
import java.io.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** What the user has to type at [verificationUri] to let the app in. */
@Serializable
data class DeviceCode(
    @SerialName("device_code") val deviceCode: String,
    @SerialName("user_code") val userCode: String,
    @SerialName("verification_uri") val verificationUri: String,
    @SerialName("expires_in") val expiresIn: Int,
    val interval: Int = 5,
)

/** A GitHub user token; with expiring tokens (the OAuth App's setting) it lasts 8 hours and comes with a refresh token. */
data class Tokens(val access: String, val refresh: String?, val expiresAt: Long?) {
    fun expiresSoon(now: Long): Boolean = expiresAt != null && now > expiresAt - 5 * 60_000
}

@Serializable
internal data class TokenResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    val error: String? = null,
    val interval: Int? = null,
)

/**
 * GitHub sign-in with the device flow: no client secret and no redirect, so it works in an app.
 * The user opens github.com/login/device, types the code and approves the app.
 */
class GitHubAuth(
    private val http: HttpClient,
    private val clientId: String,
    private val now: () -> Long = System::currentTimeMillis,
) {
    suspend fun start(): DeviceCode = http.submitForm(
        "https://github.com/login/device/code",
        parameters {
            append("client_id", clientId)
            append("scope", SCOPE)
        },
    ) { accept(ContentType.Application.Json) }.body()

    /** Waits until the user approved the code. Null when it expired or was denied. */
    suspend fun await(code: DeviceCode): Tokens? {
        var interval = code.interval
        val end = now() + code.expiresIn * 1000L
        while (now() < end) {
            delay(interval * 1000L)
            // A network hiccup (no DNS, a dropped connection on mobile data) only skips this poll
            val r = try {
                token {
                    append("device_code", code.deviceCode)
                    append("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                }
            } catch (e: IOException) {
                continue
            }
            when {
                r.accessToken != null -> return r.toTokens()
                r.error == "authorization_pending" -> Unit
                r.error == "slow_down" -> interval = r.interval ?: (interval + 5)
                else -> return null // expired_token, access_denied, …
            }
        }
        return null
    }

    /** A new token with the refresh token; null when GitHub refused it (sign in again). */
    suspend fun refresh(tokens: Tokens): Tokens? {
        val refresh = tokens.refresh ?: return null
        val r = token {
            append("grant_type", "refresh_token")
            append("refresh_token", refresh)
        }
        return r.accessToken?.let { r.toTokens() }
    }

    private suspend fun token(extra: io.ktor.http.ParametersBuilder.() -> Unit): TokenResponse = http.submitForm(
        "https://github.com/login/oauth/access_token",
        parameters {
            append("client_id", clientId)
            extra()
        },
    ) { accept(ContentType.Application.Json) }.body()

    private fun TokenResponse.toTokens() =
        Tokens(accessToken!!, refreshToken, expiresIn?.let { now() + it * 1000 })

    companion object {
        /** Enough to create the page repo from the template, upload files and turn on Pages. */
        const val SCOPE = "public_repo"
    }
}
