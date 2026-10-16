package com.audiocrawl.ui.theme

import androidx.compose.ui.graphics.Color

// ========================================================
// Acoustic Clean - Modern Human Music App Palette
// ========================================================

// Backgrounds & Surfaces (Warm carbon & soft elevated cards)
val BackgroundDark = Color(0xFF101114)        // Warm deep carbon background
val CardSurface = Color(0xFF191B20)           // Primary card surface
val CardSurfaceElevated = Color(0xFF22252D)   // Elevated cards, dialogs, sheets
val CardSurfaceHighlight = Color(0xFF2C303A)  // Hover, active pressed state
val SubtleBorder = Color(0xFF2B2E37)          // Soft hairline boundary

// Brand Accent - Warm Sunset Coral (Warm, creative, energetic)
val CoralPrimary = Color(0xFFFA5838)          // Main call to action
val CoralHover = Color(0xFFFF6D50)            // Light coral
val CoralContainer = Color(0xFF2E1712)        // Subtle coral container background
val CoralOnContainer = Color(0xFFFF8B73)      // Coral text on dark tint

// Success & Secondary Colors
val EmeraldSuccess = Color(0xFF10B981)        // Completed / music found / healthy
val EmeraldContainer = Color(0xFF132820)      // Subtle green container
val AccentBlue = Color(0xFF3B82F6)            // Informational tags
val WarningAmber = Color(0xFFF59E0B)          // Warnings
val ErrorRed = Color(0xFFEF4444)              // Errors / delete actions
val ErrorContainer = Color(0xFF2A1517)        // Delete button background

// Typographic Contrast Hierarchy
val TextPrimary = Color(0xFFF5F6F8)           // High emphasis headlines & titles
val TextSecondary = Color(0xFFA1A4B0)         // Clean readable body & artist tags
val TextMuted = Color(0xFF6B7080)             // Subtle metadata, folder paths, hints

// ========================================================
// Compatibility Mappings (maps old tokens to new refined palette)
// ========================================================
val ObsidianVoid = BackgroundDark
val ObsidianSurface = BackgroundDark
val ObsidianContainerLow = CardSurface
val ObsidianContainer = CardSurface
val ObsidianContainerHigh = CardSurfaceElevated
val ObsidianDelimiter = SubtleBorder

val SpectralCyan = CoralPrimary
val SpectralCyanLight = CoralHover
val SpectralCyanDim = CoralHover
val EngineEmerald = EmeraldSuccess
val AtmosphereBlue = AccentBlue

val TextPhosphor = TextPrimary
val TextBody = TextSecondary
val TextDim = TextMuted

val AudioTeal = CoralPrimary
val AudioTealDark = CoralContainer
val AudioNavy = BackgroundDark
val AudioAccent = CoralPrimary
val DarkSurface = CardSurface
val DarkBackground = BackgroundDark
val DarkCard = CardSurface
val SuccessGreen = EmeraldSuccess
val WarningOrange = WarningAmber
val FilterRed = ErrorRed

// Aliases for modern screen layouts
val AccentWarm = CoralPrimary
val AccentWarmGlow = Color(0x33FA5838)
val ScreenBackground = BackgroundDark
val DividerSubtle = SubtleBorder
