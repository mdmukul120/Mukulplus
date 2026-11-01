package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChannelItem
import com.example.ui.theme.*

@Composable
fun ChannelLogo(channel: ChannelItem, modifier: Modifier = Modifier, iconSize: Int = 30) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (!channel.logo.isNullOrBlank()) {
            AsyncImage(
                model = channel.logo,
                contentDescription = channel.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            )
        } else {
            Icon(Icons.Rounded.Tv, contentDescription = null, tint = MukulGold, modifier = Modifier.size(iconSize.dp))
        }
    }
}

/** Grid tile: logo card with live dot and the channel name underneath. */
@Composable
fun ChannelTile(
    channel: ChannelItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("channel_card_${channel.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(listOf(MukulCardHover, MukulDarkCard))
                )
                .border(1.dp, MukulBorder, RoundedCornerShape(18.dp))
        ) {
            ChannelLogo(channel, Modifier.fillMaxSize())
            LiveDot(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                size = 7
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = channel.cleanName,
            color = MukulTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = channel.displayGroup,
            color = MukulTextMuted,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
    }
}

/** Large featured channel banner. */
@Composable
fun FeaturedChannelCard(
    channel: ChannelItem,
    onWatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2A1F08), Color(0xFF14192B), Color(0xFF062A33))
                )
            )
            .border(1.dp, MukulBorder, RoundedCornerShape(28.dp))
            .clickable(onClick = onWatch)
    ) {
        // Glow behind the logo
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
                .size(130.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.07f))
        ) {
            ChannelLogo(channel, Modifier.fillMaxSize(), iconSize = 48)
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 22.dp, end = 160.dp)
        ) {
            LivePill()
            Spacer(Modifier.height(10.dp))
            Text(
                text = channel.cleanName,
                color = Color.White,
                fontSize = 22.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(channel.displayGroup, color = MukulTextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(14.dp))
            PrimaryButton("Watch Live", Icons.Rounded.PlayArrow, onClick = onWatch)
        }
    }
}
