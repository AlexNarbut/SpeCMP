package ru.alarmtrade.specmp.core.barcode

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize

/** Камера есть на платформе (Android, iOS); на desktop сканера нет. */
expect val isBarcodeScanAvailable: Boolean

/**
 * Превью задней камеры и распознавание кодов внутри рамки [frame] (по центру области). [onDetect] — тексты кодов
 * кадра на главном потоке; [onUnavailable] — камеры нет, она занята или не запустилась.
 */
@Composable
internal expect fun CameraPreview(
    frame: DpSize,
    torch: Boolean,
    onDetect: (List<String>) -> Unit,
    onUnavailable: () -> Unit,
    modifier: Modifier = Modifier,
)
