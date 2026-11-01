package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MukulGold,
    onPrimary = MukulDarkBg,
    primaryContainer = MukulGoldDim,
    onPrimaryContainer = MukulGoldBright,
    secondary = MukulCyan,
    onSecondary = MukulDarkBg,
    tertiary = MukulLiveRed,
    background = MukulDarkBg,
    onBackground = MukulTextPrimary,
    surface = MukulDarkSurface,
    onSurface = MukulTextPrimary,
    surfaceVariant = MukulDarkCard,
    onSurfaceVariant = MukulTextSecondary,
    outline = MukulBorder
)

@Composable
fun MukulPlusTheme(
    darkTheme: Boolean = true, // Streaming OTT apps look best in dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = MukulDarkBg.toArgb()
            window.navigationBarColor = MukulDarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
