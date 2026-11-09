package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.io.File
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveTarget

@Composable
actual fun rememberFirmwareFileExporter(): FirmwareFileExporter {
    val saver = rememberSupportChatFileSaver()
    val ioDispatcher = koinInject<AppDispatchers>().io
    return remember(saver, ioDispatcher) {
        object : FirmwareFileExporter {
            override val target = FirmwareSaveTarget.Folder
            override val canShare = false
            override val canOpenFolder = Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.OPEN)

            override suspend fun save(fileName: String, bytes: ByteArray): FirmwareSaveResult =
                saver.saveToDownloads(fileName, bytes).toFirmware { path -> File(path).parent }

            override suspend fun share(fileName: String, bytes: ByteArray): Boolean = false

            override suspend fun openFolder(path: String): Boolean = withContext(ioDispatcher) {
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
