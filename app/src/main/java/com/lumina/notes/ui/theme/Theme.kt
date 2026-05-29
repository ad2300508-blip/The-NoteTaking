package com.lumina.notes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = md_primary, onPrimary = md_onPrimary,
    primaryContainer = md_primaryContainer, onPrimaryContainer = md_onPrimaryContainer,
    secondary = md_secondary, onSecondary = md_onSecondary,
    secondaryContainer = md_secondaryContainer, onSecondaryContainer = md_onSecondaryContainer,
    tertiary = md_tertiary, onTertiary = md_onTertiary,
    background = md_background, onBackground = md_onBackground,
    surface = md_surface, onSurface = md_onSurface,
    surfaceVariant = md_surfaceVariant, onSurfaceVariant = md_onSurfaceVariant,
)

private val DarkColors = darkColorScheme(
    primary = md_primary_dark, onPrimary = md_onPrimary_dark,
    primaryContainer = md_primaryContainer_dark, onPrimaryContainer = md_onPrimaryContainer_dark,
    secondary = md_secondary_dark, onSecondary = md_onSecondary_dark,
    secondaryContainer = md_secondaryContainer_dark, onSecondaryContainer = md_onSecondaryContainer_dark,
    tertiary = md_tertiary_dark, onTertiary = md_onTertiary_dark,
    background = md_background_dark, onBackground = md_onBackground_dark,
    surface = md_surface_dark, onSurface = md_onSurface_dark,
    surfaceVariant = md_surfaceVariant_dark, onSurfaceVariant = md_onSurfaceVariant_dark,
)

@Composable
fun LuminaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LuminaTypography,
        content = content,
    )
}
