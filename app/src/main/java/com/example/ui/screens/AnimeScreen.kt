package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AnimeItem
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeScreen(
    animeList: List<AnimeItem>,
    isLoading: Boolean,
    onCategorySelected: (String) -> Unit,
    onSearch: (String) -> Unit,
    onPlayAnime: (title: String, streamUrl: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Trending") }
    var detailAnime by remember { mutableStateOf<AnimeItem?>(null) }

    val categories = listOf("Trending", "Top Airing", "Most Popular", "Movies", "Action", "Isekai", "Series")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MukulDarkBg)
    ) {
        if (isLoading && animeList.isEmpty()) {
            CircularProgressIndicator(color = MukulGold, modifier = Modifier.align(Alignment.Center))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            GlassIconButton(Icons.Rounded.Search, "Search", {
                                searching = !searching
                                if (!searching) {
                                    searchQuery = ""
                                    onCategorySelected(selectedCategory)
                                }
                            })
                        }

                        AnimatedVisibility(visible = searching) {
                            Box(modifier = Modifier.padding(bottom = 14.dp)) {
                                SearchPill(
                                    query = searchQuery,
                                    onQueryChange = {
                                        searchQuery = it
                                        if (it.length >= 2) onSearch(it)
                                        else if (it.isEmpty()) onCategorySelected(selectedCategory)
                                    },
                                    placeholder = "Search Anime (Hindi, English Dub)...",
                                    onClose = {
                                        searching = false
                                        searchQuery = ""
                                        onCategorySelected(selectedCategory)
                                    },
                                    modifier = Modifier
                                        .layout20Negative()
                                        .testTag("search_anime_input")
                                )
                            }
                        }
                    }
                }

                // Filter chips
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilterChips(
                        options = categories,
                        selected = selectedCategory,
                        onSelect = {
                            selectedCategory = it
                            searchQuery = ""
                            searching = false
                            onCategorySelected(it)
                        },
                        modifier = Modifier.layout20Negative()
                    )
                }

                // Header
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$selectedCategory Anime",
                            color = MukulTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("${animeList.size} titles", color = MukulTextMuted, fontSize = 12.sp)
                    }
                }

                if (animeList.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(Icons.Rounded.AutoAwesome, "No anime found")
                    }
                } else {
                    items(animeList, key = { it.link + it.title }) { anime ->
                        AnimePosterTile(
                            anime = anime,
                            onClick = { detailAnime = anime }
                        )
                    }
                }
            }
        }

        // Anime Detail Bottom Sheet
        detailAnime?.let { anime ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { detailAnime = null },
                sheetState = sheetState,
                containerColor = MukulDarkSurface,
                contentColor = MukulTextPrimary,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                AnimeDetailSheet(
                    anime = anime,
                    onPlay = { title, url ->
                        detailAnime = null
                        onPlayAnime(title, url)
                    }
                )
            }
        }
    }
}

@Composable
private fun AnimePosterTile(
    anime: AnimeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MukulDarkCard)
                .border(1.dp, MukulBorder, RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = anime.cleanImageUrl,
                contentDescription = anime.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.6f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.9f)
                        )
                    )
            )

            // Status Pill
            Text(
                text = anime.status.uppercase(),
                color = MukulDarkBg,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (anime.status) {
                            "Complete" -> MukulGold
                            "Movie" -> Color(0xFFB44FFF)
                            else -> MukulCyan
                        }
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )

            // Dub Tag
            Text(
                text = "HINDI DUB",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = anime.title,
            color = MukulTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = anime.epInfo,
            color = MukulTextMuted,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AnimeDetailSheet(
    anime: AnimeItem,
    onPlay: (title: String, streamUrl: String) -> Unit
) {
    val episodes = anime.episodeUrls

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MukulDarkCard)
            ) {
                AsyncImage(
                    model = anime.cleanImageUrl,
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = anime.status,
                        color = MukulDarkBg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MukulCyan)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                    Text(
                        text = "Hindi / Eng / Jap",
                        color = MukulTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = anime.epInfo,
                    color = MukulGoldBright,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = "Watch ${anime.title} with multi-audio dubbing including Hindi ORG and Japanese voiceovers with English subtitles.",
            color = MukulTextSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        if (episodes.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Episodes (${episodes.size})",
                color = MukulTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(episodes.indices.toList()) { idx ->
                    val streamUrl = episodes[idx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MukulDarkCard)
                            .border(1.dp, MukulBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                onPlay("${anime.title} - Episode ${idx + 1}", streamUrl)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Episode ${idx + 1} (HD)",
                            color = MukulTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = MukulGold, modifier = Modifier.size(20.dp))
                    }
                }
            }
        } else {
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = "Watch Stream",
                icon = Icons.Rounded.PlayArrow,
                onClick = { onPlay(anime.title, anime.link) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun Modifier.layout20Negative(): Modifier = this.layout { measurable, constraints ->
    val extra = 40.dp.roundToPx()
    val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extra, minWidth = constraints.maxWidth + extra))
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-extra / 2, 0)
    }
}
