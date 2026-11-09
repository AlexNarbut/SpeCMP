package ru.alarmtrade.specmp.feature.device.autostart.data.mapper

import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningError
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningStatus
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStep
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStepKind
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassSubject

/** Критичные ожидания (LADA CAN, LADA Vesta CAN, MED17, GTP): приложение передаёт данные, обрыв срывает обучение. */
@Suppress("CyclomaticComplexMethod") // таблица соответствий статус SDK → событие
internal fun BypassLearningState.toEvent(): BypassEvent = when (bypassStatus) {
    BypassLearningStatus.FINISH_SUCCESS -> BypassEvent.Finished(BypassResult.Success)

    BypassLearningStatus.FINISH_CLONE_REQUIRED -> BypassEvent.Finished(BypassResult.CloneRequired)

    BypassLearningStatus.FINISH_NOT_SUPPORTED -> BypassEvent.Finished(BypassResult.NotSupported)

    BypassLearningStatus.FINISH_ERROR -> {
        val error = bypassLearningError.toDomain()
        BypassEvent.Finished(
            BypassResult.Error(error, code = errorCode.takeIf { error == BypassError.VAG_ECU_EXCHANGE }),
        )
    }

    BypassLearningStatus.WAIT -> step(BypassStepKind.WAIT, BypassSubject.PREPARE)

    BypassLearningStatus.TURN_ON_IGNITION_REQUEST -> step(BypassStepKind.ACTION, BypassSubject.IGNITION_ON)

    BypassLearningStatus.TURN_ON_IGNITION_SUCCESS -> step(BypassStepKind.ACCEPTED, BypassSubject.IGNITION_ON)

    BypassLearningStatus.TURN_OFF_IGNITION_REQUEST -> step(BypassStepKind.ACTION, BypassSubject.IGNITION_OFF)

    BypassLearningStatus.TURN_OFF_IGNITION_SUCCESS -> step(BypassStepKind.ACCEPTED, BypassSubject.IGNITION_OFF)

    BypassLearningStatus.START_ENGINE_REQUEST -> step(BypassStepKind.ACTION, BypassSubject.ENGINE_START)

    BypassLearningStatus.START_ENGINE_SUCCESS -> step(BypassStepKind.ACCEPTED, BypassSubject.ENGINE_START)

    BypassLearningStatus.STOP_ENGINE_REQUEST -> step(BypassStepKind.ACTION, BypassSubject.ENGINE_STOP)

    BypassLearningStatus.STOP_ENGINE_SUCCESS -> step(BypassStepKind.ACCEPTED, BypassSubject.ENGINE_STOP)

    BypassLearningStatus.TURN_OFF_IGNITION_AND_REMOVE_KEY_REQUEST ->
        step(BypassStepKind.ACTION, BypassSubject.REMOVE_KEY)

    BypassLearningStatus.TURN_OFF_IGNITION_AND_REMOVE_KEY_SUCCESS ->
        step(BypassStepKind.ACCEPTED, BypassSubject.REMOVE_KEY)

    BypassLearningStatus.OPEN_HOOD_REQUEST -> step(BypassStepKind.ACTION, BypassSubject.HOOD_OPEN)

    BypassLearningStatus.OPEN_HOOD_SUCCESS -> step(BypassStepKind.ACCEPTED, BypassSubject.HOOD_OPEN)

    BypassLearningStatus.CLOSE_HOOD_REQUEST -> step(BypassStepKind.ACTION, BypassSubject.HOOD_CLOSE)

    BypassLearningStatus.CLOSE_HOOD_SUCCESS -> step(BypassStepKind.ACCEPTED, BypassSubject.HOOD_CLOSE)

    BypassLearningStatus.EXCHANGE_DATA_WITH_ECU_STARTED -> step(BypassStepKind.WAIT, BypassSubject.ECU_EXCHANGE)

    BypassLearningStatus.EXCHANGE_DATA_WITH_ECU_FINISHED ->
        step(BypassStepKind.WAIT, BypassSubject.ECU_EXCHANGE_FINISHED)

    BypassLearningStatus.LEARNING_PROCESS -> step(BypassStepKind.WAIT, BypassSubject.LEARNING)

    BypassLearningStatus.REQUEST_MED_17_5_25,
    BypassLearningStatus.REQUEST_MED_17_9_63,
    -> step(BypassStepKind.CRITICAL, BypassSubject.ALARM_EXCHANGE)

    BypassLearningStatus.GTP_AUTHORIZATION_ALGORITHM -> step(BypassStepKind.CRITICAL, BypassSubject.ECU_EXCHANGE)

    BypassLearningStatus.LADA_CAN_IMMO_BYPASS -> step(BypassStepKind.CRITICAL, BypassSubject.LADA_CAN)

    BypassLearningStatus.REQUEST_LADA_VESTA_CAN_PATCH ->
        step(BypassStepKind.CRITICAL, BypassSubject.LADA_VESTA_CAN)

    BypassLearningStatus.LADA_APS_IMMO_BYPASS -> step(BypassStepKind.INSTRUCTION, BypassSubject.LADA_APS)

    BypassLearningStatus.LEARN_TOYOTA_DAIHATSU -> step(BypassStepKind.INSTRUCTION, BypassSubject.TOYOTA_DAIHATSU)
}

