package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_close_viewer
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpFullScreenDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.image.CmpAsyncImage
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox

@Composable
internal fun SupportChatImageViewer(imageSource: String, onDismiss: () -> Unit) {
    val colors = cmpThemeColors()

    CmpFullScreenDialog(onDismissRequest = onDismiss) {
        CmpBox(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.surface)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            CmpAsyncImage(
                model = imageSource,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )

            CmpIconButton(
                onClick = onDismiss,
                icon = CmpIcons.Close,
                contentDescription = stringResource(Strings.string.supportchat_chat_a11y_close_viewer),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp),
                tint = colors.onSurface,
            )
        }
    }
}
