package ru.alarmtrade.specmp.core.push

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response

class MessagingClientPushTokenSourceTest {

    private class FakeClient(private val token: String?, private val error: String?) : PushMessagingClient {
        var deleted = 0

        override fun fetchToken(completion: (token: String?, error: String?) -> Unit) = completion(token, error)

        override fun deleteToken(completion: (error: String?) -> Unit) {
            deleted++
            completion(error)
        }
    }

    @AfterTest
    fun tearDown() {
        PushOpenRequests.clear()
    }

    @Test
    fun `no client means no push`() = runTest {
        val source = MessagingClientPushTokenSource(client = { null }, removeDelivered = {})

        assertEquals(Response.Success(null), source.token())
    }

    @Test
    fun `token comes from the client`() = runTest {
        val source = MessagingClientPushTokenSource(client = { FakeClient("fcm", null) }, removeDelivered = {})

        assertEquals(Response.Success("fcm"), source.token())
    }

    @Test
    fun `client error is a failed response`() = runTest {
        val source = MessagingClientPushTokenSource(client = { FakeClient(null, "APNs") }, removeDelivered = {})

        assertIs<Response.Error>(source.token())
    }

    @Test
    fun `release deletes token, delivered notifications and pending tap even if delete fails`() = runTest {
        val client = FakeClient(null, "offline")
        var removed = 0
        val source = MessagingClientPushTokenSource(client = { client }, removeDelivered = { removed++ })
        PushOpenRequests.open(PushTarget.Chat)

        source.release()

        assertEquals(1, client.deleted)
        assertEquals(1, removed)
        assertNull(PushOpenRequests.pending.value)
    }
}
