package dev.elkuku.kmybanners

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.Serializable

/** The page's banners.json (written by the build): the medal rows, matched or still to check. */
@Serializable
data class PageBanners(val banners: List<PageBanner> = emptyList())

@Serializable
data class PageBanner(
    val rows: String,
    val status: String,             // match, none (not on Bannergress), review (to check)
    val img: String,                // the row's medals, relative to the page
    val fp: String,                 // the first row's fingerprint (what corrections.json stores)
    val count: Int = 0,
    val title: String? = null,
    val options: List<BannerOption> = emptyList(), // for review: the closest banners of the done list
)

/** A banner of the done list that may be the one of a medal row. */
@Serializable
data class BannerOption(
    val id: String,
    val title: String = "",
    val missions: Int? = null,
    val picture: String? = null,
    val dist: Double = 0.0,
) {
    val pictureUrl: String? get() = picture?.let { if (it.startsWith("http")) it else "https://api.bannergress.com$it" }
}

/** A row the agent decided: a Bannergress banner ([id]) or not on Bannergress ([id] null, with a [name]). */
@Serializable
data class Correction(val fp: String, val id: String?, val name: String? = null)

@Serializable
data class Corrections(val rows: List<Correction> = emptyList()) {
    /** These with [new] added; a new decision for the same row replaces the old one. */
    fun plus(new: Collection<Correction>): Corrections {
        val fps = new.map { it.fp }.toSet()
        return Corrections(rows.filter { it.fp !in fps } + new)
    }
}

/** The rows to check of the published page (null when it isn't published yet). */
suspend fun rowsToCheck(http: HttpClient, pageUrl: String): List<PageBanner>? = try {
    http.get(pageUrl.trimEnd('/') + "/banners.json?t=" + System.currentTimeMillis()) {
        header("Cache-Control", "no-cache")
    }.body<PageBanners>().banners.filter { it.status == "review" }
} catch (_: Exception) {
    null
}
