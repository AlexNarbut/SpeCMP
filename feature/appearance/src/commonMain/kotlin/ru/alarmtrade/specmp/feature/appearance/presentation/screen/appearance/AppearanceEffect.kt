package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance

internal sealed interface AppearanceEffect {
    data object Applied : AppearanceEffect

    data class Exported(val fileName: String) : AppearanceEffect

    data object ExportFailed : AppearanceEffect

    /** Файл не прочитан: ошибка чтения или слишком большой. */
    data object ImportFailed : AppearanceEffect

    data object NotThemeFile : AppearanceEffect

    /** [colorsReset] — цвета не спасти, оставлены стандартные; иначе исправлено [fixedColors] цветов. */
    data class Imported(val fixedColors: Int, val colorsReset: Boolean) : AppearanceEffect
}
