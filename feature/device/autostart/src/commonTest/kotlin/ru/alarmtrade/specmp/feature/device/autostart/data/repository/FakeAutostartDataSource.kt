package ru.alarmtrade.specmp.feature.device.autostart.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.IntWrapper
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneBypassState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneProcedureState
import ru.alarmtrade.specmp.feature.device.autostart.data.datasource.AutostartDataSource

internal class FakeAutostartDataSource : AutostartDataSource {
    var bypass: Flow<SpecialistResponse<BypassLearningState>>? = emptyFlow()
    var attempts: SpecialistResponse<IntWrapper>? = null
    var clone: Flow<SpecialistResponse<CloneProcedureState>>? = emptyFlow()
    var state: SpecialistResponse<CloneBypassState>? = null

    override fun learnBypass(): Flow<SpecialistResponse<BypassLearningState>>? = bypass

    override suspend fun cloneAttempts(): SpecialistResponse<IntWrapper>? = attempts

    override fun cloneProcedure(): Flow<SpecialistResponse<CloneProcedureState>>? = clone

    override suspend fun cloneBypassState(): SpecialistResponse<CloneBypassState>? = state
}
