package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.bluetoothControl.domain.bluetooth.alarmDevice.BluetoothManufacturerData
import ru.alarmtrade.bluetoothControl.domain.bluetooth.alarmDevice.BluetoothOperationMode
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.information.AlarmDeviceInformation
import ru.alarmtrade.devices.domain.alarmDevice.information.AlarmDeviceOperationMode
import ru.alarmtrade.devices.domain.alarmDevice.information.CanInformation
import ru.alarmtrade.devices.domain.alarmDevice.information.ModemInformation
import ru.alarmtrade.devices.domain.errors.IncorrectServicePinException
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemFeature
import ru.alarmtrade.specialist.domain.appUpdates.SettingsUpdatingStatus
import ru.alarmtrade.specialist.domain.bluetooth.ConnectionState
import ru.alarmtrade.specialist.domain.errors.alarmDevice.AlarmDeviceNotFoundException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModuleMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin

class SpecialistDeviceSessionRepositoryTest {
    private val dataSource = FakeDeviceSessionDataSource()
    private val repository = SpecialistDeviceSessionRepository(dataSource)

    @Test
    fun `link state follows the current SDK session`() = runTest {
        dataSource.connectionStates = flowOf(
            SpecialistResponse.Success(ConnectionState.CONNECTING),
            SpecialistResponse.Success(ConnectionState.CONNECTED),
            SpecialistResponse.Success(ConnectionState.READY),
            SpecialistResponse.Success(ConnectionState.UNKNOWN),
        )

        assertEquals(
            listOf(
                DeviceLinkState.CONNECTING,
                DeviceLinkState.CONNECTING,
                DeviceLinkState.READY,
                DeviceLinkState.DISCONNECTED,
            ).map { Response.Success(it) },
            repository.linkState().toList(),
        )
    }

    @Test
    fun `link state without the interactor is disconnected, a broken stream ends with an error`() = runTest {
        dataSource.connectionStates = null
        assertEquals(listOf(Response.Success(DeviceLinkState.DISCONNECTED)), repository.linkState().toList())

        val failure = IllegalStateException("session closed")
        dataSource.connectionStates = flow { throw failure }
        assertSame(failure, assertIs<Response.Error>(repository.linkState().toList().single()).exception)
    }

    @Test
    fun `bluetooth module combines manufacturer data and mode`() = runTest {
        dataSource.manufacturer = SpecialistResponse.Success(manufacturer())
        dataSource.operationMode = SpecialistResponse.Success(BluetoothOperationMode.BLE_BOOT_MODE)

        val module = assertIs<Response.Success<BluetoothModule>>(repository.readBluetoothModule()).value
        assertEquals(BluetoothModuleMode.BOOT, module.mode)
        assertEquals("SN-1", module.manufacturer.serialNumber)
        assertEquals("2.0", module.manufacturer.mainFirmwareVersion)

        dataSource.operationMode = SpecialistResponse.Success(BluetoothOperationMode.ERROR)
        assertIs<Response.Error>(repository.readBluetoothModule())
    }

    @Test
    fun `module without manufacturer data does not ask for the mode`() = runTest {
        val failure = IllegalStateException("no answer")
        dataSource.manufacturer = SpecialistResponse.Error.General(failure)

        assertSame(failure, assertIs<Response.Error>(repository.readBluetoothModule()).exception)
        assertEquals(emptyList(), dataSource.calls)
    }

    @Test
    fun `device maps information, features and CAN`() = runTest {
        dataSource.information = SpecialistResponse.Success(information(AlarmDeviceOperationMode.BOOT_FLD))
        dataSource.features = SpecialistResponse.Success(
            listOf(SubsystemFeature.DEVICE_SETTINGS, SubsystemFeature.SINGLE_CAN_FIRMWARE, SubsystemFeature.CLONE),
        )

        val device = assertIs<Response.Success<AlarmDevice>>(repository.readDevice()).value

        assertEquals(42L, device.systemId)
        assertEquals("DX-9X", device.systemName)
        assertEquals(DeviceOperationMode.BOOT, device.mode)
        assertEquals(setOf(DeviceFeature.SETTINGS, DeviceFeature.CLONE), device.features)
        assertIs<CanInfo>(device.can)
        assertTrue(device.modelInDatabase)
    }

