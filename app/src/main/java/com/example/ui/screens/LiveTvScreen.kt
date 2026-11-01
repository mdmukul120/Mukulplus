package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChannelItem
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun LiveTvScreen(
    channels: List<ChannelItem>,
    isLoading: Boolean,
    onPlayChannel: (ChannelItem) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember(channels) {
        listOf("All") + channels.map { it.displayGroup }.distinct().sorted()
    }

    val filtered = remember(channels, searchQuery, selectedCategory) {
        channels.filter { ch ->
            val inCat = selectedCategory == "All" || ch.displayGroup.equals(selectedCategory, ignoreCase = true)
            val inQuery = searchQuery.isBlank() ||
                ch.name.contains(searchQuery, ignoreCase = true) ||
                ch.group?.contains(searchQuery, ignoreCase = true) == true
            inCat && inQuery
        }
    }

    val featured = remember(channels) {
        channels.firstOrNull { it.displayGroup == "Sports" && !it.logo.isNullOrBlank() }
            ?: channels.firstOrNull { !it.logo.isNullOrBlank() }
            ?: channels.firstOrNull()
    }

    Box(modifier = modifier.fillMaxSize().background(MukulDarkBg)) {
        if (isLoading && channels.isEmpty()) {
            CircularProgressIndicator(color = MukulGold, modifier = Modifier.align(Alignment.Center))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top bar
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.statusBarsPadding()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LogoMark()
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                GlassIconButton(Icons.Rounded.Search, "Search", { searching = !searching })
                                GlassIconButton(Icons.Rounded.Refresh, "Refresh", onRefresh)
                            }
                        }
                        AnimatedVisibility(visible = searching) {
                            Box(modifier = Modifier.padding(bottom = 14.dp)) {
                                SearchPill(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    placeholder = "Search live channels...",
                                    onClose = { searching = false },
                                    modifier = Modifier
                                        .layout20Negative()
                                        .testTag("search_channels_input")
                                )
                            }
                        }
                    }
                }

                // Featured banner
                if (!searching && searchQuery.isBlank() && selectedCategory == "All" && featured != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FeaturedChannelCard(
                            channel = featured,
                            onWatch = { onPlayChannel(featured) },
                            modifier = Modifier.padding(bottom = 18.dp)
                        )
                    }
                }

                // Category chips
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilterChips(
                        options = categories,
                        selected = selectedCategory,
                        onSelect = { selectedCategory = it },
                        modifier = Modifier.layout20Negative()
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (selectedCategory == "All") "All Channels" else selectedCategory,
                            color = MukulTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("${filtered.size} live", color = MukulTextMuted, fontSize = 12.sp)
                    }
                }

                if (filtered.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(Icons.Rounded.Tv, "No channels found")
                    }
                } else {
                    items(filtered, key = { it.id }) { channel ->
                        ChannelTile(channel = channel, onClick = { onPlayChannel(channel) })
                    }
                }
            }
        }
    }
}

/**
 * The grid already applies a 20dp content padding. Full-width children such as the
 * chip row and search pill carry their own 20dp padding, so we let them bleed out.
 */
private fun Modifier.layout20Negative(): Modifier = this.layout { measurable, constraints ->
    val extra = 40.dp.roundToPx()
    val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extra, minWidth = constraints.maxWidth + extra))
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-extra / 2, 0)
    }
}
