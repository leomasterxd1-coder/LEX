package com.lex.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LexRed,
    onPrimary = LexWarmWhite,
    primaryContainer = LexRedContainer,
    onPrimaryContainer = LexWarmWhite,
    secondary = LexRedDark,
    onSecondary = LexWarmWhite,
    secondaryContainer = LexRedContainer,
    onSecondaryContainer = LexWarmWhite,
    tertiary = LexRedDark,
    onTertiary = LexWarmWhite,
    background = LexWarmWhite,
    onBackground = LexOnSurface,
    surface = LexWarmSurface,
    onSurface = LexOnSurface,
    surfaceVariant = LexWarmSurfaceVariant,
    onSurfaceVariant = LexOnSurfaceVariant,
    outline = LexOutline,
    error = ColorError,
    onError = LexWarmWhite
)

private val DarkColors = darkColorScheme(
    primary = ColorPrimaryDark,
    onPrimary = Color(0xFF1A0000),
    primaryContainer = LexRedContainerDark,
    onPrimaryContainer = LexWarmWhite,
    secondary = ColorSecondaryDark,
    onSecondary = Color(0xFF1A0000),
    secondaryContainer = LexRedContainerDark,
    onSecondaryContainer = LexWarmWhite,
    tertiary = ColorSecondaryDark,
    onTertiary = Color(0xFF1A0000),
    background = LexDarkBackground,
    onBackground = LexDarkOnSurface,
    surface = LexDarkSurface,
    onSurface = LexDarkOnSurface,
    surfaceVariant = LexDarkSurfaceVariant,
    onSurfaceVariant = LexDarkOnSurfaceVariant,
    outline = ColorOutlineDark,
    error = ColorErrorDark,
    onError = Color(0xFF1A0000)
)

@Composable
fun LexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = LexTypography,
        content = content
    )
}