    @Test
    fun `device missing in the SDK database keeps the device without features`() = runTest {
        dataSource.information = SpecialistResponse.Success(information(AlarmDeviceOperationMode.MAIN))
        dataSource.features = SpecialistResponse.Error.General(AlarmDeviceNotFoundException("DX-9X", null))

        val device = assertIs<Response.Success<AlarmDevice>>(repository.readDevice()).value

        assertEquals(DeviceOperationMode.MAIN, device.mode)
        assertNull(device.features)
        assertNull(device.can)
        assertEquals(false, device.modelInDatabase)
    }

    @Test
    fun `device information error and unavailable interactor are errors`() = runTest {
        val failure = IllegalStateException("timeout")
        dataSource.information = SpecialistResponse.Error.General(failure)
        assertSame(failure, assertIs<Response.Error>(repository.readDevice()).exception)

        dataSource.information = null
        assertIs<Response.Error>(repository.readDevice())
    }

    @Test
    fun `database sync takes the last status`() = runTest {
        dataSource.synchronization = flowOf(
            SpecialistResponse.Success(SettingsUpdatingStatus.CheckLocalVersion()),
            SpecialistResponse.Success(SettingsUpdatingStatus.CheckNetworkVersion()),
        )
        assertEquals(Response.Success(Unit), repository.syncDeviceDatabase())

        val failure = IllegalStateException("broken database")
        dataSource.synchronization = flow { throw failure }
        assertSame(failure, assertIs<Response.Error>(repository.syncDeviceDatabase()).exception)

        dataSource.synchronization = emptyFlow()
        assertIs<Response.Error>(repository.syncDeviceDatabase())

        dataSource.synchronization = null
        assertIs<Response.Error>(repository.syncDeviceDatabase())
    }

    @Test
    fun `boot mode goes to the target and a wrong pin is an outcome`() = runTest {
        assertEquals(Response.Success(BootModeResult.Entered), repository.setBootMode(BootModeTarget.MainProgram))
        assertEquals(Response.Success(Unit), repository.exitBootMode(BootModeTarget.BluetoothModule))

        dataSource.unitResult = SpecialistResponse.Error.General(IncorrectServicePinException())
        assertEquals(
            Response.Success(BootModeResult.WrongPin),
            repository.setBootMode(BootModeTarget.BluetoothModule),
        )
        assertEquals(
            listOf("setAlarmDeviceBootMode", "exitBluetoothModemBootMode", "setBluetoothModemBootMode"),
            dataSource.calls,
        )
    }

    @Test
    fun `boot mode failures stay errors`() = runTest {
        val failure = IllegalStateException("no answer")
        dataSource.unitResult = SpecialistResponse.Error.General(failure)

        assertSame(failure, assertIs<Response.Error>(repository.setBootMode(BootModeTarget.MainProgram)).exception)
    }

    @Test
    fun `pin, imei and reconnect go to the SDK`() = runTest {
        dataSource.modem = SpecialistResponse.Success(ModemInformation("860000000000001"))

        assertEquals(Response.Success("860000000000001"), repository.readImei())
        assertEquals(Response.Success(Unit), repository.setServicePin(ServicePin.parseOrNull("1234")!!))
        assertEquals(Response.Success(Unit), repository.reconnect())
        assertEquals(Response.Success(Unit), repository.disconnect())
        assertEquals(listOf("setPcBasePin:1234", "connect", "disconnect"), dataSource.calls)
    }

    private fun manufacturer() = BluetoothManufacturerData(
        serialNumber = "SN-1",
        firmwareName = "BLE",
        deviceName = "Module",
        productionDate = LocalDateTime(2025, 1, 1, 0, 0),
        bootFirmwareVersion = "1.0",
        mainFirmwareVersion = "2.0",
        compileDate = LocalDateTime(2025, 1, 1, 0, 0),
    )

    private fun information(mode: AlarmDeviceOperationMode) = AlarmDeviceInformation(
        systemName = "DX-9X",
        firmwareVersion = "3.57",
        systemId = 42L,
        productId = 7L,
        operationMode = mode,
        isMldFirmwareSupported = false,
        canInformation = CanInformation.Undef,
    )
}
