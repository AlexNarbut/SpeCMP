package ru.alarmtrade.specmp.feature.device.peripherals.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.alarmtrade.devices.interactors.alarmDeviceInteractor.requests.DynamicDeviceCommandRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.core.specialist.sdk.toResponseOrUnavailable
import ru.alarmtrade.specmp.feature.device.peripherals.data.datasource.PeripheralsDataSource
import ru.alarmtrade.specmp.feature.device.peripherals.data.mapper.toDynamicPeripherals
import ru.alarmtrade.specmp.feature.device.peripherals.data.mapper.withPeripheralFailure
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.DynamicPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.repository.DynamicPeripheralsRepository

internal class SpecialistDynamicPeripheralsRepository(private val dataSource: PeripheralsDataSource) :
    DynamicPeripheralsRepository {

    override fun slots(): Flow<Response<List<DynamicPeripheral>>> = flow {
        val slots = safeFlowRun {
            dataSource.dynamicDevices()
                ?.map { sdk -> sdk.toResponse().map { it.toDynamicPeripherals() }.withPeripheralFailure() }
                ?: flowOf(interactorUnavailable())
        }
        emitAll(slots)
    }

    override suspend fun pair(slot: Int, address: String?): Response<Unit> = dataSource.pairDynamic(
        DynamicDeviceCommandRequest(slot, address),
    ).toResponseOrUnavailable().withPeripheralFailure()

    override suspend fun delete(slot: Int): Response<Unit> =
        dataSource.deleteDynamic(DynamicDeviceCommandRequest(slot)).toResponseOrUnavailable().withPeripheralFailure()

    override suspend fun update(slot: Int): Response<Unit> =
        dataSource.updateDynamic(DynamicDeviceCommandRequest(slot)).toResponseOrUnavailable().withPeripheralFailure()
}
