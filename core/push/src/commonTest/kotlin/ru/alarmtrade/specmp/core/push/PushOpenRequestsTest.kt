package ru.alarmtrade.specmp.core.push

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PushOpenRequestsTest {

    @AfterTest
    fun tearDown() {
        PushOpenRequests.clear()
    }

    @Test
    fun `launch without push data leaves nothing pending`() {
        PushOpenRequests.offer(mapOf("android.intent.extra.X" to "1"))

        assertNull(PushOpenRequests.pending.value)
    }

    @Test
    fun `tap waits until consumed once`() {
        PushOpenRequests.offer(mapOf("type_id" to "5"))

        assertEquals(PushTarget.Chat, PushOpenRequests.pending.value)
        assertTrue(PushOpenRequests.consume(PushTarget.Chat))
        assertFalse(PushOpenRequests.consume(PushTarget.Chat))
        assertNull(PushOpenRequests.pending.value)
    }

    @Test
    fun `later tap replaces the earlier one`() {
        PushOpenRequests.offer(mapOf("type_id" to "5"))
        PushOpenRequests.offer(mapOf("type_id" to "1", "action_url" to "https://a"))

        assertFalse(PushOpenRequests.consume(PushTarget.Chat))
        assertEquals(PushTarget.Url("https://a"), PushOpenRequests.pending.value)
    }

    @Test
    fun `logout drops the pending tap`() {
        PushOpenRequests.offer(mapOf("type_id" to "5"))

        PushOpenRequests.clear()

        assertNull(PushOpenRequests.pending.value)
    }

    @Test
    fun `open without push data replaces a waiting tap`() {
        PushOpenRequests.offer(mapOf("type_id" to "1", "action_url" to "https://example.com"))
        PushOpenRequests.open(PushTarget.Chat)

        assertEquals(PushTarget.Chat, PushOpenRequests.pending.value)
    }
}
