package ru.alarmtrade.specmp.core.push

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApsUserInfoTest {

    @Test
    fun `server data is top level keys without aps`() {
        val userInfo = mapOf(
            "aps" to mapOf("alert" to mapOf("title" to "T")),
            "type_id" to "5",
            "gcm.message_id" to "m1",
            "empty" to null,
        )

        assertEquals(mapOf("type_id" to "5", "gcm.message_id" to "m1"), ApsUserInfo.data(userInfo))
    }

    @Test
    fun `alert is detected only inside aps`() {
        assertTrue(ApsUserInfo.hasAlert(mapOf("aps" to mapOf("alert" to "text"))))
        assertFalse(ApsUserInfo.hasAlert(mapOf("aps" to mapOf("content-available" to "1"), "alert" to "x")))
        assertFalse(ApsUserInfo.hasAlert(mapOf("type_id" to "1")))
    }
}
