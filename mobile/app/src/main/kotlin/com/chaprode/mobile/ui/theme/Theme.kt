package com.chaprode.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ChaProdeCta,
    onPrimary = Color.White,
    secondary = ChaProdeSky,
    onSecondary = ChaProdeBg,
    tertiary = ChaProdeGold,
    background = ChaProdeBg,
    onBackground = ChaProdeText,
    surface = ChaProdeSurface,
    onSurface = ChaProdeText,
    surfaceVariant = ChaProdeSurfaceSoft,
    onSurfaceVariant = ChaProdeMuted,
    outline = ChaProdeBorder,
    error = ChaProdeDanger,
    onError = Color.White
)

@Composable
fun ChaProdeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
