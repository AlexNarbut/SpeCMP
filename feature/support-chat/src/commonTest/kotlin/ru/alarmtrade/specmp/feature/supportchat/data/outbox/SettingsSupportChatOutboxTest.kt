package ru.alarmtrade.specmp.feature.supportchat.data.outbox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus

class SettingsSupportChatOutboxTest {

    private val root = InMemorySettings()
    private val outbox = SettingsSupportChatOutbox(AppSettingsFactory(root).create("supportchat"))

    @Test
    fun `entries round trip with attachment and status`() = runTest {
        val message = pending("a").copy(
            attachment = SupportChatAttachment.Document(
                id = "doc",
                name = "scheme.pdf",
                localPath = "/files/scheme.pdf",
                mimeType = "application/pdf",
                sizeBytes = 42,
            ),
        )

        outbox.enqueue(message)

        assertEquals(listOf(message), outbox.load())
    }

    @Test
    fun `enqueue updates an existing entry and remove drops it`() = runTest {
        outbox.enqueue(pending("a"))
        outbox.enqueue(pending("a").copy(status = SupportChatMessageStatus.Failed))

        assertEquals(listOf(SupportChatMessageStatus.Failed), outbox.load().map { it.status })

        outbox.remove("a")
        assertTrue(outbox.load().isEmpty())
    }

    @Test
    fun `markSent updates only an existing entry`() = runTest {
        outbox.enqueue(pending("a"))
        outbox.markSent("a")
        // Эхо пришло раньше ответа SDK: запись уже удалена — markSent её не воскрешает.
        outbox.markSent("gone")

        assertEquals(listOf("a"), outbox.load().map { it.id })
        // Sent (эха не было) восстанавливается как Pending.
        assertEquals(listOf(SupportChatMessageStatus.Pending), outbox.load().map { it.status })
    }

    @Test
    fun `only unconfirmed pending or failed messages are stored`() = runTest {
        outbox.enqueue(pending("sent").copy(status = SupportChatMessageStatus.Sent))
        outbox.enqueue(pending("server").copy(serverId = 7))
        outbox.enqueue(pending("no-file").copy(attachment = SupportChatAttachment.Image(id = "i", name = "a.png")))

        assertTrue(outbox.load().isEmpty())
    }

    @Test
    fun `concurrent transitions are not lost`() = runTest {
        // P-5: read-modify-write из разных корутин под мьютексом реализации.
        withContext(Dispatchers.Default) {
            (1..50).map { index -> async { outbox.enqueue(pending("m$index")) } }.awaitAll()
            (1..25).map { index -> async { outbox.remove("m$index") } }.awaitAll()
        }

        assertEquals((26..50).map { "m$it" }.toSet(), outbox.load().map { it.id }.toSet())
    }

    @Test
    fun `release clears only the chat namespace`() = runTest {
        root.putString("app_locale", "ru")
        outbox.enqueue(pending("a"))

        outbox.release()

        assertTrue(outbox.load().isEmpty())
        assertEquals("ru", root.getStringOrNull("app_locale"))
    }

    private fun pending(id: String): SupportChatMessage = SupportChatMessage(
        id = id,
        serverId = null,
        text = "text $id",
        attachment = null,
        time = Instant.fromEpochMilliseconds(1_000),
        isMine = true,
        authorEmail = null,
        status = SupportChatMessageStatus.Pending,
    )
}
