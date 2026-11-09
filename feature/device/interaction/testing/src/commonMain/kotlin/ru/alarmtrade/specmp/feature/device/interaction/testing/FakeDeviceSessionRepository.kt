package ru.alarmtrade.specmp.feature.device.interaction.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothManufacturer
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModuleMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository

val testAlarmDevice = AlarmDevice(
    systemId = 77,
    systemName = "DX4G",
    firmwareVersion = "VER3.57",
    mode = DeviceOperationMode.MAIN,
    features = emptySet(),
    can = null,
)

val testBluetoothModule = BluetoothModule(
    BluetoothModuleMode.MAIN,
    BluetoothManufacturer("1", "BT", "DX4G", "1.0", "1.14"),
)

/** Связь — [link] (тест переключает сам), блок — [device]; списки ответов отдаются по порядку, последний повторяется. */
class FakeDeviceSessionRepository(
    var device: AlarmDevice = testAlarmDevice,
    val link: MutableStateFlow<DeviceLinkState> = MutableStateFlow(DeviceLinkState.READY),
) : DeviceSessionRepository {
    var bluetoothVersions: List<Response<BluetoothModule>> = listOf(Response.Success(testBluetoothModule))
    var bluetoothReads = 0
        private set

    var imei: Response<String> = Response.Success("351756051523999")

    var reconnectCount = 0
        private set

    var bootResults: List<Response<BootModeResult>> = listOf(Response.Success(BootModeResult.Entered))
    var bootCalls = 0
        private set
    val bootTargets = mutableListOf<BootModeTarget>()

    var exitResults: List<Response<Unit>> = listOf(Response.Success(Unit))
    var exitCalls = 0
        private set
    val exitTargets = mutableListOf<BootModeTarget>()

    val pins = mutableListOf<ServicePin>()

    override fun linkState(): Flow<Response<DeviceLinkState>> = link.map { Response.Success(it) }

    override suspend fun readBluetoothModule(): Response<BluetoothModule> =
        bluetoothVersions[minOf(bluetoothReads++, bluetoothVersions.lastIndex)]

    override suspend fun readDevice(): Response<AlarmDevice> = Response.Success(device)

    override suspend fun syncDeviceDatabase(): Response<Unit> = Response.Success(Unit)

    override suspend fun readImei(): Response<String> = imei

    override suspend fun reconnect(): Response<Unit> {
        reconnectCount++
        return Response.Success(Unit)
    }

    override suspend fun disconnect(): Response<Unit> {
        link.value = DeviceLinkState.DISCONNECTED
        return Response.Success(Unit)
    }

    override suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> {
        bootTargets += target
        return bootResults[minOf(bootCalls++, bootResults.lastIndex)]
    }

    override suspend fun exitBootMode(target: BootModeTarget): Response<Unit> {
        exitTargets += target
        return exitResults[minOf(exitCalls++, exitResults.lastIndex)]
    }

    override suspend fun setServicePin(pin: ServicePin): Response<Unit> {
        pins += pin
        return Response.Success(Unit)
    }
}
