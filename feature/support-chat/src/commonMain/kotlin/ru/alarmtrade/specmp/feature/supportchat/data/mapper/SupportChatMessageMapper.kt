package ru.alarmtrade.specmp.feature.supportchat.data.mapper

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
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
 * поэтому маппинг трактует входящее время как UTC и отдаёт `Instant` — в местное
 * время переводит UI. Если формат сервера изменится, сдвиг проявится именно здесь —
 * контракт зафиксирован тестом.
 *
 * Метаданных документа (mime-тип, размер) SDK 1.1.76 не отдаёт — только id/name/url.
 */
internal class SupportChatMessageMapper {

    /**
     * `null` — сообщение, которое фича не поддерживает (не `Default`, например
     * `MountEvent`). Вызывающий логирует и пропускает его, а не превращает в
     * пустое сообщение. `when` по контенту исчерпывающий: новый вариант SDK
     * сломает компиляцию здесь.
     */
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
