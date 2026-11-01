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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VideoLibrary
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
import com.example.data.model.DramaDetailData
import com.example.data.model.DramaItem
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KoreanDramaScreen(
    dramas: List<DramaItem>,
    isLoading: Boolean,
    onCategorySelected: (String) -> Unit,
    onSearch: (String) -> Unit,
    onLoadDetail: suspend (String) -> DramaDetailData?,
    onPlayDrama: (title: String, embedUrl: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var searching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var detailDrama by remember { mutableStateOf<DramaDetailData?>(null) }
    var isDetailLoading by remember { mutableStateOf(false) }

    val categories = listOf("All", "Korean", "Chinese", "Japanese", "Indian")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MukulDarkBg)
    ) {
        if (isLoading && dramas.isEmpty()) {
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
                                    placeholder = "Search K-Drama, C-Drama (Hindi Dub)...",
                                    onClose = {
                                        searching = false
                                        searchQuery = ""
                                        onCategorySelected(selectedCategory)
                                    },
                                    modifier = Modifier
                                        .layout20Negative()
                                        .testTag("search_drama_input")
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
                            text = if (selectedCategory == "All") "Trending Asian Drama" else "$selectedCategory Dramas",
                            color = MukulTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("${dramas.size} titles", color = MukulTextMuted, fontSize = 12.sp)
                    }
                }

                if (dramas.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(Icons.Rounded.VideoLibrary, "No dramas found")
                    }
                } else {
                    items(dramas, key = { it.url + it.title }) { drama ->
                        DramaPosterTile(
                            drama = drama,
                            onClick = {
                                scope.launch {
                                    isDetailLoading = true
                                    val detail = onLoadDetail(drama.url)
                                    isDetailLoading = false
                                    detailDrama = detail
                                }
                            }
                        )
                    }
                }
            }
        }

        // Loading overlay
        if (isDetailLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MukulGold)
            }
        }

        // Drama Detail Bottom Sheet
        detailDrama?.let { detail ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { detailDrama = null },
                sheetState = sheetState,
                containerColor = MukulDarkSurface,
                contentColor = MukulTextPrimary,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                DramaDetailSheet(
                    detail = detail,
                    onPlay = { title, url ->
                        detailDrama = null
                        onPlayDrama(title, url)
                    }
                )
            }
        }
    }
}

@Composable
private fun DramaPosterTile(
    drama: DramaItem,
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
                model = drama.cleanImageUrl,
                contentDescription = drama.title,
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

            // Country Pill
            Text(
                text = drama.displayCountry.uppercase(),
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when {
                            drama.country?.contains("Korean", true) == true -> MukulLiveRed
                            drama.country?.contains("Chinese", true) == true -> Color(0xFF1E88E5)
                            drama.country?.contains("Japanese", true) == true -> Color(0xFF7B1FA2)
                            else -> MukulGold
                        }
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )

            if (!drama.rating.isNullOrBlank()) {
                RatingPill(
                    rating = drama.rating,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = drama.title,
            color = MukulTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = drama.genres ?: drama.year ?: "Hindi Dubbed",
            color = MukulTextMuted,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DramaDetailSheet(
    detail: DramaDetailData,
    onPlay: (title: String, embedUrl: String) -> Unit
) {
    val episodes = detail.episodes ?: emptyList()

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
                    model = detail.image,
                    contentDescription = detail.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detail.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Black
                )

                if (!detail.altTitle.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = detail.altTitle,
                        color = MukulTextMuted,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    detail.tag?.let { TagPill(it) }
                    detail.country?.let { TagPill(it) }
                }

                if (!detail.rating.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, tint = MukulGoldBright, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(detail.rating, color = MukulGoldBright, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("  ★ Hindi Dub", color = MukulTextMuted, fontSize = 12.sp)
                    }
                }
            }
        }

        if (!detail.description.isNullOrBlank()) {
            Spacer(Modifier.height(18.dp))
            Text(
                text = detail.description,
                color = MukulTextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }

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
                items(episodes) { ep ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MukulDarkCard)
                            .border(1.dp, MukulBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                val url = detail.embedUrl ?: "https://hindidrama.net${ep.url}"
                                onPlay(ep.title.ifBlank { detail.title }, url)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = ep.title.ifBlank { "Episode ${ep.number ?: ""}" },
                            color = MukulTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = MukulGold, modifier = Modifier.size(20.dp))
                    }
                }
            }
        } else if (!detail.embedUrl.isNullOrBlank()) {
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = "Watch Movie",
                icon = Icons.Rounded.PlayArrow,
                onClick = { onPlay(detail.title, detail.embedUrl) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TagPill(text: String) {
    Text(
        text = text,
        color = MukulTextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

private fun Modifier.layout20Negative(): Modifier = this.layout { measurable, constraints ->
    val extra = 40.dp.roundToPx()
    val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extra, minWidth = constraints.maxWidth + extra))
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-extra / 2, 0)
    }
}
