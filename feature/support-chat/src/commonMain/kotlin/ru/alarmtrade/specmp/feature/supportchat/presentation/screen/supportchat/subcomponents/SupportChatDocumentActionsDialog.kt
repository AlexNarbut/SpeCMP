package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment
import specmp.feature.support_chat.generated.resources.Res
import specmp.feature.support_chat.generated.resources.supportchat_chat_btn_download
import specmp.feature.support_chat.generated.resources.supportchat_chat_btn_open

/**
 * Действия с документом по тапу: «Открыть» (во внешнем приложении) и «Скачать»
 * (в «Загрузки»). Файл скачивается только после выбора пользователя. «Скачать»
 * есть только у вложения с url — своё неподтверждённое вложение можно лишь открыть
 * из локальной копии.
 */
@Composable
internal fun SupportChatDocumentActionsDialog(
    attachment: SupportChatUiAttachment,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { CmpText(text = attachment.name, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        confirmButton = {
            CmpTextButton(
                text = stringResource(Res.string.supportchat_chat_btn_open),
                onClick = onOpen
            )
        },
        dismissButton = if (attachment.url != null) {
            {
                CmpTextButton(
                    text = stringResource(Res.string.supportchat_chat_btn_download),
                    onClick = onDownload
                )
            }
        } else {
            null
        }
    )
}
