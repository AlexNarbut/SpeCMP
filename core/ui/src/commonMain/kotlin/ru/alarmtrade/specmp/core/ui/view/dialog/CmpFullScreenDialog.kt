package ru.alarmtrade.specmp.core.ui.view.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Полноэкранный диалог (U-4): `CmpDialog` — обёртка над `AlertDialog` и
 * полноэкранный режим не поддерживает в принципе. Используется для сценариев
 * вроде просмотра изображения на весь экран.
 */
@Composable
fun CmpFullScreenDialog(onDismissRequest: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = { Box(modifier) { content() } },
    )
}
