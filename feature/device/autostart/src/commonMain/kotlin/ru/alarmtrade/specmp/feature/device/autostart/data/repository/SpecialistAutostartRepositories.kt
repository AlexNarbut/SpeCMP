package ru.alarmtrade.specmp.feature.device.autostart.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import ru.alarmtrade.specialist.domain.errors.IncorrectServicePinException
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
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
internal class SpecialistBypassLearningRepository(
    private val bluetoothAlarmDeviceInteractor: () -> BluetoothAlarmDeviceInteractor,
) : BypassLearningRepository {

    override fun learn(): Flow<Response<BypassEvent>> = flow {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
        if (interactor == null) {
            emit(Response.Error(BypassLearningException()))
            return@flow
        }
        val events = safeFlowRun { interactor.learnBypassFlowUseCase.execute().map { it.toResponse() } }
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
internal class SpecialistCloneRepository(
    private val bluetoothAlarmDeviceInteractor: () -> BluetoothAlarmDeviceInteractor,
) : CloneRepository {

    override suspend fun attempts(): Response<Int?> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.getAvailableCloneAttemptsUseCase.execute().toResponse().map { it.int }
    }

    override fun run(): Flow<Response<CloneEvent>> = flow {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
        if (interactor == null) {
            emit(Response.Error(CloneProcedureException(CloneProcedureFailure.UNKNOWN)))
            return@flow
        }
        // Дошли ли до записи в блок: от этого зависит смысл итога «готово» (см. toEvent).
        var written = false
        val events = safeFlowRun { interactor.makeCloneProcedureFlowUseCase.execute().map { it.toResponse() } }
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
