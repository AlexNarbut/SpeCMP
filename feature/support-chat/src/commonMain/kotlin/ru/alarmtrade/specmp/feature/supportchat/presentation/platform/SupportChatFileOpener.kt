package ru.alarmtrade.specmp.feature.supportchat.presentation.platform

/**
 * Платформенный порт открытия вложения во внешнем приложении.
 *
 * Реализация живёт в `composeApp`: Android — временный файл + `FileProvider` +
 * `ACTION_VIEW`, iOS — QuickLook (`UIDocumentInteractionController`), Desktop —
 * `java.awt.Desktop.open`. Вызывается с фонового потока; UI-часть реализация сама
 * переводит на main. Не держит Activity-контекст (VM переживает её пересоздание).
 */
interface SupportChatFileOpener {

    /** Скачанное вложение: реализация пишет байты во временный файл и открывает его. */
    suspend fun openBytes(fileName: String, bytes: ByteArray): SupportChatFileOpenResult

    /** Своё неподтверждённое вложение — из локальной копии, без сети. */
    suspend fun openLocal(path: String, fileName: String): SupportChatFileOpenResult
}

sealed interface SupportChatFileOpenResult {
    data object Opened : SupportChatFileOpenResult

    /** В системе нет приложения для этого типа файла — не ошибка сети. */
    data object NoApp : SupportChatFileOpenResult
    data object Failed : SupportChatFileOpenResult
}
