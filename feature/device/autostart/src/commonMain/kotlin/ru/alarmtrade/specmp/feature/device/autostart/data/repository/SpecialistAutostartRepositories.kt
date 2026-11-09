package ru.alarmtrade.specmp.feature.device.autostart.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import ru.alarmtrade.specialist.domain.errors.IncorrectServicePinException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.core.specialist.sdk.toResponseOrUnavailable
import ru.alarmtrade.specmp.feature.device.autostart.data.datasource.AutostartDataSource
import ru.alarmtrade.specmp.feature.device.autostart.data.mapper.isWriting
import ru.alarmtrade.specmp.feature.device.autostart.data.mapper.toEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassLearningException
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneProcedureException
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneProcedureFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.BypassLearningRepository
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.CloneRepository

/**
 * [BypassLearningRepository] над `learnBypassFlowUseCase` (SDK сам пишет лог событий обучения на сервер).
 * Поток кончается на итоге или на первой ошибке SDK (`BypassLearningException`).
 */
internal class SpecialistBypassLearningRepository(private val dataSource: AutostartDataSource) :
    BypassLearningRepository {

    override fun learn(): Flow<Response<BypassEvent>> = flow {
        val source = dataSource.learnBypass()
        if (source == null) {
            emit(Response.Error(BypassLearningException()))
            return@flow
        }
        val events = safeFlowRun { source.map { it.toResponse() } }
            .transformWhile { response ->
                when (response) {
                    is Response.Success -> emit(Response.Success(response.value.toEvent()))
                    is Response.Error -> emit(Response.Error(BypassLearningException(response.exception)))
                }
                response is Response.Success
            }
        emitAll(events)
    }
}

/** [CloneRepository] над `makeCloneProcedureFlowUseCase` и `getAvailableCloneAttemptsUseCase`. */
internal class SpecialistCloneRepository(private val dataSource: AutostartDataSource) : CloneRepository {

    override suspend fun attempts(): Response<Int?> = dataSource.cloneAttempts().toResponseOrUnavailable().map {
        it.int
    }

    override fun run(): Flow<Response<CloneEvent>> = flow {
        val source = dataSource.cloneProcedure()
        if (source == null) {
            emit(Response.Error(CloneProcedureException(CloneProcedureFailure.UNKNOWN)))
            return@flow
        }
        // Дошли ли до записи в блок: от этого зависит смысл итога «готово» (см. toEvent).
        var written = false
        val events = safeFlowRun { source.map { it.toResponse() } }
            .transformWhile { response ->
                when (response) {
                    is Response.Success -> {
                        val event = response.value.toEvent(written)
                        written = written || event.isWriting
                        emit(Response.Success(event))
                    }

                    is Response.Error -> emit(Response.Error(response.exception.toCloneProcedureException()))
                }
                response is Response.Success
            }
        emitAll(events)
    }

    private fun Throwable.toCloneProcedureException(): CloneProcedureException {
        val failure = when (this) {
            is IncorrectServicePinException -> CloneProcedureFailure.WRONG_PIN
            else -> CloneProcedureFailure.UNKNOWN
        }
        return CloneProcedureException(failure, this)
    }
}
