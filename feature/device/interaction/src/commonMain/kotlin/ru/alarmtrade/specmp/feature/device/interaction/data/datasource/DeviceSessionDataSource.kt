package ru.alarmtrade.specmp.feature.device.interaction.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.bluetoothControl.domain.bluetooth.alarmDevice.BluetoothManufacturerData
import ru.alarmtrade.bluetoothControl.domain.bluetooth.alarmDevice.BluetoothOperationMode
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.information.AlarmDeviceInformation
import ru.alarmtrade.devices.domain.alarmDevice.information.ModemInformation
import ru.alarmtrade.devices.interactors.alarmDeviceInteractor.requests.SetPcBasePinRequest
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemFeature
import ru.alarmtrade.specialist.domain.appUpdates.SettingsUpdatingStatus
import ru.alarmtrade.specialist.domain.bluetooth.ConnectionState
import ru.alarmtrade.specialist.interactors.appUpdates.AppUpdatesInteractor
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе.
internal interface DeviceSessionDataSource {
    fun connectionState(): Flow<SpecialistResponse<ConnectionState>>?
    suspend fun manufacturerData(): SpecialistResponse<BluetoothManufacturerData>?
    suspend fun bluetoothOperationMode(): SpecialistResponse<BluetoothOperationMode>?
    suspend fun deviceInformation(): SpecialistResponse<AlarmDeviceInformation>?
    suspend fun appFeatures(): SpecialistResponse<List<SubsystemFeature>>?
    fun synchronizeSettingsVersion(): Flow<SpecialistResponse<SettingsUpdatingStatus>>?
    suspend fun modemInformation(): SpecialistResponse<ModemInformation>?
    suspend fun connect(): SpecialistResponse<Unit>?
    suspend fun disconnect(): SpecialistResponse<Unit>?
    suspend fun setAlarmDeviceBootMode(): SpecialistResponse<Unit>?
    suspend fun setBluetoothModemBootMode(): SpecialistResponse<Unit>?
    suspend fun exitAlarmDeviceBootMode(): SpecialistResponse<Unit>?
    suspend fun exitBluetoothModemBootMode(): SpecialistResponse<Unit>?
    suspend fun setPcBasePin(request: SetPcBasePinRequest): SpecialistResponse<Unit>?
}

internal class SpecialistDeviceSessionDataSource(
    private val alarmDevice: () -> BluetoothAlarmDeviceInteractor,
    private val appUpdates: () -> AppUpdatesInteractor,
) : DeviceSessionDataSource {
    override fun connectionState(): Flow<SpecialistResponse<ConnectionState>>? =
        alarmDevice.resolveOrNull()?.let { interactor ->
            flow { emitAll(interactor.observeCurrentSessionConnectionStateUseCase.execute()) }
        }

    override suspend fun manufacturerData(): SpecialistResponse<BluetoothManufacturerData>? =
        alarmDevice.resolveOrNull()?.getBluetoothManufacturerDataUseCase?.execute()

    override suspend fun bluetoothOperationMode(): SpecialistResponse<BluetoothOperationMode>? =
        alarmDevice.resolveOrNull()?.getBluetoothOperationModeUseCase?.execute()

    override suspend fun deviceInformation(): SpecialistResponse<AlarmDeviceInformation>? =
        alarmDevice.resolveOrNull()?.getAlarmDeviceInformationUseCase?.execute()

    override suspend fun appFeatures(): SpecialistResponse<List<SubsystemFeature>>? =
        alarmDevice.resolveOrNull()?.getAlarmDeviceAppFeaturesUseCase?.execute()

    override fun synchronizeSettingsVersion(): Flow<SpecialistResponse<SettingsUpdatingStatus>>? =
        appUpdates.resolveOrNull()?.let { interactor ->
            flow { emitAll(interactor.synchronizeSettingsVersionUseCase.execute()) }
        }

    override suspend fun modemInformation(): SpecialistResponse<ModemInformation>? =
        alarmDevice.resolveOrNull()?.getModemInformationUseCase?.execute()

    override suspend fun connect(): SpecialistResponse<Unit>? = alarmDevice.resolveOrNull()?.connectUseCase?.execute()

    override suspend fun disconnect(): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.disconnectUseCase?.execute()

    override suspend fun setAlarmDeviceBootMode(): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.setAlarmDeviceBootModeUseCase?.execute()

    override suspend fun setBluetoothModemBootMode(): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.setBluetoothModemBootModeUseCase?.execute()

    override suspend fun exitAlarmDeviceBootMode(): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.exitAlarmDeviceBootModeUseCase?.execute()

    override suspend fun exitBluetoothModemBootMode(): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.exitBluetoothModemBootModeUseCase?.execute()

    override suspend fun setPcBasePin(request: SetPcBasePinRequest): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.setPcBasePinUseCase?.execute(request)
}
