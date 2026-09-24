package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class SupportChatListItemsTest {

    @Test
    fun `unordered messages produce chronological groups with unique date dividers`() {
        val firstDate = LocalDate(2024, 5, 1)
        val secondDate = LocalDate(2024, 5, 2)
        val items = buildListItems(
            messages = listOf(
                message(id = "second-late", time = LocalDateTime(2024, 5, 2, 12, 0)),
                message(id = "first", time = LocalDateTime(2024, 5, 1, 10, 0)),
                message(id = "second-early", time = LocalDateTime(2024, 5, 2, 9, 0)),
            ),
            todayLabel = "Today",
            yesterdayLabel = "Yesterday",
            monthLabels = List(12) { "Month" },
            typingAuthor = null,
        )

        val dividers = items.filterIsInstance<SupportChatListItem.DateDivider>()
        val messageIds = items.filterIsInstance<SupportChatListItem.Message>().map { it.message.id }

        assertEquals(listOf(firstDate, secondDate), dividers.map { it.date })
        assertEquals(listOf("first", "second-early", "second-late"), messageIds)
    }

    private fun message(
        id: String,
        time: LocalDateTime,
    ): SupportChatMessage {
        return SupportChatMessage(
            id = id,
            serverId = null,
            text = id,
            attachment = null,
            time = time,
            isMine = false,
            authorEmail = null,
            status = SupportChatMessageStatus.Sent,
        )
    }
}
