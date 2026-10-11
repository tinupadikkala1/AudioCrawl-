package com.audiocrawl.ui.theme

import androidx.compose.ui.graphics.Color

// Obsidian Signal Design System Palette
val ObsidianVoid = Color(0xFF0B0F14)          // Base floor chassis
val ObsidianSurface = Color(0xFF101419)       // Root surface
val ObsidianContainerLow = Color(0xFF181C21)  // Card / surface low
val ObsidianContainer = Color(0xFF1C2025)     // Pod surface
val ObsidianContainerHigh = Color(0xFF262A30) // Elevated sub-surface
val ObsidianDelimiter = Color(0xFF1E293B)     // 1px hairline border

// Primary Spectral Cyan & Emerald Highlights
val SpectralCyan = Color(0xFF00F2FE)          // Glowing hero & active channels
val SpectralCyanLight = Color(0xFFE0FDFF)     // On-primary glow
val SpectralCyanDim = Color(0xFF00DCE6)       // Focused accents
val EngineEmerald = Color(0xFF10B981)         // Pass / healthy / success
val AtmosphereBlue = Color(0xFF38BDF8)        // Auxiliary indicators & tags

// High-precision Text Colors
val TextPhosphor = Color(0xFFF8FAFC)          // Crisp headline / values
val TextBody = Color(0xFFE0E2EA)              // Readable body
val TextMuted = Color(0xFF849495)             // Telemetry / secondary labels
val TextDim = Color(0xFF64748B)               // Inactive tags

// Legacy mappings for backwards-compatibility
val AudioTeal = SpectralCyan
val AudioTealDark = Color(0xFF006A70)
val AudioNavy = Color(0xFF070A0D)
val AudioAccent = AtmosphereBlue
val DarkSurface = ObsidianSurface
val DarkBackground = ObsidianVoid
val DarkCard = ObsidianContainer
val SuccessGreen = EngineEmerald
val WarningOrange = Color(0xFFF59E0B)
val FilterRed = Color(0xFFEF4444)
