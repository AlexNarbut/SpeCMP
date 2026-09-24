package ru.alarmtrade.specmp.feature.supportchat.presentation.platform

import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile

/**
 * Платформенная абстракция для выбора файлов из галереи/файловой системы.
 *
 * Реализация живёт в платформенном слое `composeApp` (activity-result launchers,
 * desktop dialogs, iOS UIDocumentPicker). Feature-модуль получает только готовый
 * к отправке путь и мета-информацию.
 */
interface SupportChatFilePicker {
    fun launchImage(onPicked: (SupportChatPickedFileResult) -> Unit)
    fun launchDocument(onPicked: (SupportChatPickedFileResult) -> Unit)
}

/**
 * Результат работы пикера: файл выбран либо пользователь отменил диалог.
 * Отмена — не ошибка и ничего не сообщает пользователю.
 */
sealed interface SupportChatPickedFileResult {
    data class Picked(val file: SupportChatPickedFile) : SupportChatPickedFileResult
    data object Cancelled : SupportChatPickedFileResult
}
