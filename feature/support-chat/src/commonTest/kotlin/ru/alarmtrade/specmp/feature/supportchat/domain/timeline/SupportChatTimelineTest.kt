package ru.alarmtrade.specmp.feature.supportchat.domain.timeline

import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class SupportChatTimelineTest {

    @Test
    fun `messages are kept in ascending time order`() {
        val timeline = SupportChatTimeline.of(
            listOf(incoming(id = 3, second = 30), incoming(id = 1, second = 10), incoming(id = 2, second = 20)),
        )

        assertEquals(listOf(1L, 2L, 3L), timeline.messages.map { it.serverId })
    }

    @Test
    fun `history page does not duplicate known server messages`() {
        val timeline = SupportChatTimeline.of(listOf(incoming(id = 1, second = 10)))

        val merged = timeline.withHistoryPage(listOf(incoming(id = 1, second = 10), incoming(id = 0, second = 5)))

        assertEquals(listOf(0L, 1L), merged.messages.map { it.serverId })
    }

    @Test
    fun `repeated incoming delivery replaces instead of duplicating`() {
        val timeline = SupportChatTimeline.of(listOf(incoming(id = 1, second = 10, text = "old")))

        val updated = timeline.withIncoming(incoming(id = 1, second = 10, text = "edited"))

        assertEquals(listOf("edited"), updated.messages.map { it.text })
    }

    @Test
    fun `echo replaces the earliest matching optimistic message`() {
        val timeline = SupportChatTimeline.Empty.withOptimistic(
            listOf(optimistic(id = "a", second = 10, text = "Same"), optimistic(id = "b", second = 11, text = "Same")),
        )

        val updated = timeline.withIncoming(incoming(id = 100, second = 12, text = "Same", isMine = true))

        assertEquals(listOf("b", "100"), updated.messages.map { it.id })
    }

    @Test
    fun `transition happens only once from the expected status`() {
        val timeline = SupportChatTimeline.Empty.withOptimistic(
            listOf(optimistic(id = "a", second = 10, text = "x", status = SupportChatMessageStatus.Failed)),
        )
        val retryable = setOf(SupportChatMessageStatus.Failed)

        val started = assertNotNull(timeline.tryTransition("a", retryable, SupportChatMessageStatus.Sending))
        assertNull(started.tryTransition("a", retryable, SupportChatMessageStatus.Sending))
        assertNull(timeline.tryTransition("missing", retryable, SupportChatMessageStatus.Sending))
    }

    @Test
    fun `read event marks server messages`() {
        val timeline = SupportChatTimeline.of(listOf(incoming(id = 1, second = 1), incoming(id = 2, second = 2)))

        val read = timeline.withRead(listOf(2L))

        assertEquals(
            listOf(SupportChatMessageStatus.Sent, SupportChatMessageStatus.Read),
            read.messages.map { it.status },
        )
    }

    @Test
    fun `removing an unknown id keeps the same instance`() {
        val timeline = SupportChatTimeline.of(listOf(incoming(id = 1, second = 1)))

        assertSame(timeline, timeline.without("missing"))
        assertEquals(emptyList(), timeline.without("1").messages)
    }

    @Test
    fun `history page confirms a fresh optimistic message but not with an old namesake`() {
        val timeline = SupportChatTimeline.Empty.withOptimistic(
            listOf(optimistic(id = "local", second = 10_000, text = "ok", status = SupportChatMessageStatus.Pending)),
        )

        // Моё старое «ok» неделей раньше — не подтверждение свежего.
        val withOld = timeline.withHistoryPage(listOf(incoming(id = 1, second = 10, text = "ok", isMine = true)))
        assertEquals(setOf("local", "1"), withOld.messages.map { it.id }.toSet())

        // Серверная копия того же сообщения (с расхождением часов) — подтверждение.
        val withFresh = timeline.withHistoryPage(listOf(incoming(id = 2, second = 9_990, text = "ok", isMine = true)))
        assertEquals(listOf("2"), withFresh.messages.map { it.id })
    }

    @Test
    fun `history does not confirm a queued text with another own text from the skew window`() {
        // Эмулятор: «QA offline» (Pending после рестарта) съедало моё «QA doc caption» за 1,5 мин до него.
        val timeline = SupportChatTimeline.Empty.withOptimistic(
            listOf(optimistic(id = "local", second = 1_000, text = "QA offline", status = SupportChatMessageStatus.Pending)),
        )

        val merged = timeline.withHistoryPage(listOf(incoming(id = 1, second = 910, text = "QA doc caption", isMine = true)))

        assertEquals(listOf("1", "local"), merged.messages.map { it.id })
        assertEquals(SupportChatMessageStatus.Pending, merged.find("local")?.status)
    }

    @Test
    fun `live echo of another text does not confirm a waiting text`() {
        val timeline = SupportChatTimeline.Empty.withOptimistic(
            listOf(optimistic(id = "local", second = 10, text = "Waiting", status = SupportChatMessageStatus.Pending)),
        )

        val updated = timeline.withIncoming(incoming(id = 7, second = 11, text = "From another device", isMine = true))

        assertEquals(listOf("local", "7"), updated.messages.map { it.id })
    }

    @Test
    fun `renamed attachment is still confirmed by shape`() {
        val timeline = SupportChatTimeline.Empty.withOptimistic(
            listOf(optimistic(id = "local", second = 10, text = null, attachment = image("local.png"))),
        )

        val updated = timeline.withIncoming(
            incoming(id = 8, second = 11, text = null, isMine = true).copy(attachment = image("server-renamed.png")),
        )

        assertEquals(listOf("8"), updated.messages.map { it.id })
    }

    private fun image(name: String) = SupportChatAttachment.Image(id = name, name = name, url = null)

    private fun incoming(
        id: Long,
        second: Long,
        text: String? = "m$id",
        isMine: Boolean = false,
    ): SupportChatMessage {
        return SupportChatMessage(
            id = id.toString(),
            serverId = id,
            text = text,
            attachment = null,
            time = Instant.fromEpochSeconds(second),
            isMine = isMine,
            authorEmail = null,
            status = SupportChatMessageStatus.Sent,
        )
    }

    private fun optimistic(
        id: String,
        second: Long,
        text: String?,
        status: SupportChatMessageStatus = SupportChatMessageStatus.Sending,
        attachment: SupportChatAttachment? = null,
    ): SupportChatMessage {
        return SupportChatMessage(
            id = id,
            serverId = null,
            text = text,
            attachment = attachment,
            time = Instant.fromEpochSeconds(second),
            isMine = true,
            authorEmail = null,
            status = status,
        )
    }
}
