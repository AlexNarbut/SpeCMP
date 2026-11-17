package ru.alarmtrade.specmp.core.ui.theme.custom

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import ru.alarmtrade.specmp.core.ui.theme.custom.data.ThemeFileDto
import ru.alarmtrade.specmp.core.ui.theme.custom.data.mapper.applyTo
import ru.alarmtrade.specmp.core.ui.theme.custom.data.mapper.toThemeModeDto

sealed interface ThemeImportResult {
    data object NotThemeFile : ThemeImportResult

    /** [fixedColors] — исправлено по контрасту; [colorsReset] — цвета не спасти, оставлены стандартные. */
    data class Imported(val spec: ThemeSpec, val fixedColors: Int, val colorsReset: Boolean) : ThemeImportResult
}

/** Файл темы и сохранённая тема — один формат; неизвестные и неверные поля пропускаются. */
object ThemeFileCodec {
    const val FORMAT = "pandora-spec-theme"
    const val VERSION = 1

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        explicitNulls = false
    }

    fun encode(spec: ThemeSpec): String = json.encodeToString(
        ThemeFileDto(
            format = FORMAT,
            version = VERSION,
            light = spec.light.toThemeModeDto(),
            dark = spec.dark.toThemeModeDto(),
        ),
    )

    /** Поля поверх [base] (текущий черновик), затем исправление контраста относительно темы по умолчанию. */
    fun decode(text: String, base: ThemeSpec = ThemePreset.Default): ThemeImportResult {
        val dto = try {
            json.decodeFromString<ThemeFileDto>(text)
        } catch (_: SerializationException) {
            return ThemeImportResult.NotThemeFile
        } catch (_: IllegalArgumentException) {
            return ThemeImportResult.NotThemeFile
        }
        if (dto.format != FORMAT) return ThemeImportResult.NotThemeFile
        val defaults = ThemePreset.Default
        val light = dto.light.applyTo(base.light).sanitized(defaults.light)
        val dark = dto.dark.applyTo(base.dark).sanitized(defaults.dark)
        return ThemeImportResult.Imported(
            spec = ThemeSpec(light = light.mode, dark = dark.mode),
            fixedColors = light.fixed + dark.fixed,
            colorsReset = light.reset || dark.reset,
        )
    }

    /** Сохранённая тема: битые данные — тема по умолчанию. */
    fun decodeStored(text: String): ThemeSpec =
        (decode(text) as? ThemeImportResult.Imported)?.spec ?: ThemePreset.Default
}

private class Sanitized(val mode: ThemeModeSpec, val fixed: Int, val reset: Boolean)

private fun ThemeModeSpec.sanitized(default: ThemeModeSpec): Sanitized {
    var mode = this
    var fixed = 0
    for (check in ThemeContrast.checks(mode, default)) {
        if (check.passes(mode.palette)) continue
        val role = check.kind.foreground
        ThemeContrast.nearest(mode, default, role, mode.palette[role])?.let { color ->
            mode = mode.copy(palette = mode.palette.with(role, color))
            fixed++
        }
    }
    if (ThemeContrast.passes(mode, default)) return Sanitized(mode, fixed, reset = false)
    return Sanitized(mode.copy(palette = default.palette, buttonFill = default.buttonFill), 0, reset = true)
}
