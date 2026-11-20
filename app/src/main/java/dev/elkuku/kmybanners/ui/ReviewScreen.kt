package dev.elkuku.kmybanners.ui

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import dev.elkuku.kmybanners.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import dev.elkuku.kmybanners.BannerOption
import dev.elkuku.kmybanners.Correction

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
            TopAppBar(title = { Text(stringResource(R.string.review_title, i + 1, s.toCheck.size)) }, actions = {
                TextButton(onClick = vm::closeReview) { Text(stringResource(R.string.review_done)) }
            })
        }) { pad ->
            Column(
                Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.review_these))
                AsyncImage(model = s.pageUrl!!.trimEnd('/') + "/" + row.img, contentDescription = stringResource(R.string.review_medals),
                    modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
                Text(stringResource(R.string.review_which), style = MaterialTheme.typography.titleMedium)
                if (row.options.isEmpty()) Text(stringResource(R.string.review_none))
                row.options.forEach { o ->
                    OptionCard(o, chosen = decided?.id == o.id) { vm.decide(Correction(row.fp, o.id)) }
                }
                if (!notOnBannergress) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { notOnBannergress = true }) { Text(stringResource(R.string.not_on_bannergress)) }
                        OutlinedButton(onClick = { vm.decide(null) }) { Text(stringResource(R.string.skip)) }
                    }
                } else {
                    OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.review_name)) },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { vm.decide(Correction(row.fp, null, name.trim().ifEmpty { "Not on Bannergress" })) }) {
                        Text(stringResource(R.string.review_save_none))
                    }
                }
                if (i > 0) TextButton(onClick = vm::previousRow) { Text(stringResource(R.string.review_previous)) }
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
                o.missions?.let { Text(pluralStringResource(R.plurals.missions, it, it), style = MaterialTheme.typography.bodySmall) }
                if (chosen) Text(stringResource(R.string.your_choice), color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
