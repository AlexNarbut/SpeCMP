package ru.alarmtrade.specmp.feature.device.interaction.domain.session

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeviceSessionTest {

    @Test
    fun `ready link reads device`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = DeviceSession(repository, FakeDeviceConnectionRepository(), backgroundScope)
        backgroundScope.launch { session.state.collect {} }
        advanceUntilIdle()

        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
    }

    @Test
    fun `read failures are retried, then disconnected`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
            readResults = listOf(Response.Error(IllegalStateException("no answer")))
        }
        val session = DeviceSession(repository, FakeDeviceConnectionRepository(), backgroundScope)
        backgroundScope.launch { session.state.collect {} }
        // Повторы с delay идут в backgroundScope: advanceUntilIdle время для них не двигает.
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(DeviceSessionState.Disconnected, session.state.value)
        assertEquals(3, repository.readCount)
    }

    @Test
    fun `retry succeeds on second attempt`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
            readResults = listOf(Response.Error(IllegalStateException("busy")), Response.Success(testDevice))
        }
        val session = DeviceSession(repository, FakeDeviceConnectionRepository(), backgroundScope)
        backgroundScope.launch { session.state.collect {} }
        // Повторы с delay идут в backgroundScope: advanceUntilIdle время для них не двигает.
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
    }

    @Test
    fun `link loss and restore`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = DeviceSession(repository, FakeDeviceConnectionRepository(), backgroundScope)
        backgroundScope.launch { session.state.collect {} }
        advanceUntilIdle()

        repository.link.value = DeviceLinkState.DISCONNECTED
        assertEquals(DeviceSessionState.Disconnected, session.state.value)

        repository.link.value = DeviceLinkState.READY
        advanceUntilIdle()
        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
    }

    @Test
    fun `close closes sessions`() = runTest(UnconfinedTestDispatcher()) {
        val connection = FakeDeviceConnectionRepository()
        val session = DeviceSession(FakeDeviceSessionRepository(), connection, backgroundScope)

        session.close()

        assertEquals(1, connection.closeCount)
    }

    @Test
    fun `device scope cancels work on disconnect and reruns onReady on reconnect`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = DeviceSession(repository, FakeDeviceConnectionRepository(), backgroundScope)
        val scope = DeviceScope(backgroundScope, session)
        val readyCalls = mutableListOf<Long>()
        scope.onReady { readyCalls += it.systemId }
        advanceUntilIdle()

        val never = CompletableDeferred<Unit>()
        val job = scope.launch { never.await() }
        assertTrue(job != null && job.isActive)

        repository.link.value = DeviceLinkState.DISCONNECTED
        advanceUntilIdle()
        assertTrue(job.isCancelled)
        assertEquals(null, scope.launch { })

        repository.link.value = DeviceLinkState.READY
        advanceUntilIdle()
        assertEquals(listOf(testDevice.systemId, testDevice.systemId), readyCalls)
    }
}
