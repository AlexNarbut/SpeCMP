package ru.alarmtrade.specmp.feature.supportchat.presentation.platform

interface SupportChatFileSaver {

    /** Отмена пользователем — не ошибка; ошибка записи/прав — [SupportChatFileSaveResult.Failed]. */
    suspend fun saveToDownloads(fileName: String, bytes: ByteArray): SupportChatFileSaveResult
}

sealed interface SupportChatFileSaveResult {
    data class Saved(val path: String) : SupportChatFileSaveResult
    data object Cancelled : SupportChatFileSaveResult

    /** Android 7–9 (API 24–28): пользователь не дал `WRITE_EXTERNAL_STORAGE`. */
    data object PermissionDenied : SupportChatFileSaveResult
    data object Failed : SupportChatFileSaveResult
}
