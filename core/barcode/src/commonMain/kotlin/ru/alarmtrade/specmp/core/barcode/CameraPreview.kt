package ru.alarmtrade.specmp.core.barcode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize

/** Камера есть на платформе (Android, iOS); на desktop сканера нет. */
expect val isBarcodeScanAvailable: Boolean

/** Фокус камеры в точку ([x], [y] — от левого верхнего угла превью); [seq] — повторный тап в ту же точку. */
internal data class FocusRequest(val x: Dp, val y: Dp, val seq: Int)

/**
 * Превью задней камеры и распознавание кодов внутри рамки [frame] (по центру области). [onDetect] — тексты кодов
 * кадра на главном потоке; [onUnavailable] — камеры нет, она занята или не запустилась.
 */
@Composable
internal expect fun CameraPreview(
    frame: DpSize,
    torch: Boolean,
    focus: FocusRequest?,
    onDetect: (List<String>) -> Unit,
    onUnavailable: () -> Unit,
    modifier: Modifier = Modifier,
)
