package ru.alarmtrade.specmp.feature.device.autostart.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specialist.domain.errors.IncorrectServicePinException
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.BypassLearningRepository
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.CloneRepository

/**
 * [BypassLearningRepository] над `learnBypassFlowUseCase` (SDK сам пишет лог событий обучения на сервер).
 * Поток кончается на итоге или сбое; ошибка SDK — [BypassEvent.Failure].
 */
class SpecialistBypassLearningRepository(
    private val bluetoothAlarmDeviceInteractor: () -> BluetoothAlarmDeviceInteractor
) : BypassLearningRepository {

    override fun learn(): Flow<BypassEvent> = flow {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
        if (interactor == null) {
            emit(BypassEvent.Failure)
            return@flow
        }
        safeFlowRun { interactor.learnBypassFlowUseCase.execute().map { it.toResponse() } }
            .collect { response ->
                val event = when (response) {
                    is Response.Success -> response.value.toEvent()
                    is Response.Error -> BypassEvent.Failure
                }
                emit(event)
            }
    }
}

/** [CloneRepository] над `makeCloneProcedureFlowUseCase` и `getAvailableCloneAttemptsUseCase`. */
class SpecialistCloneRepository(private val bluetoothAlarmDeviceInteractor: () -> BluetoothAlarmDeviceInteractor) :
    CloneRepository {

    override suspend fun attempts(): Int? {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull() ?: return null
        return when (val response = interactor.getAvailableCloneAttemptsUseCase.execute().toResponse()) {
            is Response.Success -> response.value.int
            is Response.Error -> null
        }
    }

    override fun run(): Flow<CloneEvent> = flow {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
        if (interactor == null) {
            emit(CloneEvent.Failure)
            return@flow
        }
        // Дошли ли до записи в блок: от этого зависит смысл итога «готово» (см. toEvent).
        var written = false
        safeFlowRun { interactor.makeCloneProcedureFlowUseCase.execute().map { it.toResponse() } }
            .collect { response ->
                val event = when (response) {
                    is Response.Success -> response.value.toEvent(written)

                    is Response.Error -> when (response.exception) {
                        is IncorrectServicePinException -> CloneEvent.WrongPin
                        else -> CloneEvent.Failure
                    }
                }
                written = written || event.isWriting
                emit(event)
            }
    }
}
