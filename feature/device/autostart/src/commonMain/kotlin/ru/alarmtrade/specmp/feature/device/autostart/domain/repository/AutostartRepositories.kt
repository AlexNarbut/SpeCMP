package ru.alarmtrade.specmp.feature.device.autostart.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.AutostartState
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent

interface BypassLearningRepository {
    /** Блок должен быть в режиме программирования (`ProgramModeRepository.enter`). */
    fun learn(): Flow<Response<BypassEvent>>
}

interface CloneRepository {
    suspend fun attempts(): Response<Int?>

    /** Режим программирования SDK включает сам; неверный PIN — `CloneProcedureFailure.WRONG_PIN`. */
    fun run(): Flow<Response<CloneEvent>>
}

/** Читает состояние без режима программирования. */
interface AutostartStatusRepository {
    suspend fun status(): Response<AutostartState>
}
