package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform

interface FirmwareFileExporter {
    val target: FirmwareSaveTarget

    val canShare: Boolean

    val canOpenFolder: Boolean

    /** Сохраняет [bytes] под именем [fileName]; отмена пользователем — не ошибка. */
    suspend fun save(fileName: String, bytes: ByteArray): FirmwareSaveResult

    suspend fun share(fileName: String, bytes: ByteArray): Boolean

    suspend fun openFolder(path: String): Boolean
}

enum class FirmwareSaveTarget { Downloads, Folder, Chosen }

sealed interface FirmwareSaveResult {
    data class Saved(val path: String, val folder: String?) : FirmwareSaveResult

    data object Cancelled : FirmwareSaveResult

    data object PermissionDenied : FirmwareSaveResult

    data object Failed : FirmwareSaveResult
}
