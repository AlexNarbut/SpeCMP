package ru.alarmtrade.specmp.feature.device.firmware.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeFirmwareRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testAlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession

class CheckFirmwareUseCaseTest {
    private val device = testAlarmDevice.copy(can = CanInfo("12.05", "1234", isFactory = false, version = 5))
    private val main =
        FirmwareModule(1, FirmwareModuleKind.AlarmMain, "DX4G", "Pandora DX 4G", BootModeTarget.MainProgram)
    private val bluetooth =
        FirmwareModule(2, FirmwareModuleKind.BluetoothModule, "BTM2", "Pandora BT", BootModeTarget.BluetoothModule)
    private val watch = FirmwareModule(3, FirmwareModuleKind.WatchBluetooth, "PW2", "Pandora Watch", bootTarget = null)

    private val repository = FakeFirmwareRepository()
    private val sessionRepository = FakeDeviceSessionRepository(device)

    private fun version(value: String) = FirmwareVersion(0, value, LocalDate(2026, 10, 14), "", "https://fw/$value")

    private fun TestScope.useCase() = CheckFirmwareUseCase(
        repository,
        testDeviceSession(
            sessionRepository,
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            transport = ConnectionTransport.BLE,
        ),
    )

    @Test
    fun `installed versions are compared with release versions`() = runTest {
        repository.modulesResult = Response.Success(listOf(main, bluetooth, watch))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.58"))
        repository.latest[2L to FirmwareChannel.RELEASE] = Response.Success(version("1.14"))

        val statuses = (useCase()(device) as Response.Success).value

        assertEquals(3, statuses.size)
        assertEquals("3.57", statuses[0].installed)
        assertTrue(statuses[0].updateAvailable)
        assertEquals("1.14", statuses[1].installed)
        assertEquals(false, statuses[1].updateAvailable)
        assertNull(statuses[2].latest)
        assertEquals(listOf(FirmwareCan("1234", 5, "12.05"), null), repository.latestCans.take(2))
    }

    @Test
    fun `main module lookup error fails the check, others only lose the version`() = runTest {
        repository.modulesResult = Response.Success(listOf(main, bluetooth))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.57"))

        val statuses = (useCase()(device) as Response.Success).value
        assertNull(statuses[1].latest)

        repository.latest.remove(1L to FirmwareChannel.RELEASE)
        assertIs<Response.Error>(useCase()(device))
    }

    @Test
    fun `module list error is the result`() = runTest {
        repository.modulesResult = Response.Error(IllegalStateException("offline"))
        assertIs<Response.Error>(useCase()(device))
    }
}
