package ru.alarmtrade.specmp.feature.device.state.data.repository

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import ru.alarmtrade.common.core.response.Response as SdkResponse
import ru.alarmtrade.devices.domain.alarmDevice.commands.AlarmControlCommand
import ru.alarmtrade.devices.domain.alarmDevice.commands.ArmCommand
import ru.alarmtrade.devices.domain.alarmDevice.commands.BeepCommand
import ru.alarmtrade.devices.domain.alarmDevice.commands.DisarmCommand
import ru.alarmtrade.devices.domain.alarmDevice.commands.OpenTrunkCommand
import ru.alarmtrade.devices.domain.alarmDevice.commands.ServiceModeCommand
import ru.alarmtrade.devices.domain.alarmDevice.commands.StartEngineCommand
import ru.alarmtrade.devices.domain.alarmDevice.commands.StopEngineCommand
import ru.alarmtrade.devices.domain.alarmDevice.commands.TurnOffHeaterCommand
import ru.alarmtrade.devices.domain.alarmDevice.commands.TurnOnHeaterCommand
import ru.alarmtrade.devices.domain.alarmDevice.commands.TurnOnLightCommand
import ru.alarmtrade.devices.domain.alarmDevice.state.DeviceState
import ru.alarmtrade.devices.interactors.alarmDeviceInteractor.requests.SendControlCommandRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.core.specialist.sdk.toResponseOrUnavailable
import ru.alarmtrade.specmp.feature.device.state.data.datasource.DeviceStateDataSource
import ru.alarmtrade.specmp.feature.device.state.data.mapper.toVehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.CommandReply
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.repository.DeviceStateRepository

/**
 * [DeviceStateRepository]: поток телеметрии блока, пока идёт опрос (период 3 с задан в SDK), и команды управления.
 */
internal class SpecialistDeviceStateRepository(private val dataSource: DeviceStateDataSource) : DeviceStateRepository {

    override fun states(): Flow<Response<VehicleState>> = flow {
        val started = dataSource.startRequestingState().toResponseOrUnavailable()
        if (started is Response.Error) {
            emit(started)
            return@flow
        }
        try {
            safeFlowRun { dataSource.telemetry()?.toProjectFlow() ?: flowOf(interactorUnavailable()) }
                .collect(::emit)
        } finally {
            // Опрос — ресурс блока: останавливаем и при отмене сбора (уход с экрана, потеря связи).
            withContext(NonCancellable) { dataSource.stopRequestingState() }
        }
    }

    override suspend fun send(command: StateCommand): Response<CommandReply> {
        val sdkCommand = command.toSdk() ?: return Response.Error(IllegalArgumentException("Not a control command"))
        return dataSource.sendCommand(SendControlCommandRequest(sdkCommand)).toResponseOrUnavailable()
            .map { if (it.isSuccess) CommandReply.DONE else CommandReply.REJECTED }
    }
}

private fun Flow<SdkResponse<DeviceState>>.toProjectFlow(): Flow<Response<VehicleState>> = mapNotNull { sdk ->
    when (val response = sdk.toResponse()) {
        is Response.Success -> response.value.toVehicleState()?.let { Response.Success(it) }
        is Response.Error -> response
    }
}

private fun StateCommand.toSdk(): AlarmControlCommand? = when (this) {
    StateCommand.ARM -> ArmCommand()
    StateCommand.DISARM -> DisarmCommand()
    StateCommand.START_ENGINE, StateCommand.EXTEND_AUTOSTART -> StartEngineCommand()
    StateCommand.STOP_ENGINE -> StopEngineCommand()
    StateCommand.OPEN_TRUNK -> OpenTrunkCommand()
    StateCommand.HEATER_ON -> TurnOnHeaterCommand()
    StateCommand.HEATER_OFF -> TurnOffHeaterCommand()
    StateCommand.SERVICE_MODE_ON, StateCommand.SERVICE_MODE_OFF -> ServiceModeCommand()
    StateCommand.PANIC -> BeepCommand()
    StateCommand.LIGHT_WAY -> TurnOnLightCommand()
    StateCommand.PROGRAM_MODE_ON, StateCommand.PROGRAM_MODE_OFF -> null
}
