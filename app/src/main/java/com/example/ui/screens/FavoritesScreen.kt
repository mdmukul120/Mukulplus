package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChannelItem
import com.example.data.model.MovieItem
import com.example.data.model.PlayerItem
import com.example.ui.components.ChannelCard
import com.example.ui.components.MovieCard
import com.example.ui.theme.*

@Composable
fun FavoritesScreen(
    allChannels: List<ChannelItem>,
    favoriteChannelIds: Set<String>,
    allMovies: List<MovieItem>,
    favoriteMovieIds: Set<Long>,
    onToggleFavoriteChannel: (String) -> Unit,
    onPlayChannel: (ChannelItem) -> Unit,
    onPlayMovie: (MovieItem, PlayerItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val favChannels = remember(allChannels, favoriteChannelIds) {
        allChannels.filter { favoriteChannelIds.contains(it.id) }
    }
    val favMovies = remember(allMovies, favoriteMovieIds) {
        allMovies.filter { favoriteMovieIds.contains(it.movieId) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MukulDarkBg)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MukulDarkSurface,
            contentColor = MukulGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MukulGold
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Channels (${favChannels.size})", fontWeight = FontWeight.Bold) },
                selectedContentColor = MukulGold,
                unselectedContentColor = MukulTextMuted
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Movies (${favMovies.size})", fontWeight = FontWeight.Bold) },
                selectedContentColor = MukulGold,
                unselectedContentColor = MukulTextMuted
            )
        }

        if (selectedTab == 0) {
            // Favorite Channels
            if (favChannels.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MukulTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No favorite channels added yet",
                            color = MukulTextMuted,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the heart on any channel to save it here",
                            color = MukulTextMuted.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(favChannels, key = { it.id }) { channel ->
                        ChannelCard(
                            channel = channel,
                            isFavorite = true,
                            onToggleFavorite = { onToggleFavoriteChannel(channel.id) },
                            onClick = { onPlayChannel(channel) }
                        )
                    }
                }
            }
        } else {
            // Favorite Movies
            if (favMovies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MukulTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No saved movies yet",
                            color = MukulTextMuted,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favMovies.chunked(3)) { rowMovies ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (movie in rowMovies) {
                                MovieCard(
                                    movie = movie,
                                    onClick = { onPlayMovie(movie, movie.player?.firstOrNull()) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowMovies.size < 3) {
                                repeat(3 - rowMovies.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
