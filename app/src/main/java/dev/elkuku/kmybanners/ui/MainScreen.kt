package dev.elkuku.kmybanners.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.elkuku.kmybanners.AppTheme
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import dev.elkuku.kmybanners.R
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(s: UiState, vm: MainViewModel, theme: AppTheme, onTheme: (AppTheme) -> Unit) {
    var settings by rememberSaveable { mutableStateOf(false) }
    Scaffold(topBar = {
        TopAppBar(title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.app_name))
                if (isDev) DevBadge()
            }
        }, actions = {
            TextButton(onClick = { settings = true }) { Text(stringResource(R.string.settings)) }
        })
    }) { pad ->
        if (settings) SettingsDialog(s, vm, theme, onTheme) { settings = false }
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            s.error?.let { ErrorCard(errorText(it), vm::dismissError) }
            when {
                s.loading -> CircularProgressIndicator()
                !s.signedIn -> SignIn(s, vm)
                s.repo == null -> CreatePage(s, vm)
                else -> Page(s, vm)
            }
        }
    }
}

@Composable
private fun SignIn(s: UiState, vm: MainViewModel) {
    val uri = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    Text(stringResource(R.string.intro_title), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(R.string.intro_text))
    val code = s.code
    if (code == null) {
        Button(onClick = vm::signIn, enabled = !s.busy) { Text(stringResource(R.string.sign_in_github)) }
    } else {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.enter_code))
                Text(code.userCode, fontFamily = FontFamily.Monospace, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.code_hint), style = MaterialTheme.typography.bodySmall)
                Button(onClick = {
                    clipboard.setText(AnnotatedString(code.userCode))
                    uri.openUri(code.verificationUri)
                }) { Text(stringResource(R.string.copy_code)) }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LinearProgressIndicator(Modifier.weight(1f).padding(top = 8.dp))
                    TextButton(onClick = vm::cancelSignIn) { Text(stringResource(R.string.cancel)) }
                }
                Text(stringResource(R.string.waiting_github), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CreatePage(s: UiState, vm: MainViewModel) {
    var agent by remember { mutableStateOf("") }
    Text(stringResource(R.string.signed_in_as, s.login.orEmpty()), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(R.string.create_text, "${s.login}/${MainViewModel.REPO}"))
    OutlinedTextField(agent, { agent = it }, label = { Text(stringResource(R.string.agent_name)) }, singleLine = true,
        modifier = Modifier.fillMaxWidth())
    Button(onClick = { vm.createPage(agent) }, enabled = !s.busy && agent.isNotBlank()) { Text(stringResource(R.string.create_page)) }
    if (s.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
}

@Composable
private fun Page(s: UiState, vm: MainViewModel) {
    PageCard(s, vm)
    when {
        // Screenshots shared from the gallery: upload them first
        s.uploaded != null -> Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.uploading, s.uploaded, s.shots.size))
                LinearProgressIndicator({ s.uploaded / s.shots.size.coerceAtLeast(1).toFloat() }, Modifier.fillMaxWidth())
            }
        }
        s.shots.isNotEmpty() -> Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(pluralStringResource(R.plurals.shots_shared, s.shots.size, s.shots.size),
                    style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.shots_range, s.shots.first().name, s.shots.last().name),
                    style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = vm::upload, enabled = !s.busy) { Text(stringResource(R.string.upload_build)) }
                    OutlinedButton(onClick = vm::clearShots) { Text(stringResource(R.string.discard)) }
                }
            }
        }
        else -> {
            DoneStep(s, vm)
            ShotsStep(s)
            CheckStep(s, vm)
        }
    }
    s.bannergressLogin?.let { BannergressLoginDialog(it, vm::bannergressCode, vm::bannergressCancel) }
    MyBannersDialog(s, vm)
    ReviewDialog(s, vm)
}

/** The page: open it, its numbers, the build; the less frequent actions as links below. */
@Composable
private fun PageCard(s: UiState, vm: MainViewModel) {
    val uri = LocalUriHandler.current
    val b = s.build
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.your_page), style = MaterialTheme.typography.titleLarge)
            val banners = s.banners.count { it.status == "match" && !it.single }
            if (s.banners.isNotEmpty()) {
                Text(
                    listOf(pluralStringResource(R.plurals.banners_count, banners, number(banners)),
                        s.banners.sumOf { it.count }.let { pluralStringResource(R.plurals.missions, it, number(it)) },
                        stringResource(R.string.to_check_count, number(s.toCheck.size))).joinToString(" · "),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            when {
                b != null && !b.done -> {
                    Text(stringResource(R.string.build_running,
                        stringResource(if (b.status == "queued") R.string.build_queued else R.string.build_in_progress)))
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                b != null && b.conclusion != "success" -> {
                    Text(stringResource(R.string.build_failed, b.conclusion.orEmpty()), color = MaterialTheme.colorScheme.error)
                    Button(onClick = vm::buildAgain, enabled = !s.busy) { Text(stringResource(R.string.try_again)) }
                }
                s.pageUrl == null -> Text(stringResource(R.string.not_published))
                else -> Button(onClick = { uri.openUri(s.pageUrl) }, Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.open_page))
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (s.banners.isNotEmpty()) TextButton(onClick = vm::openMyBanners) { Text(stringResource(R.string.my_banners)) }
                if (b?.done != false && s.pageUrl != null) {
                    TextButton(onClick = vm::buildAgain, enabled = !s.busy) { Text(stringResource(R.string.build_again)) }
                }
                b?.url?.let { TextButton(onClick = { uri.openUri(it) }) { Text(stringResource(R.string.show_build)) } }
                TextButton(onClick = { uri.openUri("https://github.com/${s.repo}") }) { Text("GitHub") }
            }
        }
    }
}

