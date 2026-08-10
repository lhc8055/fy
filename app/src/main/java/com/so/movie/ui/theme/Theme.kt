package com.so.movie.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF1E88FF)
val PrimaryDark = Color(0xFF1565C0)
val Secondary = Color(0xFFFF6B6B)
val Accent = Color(0xFFFFD93D)

val BackgroundLight = Color(0xFFF5F7FA)
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1A1A1A)
val TextSecondary = Color(0xFF666666)
val TextTertiary = Color(0xFF999999)
val Divider = Color(0xFFE5E7EB)
val SurfaceVariant = Color(0xFFF0F2F5)

val VipGradientStart = Color(0xFFFFD700)
val VipGradientEnd = Color(0xFFFFA500)

val ScoreHigh = Color(0xFFFF6B6B)
val ScoreMedium = Color(0xFFFF9800)
val ScoreLow = Color(0xFF8BC34A)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Accent,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = Divider
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Accent,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2A2A2A),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF444444)
)

@Composable
fun SOMovieTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
