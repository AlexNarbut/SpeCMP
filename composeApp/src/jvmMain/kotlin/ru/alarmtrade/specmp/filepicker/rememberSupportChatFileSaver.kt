package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatFileSaver
import java.io.File

@Composable
actual fun rememberSupportChatFileSaver(): SupportChatFileSaver {
    return remember {
        object : SupportChatFileSaver {
            override suspend fun saveToDownloads(fileName: String, bytes: ByteArray): String? {
                return try {
                    val dir = File(System.getProperty("user.home"), "Downloads/Pandora Spec")
                    if (!dir.exists() && !dir.mkdirs()) return null
                    val file = File(dir, fileName)
                    file.writeBytes(bytes)
                    file.absolutePath
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}
