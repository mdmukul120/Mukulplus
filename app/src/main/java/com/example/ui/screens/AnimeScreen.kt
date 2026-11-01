package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AnimeItem
import com.example.ui.theme.*

@Composable
fun AnimeScreen(
    animeList: List<AnimeItem>,
    isLoading: Boolean,
    onCategorySelected: (String) -> Unit,
    onSearch: (String) -> Unit,
    onPlayAnime: (title: String, streamUrl: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Trending") }
    var selectedAnimeForDetail by remember { mutableStateOf<AnimeItem?>(null) }

    val categories = listOf("Trending", "Top Airing", "Most Popular", "Movies", "Action", "Isekai", "Series")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MukulDarkBg)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                if (it.length >= 2) onSearch(it)
                else if (it.isEmpty()) onCategorySelected(selectedCategory)
            },
            placeholder = { Text("Search Anime (Hindi, English Dub)...", color = MukulTextMuted, fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MukulGold)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        onCategorySelected(selectedCategory)
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MukulTextMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MukulDarkCard,
                unfocusedContainerColor = MukulDarkCard,
                focusedBorderColor = MukulGold,
                unfocusedBorderColor = MukulBorder,
                focusedTextColor = MukulTextPrimary,
                unfocusedTextColor = MukulTextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_anime_input")
        )

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) MukulGold else MukulDarkCard)
                        .clickable {
                            selectedCategory = category
                            searchQuery = ""
                            onCategorySelected(category)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) MukulDarkBg else MukulTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        if (isLoading && animeList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MukulGold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "$selectedCategory Anime (${animeList.size})",
                        color = MukulTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(animeList.chunked(3)) { rowAnimes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (anime in rowAnimes) {
                            AnimeCard(
                                anime = anime,
                                onClick = { selectedAnimeForDetail = anime },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowAnimes.size < 3) {
                            repeat(3 - rowAnimes.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Anime Detail Dialog
        selectedAnimeForDetail?.let { anime ->
            AlertDialog(
                onDismissRequest = { selectedAnimeForDetail = null },
                containerColor = MukulDarkSurface,
                titleContentColor = MukulTextPrimary,
                textContentColor = MukulTextSecondary,
                title = {
                    Text(
                        text = anime.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .width(85.dp)
                                    .height(125.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MukulDarkCard)
                            ) {
                                AsyncImage(
                                    model = anime.cleanImageUrl,
                                    contentDescription = anime.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MukulCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = anime.status,
                                        color = MukulCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Audio: Hindi / Eng / Jap",
                                    color = MukulTextSecondary,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = anime.epInfo,
                                    color = MukulGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val epUrls = anime.episodeUrls
                        if (epUrls.isNotEmpty()) {
                            Text(
                                text = "Episodes (${epUrls.size}):",
                                color = MukulTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(epUrls.indices.toList()) { idx ->
                                    val stream = epUrls[idx]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MukulDarkCard)
                                            .border(1.dp, MukulBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedAnimeForDetail = null
                                                onPlayAnime("${anime.title} - Ep ${idx + 1}", stream)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Episode ${idx + 1}",
                                            color = MukulTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = MukulGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    selectedAnimeForDetail = null
                                    onPlayAnime(anime.title, anime.link)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MukulGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MukulDarkBg)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Watch Now", color = MukulDarkBg, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedAnimeForDetail = null }) {
                        Text("Close", color = MukulTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun AnimeCard(
    anime: AnimeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MukulDarkCard)
                .border(1.dp, MukulBorder, RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = anime.cleanImageUrl,
                contentDescription = anime.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Status Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when (anime.status) {
                            "Complete" -> MukulGold
                            "Movie" -> Color(0xFFB44FFF)
                            else -> MukulCyan
                        }
                    )
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = anime.status,
                    color = MukulDarkBg,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Dual Audio Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(5.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Hindi Dub",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

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