private fun step(kind: BypassStepKind, subject: BypassSubject) = BypassEvent.Step(BypassStep(kind, subject))

@Suppress("CyclomaticComplexMethod")
internal fun BypassLearningError?.toDomain(): BypassError = when (this) {
    BypassLearningError.BYPASS_OPTION_IS_NOT_SELECTED -> BypassError.OPTION_NOT_SELECTED

    BypassLearningError.BYPASS_IS_NOT_SUPPORTED -> BypassError.NOT_SUPPORTED

    BypassLearningError.TOYOTA_MISSED_INFORMATION_FROM_EFI -> BypassError.TOYOTA_EFI

    BypassLearningError.TOYOTA_ANOTHER_REMOTE_START_MODULE_BLOCKS_DIAG_02 -> BypassError.TOYOTA_DIAG_BLOCKED

    BypassLearningError.TOYOTA_ANOTHER_REMOTE_START_MODULE_04 -> BypassError.TOYOTA_REMOTE_START_04

    BypassLearningError.TOYOTA_ANOTHER_REMOTE_START_MODULE_05 -> BypassError.TOYOTA_REMOTE_START_05

    BypassLearningError.TOYOTA_CHECK_RX_TX -> BypassError.TOYOTA_CHECK_RX_TX

    BypassLearningError.KIA_IGNITION_STATUS_TIMEOUT -> BypassError.KIA_IGNITION_TIMEOUT

    BypassLearningError.KIA_OLD_ALGORITHM_IS_SELECTED_02 -> BypassError.KIA_OLD_ALGORITHM_02

    BypassLearningError.KIA_OLD_ALGORITHM_IS_SELECTED_03 -> BypassError.KIA_OLD_ALGORITHM_03

    BypassLearningError.KIA_NEW_ALGORITHM_IS_SELECTED -> BypassError.KIA_NEW_ALGORITHM

    BypassLearningError.KIA_IMMOBILIZER_IS_OFF -> BypassError.KIA_IMMOBILIZER_OFF

    BypassLearningError.VAG_CAN_BUS_CONNECTION_ERROR -> BypassError.VAG_CAN_BUS

    BypassLearningError.VAG_OLD_ALGORITHM_NOT_SUPPORTED_02 -> BypassError.VAG_OLD_ALGORITHM_02

    BypassLearningError.VAG_OLD_ALGORITHM_NOT_SUPPORTED_03 -> BypassError.VAG_OLD_ALGORITHM_03

    BypassLearningError.VAG_NEW_ALGORITHM_NOT_SUPPORTED -> BypassError.VAG_NEW_ALGORITHM

    BypassLearningError.VAG_BYPASS_ERROR -> BypassError.VAG_ECU_EXCHANGE

    BypassLearningError.MERCEDES_DRIVER_DOOR_NOT_OPEN -> BypassError.MERCEDES_DRIVER_DOOR

    BypassLearningError.MERCEDES_NO_RESPONSE_FROM_CAR_UNITS_02 -> BypassError.MERCEDES_NO_RESPONSE_02

    BypassLearningError.MERCEDES_NO_RESPONSE_FROM_CAR_UNITS_04 -> BypassError.MERCEDES_NO_RESPONSE_04

    BypassLearningError.MERCEDES_KEY_IZS_ERROR -> BypassError.MERCEDES_KEY_IZS

    BypassLearningError.MERCEDES_KEY_WAS_ALREADY_USED -> BypassError.MERCEDES_KEY_USED

    BypassLearningError.MERCEDES_MISSED_VIN_CODE -> BypassError.MERCEDES_NO_VIN

    BypassLearningError.MERCEDES_NO_RESPONSE_FROM_CAR_UNITS_0F -> BypassError.MERCEDES_NO_RESPONSE_0F

    BypassLearningError.LADA_CAN_BUS_CONNECTION_ERROR -> BypassError.LADA_CAN_BUS

    BypassLearningError.LADA_REPEAT_LEARNING -> BypassError.LADA_REPEAT

    BypassLearningError.LADA_VESTA_CAN_PATCH_SEND -> BypassError.LADA_VESTA_CAN_PATCH_SEND

    // Без своего текста (в т.ч. новые в SDK): общая ошибка «проверьте настройки».
    BypassLearningError.DEFAULT,
    BypassLearningError.NO_ERROR,
    BypassLearningError.MED_TABLE_SEND,
    BypassLearningError.GTP_CALCULATE_NOT_VALID,
    BypassLearningError.GTP_SENDING_DATA,
    null,
    -> BypassError.GENERAL
}
