package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_close_viewer
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButtonVariant
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpFullScreenDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.image.CmpAsyncImage
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox

@Composable
internal fun SupportChatImageViewer(imageSource: String, onDismiss: () -> Unit) {
    CmpFullScreenDialog(onDismissRequest = onDismiss) {
        CmpBox(
            modifier = Modifier
                .fillMaxSize()
                .background(CmpTheme.colors.backgroundPagePrimary)
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
                    .padding(CmpSpacing.space16),
                variant = CmpIconButtonVariant.Glass,
            )
        }
    }
}
