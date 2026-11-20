package dev.elkuku.kmybanners

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReviewTest {
    @Test
    fun newDecisionReplacesTheOldOneForTheSameRow() {
        val old = Corrections(listOf(Correction("fp1", "a"), Correction("fp2", "b")))
        val merged = old.plus(listOf(Correction("fp2", null, "Not on Bannergress"), Correction("fp3", "c")))
        assertEquals(listOf("fp1", "fp2", "fp3"), merged.rows.map { it.fp })
        assertNull(merged.rows[1].id)
        assertEquals("""{"rows":[{"fp":"fp1","id":"a"}]}""", json.encodeToString(Corrections.serializer(), Corrections(listOf(Correction("fp1", "a")))))
    }

    @Test
    fun rowsToCheckReadsTheReviewRowsOfBannersJson() = runTest {
        val http = httpClient(MockEngine { req ->
            assertEquals("/ingress-banners/banners.json", req.url.encodedPath)
            respond("""{"agent":"x","banners":[
                {"i":0,"rows":"0-2","status":"match","img":"img/m000.jpg?v=1","fp":"A","count":18,"id":"b1"},
                {"i":1,"rows":"3","status":"review","img":"img/m001.jpg?v=2","fp":"B","count":6,
                 "options":[{"id":"b2","dist":0.12,"title":"MD","missions":24,"picture":"/bnrs/pictures/p"}]}]}""",
                HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        val all = pageBanners(http, "https://agent.github.io/ingress-banners/")!!
        assertEquals(2, all.size)
        val rows = all.filter { it.status == "review" }
        assertEquals(1, rows.size)
        assertEquals("https://api.bannergress.com/bnrs/pictures/p", rows[0].options[0].pictureUrl)
    }

    @Test
    fun bannerIdFromLinks() {
        assertEquals("glyph-1567", bannerId("https://bannergress.com/banner/glyph-1567"))
        assertEquals("glyph-1567", bannerId(" https://bannergress.com/banner/glyph-1567?x=1 "))
        assertEquals("kleiner-mainzer-höhenweg-7629", bannerId("https://bannergress.com/banner/kleiner-mainzer-h%C3%B6henweg-7629"))
        assertEquals("glyph-1567", bannerId("glyph-1567"))
        assertNull(bannerId("not a link"))
        assertNull(bannerId(""))
    }

    @Test
    fun aChoiceAppliesToEveryRowOfTheEntry() {
        val entry = PageBanner(rows = "3-5", status = "match", img = "i", fp = "a", fps = listOf("a", "b", "c"))
        val c = Choice(single = true).corrections(entry)
        assertEquals(listOf("a", "b", "c"), c.map { it.fp })
        assertEquals(true, c[0].single)
        assertEquals("""{"fp":"a","id":null,"single":true}""".replace(",\"id\":null", ""),
            json.encodeToString(Correction.serializer(), c[0]))
        assertEquals(listOf("x"), Choice(id = "b1").corrections(PageBanner(rows = "1", status = "review", img = "i", fp = "x")).map { it.fp })
    }
}
