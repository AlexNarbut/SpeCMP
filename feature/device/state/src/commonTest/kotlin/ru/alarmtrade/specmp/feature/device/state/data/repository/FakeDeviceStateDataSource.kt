package ru.alarmtrade.specmp.feature.device.state.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.commands.ControlCommandResponse
import ru.alarmtrade.devices.domain.alarmDevice.state.DeviceState
import ru.alarmtrade.devices.interactors.alarmDeviceInteractor.requests.SendControlCommandRequest
import ru.alarmtrade.specmp.feature.device.state.data.datasource.DeviceStateDataSource

internal class FakeDeviceStateDataSource : DeviceStateDataSource {
    var start: SpecialistResponse<Unit>? = SpecialistResponse.Success(Unit)
    var states: Flow<SpecialistResponse<DeviceState>>? = emptyFlow()
    var reply: SpecialistResponse<ControlCommandResponse>? = null

    val calls = mutableListOf<String>()
    val commands = mutableListOf<SendControlCommandRequest>()

    override suspend fun startRequestingState(): SpecialistResponse<Unit>? {
        calls += "start"
        return start
    }

    override suspend fun stopRequestingState(): SpecialistResponse<Unit>? {
        calls += "stop"
        return SpecialistResponse.Success(Unit)
    }

    override fun telemetry(): Flow<SpecialistResponse<DeviceState>>? = states

    override suspend fun sendCommand(request: SendControlCommandRequest): SpecialistResponse<ControlCommandResponse>? {
        commands += request
        return reply
    }
}
