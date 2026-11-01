package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/** Brand gradient used for buttons and accents. */
val GoldGradient: Brush
    get() = Brush.horizontalGradient(listOf(MukulGoldBright, MukulGold))

@Composable
fun LogoMark(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(GoldGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = MukulDarkBg,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text("Mukul", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("+", color = MukulCyan, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun GlassIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun LiveDot(modifier: Modifier = Modifier, size: Int = 8) {
    val transition = rememberInfiniteTransition(label = "live")
    val a by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "liveAlpha"
    )
    Box(
        modifier = modifier
            .size(size.dp)
            .alpha(a)
            .background(MukulLiveRed, CircleShape)
    )
}

@Composable
fun LivePill(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiveDot(size = 7)
        Spacer(Modifier.width(5.dp))
        Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GoldGradient)
            )
            Spacer(Modifier.width(10.dp))
            Text(title, color = MukulTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        if (trailing != null) {
            Text(trailing, color = MukulTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun FilterChips(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(options) { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Brush.horizontalGradient(listOf(MukulGoldBright, MukulGold)) else Brush.horizontalGradient(listOf(MukulDarkCard, MukulDarkCard)))
                    .clickable { onSelect(option) }
                    .padding(horizontal = 18.dp, vertical = 9.dp)
            ) {
                Text(
                    text = option,
                    color = if (isSelected) MukulDarkBg else MukulTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SearchPill(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, color = MukulTextMuted, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = MukulGold) },
        trailingIcon = {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Close search",
                tint = MukulTextSecondary,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { if (query.isNotEmpty()) onQueryChange("") else onClose() }
                    .padding(6.dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MukulDarkCard,
            unfocusedContainerColor = MukulDarkCard,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = MukulTextPrimary,
            unfocusedTextColor = MukulTextPrimary,
            cursorColor = MukulGold
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}

@Composable
fun PrimaryButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(GoldGradient)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = MukulDarkBg, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = MukulDarkBg, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = MukulTextMuted, modifier = Modifier.size(52.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, color = MukulTextMuted, fontSize = 14.sp)
    }
}

