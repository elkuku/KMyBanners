package dev.elkuku.kmybanners

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLPathPart
import kotlinx.serialization.Serializable

/** The page's banners.json (written by the build): every medal row group, matched or still to check. */
@Serializable
data class PageBanners(val banners: List<PageBanner> = emptyList())

@Serializable
data class PageBanner(
    val rows: String,
    val status: String,             // match, none (not on Bannergress / single missions), review (to check)
    val img: String,                // the rows' medals, relative to the page
    val fp: String,                 // the first row's fingerprint
    val fps: List<String> = emptyList(), // every row's (a decision applies to all of them)
    val ends: List<Int> = emptyList(),   // every row's place from the end of the list (0: the last row)
    val count: Int = 0,
    val id: String? = null,         // the matched Bannergress banner
    val title: String? = null,
    val name: String? = null,       // for status none: the name the agent gave it
    val single: Boolean = false,    // single missions, not a banner
    val missions: Int? = null,
    val options: List<BannerOption> = emptyList(), // for review: the closest banners
) {
    val allFps: List<String> get() = fps.ifEmpty { listOf(fp) }
}

/** A Bannergress banner that may be the one of a medal row. */
@Serializable
data class BannerOption(
    val id: String,
    val title: String = "",
    val missions: Int? = null,
    val picture: String? = null,
    val dist: Double = 0.0,
    val nearby: Boolean = false,    // found near the neighbouring banners, not on the done list
) {
    val pictureUrl: String? get() = picture?.let { if (it.startsWith("http")) it else "https://api.bannergress.com$it" }
}

/**
 * A row the agent decided: a Bannergress banner ([id]), single missions ([single]), or not on Bannergress
 * ([id] null, with a [name]).
 */
@Serializable
data class Correction(
    val fp: String,
    val id: String?,
    val name: String? = null,
    val single: Boolean? = null,
    // The row's place from the end of the list: new medals come in on top, so it stays, and the build applies the
    // decision only there (look-alike banners are closer to each other than a row to itself in a new capture)
    val end: Int? = null,
)

/** The agent's decision for a whole entry (all its rows), before it is saved. */
data class Choice(val id: String? = null, val name: String? = null, val single: Boolean = false, val title: String? = null) {
    fun corrections(entry: PageBanner): List<Correction> =
        entry.allFps.mapIndexed { k, fp -> Correction(fp, id, name, single.takeIf { s -> s }, entry.ends.getOrNull(k)) }
}

@Serializable
data class Corrections(val rows: List<Correction> = emptyList()) {
    /** These with [new] added; a new decision for the same row replaces the old one. */
    fun plus(new: Collection<Correction>): Corrections {
        val fps = new.map { it.fp }.toSet()
        return Corrections(rows.filter { it.fp !in fps } + new)
    }
}

/** All medal row groups of the published page (null when it isn't published yet). */
suspend fun pageBanners(http: HttpClient, pageUrl: String): List<PageBanner>? = try {
    http.get(pageUrl.trimEnd('/') + "/banners.json?t=" + System.currentTimeMillis()) {
        header("Cache-Control", "no-cache")
    }.body<PageBanners>().banners
} catch (_: Exception) {
    null
}

/** The banner id of a Bannergress link (bannergress.com/banner/<id>, also with ?…), or the text itself. */
fun bannerId(text: String): String? {
    val t = text.trim()
    if (t.isEmpty()) return null
    Regex("""/banner/([^/?#\s]+)""").find(t)?.let { return java.net.URLDecoder.decode(it.groupValues[1], "UTF-8") }
    return t.takeIf { !it.contains('/') && !it.contains(' ') }
}

@Serializable
private data class BannergressBanner(
    val id: String,
    val title: String = "",
    val numberOfMissions: Int? = null,
    val picture: String? = null,
)

/** The banner of [id] on Bannergress (public API), or null when there is none. */
suspend fun lookUpBanner(http: HttpClient, id: String): BannerOption? {
    val r = http.get("https://api.bannergress.com/bnrs/" + id.encodeURLPathPart())
    if (r.status == HttpStatusCode.NotFound) return null
    if (r.status != HttpStatusCode.OK) throw Exception("Bannergress: ${r.status.value}")
    val b = r.body<BannergressBanner>()
    return BannerOption(b.id, b.title, b.numberOfMissions, b.picture)
}
