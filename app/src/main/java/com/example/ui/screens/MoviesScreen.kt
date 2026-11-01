package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CatalogCategory
import com.example.data.model.MovieItem
import com.example.data.model.PlayerItem
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    categories: List<CatalogCategory>,
    isLoading: Boolean,
    onPlayMovie: (MovieItem, PlayerItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    var searching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf("All") }
    var detailMovie by remember { mutableStateOf<MovieItem?>(null) }

    val allMovies = remember(categories) {
        categories.flatMap { it.movies }.distinctBy { it.movieId }
    }

    val heroMovies = remember(allMovies) {
        allMovies.filter { !it.poster.isNullOrBlank() }.take(6)
    }

    val visibleCategories = remember(categories, typeFilter) {
        categories.mapNotNull { cat ->
            val list = when (typeFilter) {
                "Movies" -> cat.movies.filter { !it.isSeries }
                "Series" -> cat.movies.filter { it.isSeries }
                else -> cat.movies
            }
            if (list.isEmpty()) null else cat.copy(movies = list)
        }
    }

    val searchResults = remember(searchQuery, allMovies) {
        if (searchQuery.isBlank()) emptyList()
        else allMovies.filter {
            it.displayTitle.contains(searchQuery, ignoreCase = true) ||
                it.description?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    val showingSearch = searching && searchQuery.isNotBlank()

    Box(modifier = modifier.fillMaxSize().background(MukulDarkBg)) {
        if (isLoading && categories.isEmpty()) {
            CircularProgressIndicator(color = MukulGold, modifier = Modifier.align(Alignment.Center))
        } else if (showingSearch) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    MoviesTopBar(
                        searching = searching,
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onToggleSearch = { searching = !searching; if (!searching) searchQuery = "" },
                        overlay = false,
                        modifier = Modifier.padding(horizontal = 0.dp)
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "${searchResults.size} results for \"$searchQuery\"",
                        color = MukulTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
                if (searchResults.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(Icons.Rounded.Movie, "Nothing found")
                    }
                }
                gridItems(searchResults, key = { it.movieId }) { movie ->
                    PosterCard(
                        movie = movie,
                        onClick = { detailMovie = movie },
                        width = 110.dp,
                        height = 165.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Box {
                        if (heroMovies.isNotEmpty() && !searching) {
                            MovieHeroPager(
                                movies = heroMovies,
                                onPlay = { m -> onPlayMovie(m, m.player?.firstOrNull()) },
                                onInfo = { detailMovie = it }
                            )
                        }
                        MoviesTopBar(
                            searching = searching,
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onToggleSearch = { searching = !searching; if (!searching) searchQuery = "" },
                            overlay = heroMovies.isNotEmpty() && !searching
                        )
                    }
                }
                item {
                    FilterChips(
                        options = listOf("All", "Movies", "Series"),
                        selected = typeFilter,
                        onSelect = { typeFilter = it }
                    )
                }
                if (visibleCategories.isEmpty()) {
                    item { EmptyState(Icons.Rounded.Movie, "No titles available") }
                }
                items(visibleCategories, key = { it.id.toString() + it.name }) { category ->
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = category.name, trailing = "${category.movies.size} titles")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(category.movies, key = { it.movieId }) { movie ->
                                PosterCard(movie = movie, onClick = { detailMovie = movie })
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail bottom sheet
    detailMovie?.let { movie ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { detailMovie = null },
            sheetState = sheetState,
            containerColor = MukulDarkSurface,
            contentColor = MukulTextPrimary,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            MovieDetailSheet(
                movie = movie,
                onPlay = { player ->
                    detailMovie = null
                    onPlayMovie(movie, player)
                }
            )
        }
    }
}

@Composable
private fun MoviesTopBar(
    searching: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    overlay: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LogoMark()
            GlassIconButton(Icons.Rounded.Search, "Search", onToggleSearch)
        }
        AnimatedVisibility(visible = searching) {
            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                SearchPill(
                    query = query,
                    onQueryChange = onQueryChange,
                    placeholder = "Search movies & series...",
                    onClose = onToggleSearch,
                    modifier = Modifier.testTag("search_movies_input")
                )
            }
        }
    }
}

@Composable
private fun MovieDetailSheet(
    movie: MovieItem,
    onPlay: (PlayerItem?) -> Unit
) {
    val players = movie.player ?: emptyList()
    var selectedServer by remember(movie.movieId) { mutableIntStateOf(0) }

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
                    model = movie.poster,
                    contentDescription = movie.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.displayTitle,
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetaTag(if (movie.isSeries) "Series" else "Movie")
                    if (movie.displayYear.isNotBlank()) MetaTag(movie.displayYear)
                    movie.duration?.takeIf { it > 0 }?.let { d ->
                        MetaTag("${if (d / 60 > 0) "${d / 60}h " else ""}${d % 60}m")
                    }
                }
                if (movie.displayRating.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, tint = MukulGoldBright, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(movie.displayRating, color = MukulGoldBright, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("  IMDb", color = MukulTextMuted, fontSize = 12.sp)
                    }
                }
            }
        }

        if (!movie.description.isNullOrBlank()) {
            Spacer(Modifier.height(18.dp))
            Text(
                text = movie.description,
                color = MukulTextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }

        if (players.size > 1) {
            Spacer(Modifier.height(20.dp))
            Text("Server", color = MukulTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(players.indices.toList()) { index ->
                    val selected = index == selectedServer
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) MukulGold else MukulDarkCard)
                            .border(1.dp, if (selected) MukulGold else MukulBorder, RoundedCornerShape(50))
                            .clickable { selectedServer = index }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = players[index].translator?.takeIf { it.isNotBlank() } ?: "Server ${index + 1}",
                            color = if (selected) MukulDarkBg else MukulTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Play Now",
            icon = Icons.Rounded.PlayArrow,
            onClick = { onPlay(players.getOrNull(selectedServer) ?: players.firstOrNull()) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MetaTag(text: String) {
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
