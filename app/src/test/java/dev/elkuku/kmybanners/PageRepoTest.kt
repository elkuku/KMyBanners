package dev.elkuku.kmybanners

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageRepoTest {
    @Test
    fun namesGitHubTakesAsTheyAre() {
        assertTrue(PageRepo.valid("ingress-banners"))
        assertTrue(PageRepo.valid("My_Banners.2026"))
        assertFalse(PageRepo.valid(""))
        assertFalse(PageRepo.valid("my banners"))
        assertFalse(PageRepo.valid("bänner"))
        assertFalse(PageRepo.valid(".."))
        assertFalse(PageRepo.valid("x.git"))
        assertFalse(PageRepo.valid("a".repeat(101)))
    }

    @Test
    fun thePageAddress() {
        assertEquals("https://elkuku.github.io/ingress-banners/", PageRepo.pageUrl("elkuku", "ingress-banners"))
        assertEquals("https://elkuku.github.io/My-Banners/", PageRepo.pageUrl("ElKuku", "My-Banners"))
        // The account's own site
        assertEquals("https://elkuku.github.io/", PageRepo.pageUrl("elkuku", "ElKuku.github.io"))
    }
}
