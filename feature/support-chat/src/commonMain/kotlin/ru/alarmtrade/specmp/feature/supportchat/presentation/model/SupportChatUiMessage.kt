package ru.alarmtrade.specmp.feature.supportchat.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus

/**
 * Presentation-модель сообщения для UI (U-12).
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
)

/**
 * Маппинг доменного сообщения в presentation-модель на границе UI.
 */
fun SupportChatMessage.toUiMessage(): SupportChatUiMessage {
    return SupportChatUiMessage(
        id = id,
        text = text,
        attachment = attachment?.toUiAttachment(),
        timeLabel = formatMessageTime(time),
        isMine = isMine,
        authorEmail = authorEmail,
        status = status,
    )
}

// Форматирование времени сообщения — презентационная логика (D-1):
// доменная модель хранит LocalDateTime, готовая строка в ней не живёт.
private fun formatMessageTime(time: LocalDateTime): String {
    val hour = time.hour.toString().padStart(2, '0')
    val minute = time.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}
