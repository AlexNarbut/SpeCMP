package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentFiles
import java.io.File

/**
 * Удаляет копии вложений, созданные пикером, — только внутри собственного каталога
 * (`filesDir/support-chat-attachments/<uuid>/<имя>`); чужие пути игнорируются.
 */
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
