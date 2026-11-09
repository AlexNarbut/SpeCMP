package ru.alarmtrade.specmp.feature.device.autostart.domain.model

/** @property locked идёт запись/проверка в блоке — выйти нельзя (прерывание может стоить попытки). */
enum class CloneStage(val locked: Boolean = false) {
    PREPARING,
    READING,
    CALCULATING,
    WRITING(locked = true),
    VERIFYING(locked = true),
    ;

    val number: Int get() = ordinal + 1

    companion object {
        val COUNT: Int = entries.size
    }
}

/** @property retryable есть ли смысл в «Повторить»; иначе — только чат поддержки и «Закрыть». */
enum class CloneFailure(val retryable: Boolean = true) {
    UNKNOWN,
    READING,
    WRITING,
    VERIFYING,

    REQUIRED,
    DATA,
    ENCRYPT,
    ALGORITHM,
    CALC_ERROR,
    ALGORITHM_ERROR,
    WRONG_REQUEST,
    DEBUG,
    UNSUPPORTED(retryable = false),
    WRONG_DEVICE_ID(retryable = false),
    ATTEMPTS_EXCEEDED(retryable = false),
    DEVICE_DENIED(retryable = false),
}

sealed interface CloneResult {
    /** @property already Clone был выполнен раньше: блок сразу ответил «готово», попытка не потрачена. */
    data class Completed(val already: Boolean) : CloneResult

    data object NotSupported : CloneResult

    /** @property tips советы сервера (произвольные строки, без перевода). */
    data class Failed(val failure: CloneFailure, val tips: List<String> = emptyList()) : CloneResult
}

sealed interface CloneEvent {
    /** @property inQueue на [CloneStage.CALCULATING]: задача ещё в очереди сервера (иначе — обрабатывается). */
    data class Stage(val stage: CloneStage, val inQueue: Boolean = false) : CloneEvent

    data class Finished(val result: CloneResult) : CloneEvent
}

enum class CloneProcedureFailure {
    WRONG_PIN,

    /** Сбой SDK без статуса; обрыв связи или ошибка — решает ViewModel по состоянию сессии. */
    UNKNOWN,
}

class CloneProcedureException(val failure: CloneProcedureFailure, cause: Throwable? = null) :
    Exception("Clone procedure failed: $failure", cause)

fun Throwable.toCloneProcedureFailure(): CloneProcedureFailure =
    (this as? CloneProcedureException)?.failure ?: CloneProcedureFailure.UNKNOWN
