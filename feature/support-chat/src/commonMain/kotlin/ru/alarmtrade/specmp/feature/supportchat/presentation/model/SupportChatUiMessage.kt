package ru.alarmtrade.specmp.feature.supportchat.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus

/** Доменный `SupportChatMessage` параметром composable нестабилен; время форматируется один раз при маппинге. */
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
    val canDelete: Boolean,
)

/** [localTime] — время в поясе устройства (вычисляет вызывающий). */
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

fun SupportChatMessage.isResendable(): Boolean {
    val attachment = attachment
    return if (attachment != null) attachment.localPath != null else !text.isNullOrBlank()
}

private fun formatMessageTime(time: LocalDateTime): String {
    val hour = time.hour.toString().padStart(2, '0')
    val minute = time.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}
