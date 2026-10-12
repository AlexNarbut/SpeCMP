@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSFileManager
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentFiles

/**
 * Удаляет копии вложений, созданные пикером, — только внутри собственного каталога
 * (`Application Support/support-chat-attachments/<uuid>/<имя>`); чужие пути игнорируются.
 */
@Composable
actual fun rememberSupportChatAttachmentFiles(): SupportChatAttachmentFiles {
    return remember {
        object : SupportChatAttachmentFiles {
            override fun discard(path: String) {
                val directory = supportChatAttachmentsDirectory() ?: return
                if (!path.startsWith("$directory/") || path.contains("/../")) return
                // Копия лежит в персональной папке `<uuid>/<имя>` — удаляем папку целиком.
                val copyDirectory = path.substringBeforeLast('/')
                if (copyDirectory != directory) {
                    NSFileManager.defaultManager.removeItemAtPath(copyDirectory, error = null)
                }
            }
        }
    }
}
