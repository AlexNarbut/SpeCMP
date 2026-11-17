package ru.alarmtrade.specmp.core.ui.theme.custom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ThemeContrastTest {
    private val light = ThemePreset.Default.light
    private val dark = ThemePreset.Default.dark

    private fun color(hex: String) = requireNotNull(ThemeColor.parse(hex))

    @Test
    fun hexRoundTripAndInvalidInput() {
        assertEquals("#0A1A3A", color("0a1a3a").hex)
        assertNull(ThemeColor.parse("#12345"))
        assertNull(ThemeColor.parse("#GGGGGG"))
        assertEquals(21.0, ThemeColor.Black.contrast(ThemeColor.White), 0.001)
    }

    @Test
    fun defaultPaletteIsCssTokens() {
        assertEquals("#2F6BFF", light.palette.primary.hex)
        assertEquals("#F3F6FB", light.palette.background.hex)
        assertEquals("#FFFFFF", light.palette.card.hex)
        assertEquals("#0A1A3A", light.palette.ink.hex)
        assertEquals("#0B1220", dark.palette.background.hex)
        assertEquals(ThemeBackground.Glow, light.background)
    }

    @Test
    fun defaultThemePassesBecauseUntouchedPairsAreNotChecked() {
        assertTrue(ThemeContrast.passes(light, light))
        assertTrue(ThemeContrast.passes(dark, dark))
    }

    @Test
    fun everyPresetPassesAgainstDefault() {
        ThemePreset.entries.forEach { preset ->
            assertEquals(emptyList(), ThemeContrast.failing(preset.spec.light, light), "${preset.name} light")
            assertEquals(emptyList(), ThemeContrast.failing(preset.spec.dark, dark), "${preset.name} dark")
        }
    }

    @Test
    fun changedCardBringsOutlineCheckBack() {
        val edited = light.copy(palette = light.palette.with(ThemeColorRole.Card, color("#FEFEFE")))
        val failing = ThemeContrast.failing(edited, light).map { it.kind }
        assertTrue(ContrastCheckKind.OutlineOnCard in failing)
    }

    @Test
    fun outlinedFillTightensAccentEvenWhenUntouched() {
        assertFalse(ThemeContrast.canUseFill(light, light, ButtonFill.Outlined))
        assertTrue(ThemeContrast.canUseFill(light, light, ButtonFill.Tonal))
        val strongAccent = light.copy(palette = light.palette.with(ThemeColorRole.Primary, color("#1746C2")))
        assertTrue(ThemeContrast.canUseFill(strongAccent, light, ButtonFill.Outlined))
    }

    @Test
    fun gradientEndIsAlwaysChecked() {
        val gradient = light.copy(
            background = ThemeBackground.Gradient,
            palette = light.palette.with(ThemeColorRole.Background2, color("#1A1A1A")),
        )
        val failing = ThemeContrast.failing(gradient, light).map { it.kind }
        assertTrue(ContrastCheckKind.InkOnGradientEnd in failing)
    }

    @Test
    fun nearestFindsPassingColorByMixing() {
        val pale = color("#AFC6FF")
        assertFalse(ThemeContrast.passes(light, light, ThemeColorRole.Primary, pale))
        val fixed = assertNotNull(ThemeContrast.nearest(light, light, ThemeColorRole.Primary, pale))
        assertTrue(ThemeContrast.passes(light, light, ThemeColorRole.Primary, fixed))
    }

    @Test
    fun nearestGivesUpWhenRelatedColorsBlock() {
        val boxed = light.copy(
            palette = light.palette
                .with(ThemeColorRole.Background, color("#DDDDDD"))
                .with(ThemeColorRole.Card, ThemeColor.Black),
        )
        assertNull(ThemeContrast.nearest(boxed, light, ThemeColorRole.Ink, color("#777777")))
    }

    @Test
    fun radiusSliderMovesAllButChips() {
        val spec = light.withRadius(6)
        assertEquals(ThemeRadii(buttons = 6, inputs = 6, cards = 10, chips = ThemeRadii.MAX, sheets = 10), spec.radii)
        assertEquals(ThemeRadii.MAX, light.withRadius(40).radius)
    }
}
