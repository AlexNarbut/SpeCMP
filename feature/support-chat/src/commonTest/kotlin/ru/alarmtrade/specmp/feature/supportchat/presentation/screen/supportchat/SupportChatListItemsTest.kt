package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class SupportChatListItemsTest {

    // Инвариант state (P-16): messages уже отсортированы по времени в VM.
    // buildListItems только разворачивает их в отображаемый порядок (newest-first,
    // reverseLayout) и расставляет разделители дат над группами.
    @Test
    fun `messages render newest first with a divider closing each date group`() {
        val firstDate = LocalDate(2024, 5, 1)
        val secondDate = LocalDate(2024, 5, 2)
        val items = buildListItems(
            messages = listOf(
                message(id = "first", time = LocalDateTime(2024, 5, 1, 10, 0)),
                message(id = "second-early", time = LocalDateTime(2024, 5, 2, 9, 0)),
                message(id = "second-late", time = LocalDateTime(2024, 5, 2, 12, 0)),
            ),
            todayLabel = "Today",
            yesterdayLabel = "Yesterday",
            monthLabels = List(12) { "Month" },
        )

        val types = items.map {
            when (it) {
                is SupportChatListItem.Message -> "msg:${it.message.id}"
                is SupportChatListItem.DateDivider -> "divider:${it.date}"
                is SupportChatListItem.Typing -> "typing:${it.author}"
            }
        }

        assertEquals(
            listOf(
                "msg:second-late",
                "msg:second-early",
                "divider:$secondDate",
                "msg:first",
                "divider:$firstDate",
            ),
            types,
        )
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
