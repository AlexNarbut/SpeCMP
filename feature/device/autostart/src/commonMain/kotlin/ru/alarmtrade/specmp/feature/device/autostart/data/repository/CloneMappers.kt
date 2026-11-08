package ru.alarmtrade.specmp.feature.device.autostart.data.repository

import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneCalculatingError
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneProcedureState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneProcedureStatus
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneStatus
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage

/**
 * Состояние процедуры SDK → событие. [written] — поток уже дошёл до записи в блок: `COMPLETED` до записи значит,
 * что Clone был выполнен раньше (блок сразу ответил «готово»), после — что процедура выполнена сейчас (D7).
 */
@Suppress("CyclomaticComplexMethod")
internal fun CloneProcedureState.toEvent(written: Boolean): CloneEvent = when (status) {
    CloneProcedureStatus.PREPARING -> CloneEvent.Stage(CloneStage.PREPARING)

    CloneProcedureStatus.READING_FROM_ALARM -> CloneEvent.Stage(CloneStage.READING)

    CloneProcedureStatus.CALCULATING_NEW -> CloneEvent.Stage(CloneStage.CALCULATING, inQueue = true)

    CloneProcedureStatus.CALCULATING_WORK -> CloneEvent.Stage(CloneStage.CALCULATING)

    CloneProcedureStatus.CALCULATED,
    CloneProcedureStatus.WRITING_TO_ALARM_DEVICE,
    -> CloneEvent.Stage(CloneStage.WRITING)

    CloneProcedureStatus.READING_CONFIRMATION_FROM_ALARM_DEVICE -> CloneEvent.Stage(CloneStage.VERIFYING)

    CloneProcedureStatus.COMPLETED -> CloneEvent.Finished(cloneResult.toResult(written))

    CloneProcedureStatus.CALCULATING_ERROR ->
        CloneEvent.Finished(CloneResult.Failed(calculatingError.toFailure(), tips.orEmpty().filter(String::isNotBlank)))

    CloneProcedureStatus.READING_ERROR -> CloneEvent.Finished(CloneResult.Failed(CloneFailure.READING))

    CloneProcedureStatus.WRITING_ERROR -> CloneEvent.Finished(CloneResult.Failed(CloneFailure.WRITING))

    CloneProcedureStatus.READING_CONFIRMATION_ERROR -> CloneEvent.Finished(CloneResult.Failed(CloneFailure.VERIFYING))
}

/** Стадия, после которой «готово» от блока означает выполненную сейчас процедуру. */
internal val CloneEvent.isWriting: Boolean
    get() = this is CloneEvent.Stage && stage.locked

private fun CloneStatus.toResult(written: Boolean): CloneResult = when (this) {
    CloneStatus.OK,
    CloneStatus.COMPLETED,
    -> CloneResult.Completed(already = !written)

    CloneStatus.NOT_SUPPORTED -> CloneResult.NotSupported

    CloneStatus.REQUIRED -> CloneResult.Failed(CloneFailure.REQUIRED)

    CloneStatus.ERROR_DATA -> CloneResult.Failed(CloneFailure.DATA)

    CloneStatus.ERROR_ENCRYPT -> CloneResult.Failed(CloneFailure.ENCRYPT)

    CloneStatus.ERROR_ALGORITHM -> CloneResult.Failed(CloneFailure.ALGORITHM)

    CloneStatus.UNDEFINED -> CloneResult.Failed(CloneFailure.UNKNOWN)
}

private fun CloneCalculatingError.toFailure(): CloneFailure = when (this) {
    CloneCalculatingError.UNSUPPORTED -> CloneFailure.UNSUPPORTED

    CloneCalculatingError.WRONG_DEVICE_ID -> CloneFailure.WRONG_DEVICE_ID

    CloneCalculatingError.ALGORITHM_ERROR -> CloneFailure.ALGORITHM_ERROR

    CloneCalculatingError.CLONE_ATTEMPTS_EXCEEDED -> CloneFailure.ATTEMPTS_EXCEEDED

    CloneCalculatingError.WRONG_REQUEST -> CloneFailure.WRONG_REQUEST

    CloneCalculatingError.CALC_ERROR -> CloneFailure.CALC_ERROR

    CloneCalculatingError.DEVICE_DENIED -> CloneFailure.DEVICE_DENIED

    CloneCalculatingError.DEBUG -> CloneFailure.DEBUG

    CloneCalculatingError.UNKNOWN_ERROR,
    CloneCalculatingError.NO_ERROR,
    -> CloneFailure.UNKNOWN
}
