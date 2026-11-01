package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChannelItem
import com.example.data.model.MovieItem
import com.example.data.model.PlayerItem
import com.example.data.repository.MukulRepository
import com.example.ui.player.MoviePlayerView
import com.example.ui.player.MukulVideoPlayer
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MukulPlusTheme {
                MukulApp()
            }
        }
    }
}

enum class AppTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    LIVE_TV("Live TV", Icons.Default.LiveTv),
    MOVIES("Movies", Icons.Default.Movie),
    K_DRAMA("K-Drama", Icons.Default.VideoLibrary),
    ANIME("Anime", Icons.Default.AutoAwesome),
    FAVORITES("Favorites", Icons.Default.Favorite)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MukulApp() {
    val context = LocalContext.current
    val repository = remember { MukulRepository(context) }
    val scope = rememberCoroutineScope()

    val channels by repository.channels.collectAsStateWithLifecycle()
    val categories by repository.categories.collectAsStateWithLifecycle()
    val dramas by repository.dramas.collectAsStateWithLifecycle()
    val animeList by repository.animeList.collectAsStateWithLifecycle()
    val favoriteChannelIds by repository.favoriteChannelIds.collectAsStateWithLifecycle()
    val favoriteMovieIds by repository.favoriteMovieIds.collectAsStateWithLifecycle()
    val isLoading by repository.isLoading.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppTab.LIVE_TV) }
    var activePlayingChannel by remember { mutableStateOf<ChannelItem?>(null) }
    var activePlayingMovie by remember { mutableStateOf<Pair<MovieItem, PlayerItem?>?>(null) }

    // Initial load
    LaunchedEffect(Unit) {
        repository.fetchAllData()
    }

    // All movies list for favorites screen
    val allMovies = remember(categories) {
        categories.flatMap { it.movies }.distinctBy { it.movieId }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MukulDarkBg)
    ) {
        if (activePlayingChannel != null) {
            // Fullscreen Live TV Player
            MukulVideoPlayer(
                currentChannel = activePlayingChannel!!,
                allChannels = channels,
                isFavorite = favoriteChannelIds.contains(activePlayingChannel!!.id),
                onToggleFavorite = { repository.toggleFavoriteChannel(activePlayingChannel!!.id) },
                onSelectChannel = { activePlayingChannel = it },
                onClosePlayer = { activePlayingChannel = null }
            )
        } else if (activePlayingMovie != null) {
            // Movie / Drama / Anime Player
            MoviePlayerView(
                movie = activePlayingMovie!!.first,
                selectedPlayer = activePlayingMovie!!.second,
                onClosePlayer = { activePlayingMovie = null }
            )
        } else {
            // Main App Navigation Shell
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Mukul Plus Logo Mark
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MukulGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = MukulDarkBg,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Mukul",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "+",
                                    color = MukulCyan,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                // Online Channels Counter Badge
                                if (channels.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MukulDarkCard)
                                            .border(1.dp, MukulBorder, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(MukulGreen, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "${channels.size} Live",
                                                color = MukulTextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MukulDarkSurface,
                            titleContentColor = MukulTextPrimary
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MukulDarkSurface,
                        tonalElevation = 8.dp
                    ) {
                        AppTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) MukulGold else MukulTextMuted
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        color = if (isSelected) MukulGold else MukulTextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MukulGoldDim
                                ),
                                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        AppTab.LIVE_TV -> {
                            LiveTvScreen(
                                channels = channels,
                                favoriteChannelIds = favoriteChannelIds,
                                isLoading = isLoading,
                                onToggleFavorite = { repository.toggleFavoriteChannel(it) },
                                onPlayChannel = { activePlayingChannel = it },
                                onRefresh = { scope.launch { repository.fetchAllData(forceRefresh = true) } }
                            )
                        }
                        AppTab.MOVIES -> {
                            MoviesScreen(
                                categories = categories,
                                favoriteMovieIds = favoriteMovieIds,
                                onToggleFavoriteMovie = { repository.toggleFavoriteMovie(it) },
                                onPlayMovie = { movie, player ->
                                    activePlayingMovie = Pair(movie, player)
                                }
                            )
                        }
                        AppTab.K_DRAMA -> {
                            KoreanDramaScreen(
                                dramas = dramas,
                                isLoading = isLoading,
                                onCategorySelected = { category ->
                                    scope.launch { repository.fetchDramasByCategory(category) }
                                },
                                onSearch = { query ->
                                    scope.launch { repository.searchDramas(query) }
                                },
                                onLoadDetail = { url ->
                                    repository.getDramaDetail(url)
                                },
                                onPlayDrama = { title, embedUrl ->
                                    val dummyMovie = MovieItem(title = title, titleEn = title)
                                    val playerItem = PlayerItem(url = embedUrl, source = if (embedUrl.contains(".m3u8")) "m3u8" else "iframe")
                                    activePlayingMovie = Pair(dummyMovie, playerItem)
                                }
                            )
                        }
                        AppTab.ANIME -> {
                            AnimeScreen(
                                animeList = animeList,
                                isLoading = isLoading,
                                onCategorySelected = { category ->
                                    scope.launch { repository.fetchAnime(label = category) }
                                },
                                onSearch = { query ->
                                    scope.launch { repository.fetchAnime(query = query) }
                                },
                                onPlayAnime = { title, streamUrl ->
                                    val dummyMovie = MovieItem(title = title, titleEn = title)
                                    val playerItem = PlayerItem(url = streamUrl, source = if (streamUrl.contains(".m3u8")) "m3u8" else "iframe")
                                    activePlayingMovie = Pair(dummyMovie, playerItem)
                                }
                            )
                        }
                        AppTab.FAVORITES -> {
                            FavoritesScreen(
                                allChannels = channels,
                                favoriteChannelIds = favoriteChannelIds,
                                allMovies = allMovies,
                                favoriteMovieIds = favoriteMovieIds,
                                onToggleFavoriteChannel = { repository.toggleFavoriteChannel(it) },
                                onPlayChannel = { activePlayingChannel = it },
                                onPlayMovie = { movie, player ->
                                    activePlayingMovie = Pair(movie, player)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
