package dev.elkuku.kmybanners.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
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
fun MainScreen(s: UiState, vm: MainViewModel) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.app_name)) }, actions = {
            if (s.signedIn) TextButton(onClick = vm::signOut) { Text(stringResource(R.string.sign_out)) }
        })
    }) { pad ->
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
    val uri = LocalUriHandler.current
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.your_page), style = MaterialTheme.typography.titleMedium)
            val url = s.pageUrl
            if (url != null && (s.build == null || s.build.done)) {
                TextButton(onClick = { uri.openUri(url) }) { Text(url) }
            } else if (s.build != null) {
                Text(stringResource(R.string.building))
            } else {
                Text(stringResource(R.string.not_published))
            }
            TextButton(onClick = { uri.openUri("https://github.com/${s.repo}") }) { Text("github.com/${s.repo}") }
            if (s.build?.done != false && s.pageUrl != null) {
                TextButton(onClick = vm::buildAgain, enabled = !s.busy) { Text(stringResource(R.string.build_again)) }
            }
            if (s.banners.isNotEmpty()) {
                TextButton(onClick = vm::openMyBanners) { Text(stringResource(R.string.my_banners)) }
            }
        }
    }
    when {
        s.uploaded != null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.uploading, s.uploaded, s.shots.size))
            LinearProgressIndicator({ s.uploaded / s.shots.size.coerceAtLeast(1).toFloat() }, Modifier.fillMaxWidth())
        }
        s.shots.isNotEmpty() -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(pluralStringResource(R.plurals.shots_shared, s.shots.size, s.shots.size) + ", " +
                stringResource(R.string.shots_range, s.shots.first().name, s.shots.last().name))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = vm::upload, enabled = !s.busy) { Text(stringResource(R.string.upload_build)) }
                OutlinedButton(onClick = vm::clearShots) { Text(stringResource(R.string.discard)) }
            }
        }
        s.build != null -> BuildCard(s.build, uri::openUri, vm::buildAgain)
        else -> HowTo()
    }
    if (s.uploaded == null && s.shots.isEmpty()) {
        if (s.toCheck.isNotEmpty() || s.decisions.isNotEmpty()) ToCheck(s, vm)
        DoneList(s, vm)
    }
    s.bannergressLogin?.let { BannergressLoginDialog(it, vm::bannergressCode, vm::bannergressCancel) }
    MyBannersDialog(s, vm)
    ReviewDialog(s, vm)
}

/** The medal rows the build could not match for sure, and the agent's decisions not saved yet. */
@Composable
private fun ToCheck(s: UiState, vm: MainViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.check_title), style = MaterialTheme.typography.titleMedium)
            if (s.toCheck.isNotEmpty()) Text(pluralStringResource(R.plurals.check_text, s.toCheck.size, s.toCheck.size))
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
}

/** The Bannergress done list: the page matches the medals with its banner pictures. */
@Composable
private fun DoneList(s: UiState, vm: MainViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.done_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.done_text))
            if (!s.bannergress) {
                Button(onClick = vm::bannergressSignIn, enabled = !s.busy) { Text(stringResource(R.string.sign_in_bannergress)) }
            } else {
                Button(onClick = vm::uploadDoneList, enabled = !s.busy && s.build?.done != false) {
                    Text(stringResource(R.string.upload_done))
                }
                s.doneUploaded?.let { Text(pluralStringResource(R.plurals.done_uploaded, it, it), style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun BuildCard(b: Build, open: (String) -> Unit, again: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                when {
                    !b.done -> stringResource(R.string.build_running,
                        stringResource(if (b.status == "queued") R.string.build_queued else R.string.build_in_progress))
                    b.conclusion == "success" -> stringResource(R.string.build_ok)
                    else -> stringResource(R.string.build_failed, b.conclusion.orEmpty())
                },
            )
            if (!b.done) LinearProgressIndicator(Modifier.fillMaxWidth())
            b.url?.let { TextButton(onClick = { open(it) }) { Text(stringResource(R.string.show_build)) } }
            if (b.done && b.conclusion != "success") Button(onClick = again) { Text(stringResource(R.string.try_again)) }
        }
    }
}

@Composable
private fun HowTo() {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.howto_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.howto_1))
            Text(stringResource(R.string.howto_2))
            Text(stringResource(R.string.howto_3))
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
