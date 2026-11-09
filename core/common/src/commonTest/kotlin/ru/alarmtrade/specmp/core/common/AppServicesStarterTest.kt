package ru.alarmtrade.specmp.core.common

import kotlin.test.Test
import kotlin.test.assertEquals

class AppServicesStarterTest {

    @Test
    fun `services start once and a failing one does not stop the rest`() {
        val started = mutableListOf<String>()
        val starter = AppServicesStarter(
            listOf(
                "first" to { started += "first" },
                "failing" to { error("boom") },
                "last" to { started += "last" },
            ),
        )

        starter.start()
        starter.start()

        assertEquals(listOf("first", "last"), started)
    }
}
