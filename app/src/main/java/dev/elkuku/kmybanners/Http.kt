package dev.elkuku.kmybanners

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// encodeDefaults: GitHub needs fields like a tree entry's mode and type even when they have their usual value
val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }

/** The app's HTTP client: JSON both ways, generous timeouts for screenshot uploads. */
fun httpClient(engine: HttpClientEngine = OkHttp.create()): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) { json(json) }
    install(HttpTimeout) {
        requestTimeoutMillis = 120_000
        connectTimeoutMillis = 20_000
    }
    defaultRequest { header("User-Agent", "KMyBanners/${BuildConfig.VERSION_NAME} (Android)") }
}
