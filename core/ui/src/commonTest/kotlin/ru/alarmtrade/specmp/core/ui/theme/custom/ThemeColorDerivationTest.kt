package ru.alarmtrade.specmp.core.ui.theme.custom

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadii
import ru.alarmtrade.specmp.core.ui.theme.tokens.DarkCmpColorScheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.DarkCmpColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.LightCmpColorScheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.LightCmpColors

class ThemeColorDerivationTest {
    @Test
    fun defaultLightIsCssTokens() {
        val theme = ThemePreset.Default.light.resolve(isDark = false)
        assertSame(CmpColorFields, LightCmpColors, theme.colors)
        assertSame(ColorSchemeFields, LightCmpColorScheme, theme.scheme)
    }

    @Test
    fun defaultDarkIsCssTokens() {
        val theme = ThemePreset.Default.dark.resolve(isDark = true)
        assertSame(CmpColorFields, DarkCmpColors, theme.colors)
        assertSame(ColorSchemeFields, DarkCmpColorScheme, theme.scheme)
    }

    @Test
    fun defaultRadiiAreCssTokens() {
        assertEquals(CmpRadii(), ThemePreset.Default.light.resolve(isDark = false).radii)
        assertEquals(CmpRadii(), ThemePreset.Default.dark.resolve(isDark = true).radii)
    }

    @Test
    fun accentChangesOnlyBrandTokens() {
        for (isDark in listOf(false, true)) {
            val default = ThemePreset.Default.mode(isDark)
            val accent = default.copy(palette = default.palette.with(ThemeColorRole.Primary, ThemeColor(0xC2410C)))
            val before = default.resolve(isDark).colors
            val after = accent.resolve(isDark).colors
            assertEquals(Color(0xFFC2410C), after.brandPrimaryDefault)
            assertNotEquals(before.brandPrimaryHighlight, after.brandPrimaryHighlight)
            assertEquals(before.brandPrimaryHighlight.alpha, after.brandPrimaryHighlight.alpha)
            assertNotEquals(before.borderFocus, after.borderFocus)
            val untouched = CmpColorFields.filterNot { (name, _) -> name.startsWith("brand") || name == "borderFocus" }
            assertSame(untouched, before, after)
        }
    }

    @Test
    fun themeTextScaleStacksWithSystemUpTo150Percent() {
        assertEquals(1.5f, effectiveTextScale(theme = 1.5f, systemFontScale = 1f), TOLERANCE)
        assertEquals(1.5f / 1.3f, effectiveTextScale(theme = 1.5f, systemFontScale = 1.3f), TOLERANCE)
        assertEquals(1.154f, effectiveTextScale(theme = 1.5f, systemFontScale = 1.3f), 0.001f)
        assertEquals(1f, effectiveTextScale(theme = 1.5f, systemFontScale = 2f), TOLERANCE)
        assertEquals(1.15f, effectiveTextScale(theme = 1.15f, systemFontScale = 1f), TOLERANCE)
    }

