package com.audiocrawl.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ObsidianColorScheme = darkColorScheme(
    primary = SpectralCyan,
    onPrimary = ObsidianVoid,
    primaryContainer = SpectralCyanDim,
    onPrimaryContainer = ObsidianVoid,
    secondary = EngineEmerald,
    onSecondary = ObsidianVoid,
    secondaryContainer = Color(0xFF003824),
    onSecondaryContainer = EngineEmerald,
    tertiary = AtmosphereBlue,
    onTertiary = ObsidianVoid,
    background = ObsidianVoid,
    onBackground = TextBody,
    surface = ObsidianSurface,
    onSurface = TextPhosphor,
    surfaceVariant = ObsidianContainer,
    onSurfaceVariant = TextMuted,
    outline = ObsidianDelimiter,
    outlineVariant = Color(0xFF3A494B),
    error = FilterRed,
    onError = TextPhosphor
)

@Composable
fun AudioCrawlTheme(
    darkTheme: Boolean = true, // Force modern technical dark mode aesthetic
    dynamicColor: Boolean = false, // Keep Obsidian Signal branding consistent
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ObsidianColorScheme,
        typography = Typography,
        content = content
    )
}
