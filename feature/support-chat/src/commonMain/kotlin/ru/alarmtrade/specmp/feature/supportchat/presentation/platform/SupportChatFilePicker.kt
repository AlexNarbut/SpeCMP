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
 * Контракт — Flow, а не callback: на Android системный пикер — отдельная
 * activity, система может убить наш процесс, пока она открыта. Лямбда-колбэк
 * в `remember` при этом теряется, и результат выбора молча пропадает. Результат
 * от activity-result registry приходит уже в новый процесс — реализации
 * обязаны буферизовать его (Channel) до появления подписчика.
 */
interface SupportChatFilePicker {
    /** Результаты выбора: по одному на каждый `launch*`, буферизуются до сбора. */
    val results: Flow<SupportChatPickedFileResult>

    fun launchImage()
    fun launchDocument()
}

/**
 * Результат работы пикера: файл выбран либо пользователь отменил диалог.
 * Отмена — не ошибка и ничего не сообщает пользователю.
 */
sealed interface SupportChatPickedFileResult {
    data class Picked(
        val file: SupportChatPickedFile,
        /** Источник выбора: тип вложения определяется им, а не содержимым файла. */
        val kind: SupportChatPickKind,
    ) : SupportChatPickedFileResult

    data object Cancelled : SupportChatPickedFileResult
}

enum class SupportChatPickKind { Image, Document }
