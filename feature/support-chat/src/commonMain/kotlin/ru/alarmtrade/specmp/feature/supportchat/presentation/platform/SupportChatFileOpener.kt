package ru.alarmtrade.specmp.feature.supportchat.presentation.platform

/** Вызывается с фонового потока; не держит Activity-контекст — VM переживает её пересоздание. */
interface SupportChatFileOpener {

    suspend fun openBytes(fileName: String, bytes: ByteArray): SupportChatFileOpenResult

    suspend fun openLocal(path: String, fileName: String): SupportChatFileOpenResult
}

sealed interface SupportChatFileOpenResult {
    data object Opened : SupportChatFileOpenResult

    /** В системе нет приложения для этого типа файла — не ошибка сети. */
    data object NoApp : SupportChatFileOpenResult
    data object Failed : SupportChatFileOpenResult
}
