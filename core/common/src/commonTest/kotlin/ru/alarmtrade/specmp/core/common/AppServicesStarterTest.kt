package ru.alarmtrade.specmp.core.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

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

    @Test
    fun `parallel start calls start every service once`() = runTest {
        val starts = MutableStateFlow(0)
        val starter = AppServicesStarter(listOf("counter" to { starts.update { it + 1 } }))

        withContext(Dispatchers.Default) {
            repeat(PARALLEL_CALLS) { launch { starter.start() } }
        }

        assertEquals(1, starts.value)
    }

    private companion object {
        const val PARALLEL_CALLS = 32
    }
}
