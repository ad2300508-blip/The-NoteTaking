package com.lumina.notes.ui.theme

import androidx.compose.ui.graphics.Color

// Fallback brand scheme (used when dynamic color is off or unavailable).
val md_primary = Color(0xFF1F6FEB)
val md_onPrimary = Color(0xFFFFFFFF)
val md_primaryContainer = Color(0xFFD3E3FF)
val md_onPrimaryContainer = Color(0xFF001C3B)
val md_secondary = Color(0xFF555F71)
val md_onSecondary = Color(0xFFFFFFFF)
val md_secondaryContainer = Color(0xFFD9E3F8)
val md_onSecondaryContainer = Color(0xFF121C2B)
val md_tertiary = Color(0xFF6F5575)
val md_onTertiary = Color(0xFFFFFFFF)
val md_background = Color(0xFFFDFCFF)
val md_onBackground = Color(0xFF1A1C1E)
val md_surface = Color(0xFFFDFCFF)
val md_onSurface = Color(0xFF1A1C1E)
val md_surfaceVariant = Color(0xFFDFE2EB)
val md_onSurfaceVariant = Color(0xFF42474E)

val md_primary_dark = Color(0xFFA6C8FF)
val md_onPrimary_dark = Color(0xFF00315F)
val md_primaryContainer_dark = Color(0xFF004787)
val md_onPrimaryContainer_dark = Color(0xFFD3E3FF)
val md_secondary_dark = Color(0xFFBDC7DC)
val md_onSecondary_dark = Color(0xFF273141)
val md_secondaryContainer_dark = Color(0xFF3D4758)
val md_onSecondaryContainer_dark = Color(0xFFD9E3F8)
val md_tertiary_dark = Color(0xFFDCBCE1)
val md_onTertiary_dark = Color(0xFF3E2845)
val md_background_dark = Color(0xFF101418)
val md_onBackground_dark = Color(0xFFE2E2E6)
val md_surface_dark = Color(0xFF101418)
val md_onSurface_dark = Color(0xFFE2E2E6)
val md_surfaceVariant_dark = Color(0xFF42474E)
val md_onSurfaceVariant_dark = Color(0xFFC3C7CF)

/**
 * Soft accent tints used to color note cards. Order matters: it maps to
 * NoteEntity.colorSeed. Designed to read well in both light and dark themes.
 */
val NoteAccents = listOf(
    Color(0xFF6EA8FE), // blue
    Color(0xFF7BD389), // green
    Color(0xFFFFC861), // amber
    Color(0xFFFF8FA3), // rose
    Color(0xFFB18AE0), // violet
    Color(0xFF5FD0C5), // teal
    Color(0xFFFFA877), // coral
)

/** Default ink colors offered in the S Pen toolbar. */
val InkPalette = listOf(
    0xFF111418L, // near-black
    0xFF1F6FEBL, // blue
    0xFFE5484DL, // red
    0xFF2E9E5BL, // green
    0xFFF2A20CL, // amber
    0xFF8B5CF6L, // violet
    0xFFFFFFFFL, // white (for dark paper)
)
