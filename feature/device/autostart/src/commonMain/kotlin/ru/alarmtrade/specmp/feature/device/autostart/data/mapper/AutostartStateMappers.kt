package ru.alarmtrade.specmp.feature.device.autostart.data.mapper

import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningStatus
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneBypassState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneStatus
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.AutostartState
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneCondition

// Отдельного флага «обучен» блок не даёт — только итог последнего обучения.
internal fun CloneBypassState.toAutostartState(): AutostartState = AutostartState(
    bypassLearned = bypassLearningState.bypassStatus in LearnedStatuses,
    clone = cloneStatus.toCondition(),
)

private val LearnedStatuses = setOf(BypassLearningStatus.FINISH_SUCCESS, BypassLearningStatus.FINISH_CLONE_REQUIRED)

private fun CloneStatus.toCondition(): CloneCondition = when (this) {
    CloneStatus.NOT_SUPPORTED -> CloneCondition.NOT_SUPPORTED

    CloneStatus.REQUIRED -> CloneCondition.REQUIRED

    CloneStatus.COMPLETED,
    CloneStatus.OK,
    -> CloneCondition.COMPLETED

    CloneStatus.ERROR_DATA,
    CloneStatus.ERROR_ENCRYPT,
    CloneStatus.ERROR_ALGORITHM,
    CloneStatus.UNDEFINED,
    -> CloneCondition.FAILED
}
