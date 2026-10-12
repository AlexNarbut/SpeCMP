package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpenResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpener
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.nio.file.Files

/** Desktop: временный файл + приложение по умолчанию (`java.awt.Desktop.open`). */
@Composable
actual fun rememberSupportChatFileOpener(): SupportChatFileOpener {
    return remember {
        object : SupportChatFileOpener {
            override suspend fun openBytes(fileName: String, bytes: ByteArray): SupportChatFileOpenResult {
                val file = try {
                    val directory = Files.createTempDirectory(TEMP_PREFIX).toFile().apply { deleteOnExit() }
                    File(directory, safeFileName(fileName)).apply {
                        writeBytes(bytes)
                        deleteOnExit()
                    }
                } catch (e: Exception) {
                    return SupportChatFileOpenResult.Failed
                }
                return openWithDesktop(file)
            }

            override suspend fun openLocal(path: String, fileName: String): SupportChatFileOpenResult {
                val file = File(path)
                return if (file.isFile) openWithDesktop(file) else SupportChatFileOpenResult.Failed
            }
        }
    }
}

private fun openWithDesktop(file: File): SupportChatFileOpenResult {
    if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
        return SupportChatFileOpenResult.NoApp
    }
    return try {
        Desktop.getDesktop().open(file)
        SupportChatFileOpenResult.Opened
    } catch (e: IOException) {
        // Нет ассоциированного приложения для типа файла.
        SupportChatFileOpenResult.NoApp
    } catch (e: Exception) {
        SupportChatFileOpenResult.Failed
    }
}

private const val TEMP_PREFIX = "support-chat-open"
