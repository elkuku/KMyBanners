package com.elkuku.ingressbanners.ui

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elkuku.ingressbanners.bannergress.BannergressAuth
import com.elkuku.ingressbanners.bannergress.BannergressLogin

/**
 * The Bannergress sign-in in a WebView (as AgentTools and kBannerGuider): the sign-in only redirects to
 * bannergress.com, so the redirect is caught here and its code handed to [onCode].
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BannergressLoginDialog(login: BannergressLogin, onCode: (String) -> Unit, onCancel: () -> Unit) {
    val currentOnCode by rememberUpdatedState(onCode)
    val currentOnCancel by rememberUpdatedState(onCancel)
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("Sign in to Bannergress") }, actions = {
                TextButton(onClick = onCancel) { Text("Cancel") }
            })
        }) { pad ->
            AndroidView(
                modifier = Modifier.padding(pad).fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        // A browser's user agent, so the sign-in pages accept it (as kBannerGuider)
                        settings.userAgentString = USER_AGENT
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                val url = request.url.toString()
                                if (!url.startsWith(BannergressAuth.REDIRECT_URI)) return false
                                login.code(url)?.let(currentOnCode) ?: currentOnCancel()
                                return true
                            }
                        }
                        loadUrl(login.url)
                    }
                },
            )
        }
    }
}

private const val USER_AGENT =
    "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
