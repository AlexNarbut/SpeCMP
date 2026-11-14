package ru.alarmtrade.specmp.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

private val LightDomainColors = CmpDomainColors(
    success = Color(0xFF79BD5B),
    warning = Color(0xFF8A5300),
    warningContainer = Color(0xFFFBE4C4),
    onWarningContainer = Color(0xFF2E1A00),
    rating = Color(0xFFE0A100),
    busCan = Color(0xFF1F5FBF),
    busCanFd = Color(0xFF00838F),
    busLin = Color(0xFF7B3FA6),
    busBean = Color(0xFFB85C00),
    busImmobilizer = Color(0xFF2E7D32),
    busUnknown = Color(0xFF6B6B6B),
)

private val DarkDomainColors = CmpDomainColors(
    success = Color(0xFF79BD5B),
    warning = Color(0xFFF2B455),
    warningContainer = Color(0xFF4A3410),
    onWarningContainer = Color(0xFFFFE2B8),
    rating = Color(0xFFF2C14E),
    busCan = Color(0xFF6FA8F5),
    busCanFd = Color(0xFF4DD0E1),
    busLin = Color(0xFFC79AEB),
    busBean = Color(0xFFF2A55A),
    busImmobilizer = Color(0xFF72C476),
    busUnknown = Color(0xFF9AA4B8),
)

@Composable
fun cmpDomainColors(): CmpDomainColors = if (LocalThemeIsDark.current.value) DarkDomainColors else LightDomainColors
