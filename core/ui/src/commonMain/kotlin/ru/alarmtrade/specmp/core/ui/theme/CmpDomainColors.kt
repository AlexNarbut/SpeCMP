package ru.alarmtrade.specmp.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpColors

/** Цвета дизайн-системы, которых нет в Material-схеме; шины — цвет по семейству, номер шины — текстом. */
data class CmpDomainColors(
    val success: Color,
    /** Предупреждение (в Material-схеме роли нет): «вне зоны», слабый сигнал, «не на связи» у слота. */
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val rating: Color,
    val busCan: Color,
    val busCanFd: Color,
    val busLin: Color,
    val busBean: Color,
    val busImmobilizer: Color,
    val busUnknown: Color,
    /** Слой камеры (сканер штрих-кода): всегда тёмный, независимо от темы приложения. */
    val camera: Color = CameraBackground,
    val cameraScrim: Color = CameraScrim,
    val onCamera: Color = OnCamera,
    val onCameraVariant: Color = OnCameraVariant,
    val cameraControl: Color = CameraControl,
)

private val CameraBackground = Color(0xFF0D0F10)
private val CameraScrim = Color(0x94000000)
private val OnCamera = Color(0xFFFFFFFF)
private val OnCameraVariant = Color(0xCCFFFFFF)
private val CameraControl = Color(0x29FFFFFF)

private val LightRating = Color(0xFFE0A100)
private val DarkRating = Color(0xFFF2C14E)

private fun domainColors(c: CmpColors, rating: Color) = CmpDomainColors(
    success = c.successDefault,
    warning = c.warningDefault,
    warningContainer = c.warningHighlight,
    onWarningContainer = c.foregroundOnDarkPrimaryDefault,
    rating = rating,
    busCan = c.busCan,
    busCanFd = c.busCanfd,
    busLin = c.busLin,
    busBean = c.busBean,
    busImmobilizer = c.busImo,
    busUnknown = c.busUnknown,
)

@Composable
fun cmpDomainColors(): CmpDomainColors {
    val colors = CmpTheme.colors
    val isDark = LocalThemeIsDark.current.value
    return remember(colors, isDark) { domainColors(colors, if (isDark) DarkRating else LightRating) }
}
