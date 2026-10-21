package ru.alarmtrade.specmp.feature.device.interaction.domain.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModuleMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState

class DeviceSessionTest {

    @Test
    fun `ready link reads device`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = testSession(repository, FakeDeviceConnectionRepository())
        backgroundScope.launch { session.state.collect {} }
        advanceUntilIdle()

        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
    }

    @Test
    fun `read failures are retried then disconnected`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
            readResults = listOf(Response.Error(IllegalStateException("no answer")))
        }
        val session = testSession(repository, FakeDeviceConnectionRepository())
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
        val session = testSession(repository, FakeDeviceConnectionRepository())
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
        val session = testSession(repository, FakeDeviceConnectionRepository())
        backgroundScope.launch { session.state.collect {} }
        // Повторы с delay идут в backgroundScope: advanceUntilIdle время для них не двигает.
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
    }

    @Test
    fun `link loss and restore`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = testSession(repository, FakeDeviceConnectionRepository())
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
        val session = testSession(FakeDeviceSessionRepository(), connection)

        session.close().join()

        assertEquals(1, connection.closeCount)
    }

    @Test
    fun `device scope cancels work on disconnect and reruns onReady on reconnect`() =
        runTest(UnconfinedTestDispatcher()) {
            val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
            val session = testSession(repository, FakeDeviceConnectionRepository())
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
        val session = testSession(FakeDeviceSessionRepository(), connection)
        assertNull(session.transport.value)

        session.attach(ConnectionTransport.USB)
        assertEquals(ConnectionTransport.USB, session.transport.value)

        session.close().join()
        assertNull(session.transport.value)
        assertEquals(1, connection.closeCount)
    }

    @Test
    fun `boot mode command is repeated after a pause`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository()
        val session = testSession(repository, FakeDeviceConnectionRepository())

        val response = session.setBootMode(BootModeTarget.BluetoothModule)

        // Ответ — сразу после первой команды; повтор уходит через паузу в фоне.
        assertEquals(Response.Success(BootModeResult.Entered), response)
        assertEquals(1, repository.bootModeCount)
        advanceTimeBy(3_001)
        runCurrent()
        assertEquals(2, repository.bootModeCount)
        assertEquals(listOf(BootModeTarget.BluetoothModule, BootModeTarget.BluetoothModule), repository.bootModeTargets)
    }

    @Test
    fun `wrong pin and errors are not repeated`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository()
        val session = testSession(repository, FakeDeviceConnectionRepository())

        repository.bootModeResults = listOf(Response.Success(BootModeResult.WrongPin))
        assertEquals(Response.Success(BootModeResult.WrongPin), session.setBootMode(BootModeTarget.MainProgram))
        advanceTimeBy(10_000)
        runCurrent()
        assertEquals(1, repository.bootModeCount)

        repository.bootModeResults = listOf(Response.Error(IllegalStateException("no answer")))
        assertTrue(session.setBootMode(BootModeTarget.MainProgram) is Response.Error)
        advanceTimeBy(10_000)
        runCurrent()
        // Первая команда — одна на каждый вызов: повтор с неверным PIN продлил бы блокировку ввода на блоке.
        assertEquals(2, repository.bootModeCount)
    }

    @Test
    fun `reboot to bootloader is cleared when the block returns in the bootloader`() =
        runTest(UnconfinedTestDispatcher()) {
            val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
            val session = testSession(repository)
            session.attach(ConnectionTransport.BLE)
            backgroundScope.launch { session.state.collect {} }
            runCurrent()
            assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)

            session.expectReboot(DeviceReboot.ToBoot)
            assertEquals(DeviceReboot.ToBoot, session.reboot.value)

            repository.link.value = DeviceLinkState.DISCONNECTED
            runCurrent()
            assertEquals(DeviceReboot.ToBoot, session.reboot.value)

            repository.readResults = listOf(Response.Success(testDevice.copy(mode = DeviceOperationMode.BOOT)))
            repository.link.value = DeviceLinkState.READY
            runCurrent()
            assertNull(session.reboot.value)
        }

    @Test
    fun `expected reboot expires and is cancelled by attach, close and cancel`() = runTest(UnconfinedTestDispatcher()) {
        val session = testSession()

        session.expectReboot(DeviceReboot.ToBoot)
        advanceTimeBy(59_000)
        runCurrent()
        assertEquals(DeviceReboot.ToBoot, session.reboot.value)
        advanceTimeBy(1_001)
        runCurrent()
        assertNull(session.reboot.value)

        session.expectReboot(DeviceReboot.ToBoot)
        session.cancelReboot()
        assertNull(session.reboot.value)

        session.expectReboot(DeviceReboot.ToBoot)
        session.attach(ConnectionTransport.BLE)
        assertNull(session.reboot.value)

        session.expectReboot(DeviceReboot.ToMain)
        session.close().join()
        assertNull(session.reboot.value)
    }

    @Test
    fun `reboot to main waits for both the block and the acknowledgement`() = runTest(UnconfinedTestDispatcher()) {
        val bootDevice = testDevice.copy(mode = DeviceOperationMode.BOOT)
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
            readResults = listOf(Response.Success(bootDevice))
        }
        val session = testSession(repository)
        session.attach(ConnectionTransport.USB)
        backgroundScope.launch { session.state.collect {} }
        runCurrent()
        assertEquals(DeviceSessionState.FirmwareBoot(bootDevice), session.state.value)

        // Блок перезагружается (обрыв на «Применении») и возвращается раньше, чем пользователь нажал «OK».
        session.expectReboot(DeviceReboot.ToMain)
        repository.link.value = DeviceLinkState.DISCONNECTED
        repository.readResults = listOf(Response.Success(testDevice))
        repository.link.value = DeviceLinkState.READY
        runCurrent()
        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
        assertEquals(DeviceReboot.ToMain, session.reboot.value)

        // Тайм-аут после возврата блока ожидание не снимает: экран результата ждёт пользователя.
        advanceTimeBy(120_000)
        runCurrent()
        assertEquals(DeviceReboot.ToMain, session.reboot.value)

        session.acknowledgeReboot()
        assertNull(session.reboot.value)
    }

    @Test
    fun `acknowledgement before the block returns clears the reboot on return`() = runTest(UnconfinedTestDispatcher()) {
        val bootDevice = testDevice.copy(mode = DeviceOperationMode.BOOT)
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
            readResults = listOf(Response.Success(bootDevice))
        }
        val session = testSession(repository)
        session.attach(ConnectionTransport.USB)
        backgroundScope.launch { session.state.collect {} }
        runCurrent()

        session.expectReboot(DeviceReboot.ToMain)
        repository.link.value = DeviceLinkState.DISCONNECTED
        session.acknowledgeReboot()
        assertEquals(DeviceReboot.ToMain, session.reboot.value)

        repository.readResults = listOf(Response.Success(testDevice))
        repository.link.value = DeviceLinkState.READY
        runCurrent()
        assertNull(session.reboot.value)
    }

    @Test
    fun `ble link loss is reconnected until the link returns`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = testSession(repository, FakeDeviceConnectionRepository())
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
    fun `ble reconnect keeps Disconnected instead of Connecting`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.CONNECTING)
        val session = testSession(repository)
        session.attach(ConnectionTransport.BLE)
        val states = mutableListOf<DeviceSessionState>()
        backgroundScope.launch { session.state.collect { states += it } }
        runCurrent()
        repository.link.value = DeviceLinkState.READY
        runCurrent()
        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)

        states.clear()
        repository.link.value = DeviceLinkState.DISCONNECTED
        repository.link.value = DeviceLinkState.CONNECTING
        repository.link.value = DeviceLinkState.DISCONNECTED
        repository.link.value = DeviceLinkState.CONNECTING
        repository.link.value = DeviceLinkState.READY
        runCurrent()

        assertEquals(listOf(DeviceSessionState.Disconnected, DeviceSessionState.Ready(testDevice)), states)
    }

    @Test
    fun `first ble connection and usb reconnect show Connecting`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.CONNECTING)
        val session = testSession(repository)
        session.attach(ConnectionTransport.BLE)
        backgroundScope.launch { session.state.collect {} }
        runCurrent()
        assertEquals(DeviceSessionState.Connecting, session.state.value)

        val usbRepository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val usbSession = testSession(usbRepository)
        usbSession.attach(ConnectionTransport.USB)
        backgroundScope.launch { usbSession.state.collect {} }
        runCurrent()
        usbRepository.link.value = DeviceLinkState.DISCONNECTED
        usbRepository.link.value = DeviceLinkState.CONNECTING
        runCurrent()
        assertEquals(DeviceSessionState.Connecting, usbSession.state.value)
    }

    @Test
    fun `usb link loss is not reconnected by the app`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.DISCONNECTED)
        val session = testSession(repository, FakeDeviceConnectionRepository())
        session.attach(ConnectionTransport.USB)
        backgroundScope.launch { session.state.collect {} }

        advanceTimeBy(9_001)
        assertEquals(0, repository.reconnectCount)
    }

    @Test
    fun `closed session is not reconnected`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.DISCONNECTED)
        val session = testSession(repository, FakeDeviceConnectionRepository())
        session.attach(ConnectionTransport.BLE)
        backgroundScope.launch { session.state.collect {} }
        advanceTimeBy(1_001)
        assertEquals(1, repository.reconnectCount)

        session.close()
        advanceTimeBy(9_001)
        assertEquals(1, repository.reconnectCount)
    }

    @Test
    fun `close waits for a running reconnect`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.DISCONNECTED)
        val connection = FakeDeviceConnectionRepository()
        val session = testSession(repository, connection)
        session.attach(ConnectionTransport.BLE)
        val gate = CompletableDeferred<Unit>()
        repository.reconnectGate = gate
        backgroundScope.launch { session.state.collect {} }
        advanceTimeBy(1_001)
        assertEquals(1, repository.reconnectCount)

        val closing = session.close()
        runCurrent()
        assertNull(session.transport.value)
        assertEquals(0, connection.closeCount)

        gate.complete(Unit)
        closing.join()
        assertEquals(1, connection.closeCount)
    }

    @Test
    fun `ble module in bootloader gives BluetoothBoot without reading the main program`() =
        runTest(UnconfinedTestDispatcher()) {
            val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
                bluetoothModule = Response.Success(BluetoothModule(BluetoothModuleMode.BOOT, testManufacturer))
            }
            val session = testSession(repository)
            session.attach(ConnectionTransport.BLE)
            backgroundScope.launch { session.state.collect {} }
            runCurrent()

            assertEquals(DeviceSessionState.BluetoothBoot(testManufacturer), session.state.value)
            assertEquals(0, repository.readCount)
        }

    @Test
    fun `ble module in main mode goes on to the main program`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = testSession(repository)
        session.attach(ConnectionTransport.BLE)
        backgroundScope.launch { session.state.collect {} }
        runCurrent()

        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
        assertEquals(1, repository.bluetoothReadCount)
        assertEquals(1, repository.readCount)
    }

    @Test
    fun `usb reads the main program directly`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = testSession(repository)
        session.attach(ConnectionTransport.USB)
        backgroundScope.launch { session.state.collect {} }
        runCurrent()

        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
        assertEquals(0, repository.bluetoothReadCount)
    }

    @Test
    fun `main program in bootloader gives FirmwareBoot`() = runTest(UnconfinedTestDispatcher()) {
        val bootDevice = testDevice.copy(mode = DeviceOperationMode.BOOT)
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
            readResults =
                listOf(Response.Success(bootDevice))
        }
        val session = testSession(repository)
        session.attach(ConnectionTransport.USB)
        backgroundScope.launch { session.state.collect {} }
        runCurrent()

        assertEquals(DeviceSessionState.FirmwareBoot(bootDevice), session.state.value)
    }

    @Test
    fun `mode is detected again on every reconnect`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = testSession(repository)
        session.attach(ConnectionTransport.BLE)
        backgroundScope.launch { session.state.collect {} }
        runCurrent()
        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)

        // Блок ушёл в загрузчик BLE-модуля: перезагрузка → обрыв → переподключение.
        repository.link.value = DeviceLinkState.DISCONNECTED
        repository.bluetoothModule = Response.Success(BluetoothModule(BluetoothModuleMode.BOOT, testManufacturer))
        repository.link.value = DeviceLinkState.READY
        runCurrent()
        assertEquals(DeviceSessionState.BluetoothBoot(testManufacturer), session.state.value)

        // BLE-модуль в рабочем режиме, основная программа — в загрузчике.
        val bootDevice = testDevice.copy(mode = DeviceOperationMode.BOOT)
        repository.link.value = DeviceLinkState.DISCONNECTED
        repository.bluetoothModule = Response.Success(BluetoothModule(BluetoothModuleMode.MAIN, testManufacturer))
        repository.readResults = listOf(Response.Success(bootDevice))
        repository.link.value = DeviceLinkState.READY
        runCurrent()
        assertEquals(DeviceSessionState.FirmwareBoot(bootDevice), session.state.value)

        // Прошивка закончена — снова рабочий режим.
        repository.link.value = DeviceLinkState.DISCONNECTED
        repository.readResults = listOf(Response.Success(testDevice))
        repository.link.value = DeviceLinkState.READY
        runCurrent()
        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
    }

    @Test
    fun `unreadable ble module drops the link`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
            bluetoothModule = Response.Error(IllegalStateException("no answer"))
        }
        val session = testSession(repository)
        session.attach(ConnectionTransport.BLE)
        backgroundScope.launch { session.state.collect {} }
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(3, repository.bluetoothReadCount)
        assertEquals(0, repository.readCount)
        assertEquals(1, repository.disconnectCount)
    }
}
