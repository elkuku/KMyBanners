package com.elkuku.ingressbanners.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.elkuku.ingressbanners.BannerOption
import com.elkuku.ingressbanners.Correction

/**
 * One row to check at a time: the row's medals and the closest banners of the done list. The agent picks the
 * banner, says it isn't on Bannergress, or skips the row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDialog(s: UiState, vm: MainViewModel) {
    val i = s.reviewing ?: return
    val row = s.toCheck[i]
    val decided = s.decisions[row.fp]
    var notOnBannergress by remember(row.fp) { mutableStateOf(false) }
    var name by remember(row.fp) { mutableStateOf(decided?.name.orEmpty()) }

    Dialog(onDismissRequest = vm::closeReview, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("Medal row ${i + 1} of ${s.toCheck.size}") }, actions = {
                TextButton(onClick = vm::closeReview) { Text("Done") }
            })
        }) { pad ->
            Column(
                Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("These medals in your list:")
                AsyncImage(model = s.pageUrl!!.trimEnd('/') + "/" + row.img, contentDescription = "The medal row",
                    modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
                Text("Which banner are they from?", style = MaterialTheme.typography.titleMedium)
                if (row.options.isEmpty()) Text("No banner of your done list looks like them.")
                row.options.forEach { o ->
                    OptionCard(o, chosen = decided?.id == o.id) { vm.decide(Correction(row.fp, o.id)) }
                }
                if (!notOnBannergress) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { notOnBannergress = true }) { Text("Not on Bannergress") }
                        OutlinedButton(onClick = { vm.decide(null) }) { Text("Skip") }
                    }
                } else {
                    OutlinedTextField(name, { name = it }, label = { Text("Name (as in Ingress, optional)") },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { vm.decide(Correction(row.fp, null, name.trim().ifEmpty { "Not on Bannergress" })) }) {
                        Text("Save as not on Bannergress")
                    }
                }
                if (i > 0) TextButton(onClick = vm::previousRow) { Text("Previous row") }
            }
        }
    }
}

@Composable
private fun OptionCard(o: BannerOption, chosen: Boolean, pick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = pick),
        border = if (chosen) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = o.pictureUrl, contentDescription = o.title, modifier = Modifier.width(140.dp),
                contentScale = ContentScale.FillWidth)
            Column(Modifier.weight(1f)) {
                Text(o.title, style = MaterialTheme.typography.titleSmall)
                o.missions?.let { Text("$it missions", style = MaterialTheme.typography.bodySmall) }
                if (chosen) Text("Your choice", color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
