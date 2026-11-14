package ru.alarmtrade.specmp.core.barcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize

actual val isBarcodeScanAvailable: Boolean = false

// Desktop: камеры для сканера нет, иконку сканера экраны не показывают; на случай вызова — «камера недоступна».
@Composable
internal actual fun CameraPreview(
    frame: DpSize,
    torch: Boolean,
    focus: FocusRequest?,
    onDetect: (List<String>) -> Unit,
    onUnavailable: () -> Unit,
    modifier: Modifier,
) {
    val currentOnUnavailable by rememberUpdatedState(onUnavailable)
    LaunchedEffect(Unit) { currentOnUnavailable() }
}
