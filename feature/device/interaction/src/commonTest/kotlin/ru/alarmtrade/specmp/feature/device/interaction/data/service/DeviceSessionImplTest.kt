package ru.alarmtrade.specmp.feature.device.interaction.data.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.AppDispatchers
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
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin

class DeviceSessionImplTest {

    @Test
    fun `ready link reads device`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = testSession(repository, FakeDeviceConnectionRepository())
        backgroundScope.launch { session.state.collect {} }
        advanceUntilIdle()

        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
    }

    @Test
    fun `unknown model syncs the device database once and reads the device again`() =
        runTest(UnconfinedTestDispatcher()) {
            val unknown = testDevice.copy(features = null, modelInDatabase = false)
            val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
                readResults = listOf(Response.Success(unknown), Response.Success(testDevice))
            }
            val session = testSession(repository, FakeDeviceConnectionRepository())
            backgroundScope.launch { session.state.collect {} }
            advanceUntilIdle()

            assertEquals(1, repository.syncCount)
            assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
        }

    @Test
    fun `failed database sync keeps the device without features and is not repeated`() =
        runTest(UnconfinedTestDispatcher()) {
            val unknown = testDevice.copy(features = null, modelInDatabase = false)
            val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
                readResults = listOf(Response.Success(unknown))
                syncResult = Response.Error(IllegalStateException("no network"))
            }
            val session = testSession(repository, FakeDeviceConnectionRepository())
            backgroundScope.launch { session.state.collect {} }
            advanceUntilIdle()

            assertEquals(DeviceSessionState.Ready(unknown), session.state.value)
            assertEquals(1, repository.syncCount)
            assertEquals(1, repository.readCount)
        }

    @Test
    fun `hanging database sync times out and keeps the device without features`() =
        runTest(UnconfinedTestDispatcher()) {
            val unknown = testDevice.copy(features = null, modelInDatabase = false)
            val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
                readResults = listOf(Response.Success(unknown))
                syncHangs = true
            }
            val session = testSession(repository, FakeDeviceConnectionRepository())
            backgroundScope.launch { session.state.collect {} }
            advanceUntilIdle()
            assertEquals(DeviceSessionState.Connecting, session.state.value)

            advanceTimeBy(30_001)

            assertEquals(DeviceSessionState.Ready(unknown), session.state.value)
            assertEquals(1, repository.syncCount)
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
    fun `link error is treated as disconnect`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = testSession(repository, FakeDeviceConnectionRepository())
        backgroundScope.launch { session.state.collect {} }
        advanceUntilIdle()

        repository.linkErrors.emit(Response.Error(IllegalStateException("sdk")))

        assertEquals(DeviceSessionState.Disconnected, session.state.value)
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

        session.closeAndAwait()

        assertEquals(1, connection.closeCount)
    }

    @Test
    fun `transport is attached by the scan and cleared on close`() = runTest(UnconfinedTestDispatcher()) {
        val connection = FakeDeviceConnectionRepository()
        val session = testSession(FakeDeviceSessionRepository(), connection)
        assertNull(session.transport.value)

        session.attach(ConnectionTransport.USB)
        assertEquals(ConnectionTransport.USB, session.transport.value)

        session.closeAndAwait()
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
        session.closeAndAwait()
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

        val closing = backgroundScope.async { session.closeAndAwait() }
        runCurrent()
        assertNull(session.transport.value)
        assertEquals(0, connection.closeCount)

        gate.complete(Unit)
        assertEquals(Response.Success(Unit), closing.await())
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
    fun `serial - silent board keeps the port and is polled until it answers from the bootloader`() = runTest {
        val bootDevice = testDevice.copy(mode = DeviceOperationMode.BOOT)
        val silent = Response.Error(IllegalStateException("no answer"))
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
            // detectMode: 3 попытки — молчит; опрос: ещё раз молчит, затем загрузчик отвечает
            readResults = listOf(silent, silent, silent, silent, Response.Success(bootDevice))
        }
        val session = testSession(repository)
        session.attach(ConnectionTransport.SERIAL)
        backgroundScope.launch { session.state.collect {} }
        advanceTimeBy(1_500)
        assertEquals(DeviceSessionState.Disconnected, session.state.value)

        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(DeviceSessionState.FirmwareBoot(bootDevice), session.state.value)
        assertEquals(0, repository.disconnectCount)
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
    fun `firmware transfer keeps the bootloader mode across reconnects without reading the block`() =
        runTest(UnconfinedTestDispatcher()) {
            val bootDevice = testDevice.copy(mode = DeviceOperationMode.BOOT)
            val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
                readResults = listOf(Response.Success(bootDevice))
            }
            val session = testSession(repository)
            session.attach(ConnectionTransport.BLE)
            backgroundScope.launch { session.state.collect {} }
            runCurrent()
            assertEquals(DeviceSessionState.FirmwareBoot(bootDevice), session.state.value)
            val reads = repository.readCount to repository.bluetoothReadCount

            session.beginFirmwareTransfer()
            repository.link.value = DeviceLinkState.DISCONNECTED
            repository.link.value = DeviceLinkState.READY
            runCurrent()
            // Протокол занят передачей (SDK продолжает её сам): режим тот же, блок не читали.
            assertEquals(DeviceSessionState.FirmwareBoot(bootDevice), session.state.value)
            assertEquals(reads, repository.readCount to repository.bluetoothReadCount)

            // Передача закончилась (применение перезагружает блок) — режим снова определяется на переподключении.
            session.endFirmwareTransfer()
            repository.readResults = listOf(Response.Success(testDevice))
            repository.link.value = DeviceLinkState.DISCONNECTED
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

    @Test
    fun `reconnect now makes one attempt and does nothing without a transport`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.DISCONNECTED)
        val session = testSession(repository)

        assertNull(session.reconnectNow())
        assertEquals(0, repository.reconnectCount)

        session.attach(ConnectionTransport.USB)
        assertEquals(Response.Success(Unit), session.reconnectNow())
        assertEquals(1, repository.reconnectCount)

        session.closeAndAwait()
        assertNull(session.reconnectNow())
    }

    @Test
    fun `refresh mode rereads the block on a ready link without showing Connecting`() =
        runTest(UnconfinedTestDispatcher()) {
            val bootDevice = testDevice.copy(mode = DeviceOperationMode.BOOT)
            val repository = FakeDeviceSessionRepository(DeviceLinkState.READY).apply {
                readResults = listOf(Response.Success(bootDevice))
            }
            val session = testSession(repository)
            session.attach(ConnectionTransport.SERIAL)
            val states = mutableListOf<DeviceSessionState>()
            backgroundScope.launch { session.state.collect { states += it } }
            runCurrent()
            assertEquals(DeviceSessionState.FirmwareBoot(bootDevice), session.state.value)
            states.clear()

            repository.readResults = listOf(Response.Success(testDevice))
            session.refreshMode()
            runCurrent()

            assertEquals(listOf<DeviceSessionState>(DeviceSessionState.Ready(testDevice)), states)
        }

    @Test
    fun `refresh mode keeps the state when the block does not answer`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = testSession(repository)
        session.attach(ConnectionTransport.SERIAL)
        backgroundScope.launch { session.state.collect {} }
        runCurrent()
        val reads = repository.readCount

        repository.readResults = listOf(Response.Error(IllegalStateException("no answer")))
        session.refreshMode()
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
        assertEquals(reads + 3, repository.readCount)
        assertEquals(0, repository.disconnectCount)
    }

    @Test
    fun `refresh mode is ignored while the link is not ready`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.CONNECTING)
        val session = testSession(repository)
        session.attach(ConnectionTransport.USB)
        backgroundScope.launch { session.state.collect {} }
        runCurrent()

        session.refreshMode()
        runCurrent()

        assertEquals(DeviceSessionState.Connecting, session.state.value)
        assertEquals(0, repository.readCount)
    }

    @Test
    fun `state is reset after the last subscriber leaves`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = testSession(repository)
        session.attach(ConnectionTransport.USB)
        val subscriber = backgroundScope.launch { session.state.collect {} }
        runCurrent()
        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)

        subscriber.cancel()
        advanceTimeBy(4_000)
        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
        advanceTimeBy(1_001)
        assertEquals(DeviceSessionState.Connecting, session.state.value)

        backgroundScope.launch { session.state.collect {} }
        runCurrent()
        assertEquals(DeviceSessionState.Ready(testDevice), session.state.value)
        assertEquals(2, repository.readCount)
    }

    @Test
    fun `firmware transfer outside the bootloader does not hold the mode`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = testSession(repository)
        session.attach(ConnectionTransport.BLE)
        backgroundScope.launch { session.state.collect {} }
        runCurrent()

        session.beginFirmwareTransfer()
        val bootDevice = testDevice.copy(mode = DeviceOperationMode.BOOT)
        repository.readResults = listOf(Response.Success(bootDevice))
        repository.link.value = DeviceLinkState.DISCONNECTED
        repository.link.value = DeviceLinkState.READY
        runCurrent()

        assertEquals(DeviceSessionState.FirmwareBoot(bootDevice), session.state.value)
    }

    @Test
    fun `reboot is published to observers`() = runTest(UnconfinedTestDispatcher()) {
        val session = testSession()
        val reboots = mutableListOf<DeviceReboot?>()
        backgroundScope.launch { session.reboot.collect { reboots += it } }

        session.expectReboot(DeviceReboot.Restart)
        session.cancelReboot()

        assertEquals(listOf(null, DeviceReboot.Restart, null), reboots)
    }

    @Test
    fun `device commands are passed to the repository`() = runTest(UnconfinedTestDispatcher()) {
        val repository = FakeDeviceSessionRepository(DeviceLinkState.READY)
        val session = testSession(repository)
        val pin = requireNotNull(ServicePin.parseOrNull("1234"))

        assertEquals(repository.imei, session.readImei())
        assertEquals(Response.Success(Unit), session.exitBootMode(BootModeTarget.MainProgram))
        assertEquals(Response.Success(Unit), session.setServicePin(pin))

        assertEquals(listOf(BootModeTarget.MainProgram), repository.exitTargets)
        assertEquals(listOf(pin), repository.savedPins)
    }

    @Test
    fun `reboot is published only after start and start is idempotent`() = runTest(UnconfinedTestDispatcher()) {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val session = DeviceSessionImpl(
            FakeDeviceSessionRepository(),
            FakeDeviceConnectionRepository(),
            AppDispatchers(dispatcher, dispatcher, dispatcher),
            backgroundScope.coroutineContext,
        )
        val reboots = mutableListOf<DeviceReboot?>()
        backgroundScope.launch { session.reboot.collect { reboots += it } }

        session.expectReboot(DeviceReboot.ToBoot)
        assertNull(session.reboot.value)

        session.start()
        session.start()
        assertEquals(DeviceReboot.ToBoot, session.reboot.value)
        session.cancelReboot()

        assertEquals(listOf(null, DeviceReboot.ToBoot, null), reboots)
    }

    @Test
    fun `close does not wait and closes in the background`() = runTest(UnconfinedTestDispatcher()) {
        val connection = FakeDeviceConnectionRepository().apply {
            closeResult = Response.Error(IllegalStateException("sdk"))
        }
        val session = testSession(connection = connection)
        session.attach(ConnectionTransport.USB)

        session.close()
        runCurrent()

        assertNull(session.transport.value)
        assertEquals(1, connection.closeCount)
    }

    @Test
    fun `close and await returns the close result`() = runTest(UnconfinedTestDispatcher()) {
        val error = Response.Error(IllegalStateException("sdk"))
        val connection = FakeDeviceConnectionRepository().apply { closeResult = error }
        val session = testSession(connection = connection)

        assertEquals(error, session.closeAndAwait())
    }
}
