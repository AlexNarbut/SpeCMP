package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveTarget

/**
 * Desktop: сохранение — в `~/Downloads/Pandora Spec` (как вложения чата), «Открыть папку» —
 * `java.awt.Desktop.open`. Системного «Поделиться» нет.
 */
@Composable
actual fun rememberFirmwareFileExporter(): FirmwareFileExporter {
    val saver = rememberSupportChatFileSaver()
    return remember(saver) {
        object : FirmwareFileExporter {
            override val target = FirmwareSaveTarget.Folder
            override val canShare = false
            override val canOpenFolder = Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.OPEN)

            override suspend fun save(fileName: String, bytes: ByteArray): FirmwareSaveResult =
                saver.saveToDownloads(fileName, bytes).toFirmware { path -> File(path).parent }

            override suspend fun share(fileName: String, bytes: ByteArray): Boolean = false

            override suspend fun openFolder(path: String): Boolean = withContext(Dispatchers.IO) {
                try {
                    val folder = File(path).parentFile ?: return@withContext false
                    Desktop.getDesktop().open(folder)
                    true
                } catch (ignored: Exception) {
                    false
                }
            }
        }
    }
}
