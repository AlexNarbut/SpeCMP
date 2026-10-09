package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaveResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver
import java.io.File

@Composable
actual fun rememberSupportChatFileSaver(): SupportChatFileSaver {
    return remember {
        object : SupportChatFileSaver {
            override suspend fun saveToDownloads(fileName: String, bytes: ByteArray): SupportChatFileSaveResult {
                return try {
                    val dir = File(System.getProperty("user.home"), "Downloads/Pandora Spec")
                    if (!dir.exists() && !dir.mkdirs()) return SupportChatFileSaveResult.Failed
                    // Имя приходит с сервера: basename без `../` и запрещённых символов,
                    // одноимённый файл пользователя не перезаписывается.
                    val file = File(dir, uniqueFileName(safeFileName(fileName)) { File(dir, it).exists() })
                    file.writeBytes(bytes)
                    SupportChatFileSaveResult.Saved(file.absolutePath)
                } catch (e: Exception) {
                    SupportChatFileSaveResult.Failed
                }
            }
        }
    }
}
