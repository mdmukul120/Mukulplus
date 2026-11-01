package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.screens.AnimeScreen
import com.example.ui.screens.KoreanDramaScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.MoviesScreen
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

enum class AppTab(val title: String, val icon: ImageVector) {
    LIVE_TV("Live TV", Icons.Rounded.LiveTv),
    MOVIES("Movies", Icons.Rounded.Movie),
    K_DRAMA("K-Drama", Icons.Rounded.VideoLibrary),
    ANIME("Anime", Icons.Rounded.AutoAwesome)
}

@Composable
fun MukulApp() {
    val context = LocalContext.current
    val repository = remember { MukulRepository(context) }
    val scope = rememberCoroutineScope()

    val channels by repository.channels.collectAsStateWithLifecycle()
    val categories by repository.categories.collectAsStateWithLifecycle()
    val dramas by repository.dramas.collectAsStateWithLifecycle()
    val animeList by repository.animeList.collectAsStateWithLifecycle()
    val isLoading by repository.isLoading.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppTab.LIVE_TV) }
    var activeChannel by remember { mutableStateOf<ChannelItem?>(null) }
    var activeMovie by remember { mutableStateOf<Pair<MovieItem, PlayerItem?>?>(null) }

    LaunchedEffect(Unit) {
        repository.fetchAllData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MukulDarkBg)
    ) {
        val channel = activeChannel
        val movie = activeMovie

        when {
            channel != null -> MukulVideoPlayer(
                currentChannel = channel,
                allChannels = channels,
                onSelectChannel = { activeChannel = it },
                onClosePlayer = { activeChannel = null }
            )

            movie != null -> MoviePlayerView(
                movie = movie.first,
                selectedPlayer = movie.second,
                onClosePlayer = { activeMovie = null }
            )

            else -> {
                when (currentTab) {
                    AppTab.LIVE_TV -> LiveTvScreen(
                        channels = channels,
                        isLoading = isLoading,
                        onPlayChannel = { activeChannel = it },
                        onRefresh = { scope.launch { repository.fetchAllData(forceRefresh = true) } }
                    )

                    AppTab.MOVIES -> MoviesScreen(
                        categories = categories,
                        isLoading = isLoading,
                        onPlayMovie = { m, p -> activeMovie = Pair(m, p) }
                    )

                    AppTab.K_DRAMA -> KoreanDramaScreen(
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
                            val dummy = MovieItem(title = title, titleEn = title)
                            val player = PlayerItem(url = embedUrl, source = if (embedUrl.contains(".m3u8")) "m3u8" else "iframe")
                            activeMovie = Pair(dummy, player)
                        }
                    )

                    AppTab.ANIME -> AnimeScreen(
                        animeList = animeList,
                        isLoading = isLoading,
                        onCategorySelected = { category ->
                            scope.launch { repository.fetchAnime(label = category) }
                        },
                        onSearch = { query ->
                            scope.launch { repository.fetchAnime(query = query) }
                        },
                        onPlayAnime = { title, streamUrl ->
                            val dummy = MovieItem(title = title, titleEn = title)
                            val player = PlayerItem(url = streamUrl, source = if (streamUrl.contains(".m3u8")) "m3u8" else "iframe")
                            activeMovie = Pair(dummy, player)
                        }
                    )
                }

                FloatingNavBar(
                    current = currentTab,
                    onSelect = { currentTab = it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun FloatingNavBar(
    current: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(32.dp)

    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
            .shadow(20.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(shape)
            .background(MukulDarkSurface.copy(alpha = 0.96f))
            .border(1.dp, MukulBorder, shape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTab.values().forEach { tab ->
            val selected = tab == current
            val bg by animateColorAsState(
                targetValue = if (selected) MukulGold else Color.Transparent,
                animationSpec = tween(250),
                label = "navBg"
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(26.dp))
                    .background(bg)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("nav_item_${tab.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.title,
                    tint = if (selected) MukulDarkBg else MukulTextMuted,
                    modifier = Modifier.size(20.dp)
                )
                AnimatedVisibility(visible = selected) {
                    Row {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = tab.title,
                            color = MukulDarkBg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
