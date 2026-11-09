package ru.alarmtrade.specmp.feature.device.autostart.domain.model

/**
 * Вид шага обучения бесключевому обходу — от него зависят цвет карточки, метка и подпись полосы статуса.
 */
enum class BypassStepKind {
    /** Блок ждёт действия монтажника («Включите зажигание»). */
    ACTION,

    /** Блок увидел действие («Зажигание включено. Ожидайте»). */
    ACCEPTED,

    /** Блок работает сам, монтажнику — ждать. */
    WAIT,

    /** Обмен данными через приложение: связь прерывать нельзя («Не прерывайте соединение»). */
    CRITICAL,

    /** Инструкция из нескольких пунктов: монтажник выполняет её сам, блок ждёт. */
    INSTRUCTION
}

/** О чём шаг: определяет текст и иллюстрацию. */
enum class BypassSubject {
    PREPARE,
    IGNITION_ON,
    IGNITION_OFF,
    ENGINE_START,
    ENGINE_STOP,
    REMOVE_KEY,
    HOOD_OPEN,
    HOOD_CLOSE,
    ECU_EXCHANGE,
    ECU_EXCHANGE_FINISHED,
    ALARM_EXCHANGE,
    LEARNING,
    LADA_APS,
    LADA_CAN,
    LADA_VESTA_CAN,
    TOYOTA_DAIHATSU
}

/** Текущий шаг обучения. */
data class BypassStep(val kind: BypassStepKind, val subject: BypassSubject)

/**
 * Причина ошибки обучения — по `BypassLearningError` SDK. Ошибки без своего текста (новые в SDK, `NO_ERROR`,
 * `DEFAULT`) — [GENERAL]. [START_FAILED] — сбой SDK без статуса (не вошли в режим программирования, оборвался поток).
 */
enum class BypassError {
    GENERAL,
    START_FAILED,
    OPTION_NOT_SELECTED,
    NOT_SUPPORTED,
    TOYOTA_EFI,
    TOYOTA_DIAG_BLOCKED,
    TOYOTA_REMOTE_START_04,
    TOYOTA_REMOTE_START_05,
    TOYOTA_CHECK_RX_TX,
    KIA_IGNITION_TIMEOUT,
    KIA_OLD_ALGORITHM_02,
    KIA_OLD_ALGORITHM_03,
    KIA_NEW_ALGORITHM,
    KIA_IMMOBILIZER_OFF,
    VAG_CAN_BUS,
    VAG_OLD_ALGORITHM_02,
    VAG_OLD_ALGORITHM_03,
    VAG_NEW_ALGORITHM,
    VAG_ECU_EXCHANGE,
    MERCEDES_DRIVER_DOOR,
    MERCEDES_NO_RESPONSE_02,
    MERCEDES_NO_RESPONSE_04,
    MERCEDES_KEY_IZS,
    MERCEDES_KEY_USED,
    MERCEDES_NO_VIN,
    MERCEDES_NO_RESPONSE_0F,
    LADA_CAN_BUS,
    LADA_REPEAT,
    LADA_VESTA_CAN_PATCH_SEND
}

/** Итог обучения. */
sealed interface BypassResult {
    data object Success : BypassResult

    /** Обучение завершено, но для автозапуска нужна процедура Clone. */
    data object CloneRequired : BypassResult

    data object NotSupported : BypassResult

    /** @property code код ошибки блока (только у [BypassError.VAG_ECU_EXCHANGE]). */
    data class Error(val error: BypassError, val code: Int? = null) : BypassResult
}

/** Событие потока обучения. */
sealed interface BypassEvent {
    data class Step(val step: BypassStep) : BypassEvent

    data class Finished(val result: BypassResult) : BypassEvent

    /** Сбой SDK без статуса; что это — обрыв связи или ошибка, решает ViewModel по состоянию сессии. */
    data object Failure : BypassEvent
}
