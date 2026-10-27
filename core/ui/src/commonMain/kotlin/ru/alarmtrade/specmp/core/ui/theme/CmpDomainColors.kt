package ru.alarmtrade.specmp.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Доменные цвета дизайн-системы (токены `--success`, `--rating`, `--bus-*` из
 * `design/design-system/tokens/colors.css`): то, чего нет в Material-схеме. Шины автомобиля — по семействам:
 * цвет = семейство, номер шины — текстом.
 */
data class CmpDomainColors(
    /** «Поддерживается», успешный результат. */
    val success: Color,
    /** Звезда оценки (облачные конфигурации). */
    val rating: Color,
    val busCan: Color,
    val busCanFd: Color,
    val busLin: Color,
    val busBean: Color,
    val busImmobilizer: Color,
    val busUnknown: Color
)

private val LightDomainColors = CmpDomainColors(
    success = Color(0xFF79BD5B),
    rating = Color(0xFFE0A100),
    busCan = Color(0xFF1F5FBF),
    busCanFd = Color(0xFF00838F),
    busLin = Color(0xFF7B3FA6),
    busBean = Color(0xFFB85C00),
    busImmobilizer = Color(0xFF2E7D32),
    busUnknown = Color(0xFF6B6B6B)
)

private val DarkDomainColors = CmpDomainColors(
    success = Color(0xFF79BD5B),
    rating = Color(0xFFF2C14E),
    busCan = Color(0xFF6FA8F5),
    busCanFd = Color(0xFF4DD0E1),
    busLin = Color(0xFFC79AEB),
    busBean = Color(0xFFF2A55A),
    busImmobilizer = Color(0xFF72C476),
    busUnknown = Color(0xFF9AA4B8)
)

/** Доменные цвета текущей темы ([LocalThemeIsDark]). */
@Composable
fun cmpDomainColors(): CmpDomainColors = if (LocalThemeIsDark.current.value) DarkDomainColors else LightDomainColors
