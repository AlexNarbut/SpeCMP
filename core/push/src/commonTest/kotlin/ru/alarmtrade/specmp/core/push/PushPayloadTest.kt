package ru.alarmtrade.specmp.core.push

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PushPayloadTest {

    @Test
    fun `data fields override the notification block`() {
        val payload = PushPayload.parse(
            mapOf("type_id" to "5", "title" to "Поддержка", "body" to "Ответ", "image_url" to "https://img"),
            notificationTitle = "old",
            notificationBody = "old body",
        )

        assertEquals(PushPayload(PushType.CHAT, "Поддержка", "Ответ", null, "https://img"), payload)
    }

    @Test
    fun `notification block is used when data has no text`() {
        val payload = PushPayload.parse(mapOf("type_id" to "4"), notificationTitle = "Внимание", notificationBody = " ")

        assertEquals(PushPayload(PushType.WARNING, "Внимание", null, null, null), payload)
    }

    @Test
    fun `missing or unknown type is a plain notification`() {
        assertEquals(PushType.NOTIFICATION, PushPayload.parse(mapOf("title" to "a"))?.type)
        assertEquals(PushType.NOTIFICATION, PushPayload.parse(mapOf("type_id" to "0", "title" to "a"))?.type)
        assertEquals(PushType.NOTIFICATION, PushPayload.parse(mapOf("type_id" to "x", "title" to "a"))?.type)
        assertEquals(PushType.NOTIFICATION, PushPayload.parse(mapOf("type_id" to "42", "title" to "a"))?.type)
    }

    @Test
    fun `news video and article pushes are dropped`() {
        listOf("2", "3", "6", " 6 ").forEach { code ->
            assertNull(PushPayload.parse(mapOf("type_id" to code, "title" to "a", "body" to "b")), code)
        }
    }

    @Test
    fun `push without any text is dropped`() {
        assertNull(PushPayload.parse(mapOf("type_id" to "1", "action_url" to "https://x")))
    }

    @Test
    fun `chat opens the chat whatever the link`() {
        assertEquals(PushTarget.Chat, payload(PushType.CHAT, "https://site").target)
    }

    @Test
    fun `notification with a web link opens the link`() {
        assertEquals(PushTarget.Url("https://site/a"), payload(PushType.NOTIFICATION, "https://site/a").target)
        assertEquals(PushTarget.Url("HTTP://site"), payload(PushType.NOTIFICATION, "HTTP://site").target)
    }

    @Test
    fun `other links and warnings just open the app`() {
        assertEquals(PushTarget.App, payload(PushType.NOTIFICATION, null).target)
        assertEquals(PushTarget.App, payload(PushType.NOTIFICATION, "vnd.youtube:abc").target)
        assertEquals(PushTarget.App, payload(PushType.NOTIFICATION, "javascript:alert(1)").target)
        assertEquals(PushTarget.App, payload(PushType.WARNING, "https://site").target)
    }

    @Test
    fun `tap target needs push keys`() {
        assertNull(PushPayload.targetOf(emptyMap()))
        assertNull(PushPayload.targetOf(mapOf("profile" to "0")))
        assertEquals(PushTarget.Chat, PushPayload.targetOf(mapOf("type_id" to "5")))
        assertEquals(PushTarget.Url("https://a"), PushPayload.targetOf(mapOf("action_url" to "https://a")))
        assertEquals(PushTarget.App, PushPayload.targetOf(mapOf("type_id" to "2", "action_url" to "https://a")))
    }

    private fun payload(type: PushType, url: String?) = PushPayload(type, "t", "b", url, null)
}
