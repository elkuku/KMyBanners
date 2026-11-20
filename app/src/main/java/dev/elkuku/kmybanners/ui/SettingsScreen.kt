package dev.elkuku.kmybanners.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.elkuku.kmybanners.AppLanguage
import dev.elkuku.kmybanners.AppTheme
import dev.elkuku.kmybanners.BuildConfig
import dev.elkuku.kmybanners.R

/** The dev build (debug, or the published dev APK): marked DEV. */
val isDev: Boolean get() = BuildConfig.DEV

/** The small "DEV" mark next to the app name in the dev build. */
@Composable
fun DevBadge() {
    Surface(color = MaterialTheme.colorScheme.tertiary, shape = MaterialTheme.shapes.small) {
        Text("DEV", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.onTertiary, style = MaterialTheme.typography.labelMedium)
    }
}

/** The settings: the language, light/dark, the GitHub account, the version. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(s: UiState, vm: MainViewModel, theme: AppTheme, onTheme: (AppTheme) -> Unit, close: () -> Unit) {
    FullScreenDialog(onDismiss = close) {
        Scaffold(topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings)) }, actions = {
                TextButton(onClick = close) { Text(stringResource(R.string.close)) }
            })
        }) { pad ->
            Column(
                Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LanguageSection()
                Section(stringResource(R.string.theme)) {
                    Choices(
                        listOf(AppTheme.SYSTEM to stringResource(R.string.theme_system),
                            AppTheme.LIGHT to stringResource(R.string.theme_light),
                            AppTheme.DARK to stringResource(R.string.theme_dark)),
                        theme, onTheme,
                    )
                }
                if (s.signedIn) {
                    Section(stringResource(R.string.github_account)) {
                        Text(s.login.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                        OutlinedButton(onClick = { close(); vm.signOut() }) { Text(stringResource(R.string.sign_out)) }
                    }
                }
                Section(stringResource(R.string.version)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.bodyLarge)
                        if (isDev) DevBadge()
                    }
                    Text(stringResource(if (isDev) R.string.build_dev else R.string.build_release),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/** The app's language, picked from a list: the phone's or one of [AppLanguage.SUPPORTED]; choosing one recreates the activity. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSection() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val current = remember { AppLanguage.get(context) }
    // The languages in their own names: findable whatever the app's language is now
    val options = listOf(null to stringResource(R.string.language_system), "en" to "English", "de" to "Deutsch",
        "es" to "Español")
    var expanded by remember { mutableStateOf(false) }
    Section(stringResource(R.string.language)) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = options.firstOrNull { it.first == current }?.second ?: options.first().second,
                onValueChange = {}, readOnly = true, singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (tag, label) ->
                    DropdownMenuItem(text = { Text(label) }, onClick = {
                        expanded = false
                        if (tag != current) activity?.let { AppLanguage.set(it, tag) }
                    })
                }
            }
        }
    }
}

/** The activity behind a context (in a dialog the context is a wrapper around it). */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun <T> Choices(options: List<Pair<T, String>>, selected: T, choose: (T) -> Unit) {
    Column(Modifier.selectableGroup()) {
        options.forEach { (value, label) ->
            Row(
                Modifier.fillMaxWidth().selectable(selected = value == selected, onClick = { choose(value) },
                    role = Role.RadioButton).padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = value == selected, onClick = null)
                Text(label, Modifier.padding(start = 12.dp))
            }
        }
    }
}
