package ru.alarmtrade.specmp.feature.device.state.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.commands.ControlCommandResponse
import ru.alarmtrade.devices.domain.alarmDevice.state.DeviceState
import ru.alarmtrade.devices.interactors.alarmDeviceInteractor.requests.SendControlCommandRequest
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе.
internal interface DeviceStateDataSource {
    suspend fun startRequestingState(): SpecialistResponse<Unit>?
    suspend fun stopRequestingState(): SpecialistResponse<Unit>?
    fun telemetry(): Flow<SpecialistResponse<DeviceState>>?
    suspend fun sendCommand(request: SendControlCommandRequest): SpecialistResponse<ControlCommandResponse>?
}

internal class SpecialistDeviceStateDataSource(private val alarmDevice: () -> BluetoothAlarmDeviceInteractor) :
    DeviceStateDataSource {
    override suspend fun startRequestingState(): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.startRequestingDeviceStateUseCase?.execute()

    override suspend fun stopRequestingState(): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.stopRequestingDeviceStateUseCase?.execute()

    override fun telemetry(): Flow<SpecialistResponse<DeviceState>>? = alarmDevice.resolveOrNull()?.let { interactor ->
        flow { emitAll(interactor.getDeviceTelemetryStateFlowUseCase.execute()) }
    }

    override suspend fun sendCommand(request: SendControlCommandRequest): SpecialistResponse<ControlCommandResponse>? =
        alarmDevice.resolveOrNull()?.sendControlCommandUseCase?.execute(request)
}
