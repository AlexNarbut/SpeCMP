package ru.alarmtrade.specmp.core.network.monitor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class JvmNetworkMonitorTest {
    private var network = true
    private var polls = 0

    private fun TestScope.monitor() = JvmNetworkMonitor(backgroundScope, PERIOD) {
        polls++
        network
    }

    private fun TestScope.subscribe(monitor: JvmNetworkMonitor) {
        backgroundScope.launch { monitor.isAvailable.collect {} }
        runCurrent()
    }

    private fun TestScope.nextPoll() {
        advanceTimeBy(PERIOD)
        runCurrent()
    }

    @Test
    fun `initial value is read on creation without subscribers`() = runTest {
        assertTrue(monitor().isAvailable.value)

        network = false
        assertFalse(monitor().isAvailable.value)
    }

    @Test
    fun `does not poll without subscribers`() = runTest {
        val monitor = monitor()
        network = false

        advanceTimeBy(PERIOD * 10)
        runCurrent()

        assertEquals(1, polls)
        assertTrue(monitor.isAvailable.value)
    }

    @Test
    fun `lost network is reported only after second poll`() = runTest {
        val monitor = monitor()
        subscribe(monitor)

        network = false
        nextPoll()
        assertTrue(monitor.isAvailable.value)

        nextPoll()
        assertFalse(monitor.isAvailable.value)
    }

    @Test
    fun `single failed poll does not change value`() = runTest {
        val monitor = monitor()
        subscribe(monitor)

        network = false
        nextPoll()
        network = true
        nextPoll()
        network = false
        nextPoll()

        assertTrue(monitor.isAvailable.value)
    }

    @Test
    fun `restored network is reported on first poll`() = runTest {
        val monitor = monitor()
        subscribe(monitor)
        network = false
        nextPoll()
        nextPoll()
        assertFalse(monitor.isAvailable.value)

        network = true
        nextPoll()

        assertTrue(monitor.isAvailable.value)
    }

    @Test
    fun `stale value is refreshed on subscription`() = runTest {
        network = false
        val monitor = monitor()
        network = true

        subscribe(monitor)

        assertTrue(monitor.isAvailable.value)
    }

    private companion object {
        val PERIOD = 3.seconds
    }
}
