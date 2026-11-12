package ru.alarmtrade.specmp.feature.device.peripherals.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.connection.DeviceType
import ru.alarmtrade.devices.domain.alarmDevice.peripheral.DynamicPeripheralDevice
import ru.alarmtrade.devices.domain.alarmDevice.peripheral.PeripheralDeviceInformation
import ru.alarmtrade.devices.interactors.alarmDeviceInteractor.requests.DynamicDeviceCommandRequest
import ru.alarmtrade.devices.interactors.alarmDeviceInteractor.requests.PeripheralDeviceAddressRequest
import ru.alarmtrade.devices.interactors.alarmDeviceInteractor.requests.ReadPeripheralDeviceByTypeRequest
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе.
internal interface PeripheralsDataSource {
    suspend fun readByType(type: DeviceType): SpecialistResponse<List<PeripheralDeviceInformation>>?
    suspend fun blink(address: String): SpecialistResponse<Unit>?
    suspend fun setFirmwareMode(address: String): SpecialistResponse<Unit>?
    fun dynamicDevices(): Flow<SpecialistResponse<List<DynamicPeripheralDevice>>>?
    suspend fun pairDynamic(request: DynamicDeviceCommandRequest): SpecialistResponse<Unit>?
    suspend fun deleteDynamic(request: DynamicDeviceCommandRequest): SpecialistResponse<Unit>?
    suspend fun updateDynamic(request: DynamicDeviceCommandRequest): SpecialistResponse<Unit>?
}

internal class SpecialistPeripheralsDataSource(private val alarmDevice: () -> BluetoothAlarmDeviceInteractor) :
    PeripheralsDataSource {
    override suspend fun readByType(type: DeviceType): SpecialistResponse<List<PeripheralDeviceInformation>>? =
        alarmDevice.resolveOrNull()?.readPeripheralDeviceInformationByTypeUseCase
            ?.execute(ReadPeripheralDeviceByTypeRequest(type))

    override suspend fun blink(address: String): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.blinkPeripheralDeviceUseCase?.execute(PeripheralDeviceAddressRequest(address))

    override suspend fun setFirmwareMode(address: String): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.setPeripheralFirmwareModeUseCase?.execute(PeripheralDeviceAddressRequest(address))

    override fun dynamicDevices(): Flow<SpecialistResponse<List<DynamicPeripheralDevice>>>? =
        alarmDevice.resolveOrNull()?.let { interactor ->
            flow { emitAll(interactor.readDynamicDevicesUseCase.execute()) }
        }

    override suspend fun pairDynamic(request: DynamicDeviceCommandRequest): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.pairDynamicDeviceUseCase?.execute(request)

    override suspend fun deleteDynamic(request: DynamicDeviceCommandRequest): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.deleteDynamicDeviceUseCase?.execute(request)

    override suspend fun updateDynamic(request: DynamicDeviceCommandRequest): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.updateDynamicDeviceUseCase?.execute(request)
}
