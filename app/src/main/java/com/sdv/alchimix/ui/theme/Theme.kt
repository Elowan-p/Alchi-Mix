package com.sdv.alchimix.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AlambicPurple,
    secondary = AlambicCyan,
    tertiary = AlambicOrange,
    background = DarkBg,
    surface = CardBg,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun AlchiMixTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
