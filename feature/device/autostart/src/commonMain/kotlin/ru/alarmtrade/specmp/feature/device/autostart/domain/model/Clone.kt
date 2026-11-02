package ru.alarmtrade.specmp.feature.device.autostart.domain.model

/**
 * Этап процедуры Clone (на экране — «Этап N из 5»).
 *
 * @property locked на этапе идёт запись/проверка в блоке — выйти нельзя (прерывание может стоить попытки).
 */
enum class CloneStage(val locked: Boolean = false) {
    PREPARING,
    READING,
    CALCULATING,
    WRITING(locked = true),
    VERIFYING(locked = true);

    /** Номер этапа с 1. */
    val number: Int get() = ordinal + 1

    companion object {
        val COUNT: Int = entries.size
    }
}

/**
 * Причина неудачи Clone.
 *
 * @property retryable есть ли смысл в «Повторить»: на остальных — только чат поддержки и «Закрыть».
 */
enum class CloneFailure(val retryable: Boolean = true) {
    /** Сбой SDK без статуса (поток оборвался, нет сети и т. п.). */
    UNKNOWN,
    READING,
    WRITING,
    VERIFYING,

    /** Блок по-прежнему требует Clone после процедуры. */
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
    DEVICE_DENIED(retryable = false)
}

/** Итог процедуры Clone. */
sealed interface CloneResult {
    /** @property already Clone был выполнен раньше: блок сразу ответил «готово», попытка не потрачена. */
    data class Completed(val already: Boolean) : CloneResult

    data object NotSupported : CloneResult

    /** @property tips советы сервера (произвольные строки, без перевода). */
    data class Failed(val failure: CloneFailure, val tips: List<String> = emptyList()) : CloneResult
}

/** Событие процедуры Clone. */
sealed interface CloneEvent {
    /** @property inQueue на [CloneStage.CALCULATING]: задача ещё в очереди сервера (иначе — обрабатывается). */
    data class Stage(val stage: CloneStage, val inQueue: Boolean = false) : CloneEvent

    data class Finished(val result: CloneResult) : CloneEvent

    /** PIN программирования неверный (BLE): нужен ввод PIN и повтор. */
    data object WrongPin : CloneEvent

    /** Сбой SDK без статуса; обрыв связи или ошибка — решает ViewModel по состоянию сессии. */
    data object Failure : CloneEvent
}
