package ru.alarmtrade.specmp.feature.supportchat.presentation.platform

/**
 * Платформенная абстракция сохранения вложений чата в пользовательские «Загрузки».
 *
 * Реализация живёт в платформенном слое `composeApp` (Android MediaStore,
 * Desktop ~/Downloads, iOS export через UIDocumentPickerViewController).
 * Файл сохраняется в подпапку `Pandora Spec` директории загрузок.
 */
interface SupportChatFileSaver {

    /**
     * Сохраняет [bytes] под именем [fileName] в `Загрузки/Pandora Spec`.
     * Три разных исхода не схлопываются в null: отмена пользователем —
     * не ошибка, ошибка записи/прав — [SupportChatFileSaveResult.Failed].
     */
    suspend fun saveToDownloads(fileName: String, bytes: ByteArray): SupportChatFileSaveResult
}

sealed interface SupportChatFileSaveResult {
    data class Saved(val path: String) : SupportChatFileSaveResult
    data object Cancelled : SupportChatFileSaveResult

    /** Android 7–9 (API 24–28): пользователь не дал `WRITE_EXTERNAL_STORAGE`. */
    data object PermissionDenied : SupportChatFileSaveResult
    data object Failed : SupportChatFileSaveResult
}
