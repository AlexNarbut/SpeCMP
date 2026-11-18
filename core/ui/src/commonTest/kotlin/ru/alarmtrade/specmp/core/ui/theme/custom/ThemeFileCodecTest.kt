package ru.alarmtrade.specmp.core.ui.theme.custom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ThemeFileCodecTest {
    private fun imported(text: String, base: ThemeSpec = ThemePreset.Default) =
        assertIs<ThemeImportResult.Imported>(ThemeFileCodec.decode(text, base))

    @Test
    fun everyPresetSurvivesRoundTrip() {
        ThemePreset.entries.forEach { preset ->
            val result = imported(ThemeFileCodec.encode(preset.spec))
            assertEquals(preset.spec, result.spec, preset.name)
            assertEquals(0, result.fixedColors)
        }
    }

    @Test
    fun foreignFilesAreRejected() {
        listOf("", "not json", "[]", """{"format":"other","light":{}}""", """{"light":{}}""").forEach {
            assertEquals(ThemeImportResult.NotThemeFile, ThemeFileCodec.decode(it), it)
        }
    }

    @Test
    fun designerFileIsRead() {
        val text = """
            {"format":"pandora-spec-theme","version":1,
             "light":{"c":{"primary":"#1F7A55","bg":"#F1F5F2"},"bgType":"gradient","bgDir":"radial","text":130,
                      "fill":"tonal","shape":"pill","radius":20,"r":{"buttons":20,"chips":28},"btnPad":"l","chipPad":"s",
                      "future":true}}
        """.trimIndent()
        val light = imported(text).spec.light
        assertEquals("#1F7A55", light.palette.primary.hex)
        assertEquals(ThemeBackground.Gradient, light.background)
        assertEquals(GradientDirection.Corner, light.gradientDirection)
        assertEquals(TextScale.Larger, light.textScale)
        assertEquals(ButtonFill.Tonal, light.buttonFill)
        assertEquals(ButtonShape.Pill, light.buttonShape)
        assertEquals(20, light.radii.buttons)
        assertEquals(ControlPadding.Large, light.buttonPadding)
        assertEquals(ControlPadding.Small, light.chipPadding)
    }

    @Test
    fun everyTextScaleSurvivesRoundTrip() {
        TextScale.entries.forEach { scale ->
            val spec = ThemePreset.Default.let { it.copy(light = it.light.copy(textScale = scale)) }
            assertEquals(scale, imported(ThemeFileCodec.encode(spec)).spec.light.textScale, scale.name)
        }
    }

    @Test
    fun smallTextScaleIsReadFromFile() {
        val text = """{"format":"pandora-spec-theme","light":{"text":80}}"""
        assertEquals(TextScale.Small, imported(text).spec.light.textScale)
        assertEquals(TextScale.Small, ThemeFileCodec.decodeStored(text).light.textScale)
    }

    @Test
    fun invalidFieldsKeepBaseValues() {
        val base = ThemePreset.Workshop.spec
        val text = """{"format":"pandora-spec-theme","dark":{"c":{"ink":"red"},"text":90,"radius":40,"fill":"neon"}}"""
        assertEquals(base, imported(text, base).spec)
    }

    @Test
    fun lowContrastColorsAreFixedAndCounted() {
        val text = """{"format":"pandora-spec-theme","light":{"c":{"ink":"#C0C0C0","ink2":"#D0D0D0"}}}"""
        val result = imported(text)
        assertEquals(2, result.fixedColors)
        assertTrue(ThemeContrast.passes(result.spec.light, ThemePreset.Default.light))
    }

    @Test
    fun hopelessColorsFallBackToDefault() {
        val text = """{"format":"pandora-spec-theme","light":{"c":{"bg":"#DDDDDD","card":"#000000"},"shape":"pill"}}"""
        val result = imported(text)
        assertTrue(result.colorsReset)
        assertEquals(ThemePreset.Default.light.palette, result.spec.light.palette)
        assertEquals(ButtonShape.Pill, result.spec.light.buttonShape)
    }

    @Test
    fun brokenStoredThemeFallsBackToDefault() {
        assertEquals(ThemePreset.Default, ThemeFileCodec.decodeStored("{"))
    }
}
