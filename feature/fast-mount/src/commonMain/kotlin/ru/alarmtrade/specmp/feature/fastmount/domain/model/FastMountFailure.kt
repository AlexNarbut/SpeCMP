package ru.alarmtrade.specmp.feature.fastmount.domain.model

/** Предметная ошибка мастера (ошибки SDK `FastMountErrors`); общие ошибки (сеть, связь) — `AppError`. */
sealed interface FastMountFailure {
    /** Сессии монтажа нет (не стартовала или потеряна) — начать заново. */
    data object SessionLost : FastMountFailure

    /** Обход не закончился успехом или «нужен Clone» — «Далее» невозможно. */
    data object BypassNotFinished : FastMountFailure

    data object CloneNotFinished : FastMountFailure

    data object ChecksNotDone : FastMountFailure

    data object FeedbackRequired : FastMountFailure

    /** Шаг не работает на этом транспорте (USB: Slave, проверка системы). */
    data object TransportNotSupported : FastMountFailure
}

/** Исключение репозитория с предметной ошибкой — лежит в `Response.Error.exception`. */
class FastMountException(val failure: FastMountFailure, cause: Throwable? = null) :
    Exception(failure.toString(), cause)

/** Класс подключённого блока для быстрого монтажа: сейчас поддерживаются только сигнализации. */
enum class MountDeviceClass {
    ALARM,

    /** Позже — свой мастер; пока не поддерживается. */
    MICROBOT,
    OTHER
}
