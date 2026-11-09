package ru.alarmtrade.specmp.feature.supportchat.presentation.platform

/**
 * Копия нужна, пока сообщение не подтверждено сервером (в т. ч. для outbox после рестарта). Реализация удаляет
 * только файлы своего каталога: Desktop отправляет оригинал пользователя.
 */
interface SupportChatAttachmentFiles {
    fun discard(path: String)
}