    private fun <T> assertSame(fields: List<Pair<String, (T) -> Color>>, expected: T, actual: T) {
        val diff = fields.filter { (_, get) -> get(expected) != get(actual) }
            .map { (name, get) -> "$name: ${get(expected)} != ${get(actual)}" }
        assertEquals(emptyList(), diff)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}

private val CmpColorFields: List<Pair<String, (CmpColors) -> Color>> = listOf(
    "brandPrimaryDefault" to { it.brandPrimaryDefault },
    "brandPrimaryHover" to { it.brandPrimaryHover },
    "brandPrimaryPressed" to { it.brandPrimaryPressed },
    "brandPrimaryDisabled" to { it.brandPrimaryDisabled },
    "brandPrimaryHighlight" to { it.brandPrimaryHighlight },
    "brandSecondaryDefault" to { it.brandSecondaryDefault },
    "onBrand" to { it.onBrand },
    "foregroundOnDarkPrimaryDefault" to { it.foregroundOnDarkPrimaryDefault },
    "foregroundOnDarkPrimaryPressed" to { it.foregroundOnDarkPrimaryPressed },
    "foregroundOnDarkPrimaryDisabled" to { it.foregroundOnDarkPrimaryDisabled },
    "foregroundOnDarkSecondaryDefault" to { it.foregroundOnDarkSecondaryDefault },
    "foregroundOnDarkTertiary" to { it.foregroundOnDarkTertiary },
    "foregroundOnLightPrimaryDefault" to { it.foregroundOnLightPrimaryDefault },
    "foregroundOnLightSecondaryDefault" to { it.foregroundOnLightSecondaryDefault },
    "backgroundPagePrimary" to { it.backgroundPagePrimary },
    "backgroundSurfacePrimary" to { it.backgroundSurfacePrimary },
    "backgroundSurfaceSemi" to { it.backgroundSurfaceSemi },
    "backgroundSurfaceSecondary" to { it.backgroundSurfaceSecondary },
    "backgroundSurfaceTertiary" to { it.backgroundSurfaceTertiary },
    "backgroundSurfaceLight" to { it.backgroundSurfaceLight },
    "backgroundSurfaceMediumHighlight" to { it.backgroundSurfaceMediumHighlight },
    "backgroundSurfaceHighlight" to { it.backgroundSurfaceHighlight },
    "backgroundSurfaceUltraHighlight" to { it.backgroundSurfaceUltraHighlight },
    "backgroundSurfaceCard" to { it.backgroundSurfaceCard },
    "backgroundOverlay" to { it.backgroundOverlay },
    "backgroundGlass" to { it.backgroundGlass },
    "borderPrimary" to { it.borderPrimary },
    "borderSecondary" to { it.borderSecondary },
    "borderTertiary" to { it.borderTertiary },
    "borderLight" to { it.borderLight },
    "borderMediumHighlight" to { it.borderMediumHighlight },
    "borderHighlight" to { it.borderHighlight },
    "borderUltraHighlight" to { it.borderUltraHighlight },
    "borderFocus" to { it.borderFocus },
    "successDefault" to { it.successDefault },
    "successHighlight" to { it.successHighlight },
    "warningDefault" to { it.warningDefault },
    "warningHighlight" to { it.warningHighlight },
    "errorDefault" to { it.errorDefault },
    "errorPressed" to { it.errorPressed },
    "errorHighlight" to { it.errorHighlight },
    "infoDefault" to { it.infoDefault },
    "infoHighlight" to { it.infoHighlight },
    "staticWhite" to { it.staticWhite },
    "staticBlack" to { it.staticBlack },
    "busCan" to { it.busCan },
    "busCanfd" to { it.busCanfd },
    "busLin" to { it.busLin },
    "busBean" to { it.busBean },
    "busImo" to { it.busImo },
    "busUnknown" to { it.busUnknown },
)

private val ColorSchemeFields: List<Pair<String, (ColorScheme) -> Color>> = listOf(
    "primary" to { it.primary },
    "onPrimary" to { it.onPrimary },
    "primaryContainer" to { it.primaryContainer },
    "onPrimaryContainer" to { it.onPrimaryContainer },
    "inversePrimary" to { it.inversePrimary },
    "secondary" to { it.secondary },
    "onSecondary" to { it.onSecondary },
    "secondaryContainer" to { it.secondaryContainer },
    "onSecondaryContainer" to { it.onSecondaryContainer },
    "tertiary" to { it.tertiary },
    "onTertiary" to { it.onTertiary },
    "tertiaryContainer" to { it.tertiaryContainer },
    "onTertiaryContainer" to { it.onTertiaryContainer },
    "background" to { it.background },
    "onBackground" to { it.onBackground },
    "surface" to { it.surface },
    "onSurface" to { it.onSurface },
    "surfaceVariant" to { it.surfaceVariant },
    "onSurfaceVariant" to { it.onSurfaceVariant },
    "surfaceTint" to { it.surfaceTint },
    "inverseSurface" to { it.inverseSurface },
    "inverseOnSurface" to { it.inverseOnSurface },
    "error" to { it.error },
    "onError" to { it.onError },
    "errorContainer" to { it.errorContainer },
    "onErrorContainer" to { it.onErrorContainer },
    "outline" to { it.outline },
    "outlineVariant" to { it.outlineVariant },
    "scrim" to { it.scrim },
    "surfaceBright" to { it.surfaceBright },
    "surfaceDim" to { it.surfaceDim },
    "surfaceContainer" to { it.surfaceContainer },
    "surfaceContainerHigh" to { it.surfaceContainerHigh },
    "surfaceContainerHighest" to { it.surfaceContainerHighest },
    "surfaceContainerLow" to { it.surfaceContainerLow },
    "surfaceContainerLowest" to { it.surfaceContainerLowest },
)
