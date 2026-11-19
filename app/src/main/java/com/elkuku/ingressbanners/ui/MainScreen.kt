package com.elkuku.ingressbanners.ui

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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(s: UiState, vm: MainViewModel) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Ingress Banners") }, actions = {
            if (s.signedIn) TextButton(onClick = vm::signOut) { Text("Sign out") }
        })
    }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            s.error?.let { ErrorCard(it, vm::dismissError) }
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
    Text("Your Ingress mission banners on a web page of your own.", style = MaterialTheme.typography.titleMedium)
    Text("The app keeps the page in a repository on your GitHub account and publishes it with GitHub Pages. " +
        "Sign in with GitHub to start.")
    val code = s.code
    if (code == null) {
        Button(onClick = vm::signIn, enabled = !s.busy) { Text("Sign in with GitHub") }
    } else {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Enter this code on GitHub and approve the app:")
                Text(code.userCode, fontFamily = FontFamily.Monospace, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Button(onClick = {
                    clipboard.setText(AnnotatedString(code.userCode))
                    uri.openUri(code.verificationUri)
                }) { Text("Copy the code and open GitHub") }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LinearProgressIndicator(Modifier.weight(1f).padding(top = 8.dp))
                    TextButton(onClick = vm::cancelSignIn) { Text("Cancel") }
                }
                Text("Waiting for GitHub…", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CreatePage(s: UiState, vm: MainViewModel) {
    var agent by remember { mutableStateOf("") }
    Text("Signed in as ${s.login}", style = MaterialTheme.typography.titleMedium)
    Text("The app creates the repository ${s.login}/${MainViewModel.REPO} for your page. " +
        "It is public: the page shows on a map where you did banners.")
    OutlinedTextField(agent, { agent = it }, label = { Text("Agent name") }, singleLine = true,
        modifier = Modifier.fillMaxWidth())
    Button(onClick = { vm.createPage(agent) }, enabled = !s.busy && agent.isNotBlank()) { Text("Create my page") }
    if (s.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
}

@Composable
private fun Page(s: UiState, vm: MainViewModel) {
    val uri = LocalUriHandler.current
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Your page", style = MaterialTheme.typography.titleMedium)
            val url = s.pageUrl
            if (url != null && (s.build == null || s.build.done)) {
                TextButton(onClick = { uri.openUri(url) }) { Text(url) }
            } else if (s.build != null) {
                Text("Building…")
            } else {
                Text("Not published yet: share your mission medal screenshots.")
            }
            TextButton(onClick = { uri.openUri("https://github.com/${s.repo}") }) { Text("github.com/${s.repo}") }
        }
    }
    when {
        s.uploaded != null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Uploading screenshot ${s.uploaded} of ${s.shots.size}…")
            LinearProgressIndicator({ s.uploaded / s.shots.size.coerceAtLeast(1).toFloat() }, Modifier.fillMaxWidth())
        }
        s.shots.isNotEmpty() -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${s.shots.size} screenshots, from ${s.shots.first().name} to ${s.shots.last().name}")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = vm::upload, enabled = !s.busy) { Text("Upload and build my page") }
                OutlinedButton(onClick = vm::clearShots) { Text("Discard") }
            }
        }
        s.build != null -> BuildCard(s.build, uri::openUri)
        else -> HowTo()
    }
    if (s.uploaded == null && s.shots.isEmpty()) DoneList(s, vm)
    s.bannergressLogin?.let { BannergressLoginDialog(it, vm::bannergressCode, vm::bannergressCancel) }
}

/** The Bannergress done list: the page matches the medals with its banner pictures. */
@Composable
private fun DoneList(s: UiState, vm: MainViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Your Bannergress done list", style = MaterialTheme.typography.titleMedium)
            Text("The page finds your banners by comparing your medals with the pictures of the banners on " +
                "your Bannergress done list. Upload it again after you marked new banners done.")
            if (!s.bannergress) {
                Button(onClick = vm::bannergressSignIn, enabled = !s.busy) { Text("Sign in to Bannergress") }
            } else {
                Button(onClick = vm::uploadDoneList, enabled = !s.busy && s.build?.done != false) {
                    Text("Upload my done list")
                }
                s.doneUploaded?.let { Text("$it banners uploaded.", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun BuildCard(b: Build, open: (String) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                when {
                    !b.done -> "Building your page (${b.status.replace('_', ' ')})…"
                    b.conclusion == "success" -> "Your page is up to date."
                    else -> "The build failed (${b.conclusion})."
                },
            )
            if (!b.done) LinearProgressIndicator(Modifier.fillMaxWidth())
            b.url?.let { TextButton(onClick = { open(it) }) { Text("Show the build on GitHub") } }
        }
    }
}

@Composable
private fun HowTo() {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Add your mission medals", style = MaterialTheme.typography.titleMedium)
            Text("1. In Ingress, open your agent profile, scroll to the mission medals and tap the arrow next to them.")
            Text("2. Take a screenshot, scroll about half a screen, wait until the pictures have loaded, " +
                "take the next one — down to the end of the list.")
            Text("3. Select the screenshots in your gallery and share them with Ingress Banners.")
        }
    }
}

@Composable
private fun ErrorCard(message: String, dismiss: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(message, Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = dismiss) { Text("OK") }
        }
    }
}
