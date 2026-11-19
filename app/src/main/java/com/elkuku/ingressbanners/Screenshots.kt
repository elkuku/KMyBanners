package com.elkuku.ingressbanners

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.IntentCompat
import java.io.ByteArrayOutputStream

/** A shared screenshot: where to read it and its file name (Screenshot_20261119-171604.png). */
data class Shot(val uri: Uri, val name: String)

fun isSharedImages(intent: Intent): Boolean =
    (intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE) &&
        intent.type?.startsWith("image/") == true

/**
 * The shared screenshots in the order they were taken: sorted by file name, which holds the time for
 * screenshots, so the order the user picked them in doesn't matter.
 */
fun sharedShots(intent: Intent, resolver: ContentResolver): List<Shot> {
    val uris = if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
        IntentCompat.getParcelableArrayListExtra(intent, Intent.EXTRA_STREAM, Uri::class.java).orEmpty()
    } else {
        listOfNotNull(IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java))
    }
    return uris.mapIndexed { n, uri -> Shot(uri, displayName(uri, resolver) ?: "%04d".format(n)) }.sortedBy { it.name }
}

private fun displayName(uri: Uri, resolver: ContentResolver): String? = try {
    resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
        if (c.moveToFirst()) c.getString(0) else null
    }
} catch (_: Exception) {
    null
}

/**
 * The screenshot as JPEG (a PNG screenshot is ~2 MB, the JPEG a fifth of that; the stitching works on grey and
 * doesn't mind). Null when it can't be read. Blocking: call off the main thread.
 */
fun shotJpeg(shot: Shot, resolver: ContentResolver, quality: Int = 90): ByteArray? = try {
    resolver.openInputStream(shot.uri)?.use { BitmapFactory.decodeStream(it) }?.let { bmp ->
        ByteArrayOutputStream().also { bmp.compress(Bitmap.CompressFormat.JPEG, quality, it); bmp.recycle() }.toByteArray()
    }
} catch (_: Exception) {
    null
}
