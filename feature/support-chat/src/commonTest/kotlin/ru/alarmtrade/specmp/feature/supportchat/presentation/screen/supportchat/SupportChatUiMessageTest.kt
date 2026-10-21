package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.toUiMessage

class SupportChatUiMessageTest {

    private val localTime = LocalDateTime(2026, 1, 15, 10, 0)

    @Test
    fun `failed attachment without local file can be deleted but not retried`() {
        val message = failed(attachment = SupportChatAttachment.Image(id = "i", name = "a.png", url = null))

        val ui = message.toUiMessage(localTime)

        assertTrue(ui.canDelete)
        assertFalse(ui.canRetry)
    }

    @Test
    fun `failed text can be retried and deleted`() {
        val ui = failed(text = "Hello").toUiMessage(localTime)

        assertTrue(ui.canRetry)
        assertTrue(ui.canDelete)
    }

    @Test
    fun `sent message has no failure actions`() {
        val ui = failed(text = "Hello").copy(status = SupportChatMessageStatus.Sent).toUiMessage(localTime)

        assertFalse(ui.canRetry)
        assertFalse(ui.canDelete)
    }

    private fun failed(text: String? = null, attachment: SupportChatAttachment? = null): SupportChatMessage =
        SupportChatMessage(
            id = "local-1",
            serverId = null,
            text = text,
            attachment = attachment,
            time = Instant.fromEpochSeconds(0),
            isMine = true,
            authorEmail = null,
            status = SupportChatMessageStatus.Failed
        )
}
