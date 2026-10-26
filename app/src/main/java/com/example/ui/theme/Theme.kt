package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = AccentPurple,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF332066),
    onSecondaryContainer = Color(0xFFE8DDFF),
    tertiary = AccentEmerald,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF005328),
    onTertiaryContainer = Color(0xFF6BFF9E),
    background = DeepNavyBg,
    onBackground = TextPrimary,
    surface = SlateNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateNavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderOutline,
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
