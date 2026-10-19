package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus

class SupportChatListItemsTest {

    // Порядок держит SupportChatTimeline: buildListItems только разворачивает
    // сообщения в отображаемый порядок (newest-first, reverseLayout) и расставляет
    // разделители дат над группами.
    @Test
    fun `messages render newest first with a divider closing each date group`() {
        val firstDate = LocalDate(2024, 5, 1)
        val secondDate = LocalDate(2024, 5, 2)
        val items = buildListItems(
            messages = listOf(
                message(id = "first", time = LocalDateTime(2024, 5, 1, 10, 0)),
                message(id = "second-early", time = LocalDateTime(2024, 5, 2, 9, 0)),
                message(id = "second-late", time = LocalDateTime(2024, 5, 2, 12, 0))
            ),
            dateLabeler = { date -> "label:$date" },
            timeZone = TimeZone.UTC
        )

        val types = items.map {
            when (it) {
                is SupportChatListItem.Message -> "msg:${it.message.id}"
                is SupportChatListItem.DateDivider -> "divider:${it.date}"
            }
        }

        assertEquals(
            listOf(
                "msg:second-late",
                "msg:second-early",
                "divider:$secondDate",
                "msg:first",
                "divider:$firstDate"
            ),
            types
        )
    }

    @Test
    fun `time and date group use device time zone`() {
        // 22:30 UTC → 01:30 следующего дня в UTC+3 (фиксированный офсет, без DST).
        val items = buildListItems(
            messages = listOf(message(id = "late", time = LocalDateTime(2026, 1, 15, 22, 30))),
            dateLabeler = { date -> "label:$date" },
            timeZone = TimeZone.of("UTC+03:00")
        )

        val message = items.filterIsInstance<SupportChatListItem.Message>().single().message
        val divider = items.filterIsInstance<SupportChatListItem.DateDivider>().single()
        assertEquals("01:30", message.timeLabel)
        assertEquals(LocalDate(2026, 1, 16), divider.date)
    }

    private fun message(id: String, time: LocalDateTime): SupportChatMessage = SupportChatMessage(
        id = id,
        serverId = null,
        text = id,
        attachment = null,
        time = time.toInstant(TimeZone.UTC),
        isMine = false,
        authorEmail = null,
        status = SupportChatMessageStatus.Sent
    )
}
