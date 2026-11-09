package ru.alarmtrade.specmp.feature.supportchat.data.mapper

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ru.alarmtrade.specialist.domain.chat.ChatEvent
import ru.alarmtrade.specialist.domain.chat.ChatMessageContent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus

/**
 * SDK отдаёт `time` в UTC (проверено по исходникам 1.1.76, контракт закреплён тестом) — маппинг отдаёт `Instant`,
 * в местное время переводит UI. Метаданных документа (mime, размер) SDK не отдаёт.
 */
internal class SupportChatMessageMapper {

    /** `null` — сообщение, которое фича не поддерживает (например `MountEvent`): вызывающий логирует и пропускает. */
    fun map(message: ChatEvent.Chat.Message, myEmail: String?): SupportChatMessage? {
        val default = message as? ChatEvent.Chat.Message.Default ?: return null

        // Контент эксклюзивен (sealed: Text | Image | Document) — подписей к
        // вложениям SDK не поддерживает, маппим единственное содержимое.
        var text: String? = null
        var attachment: SupportChatAttachment? = null
        when (val content = default.messageContent) {
            is ChatMessageContent.Text -> text = content.text

            is ChatMessageContent.Image -> attachment = SupportChatAttachment.Image(
                id = content.id,
                name = content.name,
                url = content.url,
            )

            is ChatMessageContent.Document -> attachment = SupportChatAttachment.Document(
                id = content.id,
                name = content.name,
                url = content.url,
            )
        }

        return SupportChatMessage(
            id = message.id.toString(),
            serverId = message.id,
            text = text,
            attachment = attachment,
            time = message.time.toInstant(TimeZone.UTC),
            isMine = myEmail != null && message.sender.login == myEmail,
            authorEmail = message.sender.login,
            status = if (message.wasRead) SupportChatMessageStatus.Read else SupportChatMessageStatus.Sent,
        )
    }
}
