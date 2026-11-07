package com.lex.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = LexRed,
    onPrimary = LexWarmWhite,
    primaryContainer = LexRedContainer,
    onPrimaryContainer = LexRedDeep,
    secondary = LexRedDark,
    onSecondary = LexWarmWhite,
    secondaryContainer = LexRedContainer,
    onSecondaryContainer = LexRedDeep,
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
    onPrimary = LexRedDeep,
    primaryContainer = LexRedContainerDark,
    onPrimaryContainer = LexRedContainer,
    secondary = ColorSecondaryDark,
    onSecondary = LexRedDeep,
    secondaryContainer = LexRedContainerDark,
    onSecondaryContainer = LexRedContainer,
    tertiary = ColorSecondaryDark,
    onTertiary = LexRedDeep,
    background = LexDarkBackground,
    onBackground = LexDarkOnSurface,
    surface = LexDarkSurface,
    onSurface = LexDarkOnSurface,
    surfaceVariant = LexDarkSurfaceVariant,
    onSurfaceVariant = LexDarkOnSurfaceVariant,
    outline = ColorOutlineDark,
    error = ColorErrorDark,
    onError = LexRedDeep
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
