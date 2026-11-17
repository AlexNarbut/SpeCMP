package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_btn_download
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_btn_open
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment

/** Файл скачивается только после выбора; «Скачать» есть только у вложения с url. */
@Composable
internal fun SupportChatDocumentActionsDialog(
    attachment: SupportChatUiAttachment,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = {
            CmpText(
                text = attachment.name,
                style = CmpTextStyle.Paragraph2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        confirmButton = {
            CmpTextButton(
                text = stringResource(Strings.string.supportchat_chat_btn_open),
                onClick = onOpen,
            )
        },
        dismissButton = if (attachment.url != null) {
            {
                CmpTextButton(
                    text = stringResource(Strings.string.supportchat_chat_btn_download),
                    onClick = onDownload,
                )
            }
        } else {
            null
        },
    )
}
