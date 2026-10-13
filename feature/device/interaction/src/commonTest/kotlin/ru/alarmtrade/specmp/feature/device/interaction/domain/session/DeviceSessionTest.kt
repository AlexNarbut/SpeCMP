package ru.alarmtrade.specmp.feature.device.interaction.domain.session

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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
        // Блок не прочитался при «готовой» связи — соединение разорвано, дальше переподключение.
        assertEquals(1, repository.disconnectCount)
    }

    @Test
    fun `hanging device read times out and drops the link for reconnect`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply { readHangs = true }
        val session = DeviceSession(repository, FakeDeviceConnectionRepository(), backgroundScope)
        session.attach(ConnectionTransport.BLE)
        backgroundScope.launch { session.state.collect {} }
        runCurrent()
        assertEquals(DeviceSessionState.Connecting, session.state.value)

        // 3 попытки × 10 с тайм-аута + паузы.
        advanceTimeBy(31_500)
        runCurrent()

        assertEquals(3, repository.readCount)
        assertEquals(1, repository.disconnectCount)
        assertEquals(DeviceSessionState.Disconnected, session.state.value)
        advanceTimeBy(1_001)
        assertTrue(repository.reconnectCount >= 1)
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

    @Test
    fun `transport is attached by the scan and cleared on close`() = runTest(UnconfinedTestDispatcher()) {
        val connection = FakeDeviceConnectionRepository()
        val session = DeviceSession(FakeDeviceSessionRepository(), connection, backgroundScope)
        assertNull(session.transport.value)

        session.attach(ConnectionTransport.USB)
        assertEquals(ConnectionTransport.USB, session.transport.value)

        session.close()
        assertNull(session.transport.value)
        assertEquals(1, connection.closeCount)
    }

    @Test
    fun `boot mode command is repeated after a pause`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository()
        val session = DeviceSession(repository, FakeDeviceConnectionRepository(), backgroundScope)

        val response = session.setBootMode()

        assertTrue(response is Response.Success)
        assertEquals(2, repository.bootModeCount)
    }

    @Test
    fun `ble link loss is reconnected until the link returns`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = DeviceSession(repository, FakeDeviceConnectionRepository(), backgroundScope)
        session.attach(ConnectionTransport.BLE)
        backgroundScope.launch { session.state.collect {} }
        runCurrent()

        repository.link.value = DeviceLinkState.DISCONNECTED
        advanceTimeBy(3_001)
        assertEquals(3, repository.reconnectCount)

        repository.link.value = DeviceLinkState.READY
        runCurrent()
        advanceTimeBy(3_001)
        assertEquals(3, repository.reconnectCount)
        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
    }

    @Test
    fun `usb link loss is not reconnected by the app`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.DISCONNECTED)
        val session = DeviceSession(repository, FakeDeviceConnectionRepository(), backgroundScope)
        session.attach(ConnectionTransport.USB)
        backgroundScope.launch { session.state.collect {} }

        advanceTimeBy(9_001)
        assertEquals(0, repository.reconnectCount)
    }

    @Test
    fun `closed session is not reconnected`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.DISCONNECTED)
        val session = DeviceSession(repository, FakeDeviceConnectionRepository(), backgroundScope)
        session.attach(ConnectionTransport.BLE)
        backgroundScope.launch { session.state.collect {} }
        advanceTimeBy(1_001)
        assertEquals(1, repository.reconnectCount)

        session.close()
        advanceTimeBy(9_001)
        assertEquals(1, repository.reconnectCount)
    }
}
