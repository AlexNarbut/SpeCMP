package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.alarmtrade.bluetoothControl.domain.bluetooth.alarmDevice.BluetoothManufacturerData
import ru.alarmtrade.bluetoothControl.domain.bluetooth.alarmDevice.BluetoothOperationMode
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.information.AlarmDeviceInformation
import ru.alarmtrade.devices.domain.alarmDevice.information.ModemInformation
import ru.alarmtrade.devices.interactors.alarmDeviceInteractor.requests.SetPcBasePinRequest
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemFeature
import ru.alarmtrade.specialist.domain.appUpdates.SettingsUpdatingStatus
import ru.alarmtrade.specialist.domain.bluetooth.ConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.data.datasource.DeviceSessionDataSource

internal class FakeDeviceSessionDataSource : DeviceSessionDataSource {
    var connectionStates: Flow<SpecialistResponse<ConnectionState>>? = emptyFlow()
    var manufacturer: SpecialistResponse<BluetoothManufacturerData>? = null
    var operationMode: SpecialistResponse<BluetoothOperationMode>? = null
    var information: SpecialistResponse<AlarmDeviceInformation>? = null
    var features: SpecialistResponse<List<SubsystemFeature>>? = SpecialistResponse.Success(emptyList())
    var synchronization: Flow<SpecialistResponse<SettingsUpdatingStatus>>? = emptyFlow()
    var modem: SpecialistResponse<ModemInformation>? = null
    var unitResult: SpecialistResponse<Unit>? = SpecialistResponse.Success(Unit)

    val calls = mutableListOf<String>()

    override fun connectionState(): Flow<SpecialistResponse<ConnectionState>>? = connectionStates

    override suspend fun manufacturerData(): SpecialistResponse<BluetoothManufacturerData>? = manufacturer

    override suspend fun bluetoothOperationMode(): SpecialistResponse<BluetoothOperationMode>? {
        calls += "operationMode"
        return operationMode
    }

    override suspend fun deviceInformation(): SpecialistResponse<AlarmDeviceInformation>? = information

    override suspend fun appFeatures(): SpecialistResponse<List<SubsystemFeature>>? = features

    override fun synchronizeSettingsVersion(): Flow<SpecialistResponse<SettingsUpdatingStatus>>? = synchronization

    override suspend fun modemInformation(): SpecialistResponse<ModemInformation>? = modem

    override suspend fun connect(): SpecialistResponse<Unit>? = unit("connect")

    override suspend fun disconnect(): SpecialistResponse<Unit>? = unit("disconnect")

    override suspend fun setAlarmDeviceBootMode(): SpecialistResponse<Unit>? = unit("setAlarmDeviceBootMode")

    override suspend fun setBluetoothModemBootMode(): SpecialistResponse<Unit>? = unit("setBluetoothModemBootMode")

    override suspend fun exitAlarmDeviceBootMode(): SpecialistResponse<Unit>? = unit("exitAlarmDeviceBootMode")

    override suspend fun exitBluetoothModemBootMode(): SpecialistResponse<Unit>? = unit("exitBluetoothModemBootMode")

    override suspend fun setPcBasePin(request: SetPcBasePinRequest): SpecialistResponse<Unit>? =
        unit("setPcBasePin:${request.pin}")

    private fun unit(call: String): SpecialistResponse<Unit>? {
        calls += call
        return unitResult
    }
}
