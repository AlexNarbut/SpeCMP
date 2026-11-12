package ru.alarmtrade.specmp.feature.device.peripherals.data.repository

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import ru.alarmtrade.devices.domain.errors.PeripheralInformationTimeoutException
import ru.alarmtrade.devices.domain.errors.PeripheralInformationUnknownException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.specialist.sdk.toResponseOrUnavailable
import ru.alarmtrade.specmp.feature.device.peripherals.data.datasource.PeripheralsDataSource
import ru.alarmtrade.specmp.feature.device.peripherals.data.mapper.pollTypes
import ru.alarmtrade.specmp.feature.device.peripherals.data.mapper.toSystemPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.data.mapper.withPeripheralFailure
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralPollStep
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.repository.SystemPeripheralsRepository

internal class SpecialistSystemPeripheralsRepository(
    private val dataSource: PeripheralsDataSource,
    private val typePause: Duration = TYPE_PAUSE,
    private val passPause: Duration = PASS_PAUSE,
) : SystemPeripheralsRepository {

    override fun poll(types: List<PeripheralType>): Flow<Response<PeripheralPollStep>> = flow {
        while (currentCoroutineContext().isActive) {
            types.forEachIndexed { index, type ->
                val devices = when (val read = read(type)) {
                    is Response.Success -> read.value

                    is Response.Error -> {
                        emit(read)
                        return@flow
                    }
                }
                emit(Response.Success(PeripheralPollStep(type, index, types.size, devices)))
                delay(typePause)
            }
            delay(passPause)
        }
    }

    /** `Success(null)` — тип не ответил (старый модуль или нет связи с устройством). */
    private suspend fun read(type: PeripheralType): Response<List<SystemPeripheral>?> {
        var answered = false
        val devices = mutableListOf<SystemPeripheral>()
        for (sdkType in type.pollTypes()) {
            when (val response = dataSource.readByType(sdkType).toResponseOrUnavailable()) {
                is Response.Success -> {
                    answered = true
                    devices += response.value.mapNotNull { it.toSystemPeripheral(type) }
                }

                is Response.Error -> if (!response.exception.isNoAnswer()) return response
            }
        }
        // RHM и сирена в одной группе: блок может вернуть одно устройство на оба запроса.
        return Response.Success(devices.distinctBy { it.address }.takeIf { answered })
    }

    override suspend fun blink(address: String): Response<Unit> =
        dataSource.blink(address).toResponseOrUnavailable().withPeripheralFailure()

    override suspend fun enterFirmwareMode(address: String): Response<Unit> =
        dataSource.setFirmwareMode(address).toResponseOrUnavailable().withPeripheralFailure()

    private companion object {
        val TYPE_PAUSE = 300.milliseconds
        val PASS_PAUSE = 1_500.milliseconds
    }
}

private fun Throwable.isNoAnswer(): Boolean =
    this is PeripheralInformationTimeoutException || this is PeripheralInformationUnknownException
