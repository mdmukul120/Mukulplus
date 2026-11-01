package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChannelItem
import com.example.ui.components.ChannelCard
import com.example.ui.components.HeroBanner
import com.example.ui.theme.*

@Composable
fun LiveTvScreen(
    channels: List<ChannelItem>,
    favoriteChannelIds: Set<String>,
    isLoading: Boolean,
    onToggleFavorite: (String) -> Unit,
    onPlayChannel: (ChannelItem) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember(channels) {
        val groups = channels.map { it.displayGroup }.distinct().sorted()
        listOf("All") + groups
    }

    val filteredChannels = remember(channels, searchQuery, selectedCategory) {
        channels.filter { channel ->
            val matchesCategory = selectedCategory == "All" || channel.displayGroup.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() || channel.name.contains(searchQuery, ignoreCase = true) ||
                    (channel.group?.contains(searchQuery, ignoreCase = true) == true)
            matchesCategory && matchesQuery
        }
    }

    val featuredChannel = remember(channels) {
        channels.find { it.name.contains("ZEE", ignoreCase = true) || it.group?.contains("Sports", ignoreCase = true) == true }
            ?: channels.firstOrNull()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MukulDarkBg)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search 600+ Live Channels...", color = MukulTextMuted, fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MukulGold)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
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
                .testTag("search_channels_input")
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
                        .clickable { selectedCategory = category }
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

        if (isLoading && channels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MukulGold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Featured Hero Banner
                if (searchQuery.isBlank() && selectedCategory == "All" && featuredChannel != null) {
                    item {
                        HeroBanner(
                            channel = featuredChannel,
                            onWatchClick = { onPlayChannel(featuredChannel) },
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                // Header with channel count & refresh
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredChannels.size} Channels Available",
                            color = MukulTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MukulGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (filteredChannels.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = MukulTextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No channels match \"$searchQuery\"",
                                    color = MukulTextMuted,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredChannels, key = { it.id }) { channel ->
                        ChannelCard(
                            channel = channel,
                            isFavorite = favoriteChannelIds.contains(channel.id),
                            onToggleFavorite = { onToggleFavorite(channel.id) },
                            onClick = { onPlayChannel(channel) }
                        )
                    }
                }
            }
        }
    }
}
