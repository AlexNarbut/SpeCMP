package ru.alarmtrade.specmp.feature.supportchat.data.mapper

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specialist.domain.chat.ChatEvent
import ru.alarmtrade.specialist.domain.chat.ChatMessageContent

/**
 * Маппинг SDK-сообщений в доменную модель.
 *
 * Часовой пояс: SDK сериализует `time` через `IsoDateSerializer` в UTC
 * (проверено по исходникам 1.1.76: сервер отдаёт UTC, SDK не сдвигает зону),
 * поэтому маппинг явно трактует входящее время как UTC и конвертирует в
 * [systemTimeZone]. Если формат сервера изменится, сдвиг проявится именно здесь —
 * фиксируем контракт тестом.
 */
internal class SupportChatMessageMapper(
    private val systemTimeZone: TimeZone,
) {

    fun map(message: ChatEvent.Chat.Message, myEmail: String?): SupportChatMessage {
        // Контент эксклюзивен (sealed: Text | Image | Document) — сообщение с
        // подписью SDK не поддерживает, маппим единственное содержимое.
        val content = (message as? ChatEvent.Chat.Message.Default)?.messageContent

        val attachment = when (content) {
            is ChatMessageContent.Image -> SupportChatAttachment.Image(
                id = content.id,
                name = content.name,
                url = content.url,
            )
            is ChatMessageContent.Document -> SupportChatAttachment.Document(
                id = content.id,
                name = content.name,
                url = content.url,
            )
            else -> null
        }

        val text = (content as? ChatMessageContent.Text)?.text

        val localTime = message.time.toInstant(TimeZone.UTC).toLocalDateTime(systemTimeZone)

        return SupportChatMessage(
            id = message.id.toString(),
            serverId = message.id,
            text = text,
            attachment = attachment,
            time = localTime,
            isMine = myEmail != null && message.sender.login == myEmail,
            authorEmail = message.sender.login,
            status = if (message.wasRead) SupportChatMessageStatus.Read else SupportChatMessageStatus.Sent,
        )
    }
}
