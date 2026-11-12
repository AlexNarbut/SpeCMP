package ru.alarmtrade.specmp.feature.device.peripherals.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.connection.DeviceType
import ru.alarmtrade.devices.domain.alarmDevice.peripheral.DynamicPeripheralDevice
import ru.alarmtrade.devices.domain.alarmDevice.peripheral.PeripheralDeviceInformation
import ru.alarmtrade.devices.interactors.alarmDeviceInteractor.requests.DynamicDeviceCommandRequest
import ru.alarmtrade.specmp.feature.device.peripherals.data.datasource.PeripheralsDataSource

internal class FakePeripheralsDataSource : PeripheralsDataSource {
    val byType = mutableMapOf<DeviceType, SpecialistResponse<List<PeripheralDeviceInformation>>>()
    var command: SpecialistResponse<Unit>? = SpecialistResponse.Success(Unit)
    var dynamic: Flow<SpecialistResponse<List<DynamicPeripheralDevice>>>? = emptyFlow()

    val reads = mutableListOf<DeviceType>()
    val calls = mutableListOf<String>()

    override suspend fun readByType(type: DeviceType): SpecialistResponse<List<PeripheralDeviceInformation>>? {
        reads += type
        return byType[type] ?: SpecialistResponse.Success(emptyList())
    }

    override suspend fun blink(address: String): SpecialistResponse<Unit>? = command.also { calls += "blink $address" }

    override suspend fun setFirmwareMode(address: String): SpecialistResponse<Unit>? =
        command.also { calls += "firmware $address" }

    override fun dynamicDevices(): Flow<SpecialistResponse<List<DynamicPeripheralDevice>>>? = dynamic

    override suspend fun pairDynamic(request: DynamicDeviceCommandRequest): SpecialistResponse<Unit>? =
        command.also { calls += "pair ${request.deviceId} ${request.deviceAddress}" }

    override suspend fun deleteDynamic(request: DynamicDeviceCommandRequest): SpecialistResponse<Unit>? =
        command.also { calls += "delete ${request.deviceId}" }

    override suspend fun updateDynamic(request: DynamicDeviceCommandRequest): SpecialistResponse<Unit>? =
        command.also { calls += "update ${request.deviceId}" }
}
