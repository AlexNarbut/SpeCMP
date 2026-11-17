package ru.alarmtrade.specmp.feature.appearance.presentation.model

import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColor

/** Образцы редактора цвета: быстрые — из макета, палитра — тона основных оттенков. */
internal object ThemeSwatches {
    val Quick: List<ThemeColor> = colors(
        "#2A62F5", "#1F7A55", "#C2410C", "#7B3FA6", "#0038D6", "#2C3639",
        "#D92D20", "#FFD60A", "#FFFFFF", "#F3F6FB", "#0B1220", "#000000",
    )

    val Palette: List<ThemeColor> = colors(
        "#B71C1C", "#880E4F", "#4A148C", "#311B92", "#1A237E", "#0D47A1",
        "#01579B", "#006064", "#004D40", "#1B5E20", "#33691E", "#827717",
        "#E53935", "#D81B60", "#8E24AA", "#5E35B1", "#3949AB", "#1E88E5",
        "#039BE5", "#00ACC1", "#00897B", "#43A047", "#7CB342", "#C0CA33",
        "#EF9A9A", "#F48FB1", "#CE93D8", "#B39DDB", "#9FA8DA", "#90CAF9",
        "#81D4FA", "#80DEEA", "#80CBC4", "#A5D6A7", "#C5E1A5", "#E6EE9C",
        "#F57F17", "#FF6F00", "#E65100", "#BF360C", "#3E2723", "#212121",
        "#FDD835", "#FFB300", "#FB8C00", "#F4511E", "#6D4C41", "#757575",
        "#FFF59D", "#FFE082", "#FFCC80", "#FFAB91", "#BCAAA4", "#EEEEEE",
    )

    private fun colors(vararg hex: String): List<ThemeColor> = hex.map { requireNotNull(ThemeColor.parse(it)) }
}
