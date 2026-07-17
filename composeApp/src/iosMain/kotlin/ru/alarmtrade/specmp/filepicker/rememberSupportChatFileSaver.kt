@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToURL
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatFileSaver

// У iOS нет общей директории «Загрузки»; файлы складываем в Documents/Pandora Spec —
// эта папка видна пользователю в приложении «Файлы».
@Composable
actual fun rememberSupportChatFileSaver(): SupportChatFileSaver {
    return remember {
        object : SupportChatFileSaver {
            override suspend fun saveToDownloads(fileName: String, bytes: ByteArray): String? {
                return try {
                    saveToDocuments(fileName, bytes)
                } catch (e: Throwable) {
                    null
                }
            }
        }
    }
}

private fun saveToDocuments(fileName: String, bytes: ByteArray): String? {
    val fileManager = NSFileManager.defaultManager
    val documentsUrl = fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    ) ?: return null
    val dirUrl = documentsUrl.URLByAppendingPathComponent("Pandora Spec") ?: return null
    val dirPath = dirUrl.path ?: return null
    fileManager.createDirectoryAtPath(
        path = dirPath,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    val fileUrl = dirUrl.URLByAppendingPathComponent(fileName) ?: return null
    val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
    return if (data.writeToURL(fileUrl, atomically = true)) fileUrl.path else null
}
