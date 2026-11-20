package dev.elkuku.kmybanners

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.elkuku.kmybanners.bannergress.BannergressApi
import dev.elkuku.kmybanners.bannergress.BannergressAuth
import dev.elkuku.kmybanners.github.GitHubApi
import dev.elkuku.kmybanners.github.GitHubAuth
import dev.elkuku.kmybanners.ui.MainScreen
import dev.elkuku.kmybanners.ui.MainViewModel

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val http = httpClient()
                val auth = GitHubAuth(http, BuildConfig.GITHUB_CLIENT_ID)
                val account = Account(PrefsStore(getSharedPreferences("account", MODE_PRIVATE)), auth)
                val bgAuth = BannergressAuth(http, PrefsStore(getSharedPreferences("bannergress", MODE_PRIVATE)))
                return MainViewModel(account, auth, GitHubApi(http, account::accessToken), contentResolver,
                    bgAuth, BannergressApi(http, bgAuth::accessToken), { pageBanners(http, it) }, { lookUpBanner(http, it) }) as T
            }
        }
    }

    // Before Android 13: the language chosen in the dev build's settings
    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(AppLanguage.wrap(newBase))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handle(intent)
        setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            var theme by remember { mutableStateOf(AppTheme.get(this)) }
            val dark = when (theme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                MainScreen(state, vm, theme) { theme = it; AppTheme.set(this, it) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    /** Screenshots shared from the gallery. */
    private fun handle(intent: Intent) {
        if (isSharedImages(intent)) vm.shared(sharedShots(intent, contentResolver))
    }
}
