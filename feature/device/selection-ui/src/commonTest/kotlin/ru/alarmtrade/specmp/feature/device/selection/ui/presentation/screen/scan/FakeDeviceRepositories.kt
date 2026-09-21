package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionState
import ru.alarmtrade.permissions.common.PermissionsController
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDeviceInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootData
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.AlarmDeviceRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice
import ru.alarmtrade.specmp.feature.device.selection.domain.repository.DeviceScanRepository

class FakeDeviceScanRepository(
    private val ble: List<ScannedDevice.Ble> = emptyList(),
    private val usb: List<ScannedDevice.Usb> = emptyList(),
    private val transports: Set<DeviceTransport> = setOf(DeviceTransport.BLE, DeviceTransport.USB),
) : DeviceScanRepository {
    override fun supportedTransports(): Set<DeviceTransport> = transports
    override fun scanBluetooth(): Flow<Response<List<ScannedDevice.Ble>>> = flowOf(Response.Success(ble))
    override fun scanUsb(): Flow<Response<List<ScannedDevice.Usb>>> = flowOf(Response.Success(usb))
    override suspend fun stopScan() = Unit
}

class FakeDeviceConnectionRepository(
    private val states: List<Response<DeviceConnectionState>> =
        listOf(Response.Success(DeviceConnectionState.CONNECTED)),
) : DeviceConnectionRepository {
    var closed: Boolean = false
        private set

    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> =
        states.asFlow()

    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = states.asFlow()

    override suspend fun closeSessions() {
        closed = true
    }
}

class FakePermissionsController(
    private val state: PermissionState = PermissionState.Granted,
) : PermissionsController {
    override suspend fun providePermission(permission: Permission) = Unit
    override suspend fun isPermissionGranted(permission: Permission): Boolean =
        state == PermissionState.Granted
    override suspend fun getPermissionState(permission: Permission): PermissionState = state
    override fun openAppSettings() = Unit
}

class FakePermissionsProvider(
    private val controller: PermissionsController = FakePermissionsController(),
    private val blePermissions: List<Permission> = emptyList(),
    private val locationEnabled: Boolean = true,
) : PermissionsProvider {
    override fun getController(): PermissionsController = controller
    override fun getBlePermission(): List<Permission> = blePermissions
    override fun isLocationEnabled(): Boolean = locationEnabled
    override fun openLocationSettings() = Unit
    override fun openBluetoothSettings() = Unit
    // observe* методов больше нет в интерфейсе (revert реактивного варианта)
}

class FakeAlarmDeviceRepository(
    private val info: AlarmDeviceInfo = AlarmDeviceInfo("1", "Pandora", "1.0"),
    private val boot: BootData = BootData("device", "firmware", "1.0"),
) : AlarmDeviceRepository {
    override suspend fun getAlarmDeviceInformation(): Response<AlarmDeviceInfo> = Response.Success(info)
    override suspend fun getBootData(): Response<BootData> = Response.Success(boot)
    override suspend fun disconnect() = Unit
}
