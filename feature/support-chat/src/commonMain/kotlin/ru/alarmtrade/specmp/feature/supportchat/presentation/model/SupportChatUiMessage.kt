package ru.alarmtrade.specmp.feature.supportchat.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus

/**
 * Presentation-модель сообщения для UI.
 *
 * Доменный `SupportChatMessage` не `@Immutable` и содержит sealed
 * `SupportChatAttachment` — параметром composable компилятор считает его
 * нестабильным. Маппинг выполняется на границе экрана (`buildListItems`),
 * формат времени вычисляется один раз при маппинге, а не на рекомпозиции.
 */
@Immutable
data class SupportChatUiMessage(
    val id: String,
    val text: String?,
    val attachment: SupportChatUiAttachment?,
    val timeLabel: String,
    val isMine: Boolean,
    val authorEmail: String?,
    val status: SupportChatMessageStatus,
    // Failed-сообщение, которое можно повторить: есть что отправить (текст или живой
    // локальный файл). Без файла повтор бессмыслен — остаётся только удаление.
    val canRetry: Boolean,
    // Неподтверждённое моё сообщение в Failed можно удалить из ленты и outbox.
    val canDelete: Boolean,
)

/**
 * Маппинг доменного сообщения в presentation-модель на границе UI.
 * [localTime] — время сообщения в поясе устройства (вычисляет вызывающий).
 */
fun SupportChatMessage.toUiMessage(localTime: LocalDateTime): SupportChatUiMessage {
    val isFailedMine = isMine && serverId == null && status == SupportChatMessageStatus.Failed
    return SupportChatUiMessage(
        id = id,
        text = text,
        attachment = attachment?.toUiAttachment(),
        timeLabel = formatMessageTime(localTime),
        isMine = isMine,
        authorEmail = authorEmail,
        status = status,
        canRetry = isFailedMine && isResendable(),
        canDelete = isFailedMine,
    )
}

/** Есть что отправить повторно: текст или вложение с живым локальным путём. */
fun SupportChatMessage.isResendable(): Boolean {
    val attachment = attachment
    return if (attachment != null) attachment.localPath != null else !text.isNullOrBlank()
}

// Форматирование времени сообщения — презентационная логика: доменная модель
// хранит Instant, готовая строка в ней не живёт.
private fun formatMessageTime(time: LocalDateTime): String {
    val hour = time.hour.toString().padStart(2, '0')
    val minute = time.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}
