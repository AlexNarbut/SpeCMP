package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import ru.alarmtrade.specmp.feature.supportchat.data.mapper.SupportChatMessageMapper
import ru.alarmtrade.specialist.domain.chat.ChatEvent
import ru.alarmtrade.specialist.domain.chat.ChatMessageContent
import ru.alarmtrade.specialist.domain.chat.ChatMessageSender
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SupportChatMessageMapperTest {

    private val utcPlus3 = TimeZone.of("UTC+03:00")
    private val mapper = SupportChatMessageMapper(systemTimeZone = utcPlus3)

    @Test
    fun `sdk time in utc is converted to system time zone`() {
        // 10:00 UTC → 13:00 в UTC+3 (фиксированный офсет, без DST).
        val mapped = mapper.map(sdkMessage(time = LocalDateTime(2026, 1, 15, 10, 0)), myEmail = null)

        assertEquals(LocalDateTime(2026, 1, 15, 13, 0), mapped.time)
    }

    @Test
    fun `isMine matches sender login against session email`() {
        val mine = mapper.map(sdkMessage(text = "My"), myEmail = "me@example.com")
        val theirs = mapper.map(
            sdkMessage(text = "Their", senderLogin = "agent@example.com"),
            myEmail = "me@example.com",
        )

        assertTrue(mine.isMine)
        assertFalse(theirs.isMine)
    }

    @Test
    fun `isMine is false when session email is unknown`() {
        val mapped = mapper.map(sdkMessage(text = "Hello"), myEmail = null)

        assertFalse(mapped.isMine)
    }

    @Test
    fun `exclusive content maps to text or attachment only`() {
        val text = mapper.map(
            sdkMessage(content = ChatMessageContent.Text("caption")),
            myEmail = null,
        )
        assertEquals("caption", text.text)
        assertNull(text.attachment)

        val image = mapper.map(
            sdkMessage(
                content = ChatMessageContent.Image(id = "img-1", name = "a.png", url = "https://files/a.png"),
            ),
            myEmail = null,
        )
        assertNull(image.text)
        assertEquals("img-1", image.attachment?.id)
    }

    private fun sdkMessage(
        time: LocalDateTime = LocalDateTime(2026, 1, 15, 10, 0),
        senderLogin: String = "me@example.com",
        text: String = "Hello",
        content: ChatMessageContent = ChatMessageContent.Text(text),
    ): ChatEvent.Chat.Message.Default {
        return ChatEvent.Chat.Message.Default(
            id = 1,
            time = time,
            wasRead = false,
            sender = ChatMessageSender(userId = 1, login = senderLogin),
            messageContent = content,
        )
    }
}
