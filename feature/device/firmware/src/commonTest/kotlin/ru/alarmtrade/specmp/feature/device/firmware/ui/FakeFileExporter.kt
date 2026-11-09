package ru.alarmtrade.specmp.feature.device.firmware.ui

import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveTarget

internal class FakeFileExporter(
    override val target: FirmwareSaveTarget = FirmwareSaveTarget.Downloads,
    override val canShare: Boolean = true,
    override val canOpenFolder: Boolean = false,
) : FirmwareFileExporter {
    var saveResult: FirmwareSaveResult? = null
    var shareResult: Boolean = true
    val saved = mutableListOf<Pair<String, Int>>()
    val shared = mutableListOf<String>()
    val openedFolders = mutableListOf<String>()

    override suspend fun save(fileName: String, bytes: ByteArray): FirmwareSaveResult {
        saved += fileName to bytes.size
        return saveResult ?: FirmwareSaveResult.Saved("Download/Pandora Spec/$fileName", "Download/Pandora Spec")
    }

    override suspend fun share(fileName: String, bytes: ByteArray): Boolean {
        shared += fileName
        return shareResult
    }

    override suspend fun openFolder(path: String): Boolean {
        openedFolders += path
        return true
    }
}
