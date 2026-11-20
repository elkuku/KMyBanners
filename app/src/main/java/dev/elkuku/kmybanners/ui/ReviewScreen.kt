package dev.elkuku.kmybanners.ui

import androidx.compose.foundation.BorderStroke
import io.ktor.http.encodeURLPathPart
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.elkuku.kmybanners.BannerOption
import dev.elkuku.kmybanners.Choice
import dev.elkuku.kmybanners.PageBanner
import dev.elkuku.kmybanners.R

/** What an entry is now (on the page) or will be (an unsaved choice), in words. */
@Composable
private fun label(e: PageBanner): String = when {
    e.single -> stringResource(R.string.single_missions)
    e.status == "match" -> e.title ?: e.id.orEmpty()
    e.status == "none" -> stringResource(R.string.status_none, e.name.orEmpty())
    else -> stringResource(R.string.status_review)
}

@Composable
private fun label(c: Choice): String = when {
    c.single -> stringResource(R.string.single_missions)
    c.id != null -> c.title ?: c.id
    else -> stringResource(R.string.status_none, c.name.orEmpty())
}

/**
 * One entry at a time (the rows to check, or one entry from "My banners"): its medals, what it is now, and the
 * choices: a suggested banner, a Bannergress link, single missions, not on Bannergress, skip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDialog(s: UiState, vm: MainViewModel) {
    val i = s.reviewing ?: return
    val row = s.reviewList[i]
    val decided = s.decisions[row.fp]?.second
    var notOnBannergress by remember(row.fp) { mutableStateOf(false) }
    var name by remember(row.fp) { mutableStateOf(decided?.name ?: row.name.orEmpty()) }
    var link by remember(row.fp) { mutableStateOf("") }
    val editing = s.reviewList.size == 1 && row.status != "review"

    FullScreenDialog(onDismiss = vm::closeReview) {
        Scaffold(topBar = {
            TopAppBar(title = {
                Text(if (editing) stringResource(R.string.review_change_title)
                else stringResource(R.string.review_title, i + 1, s.reviewList.size))
            }, actions = {
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
                EntryInfo(row)
                decided?.let {
                    Text(stringResource(R.string.changed_to, label(it)), color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium)
                }

                Text(stringResource(R.string.review_which), style = MaterialTheme.typography.titleMedium)
                if (row.options.isEmpty() && row.status == "review") Text(stringResource(R.string.review_none))
                row.options.forEach { o ->
                    OptionCard(o, chosen = decided?.id == o.id) { vm.decide(Choice(id = o.id, title = o.title)) }
                }

                // A Bannergress link, checked before it can be used
                HorizontalDivider()
                Text(stringResource(R.string.bannergress_link), style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(link, { link = it }, placeholder = { Text(stringResource(R.string.link_hint)) },
                        singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { vm.checkLink(link) }, enabled = link.isNotBlank() && !s.busy) {
                        Text(stringResource(R.string.check_link))
                    }
                }
                s.lookup?.let { l ->
                    when {
                        l.banner != null -> {
                            OptionCard(l.banner, chosen = decided?.id == l.banner.id) {
                                vm.decide(Choice(id = l.banner.id, title = l.banner.title))
                            }
                            Button(onClick = { vm.decide(Choice(id = l.banner.id, title = l.banner.title)) }) {
                                Text(stringResource(R.string.use_banner))
                            }
                        }
                        l.notFound -> Text(stringResource(R.string.link_not_found), color = MaterialTheme.colorScheme.error)
                        else -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }

                HorizontalDivider()
                if (!notOnBannergress) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { vm.decide(Choice(single = true)) }) {
                            Text(stringResource(R.string.single_missions))
                        }
                        OutlinedButton(onClick = { notOnBannergress = true }) { Text(stringResource(R.string.not_on_bannergress)) }
                        if (!editing) OutlinedButton(onClick = { vm.decide(null) }) { Text(stringResource(R.string.skip)) }
                    }
                } else {
                    OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.review_name)) },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { vm.decide(Choice(name = name.trim().ifEmpty { "Not on Bannergress" })) }) {
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
                if (o.nearby) Text(stringResource(R.string.nearby_label), style = MaterialTheme.typography.bodySmall)
                if (chosen) Text(stringResource(R.string.your_choice), color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/** What an entry is: its banner or what it is now, its medals and missions, place, length and event day. */
@Composable
private fun EntryInfo(e: PageBanner) {
    val uri = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(if (e.status == "review") stringResource(R.string.unassigned) else label(e),
            style = MaterialTheme.typography.titleMedium,
            color = if (e.status == "review") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
        val counts = buildList {
            add(pluralStringResource(R.plurals.medals, e.count, number(e.count)))
            e.missions?.takeIf { e.id != null && it != e.count }?.let { add(pluralStringResource(R.plurals.missions, it, number(it))) }
        }
        Text(counts.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
        listOfNotNull(e.addr, e.km?.let { "%.1f km".format(it) }, e.event?.distinct()?.joinToString(" – ")).takeIf { it.isNotEmpty() }?.let {
            Text(it.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
        }
        e.id?.let { id ->
            TextButton(onClick = { uri.openUri("https://bannergress.com/banner/${id.encodeURLPathPart()}") },
                contentPadding = PaddingValues(0.dp)) { Text(stringResource(R.string.open_bannergress)) }
        }
    }
}

/** Every medal row group of the page as its medals, a dot on the ones not assigned yet; a tap opens it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBannersDialog(s: UiState, vm: MainViewModel) {
    if (!s.myBanners) return
    FullScreenDialog(onDismiss = vm::closeMyBanners) {
        Scaffold(topBar = {
            TopAppBar(title = { Text(stringResource(R.string.my_banners)) }, actions = {
                TextButton(onClick = vm::closeMyBanners) { Text(stringResource(R.string.close)) }
            })
        }) { pad ->
            LazyColumn(
                Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(s.banners, key = { it.fp + it.rows }) { e ->
                    // Not assigned: still to check, and nothing picked for it yet
                    val open = e.status == "review" && s.decisions[e.fp] == null
                    Box(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { vm.edit(e) }) {
                        AsyncImage(model = s.pageUrl!!.trimEnd('/') + "/" + e.img, contentDescription = label(e),
                            modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
                        if (open) {
                            Box(Modifier.align(Alignment.TopEnd).padding(4.dp).size(12.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape))
                        }
                    }
                }
            }
        }
    }
}
