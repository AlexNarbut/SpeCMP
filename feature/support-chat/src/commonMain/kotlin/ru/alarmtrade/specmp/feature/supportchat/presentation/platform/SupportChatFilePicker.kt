package ru.alarmtrade.specmp.feature.supportchat.presentation.platform

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile

/**
 * Поток, а не колбэк: на Android, пока открыт системный пикер, процесс могут убить, и результат придёт в новый
 * процесс. На каждый `launch*` — ровно один результат; размер проверяется до копирования.
 */
interface SupportChatFilePicker {
    val results: Flow<SupportChatPickedFileResult>

    fun launchImage()
    fun launchDocument()
}

/** Источник выбора: тип вложения задаёт он, а не содержимое файла. */
enum class SupportChatPickKind { Image, Document }

/** Отмена — не ошибка, пользователю не сообщается. */
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
