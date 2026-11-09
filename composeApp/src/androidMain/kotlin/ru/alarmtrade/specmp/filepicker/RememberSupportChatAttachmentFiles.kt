package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentFiles

/** Удаляет только копии в своём каталоге `filesDir/support-chat-attachments`; чужие пути игнорирует. */
@Composable
actual fun rememberSupportChatAttachmentFiles(): SupportChatAttachmentFiles {
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        val dir = supportChatAttachmentsDir(appContext).canonicalFile
        object : SupportChatAttachmentFiles {
            override fun discard(path: String) {
                val file = File(path).canonicalFile
                if (file.parentFile?.parentFile == dir) file.deleteWithDirectory()
            }
        }
    }
}
