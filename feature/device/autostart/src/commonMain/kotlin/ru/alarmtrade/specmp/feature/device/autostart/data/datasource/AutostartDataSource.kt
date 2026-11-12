package ru.alarmtrade.specmp.feature.device.autostart.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.IntWrapper
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneBypassState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneProcedureState
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе.
internal interface AutostartDataSource {
    fun learnBypass(): Flow<SpecialistResponse<BypassLearningState>>?
    suspend fun cloneAttempts(): SpecialistResponse<IntWrapper>?
    fun cloneProcedure(): Flow<SpecialistResponse<CloneProcedureState>>?
    suspend fun cloneBypassState(): SpecialistResponse<CloneBypassState>?
}

internal class SpecialistAutostartDataSource(private val alarmDevice: () -> BluetoothAlarmDeviceInteractor) :
    AutostartDataSource {
    override fun learnBypass(): Flow<SpecialistResponse<BypassLearningState>>? =
        alarmDevice.resolveOrNull()?.let { interactor -> flow { emitAll(interactor.learnBypassFlowUseCase.execute()) } }

    override suspend fun cloneAttempts(): SpecialistResponse<IntWrapper>? =
        alarmDevice.resolveOrNull()?.getAvailableCloneAttemptsUseCase?.execute()

    override fun cloneProcedure(): Flow<SpecialistResponse<CloneProcedureState>>? =
        alarmDevice.resolveOrNull()?.let { interactor ->
            flow { emitAll(interactor.makeCloneProcedureFlowUseCase.execute()) }
        }

    override suspend fun cloneBypassState(): SpecialistResponse<CloneBypassState>? =
        alarmDevice.resolveOrNull()?.getCloneBypassStateUseCase?.execute()
}
