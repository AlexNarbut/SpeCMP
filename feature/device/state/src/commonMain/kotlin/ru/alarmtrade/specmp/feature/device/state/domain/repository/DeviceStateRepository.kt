package ru.alarmtrade.specmp.feature.device.state.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.state.domain.model.CommandReply
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState

interface DeviceStateRepository {
    /** Холодный поток: опрос стартует при сборе и останавливается при отмене; ошибки опроса поток не обрывают. */
    fun states(): Flow<Response<VehicleState>>

    /**
     * Команда управления. [StateCommand.PROGRAM_MODE_ON] / [StateCommand.PROGRAM_MODE_OFF] сюда не идут — это
     * `ProgramModeEntry` / `ProgramModeRepository` (PIN).
     */
    suspend fun send(command: StateCommand): Response<CommandReply>
}
