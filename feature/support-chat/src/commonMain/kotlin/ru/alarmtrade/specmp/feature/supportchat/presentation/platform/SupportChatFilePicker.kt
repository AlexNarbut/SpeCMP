package ru.alarmtrade.specmp.feature.supportchat.presentation.platform

import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile

/**
 * Платформенная абстракция для выбора файлов из галереи/файловой системы.
 *
 * Реализация живёт в платформенном слое `composeApp` (activity-result launchers,
 * desktop dialogs, iOS UIDocumentPicker). Feature-модуль получает только готовый
 * к отправке путь и мета-информацию.
 *
 * Контракт реализации:
 * - копирование файла — вне main-потока (большой файл не должен вешать UI);
 * - размер проверяется **до** копирования: больше [SupportChatAttachmentLimits.MAX_BYTES] →
 *   [SupportChatPickedFileResult.TooLarge];
 * - [onPicked][launchImage] вызывается на main-потоке ровно один раз.
 */
interface SupportChatFilePicker {
    fun launchImage(onPicked: (SupportChatPickedFileResult) -> Unit)
    fun launchDocument(onPicked: (SupportChatPickedFileResult) -> Unit)
}

/**
 * Результат работы пикера. Отмена — не ошибка и ничего не сообщает пользователю;
 * [TooLarge] и [Failed] — показываются снекбаром.
 */
sealed interface SupportChatPickedFileResult {
    data class Picked(val file: SupportChatPickedFile) : SupportChatPickedFileResult
    data object Cancelled : SupportChatPickedFileResult
    data object TooLarge : SupportChatPickedFileResult
    data object Failed : SupportChatPickedFileResult
}

object SupportChatAttachmentLimits {
    const val MAX_MEGABYTES = 20
    const val MAX_BYTES: Long = MAX_MEGABYTES * 1024L * 1024L
}
