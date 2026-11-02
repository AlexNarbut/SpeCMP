package ru.alarmtrade.specmp.feature.device.state.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.state.domain.model.CommandReply
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState

/** Живое состояние блока и команды управления (SDK — только BLE). */
interface DeviceStateRepository {
    /**
     * Снимки состояния: холодный поток — опрос блока стартует при сборе и останавливается при его отмене.
     * Пустой стартовый снимок SDK пропускается; ошибки опроса — [Response.Error], поток не обрывают.
     */
    fun states(): Flow<Response<VehicleState>>

    /**
     * Команда управления. [StateCommand.PROGRAM_MODE_ON] / [StateCommand.PROGRAM_MODE_OFF] сюда не идут — это
     * `ProgramModeEntry` / `ProgramModeRepository` (PIN).
     */
    suspend fun send(command: StateCommand): Response<CommandReply>
}
