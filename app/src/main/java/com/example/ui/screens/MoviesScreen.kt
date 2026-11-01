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
import com.example.data.model.CatalogCategory
import com.example.data.model.MovieItem
import com.example.data.model.PlayerItem
import com.example.ui.components.MovieCard
import com.example.ui.theme.*

@Composable
fun MoviesScreen(
    categories: List<CatalogCategory>,
    favoriteMovieIds: Set<Long>,
    onToggleFavoriteMovie: (Long) -> Unit,
    onPlayMovie: (MovieItem, PlayerItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMovieForDetail by remember { mutableStateOf<MovieItem?>(null) }

    // Flatten all movies for searching
    val allMovies = remember(categories) {
        categories.flatMap { it.movies }.distinctBy { it.movieId }
    }

    val searchResults = remember(searchQuery, allMovies) {
        if (searchQuery.isBlank()) emptyList()
        else allMovies.filter {
            it.displayTitle.contains(searchQuery, ignoreCase = true) ||
                    (it.description?.contains(searchQuery, ignoreCase = true) == true)
        }
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
            placeholder = { Text("Search Movies, Series & Shows...", color = MukulTextMuted, fontSize = 14.sp) },
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
                .testTag("search_movies_input")
        )

        if (searchQuery.isNotBlank()) {
            // Search Results Grid
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Results for \"$searchQuery\" (${searchResults.size})",
                        color = MukulTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(searchResults.chunked(3)) { rowMovies ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (movie in rowMovies) {
                            MovieCard(
                                movie = movie,
                                onClick = { selectedMovieForDetail = movie },
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
        } else {
            // Category Sections
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(categories) { category ->
                    if (category.movies.isNotEmpty()) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.name,
                                    color = MukulTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(category.movies) { movie ->
                                    MovieCard(
                                        movie = movie,
                                        onClick = { selectedMovieForDetail = movie }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Movie Detail Modal
        selectedMovieForDetail?.let { movie ->
            var selectedServerIndex by remember { mutableIntStateOf(0) }
            val players = movie.player ?: emptyList()
            val isFav = favoriteMovieIds.contains(movie.movieId)

            AlertDialog(
                onDismissRequest = { selectedMovieForDetail = null },
                containerColor = MukulDarkSurface,
                titleContentColor = MukulTextPrimary,
                textContentColor = MukulTextSecondary,
                title = {
                    Text(
                        text = movie.displayTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MukulDarkCard)
                            ) {
                                AsyncImage(
                                    model = movie.poster,
                                    contentDescription = movie.displayTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                if (movie.displayRating.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MukulGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${movie.displayRating} IMDb",
                                            color = MukulGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                if (movie.displayYear.isNotBlank()) {
                                    Text(
                                        text = "Year: ${movie.displayYear}",
                                        color = MukulTextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                                movie.duration?.let { dur ->
                                    val hrs = dur / 60
                                    val mins = dur % 60
                                    Text(
                                        text = "Duration: ${if (hrs > 0) "${hrs}h " else ""}${mins}m",
                                        color = MukulTextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description
                        if (!movie.description.isNullOrBlank()) {
                            Text(
                                text = movie.description,
                                color = MukulTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Servers
                        if (players.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Select Server:",
                                color = MukulTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(players.indices.toList()) { index ->
                                    val p = players[index]
                                    val isSelected = index == selectedServerIndex
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MukulGold else MukulDarkCard)
                                            .border(1.dp, if (isSelected) MukulGold else MukulBorder, RoundedCornerShape(8.dp))
                                            .clickable { selectedServerIndex = index }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = p.translator?.takeIf { it.isNotBlank() } ?: "Server ${index + 1}",
                                            color = if (isSelected) MukulDarkBg else MukulTextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val player = players.getOrNull(selectedServerIndex) ?: players.firstOrNull()
                            selectedMovieForDetail = null
                            onPlayMovie(movie, player)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MukulGold)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MukulDarkBg)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play Now", color = MukulDarkBg, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { onToggleFavoriteMovie(movie.movieId) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MukulTextPrimary)
                    ) {
                        Text(if (isFav) "Favorited" else "+ Favorite")
                    }
                }
            )
        }
    }
}
