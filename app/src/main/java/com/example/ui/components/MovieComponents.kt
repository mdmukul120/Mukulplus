package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MovieItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun RatingPill(rating: String, modifier: Modifier = Modifier) {
    if (rating.isBlank()) return
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Star, contentDescription = null, tint = MukulGoldBright, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(3.dp))
        Text(rating, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/** Portrait poster card with the title overlaid on a bottom gradient. */
@Composable
fun PosterCard(
    movie: MovieItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 140.dp,
    height: Dp = 210.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(18.dp))
            .background(MukulDarkCard)
            .clickable(onClick = onClick)
            .testTag("movie_card_${movie.movieId}")
    ) {
        AsyncImage(
            model = movie.poster,
            contentDescription = movie.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.55f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.92f)
                    )
                )
        )
        RatingPill(
            rating = movie.displayRating,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        )
        if (movie.isSeries) {
            Text(
                text = "SERIES",
                color = MukulDarkBg,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MukulCyan)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = movie.displayTitle,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            if (movie.displayYear.isNotBlank()) {
                Text(movie.displayYear, color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp)
            }
        }
    }
}

/** Auto-advancing full-bleed hero carousel. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieHeroPager(
    movies: List<MovieItem>,
    onPlay: (MovieItem) -> Unit,
    onInfo: (MovieItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movies.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { movies.size })

    LaunchedEffect(movies.size) {
        while (true) {
            delay(5500)
            val next = (pagerState.currentPage + 1) % movies.size
            pagerState.animateScrollToPage(next)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) { page ->
            val movie = movies[page]
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = movie.poster,
                    contentDescription = movie.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Black.copy(alpha = 0.55f),
                                0.25f to Color.Transparent,
                                0.55f to Color.Transparent,
                                1f to MukulDarkBg
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, end = 20.dp, bottom = 40.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (movie.isSeries) "SERIES" else "MOVIE",
                            color = MukulGoldBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                        if (movie.displayYear.isNotBlank()) {
                            Text("  •  ${movie.displayYear}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                        if (movie.displayRating.isNotBlank()) {
                            Spacer(Modifier.width(10.dp))
                            RatingPill(movie.displayRating)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = movie.displayTitle,
                        color = Color.White,
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PrimaryButton("Play", Icons.Rounded.PlayArrow, onClick = { onPlay(movie) })
                        SecondaryButton("Details", Icons.Rounded.Info, onClick = { onInfo(movie) })
                    }
                }
            }
        }

        // Page indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(movies.size) { i ->
                val active = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .height(5.dp)
                        .width(if (active) 22.dp else 5.dp)
                        .clip(CircleShape)
                        .background(if (active) MukulGold else Color.White.copy(alpha = 0.3f))
                )
            }
        }
    }
}
