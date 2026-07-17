package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.LocalSupportChatImageLoader
import specmp.feature.support_chat.generated.resources.Res
import specmp.feature.support_chat.generated.resources.support_chat_a11y_close_viewer

@Composable
internal fun SupportChatImageViewer(
    imageSource: String,
    onDismiss: () -> Unit,
) {
    val imageLoader = LocalSupportChatImageLoader.current
    val colors = cmpThemeColors()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        CmpBox(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.surface)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = imageSource,
                contentDescription = null,
                imageLoader = imageLoader,
                modifier = Modifier.fillMaxSize(),
            )

            CmpIconButton(
                onClick = onDismiss,
                icon = CmpIcons.Close,
                contentDescription = stringResource(Res.string.support_chat_a11y_close_viewer),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp),
                tint = colors.onSurface,
            )
        }
    }
}