/** A numbered step: its title, whether it is done, a line about its state, and its action. */
@Composable
private fun Step(number: Int, title: String, done: Boolean, status: String, content: @Composable () -> Unit = {}) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(
                shape = CircleShape,
                color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (done) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (done) "✓" else "$number", style = MaterialTheme.typography.titleMedium)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                content()
            }
        }
    }
}

/** ① The Bannergress done list: the page matches the medals with its banners' pictures. */
@Composable
private fun DoneStep(s: UiState, vm: MainViewModel) {
    val done = s.doneUploaded != null
    var info by rememberSaveable { mutableStateOf(false) }
    Step(1, stringResource(R.string.done_title), done,
        if (done) listOfNotNull(pluralStringResource(R.plurals.banners_count, s.doneUploaded!!, number(s.doneUploaded)),
            s.doneSyncedAt?.take(10)?.let { stringResource(R.string.done_on, it) }).joinToString(" · ")
        else stringResource(R.string.done_not_yet)) {
        if (done) {
            // Done: upload again (signing in to Bannergress first when needed)
            OutlinedButton(onClick = { if (s.bannergress) vm.uploadDoneList() else vm.bannergressSignIn() },
                enabled = !s.busy && s.build?.done != false) {
                Text(stringResource(R.string.upload_done_again))
            }
        } else if (!s.bannergress) {
            Button(onClick = vm::bannergressSignIn, enabled = !s.busy) { Text(stringResource(R.string.sign_in_bannergress)) }
        } else {
            Button(onClick = vm::uploadDoneList, enabled = !s.busy && s.build?.done != false) {
                Text(stringResource(R.string.upload_done))
            }
        }
        TextButton(onClick = { info = !info }, contentPadding = PaddingValues(0.dp)) {
            Text(stringResource(if (info) R.string.less else R.string.why))
        }
        if (info) Text(stringResource(R.string.done_text), style = MaterialTheme.typography.bodySmall)
    }
}

/** ② The mission medal screenshots, shared from the gallery. */
@Composable
private fun ShotsStep(s: UiState) {
    val rows = s.banners.sumOf { e -> e.rows.split(',').sumOf { r -> r.split('-').let { (it.last().toInt() - it.first().toInt()) + 1 } } }
    // Open until the page has screenshots, unless the agent opened or closed it
    var toggled by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val howto = toggled ?: s.banners.isEmpty()
    Step(2, stringResource(R.string.shots_title), s.banners.isNotEmpty(),
        if (s.banners.isNotEmpty()) pluralStringResource(R.plurals.shots_status, rows, number(rows)) + " " + stringResource(R.string.shots_update)
        else stringResource(R.string.shots_none)) {
        TextButton(onClick = { toggled = !howto }, contentPadding = PaddingValues(0.dp)) {
            Text(stringResource(if (howto) R.string.less else R.string.howto_show))
        }
        if (howto) {
            Text(stringResource(R.string.howto_1), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.howto_2), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.howto_3), style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** ③ The medal rows the build could not match for sure, and the decisions not saved yet. */
@Composable
private fun CheckStep(s: UiState, vm: MainViewModel) {
    if (s.banners.isEmpty()) return
    Step(3, stringResource(R.string.check_title), s.toCheck.isEmpty() && s.decisions.isEmpty(),
        if (s.toCheck.isEmpty()) stringResource(R.string.check_none)
        else pluralStringResource(R.plurals.check_text, s.toCheck.size, s.toCheck.size)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (s.toCheck.isNotEmpty()) {
                Button(onClick = vm::startReview, enabled = !s.busy) { Text(stringResource(R.string.check_them)) }
            }
            if (s.decisions.isNotEmpty()) {
                Button(onClick = vm::saveDecisions, enabled = !s.busy && s.build?.done != false) {
                    Text(pluralStringResource(R.plurals.save_decisions, s.decisions.size, s.decisions.size))
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String, dismiss: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(message, Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = dismiss) { Text(stringResource(R.string.ok)) }
        }
    }
}

/** The message of an error, in the phone's language. */
@Composable
private fun errorText(e: UiError): String = if (e.plural != 0) {
    pluralStringResource(e.plural, e.count, *e.args.toTypedArray())
} else {
    stringResource(e.res, *e.args.toTypedArray())
}

/** A count with the language's thousands separator (2,465 / 2.465). */
private fun number(n: Int): String = java.text.NumberFormat.getIntegerInstance().format(n)
