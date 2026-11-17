package ru.alarmtrade.specmp.core.ui.theme.custom.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Файл темы `pandora-spec-theme` (формат макета `design/features/appearance/README.md`) + `bgType: "glow"`. */
@Serializable
@SerialName("ui.ThemeFileDto")
internal data class ThemeFileDto(
    val format: String? = null,
    val version: Int? = null,
    val light: ThemeModeDto? = null,
    val dark: ThemeModeDto? = null,
)

@Serializable
@SerialName("ui.ThemeModeDto")
internal data class ThemeModeDto(
    val c: Map<String, String>? = null,
    val bgType: String? = null,
    val bgDir: String? = null,
    val text: Int? = null,
    val fill: String? = null,
    val shape: String? = null,
    val radius: Double? = null,
    val r: Map<String, Double>? = null,
    val btnPad: String? = null,
    val chipPad: String? = null,
)
