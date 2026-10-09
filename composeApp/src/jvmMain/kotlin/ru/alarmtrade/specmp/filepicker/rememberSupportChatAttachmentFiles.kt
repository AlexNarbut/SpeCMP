package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentFiles

/** Desktop отправляет оригинал пользователя без копии — удалять нечего. */
@Composable
actual fun rememberSupportChatAttachmentFiles(): SupportChatAttachmentFiles {
    return remember {
        object : SupportChatAttachmentFiles {
            override fun discard(path: String) = Unit
        }
    }
}
