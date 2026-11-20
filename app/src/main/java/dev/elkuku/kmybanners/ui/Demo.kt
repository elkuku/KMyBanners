package dev.elkuku.kmybanners.ui

import android.net.Uri
import dev.elkuku.kmybanners.BuildConfig
import dev.elkuku.kmybanners.PageRepo
import dev.elkuku.kmybanners.Shot
import dev.elkuku.kmybanners.github.DeviceCode

/**
 * Fixed screens for the documentation's screenshots (docs/scripts/app-screenshots.py), in the debug build only:
 * started with the intent extra `demo` (one of [DEMO_STATES]), the app shows that state instead of the real
 * one, as "KMyBanners" without the DEV mark. Nothing is sent anywhere unless a button is tapped.
 */
object Demo {
    /** The demo state shown, or null for the real app. */
    var state: UiState? = null
        private set

    val active: Boolean get() = state != null

    fun start(name: String?) {
        if (BuildConfig.DEBUG) state = name?.let(::demoState)
    }
}

/**
 * The states: signed out, the GitHub sign-in code, right after the sign-in, a new empty page, screenshots shared,
 * the first build.
 */
val DEMO_STATES = listOf("signin", "code", "create", "empty", "shared", "building")

private fun demoState(name: String): UiState? {
    val page = UiState(loading = false, signedIn = true, login = LOGIN, repo = "$LOGIN/${PageRepo.DEFAULT}",
        pageUrl = "https://$LOGIN.github.io/${PageRepo.DEFAULT}/")
    val doneList = page.copy(bannergress = true, doneUploaded = 150, doneSyncedAt = "2026-11-19T21:30:00Z")
    return when (name) {
        "signin" -> UiState(loading = false)
        "code" -> UiState(loading = false, code = DeviceCode("", "CC83-AB3B", "https://github.com/login/device", 900))
        "create" -> page.copy(repo = null, pageUrl = null)
        "empty" -> page
        "shared" -> doneList.copy(shots = (1..42).map {
            Shot(Uri.EMPTY, "Screenshot_20261119-2140%02d_Ingress.png".format(it))
        })
        "building" -> doneList.copy(build = Build("in_progress"))
        else -> null
    }
}

private const val LOGIN = "elkuku"
