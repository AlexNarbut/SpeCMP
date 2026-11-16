package ru.alarmtrade.specmp.core.push

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopPushTest {

    private class FakeClient : DesktopMessagingClient {
        val shown = mutableListOf<Pair<String, PushPayload>>()

        override fun fetchToken(completion: (token: String?, error: String?) -> Unit) = completion(null, null)

        override fun deleteToken(completion: (error: String?) -> Unit) = completion(null)

        override fun show(id: String, payload: PushPayload, data: Map<String, String>) {
            shown += id to payload
        }

        override fun removeAll() = Unit
    }

    private val client = FakeClient()

    @BeforeTest
    fun setUp() {
        DesktopPush.install(client)
    }

    @AfterTest
    fun tearDown() {
        PushOpenRequests.clear()
    }

    @Test
    fun `alert push is presented by the system except chat and hidden types`() {
        assertTrue(DesktopPush.shouldPresent(mapOf("type_id" to "1")))
        assertTrue(DesktopPush.shouldPresent(mapOf("type_id" to "4")))
        assertFalse(DesktopPush.shouldPresent(mapOf("type_id" to "5")))
        assertFalse(DesktopPush.shouldPresent(mapOf("type_id" to "2")))
    }

    @Test
    fun `data push without alert is shown by the app with message id`() {
        DesktopPush.onRemoteMessage(mapOf("type_id" to "4", "title" to "T", "body" to "B", "gcm.message_id" to "m1"))

        assertEquals(listOf("m1" to PushPayload(PushType.WARNING, "T", "B", null, null)), client.shown)
    }

    @Test
    fun `push with alert, chat push and hidden type are not shown again`() {
        DesktopPush.onRemoteMessage(mapOf("aps" to mapOf("alert" to "A"), "type_id" to "1", "title" to "T"))
        DesktopPush.onRemoteMessage(mapOf("type_id" to "5", "body" to "hi"))
        DesktopPush.onRemoteMessage(mapOf("type_id" to "3", "title" to "video"))

        assertTrue(client.shown.isEmpty())
    }

    @Test
    fun `tap opens push target`() {
        DesktopPush.onNotificationOpened(mapOf("aps" to mapOf("alert" to "A"), "type_id" to "5"))

        assertEquals(PushTarget.Chat, PushOpenRequests.pending.value)
    }
}
