package ru.alarmtrade.specmp.feature.device.firmware.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.ServicePinCooldown

class ServicePinCooldownTest {

    private val time = TestTimeSource()
    private val cooldown = ServicePinCooldown(time)

    @Test
    fun `no lockout until a wrong pin`() {
        assertEquals(Duration.ZERO, cooldown.remainingNow(SYSTEM))
    }

    @Test
    fun `wrong pin locks input for fifteen seconds and the lock is per block`() {
        cooldown.start(SYSTEM)
        assertEquals(15.seconds, cooldown.remainingNow(SYSTEM))
        assertEquals(Duration.ZERO, cooldown.remainingNow(OTHER_SYSTEM))

        time += 6.seconds
        assertEquals(9.seconds, cooldown.remainingNow(SYSTEM))

        time += 9.seconds
        assertEquals(Duration.ZERO, cooldown.remainingNow(SYSTEM))
    }

    @Test
    fun `new wrong pin restarts the count`() {
        cooldown.start(SYSTEM)
        time += 10.seconds
        cooldown.start(SYSTEM)

        assertEquals(15.seconds, cooldown.remainingNow(SYSTEM))
    }

    @Test
    fun `release forgets every lock`() = runTest {
        cooldown.start(SYSTEM)
        cooldown.release()

        assertEquals(Duration.ZERO, cooldown.remainingNow(SYSTEM))
    }

    @Test
    fun `remaining flow ticks down to zero and stays quiet`() = runTest {
        val seen = mutableListOf<Duration>()
        cooldown.start(SYSTEM)
        backgroundScope.launch { cooldown.remaining(SYSTEM).collect { seen += it } }
        runCurrent()
        assertEquals(15.seconds, seen.last())

        time += 5.seconds
        advanceTimeBy(300)
        runCurrent()
        assertEquals(10.seconds, seen.last())

        time += 10.seconds
        advanceTimeBy(300)
        runCurrent()
        assertEquals(Duration.ZERO, seen.last())
        val count = seen.size
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(count, seen.size)
    }

    @Test
    fun `restart while counting down is picked up by the same flow`() = runTest {
        val seen = mutableListOf<Duration>()
        cooldown.start(SYSTEM)
        backgroundScope.launch { cooldown.remaining(SYSTEM).collect { seen += it } }
        runCurrent()

        time += 10.seconds
        advanceTimeBy(300)
        runCurrent()
        assertEquals(5.seconds, seen.last())

        cooldown.start(SYSTEM)
        runCurrent()
        assertEquals(15.seconds, seen.last())
    }

    private companion object {
        const val SYSTEM = 1L
        const val OTHER_SYSTEM = 2L
    }
}
