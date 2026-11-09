package ru.alarmtrade.specmp.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class CmpThemeColors(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val outline: Color,
    val outlineVariant: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val scrim: Color,
)

@Composable
fun cmpThemeColors(): CmpThemeColors {
    val s = MaterialTheme.colorScheme
    return CmpThemeColors(
        background = s.background,
        onBackground = s.onBackground,
        surface = s.surface,
        onSurface = s.onSurface,
        surfaceVariant = s.surfaceVariant,
        onSurfaceVariant = s.onSurfaceVariant,
        primary = s.primary,
        onPrimary = s.onPrimary,
        primaryContainer = s.primaryContainer,
        onPrimaryContainer = s.onPrimaryContainer,
        secondary = s.secondary,
        onSecondary = s.onSecondary,
        secondaryContainer = s.secondaryContainer,
        onSecondaryContainer = s.onSecondaryContainer,
        tertiary = s.tertiary,
        onTertiary = s.onTertiary,
        tertiaryContainer = s.tertiaryContainer,
        onTertiaryContainer = s.onTertiaryContainer,
        error = s.error,
        onError = s.onError,
        errorContainer = s.errorContainer,
        onErrorContainer = s.onErrorContainer,
        outline = s.outline,
        outlineVariant = s.outlineVariant,
        surfaceContainerLowest = s.surfaceContainerLowest,
        surfaceContainerLow = s.surfaceContainerLow,
        surfaceContainer = s.surfaceContainer,
        surfaceContainerHigh = s.surfaceContainerHigh,
        surfaceContainerHighest = s.surfaceContainerHighest,
        scrim = s.scrim,
    )
}
