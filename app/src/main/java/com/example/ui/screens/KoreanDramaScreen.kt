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
import androidx.compose.material.icons.filled.Star
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
import com.example.data.model.DramaDetailData
import com.example.data.model.DramaEpisodeItem
import com.example.data.model.DramaItem
import com.example.ui.theme.*
import kotlinx.coroutines.launch

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
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    var selectedDramaForDetail by remember { mutableStateOf<DramaDetailData?>(null) }
    var isDetailLoading by remember { mutableStateOf(false) }

    val categories = listOf("All", "Korean", "Chinese", "Japanese", "Indian")

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
            placeholder = { Text("Search K-Drama, C-Drama (Hindi Dubbed)...", color = MukulTextMuted, fontSize = 14.sp) },
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
                .testTag("search_drama_input")
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
                        text = if (category == "All") "All Dramas" else "$category Dub",
                        color = if (isSelected) MukulDarkBg else MukulTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        if (isLoading && dramas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MukulGold)
            }
        } else {
            // Drama Grid
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "$selectedCategory Dramas (${dramas.size})",
                        color = MukulTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(dramas.chunked(3)) { rowDramas ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (drama in rowDramas) {
                            DramaCard(
                                drama = drama,
                                onClick = {
                                    scope.launch {
                                        isDetailLoading = true
                                        val detail = onLoadDetail(drama.url)
                                        isDetailLoading = false
                                        selectedDramaForDetail = detail
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowDramas.size < 3) {
                            repeat(3 - rowDramas.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Loading Detail Dialog
        if (isDetailLoading) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                containerColor = MukulDarkSurface,
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = MukulGold)
                        Text("Loading episodes...", color = MukulTextPrimary)
                    }
                }
            )
        }

        // Drama Detail & Episode Selector Dialog
        selectedDramaForDetail?.let { detail ->
            AlertDialog(
                onDismissRequest = { selectedDramaForDetail = null },
                containerColor = MukulDarkSurface,
                titleContentColor = MukulTextPrimary,
                textContentColor = MukulTextSecondary,
                title = {
                    Text(
                        text = detail.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
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
                                    model = detail.image,
                                    contentDescription = detail.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                detail.rating?.let { rate ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = MukulGold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("$rate ★", color = MukulGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                                detail.tag?.let {
                                    Text(it, color = MukulCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                                detail.genres?.let {
                                    Text(it, color = MukulTextMuted, fontSize = 11.sp, maxLines = 2)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (!detail.description.isNullOrBlank()) {
                            Text(
                                text = detail.description,
                                color = MukulTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Episodes or Movie Play
                        val episodes = detail.episodes ?: emptyList()
                        if (episodes.isNotEmpty()) {
                            Text(
                                text = "Episodes (${episodes.size}):",
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
                                items(episodes) { ep ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MukulDarkCard)
                                            .border(1.dp, MukulBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedDramaForDetail = null
                                                // If movie/drama has direct embedUrl or watch
                                                val embed = detail.embedUrl ?: "https://hindidrama.net${ep.url}"
                                                onPlayDrama(ep.title.ifBlank { detail.title }, embed)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ep.title.ifBlank { "Episode ${ep.number ?: ""}" },
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
                        } else if (!detail.embedUrl.isNullOrBlank()) {
                            Button(
                                onClick = {
                                    selectedDramaForDetail = null
                                    onPlayDrama(detail.title, detail.embedUrl)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MukulGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MukulDarkBg)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Watch Movie", color = MukulDarkBg, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedDramaForDetail = null }) {
                        Text("Close", color = MukulTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun DramaCard(
    drama: DramaItem,
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
                model = drama.cleanImageUrl,
                contentDescription = drama.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Country Pill
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when {
                            drama.country?.contains("Korean", ignoreCase = true) == true -> MukulLiveRed
                            drama.country?.contains("Chinese", ignoreCase = true) == true -> Color(0xFF1E88E5)
                            drama.country?.contains("Japanese", ignoreCase = true) == true -> Color(0xFF7B1FA2)
                            else -> MukulGold
                        }
                    )
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = drama.displayCountry,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Rating
            if (!drama.rating.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "★ ${drama.rating}",
                        color = MukulGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = drama.title,
            color = MukulTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = drama.genres ?: drama.year ?: "Drama",
            color = MukulTextMuted,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
