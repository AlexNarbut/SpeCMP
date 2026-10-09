package ru.alarmtrade.specmp.feature.supportchat.presentation.platform

import kotlinx.coroutines.flow.Flow
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
 * - на каждый `launch*` — ровно один результат в [results], доставка на main;
 * - результаты буферизуются до появления подписчика.
 *
 * Контракт — поток, а не колбэк: на Android системный пикер — отдельная activity, и пока
 * она открыта, система может убить наш процесс. Колбэк при этом теряется, а registry
 * доставляет результат уже в новый процесс — реализация кладёт его в буфер, экран
 * забирает, когда снова подпишется.
 */
interface SupportChatFilePicker {
    val results: Flow<SupportChatPickedFileResult>

    fun launchImage()
    fun launchDocument()
}

/** Источник выбора: тип вложения задаёт он, а не содержимое файла. */
enum class SupportChatPickKind { Image, Document }

/**
 * Результат работы пикера. Отмена — не ошибка и ничего не сообщает пользователю;
 * [TooLarge] и [Failed] — показываются снекбаром.
 */
sealed interface SupportChatPickedFileResult {
    data class Picked(val file: SupportChatPickedFile, val kind: SupportChatPickKind) : SupportChatPickedFileResult
    data object Cancelled : SupportChatPickedFileResult
    data object TooLarge : SupportChatPickedFileResult
    data object Failed : SupportChatPickedFileResult
}

object SupportChatAttachmentLimits {
    const val MAX_MEGABYTES = 20
    const val MAX_BYTES: Long = MAX_MEGABYTES * 1024L * 1024L
}
