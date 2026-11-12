package ru.alarmtrade.specmp.feature.device.peripherals.domain.model

enum class PeripheralFailure {
    /** Сохранённый сервисный PIN не подошёл. */
    WRONG_PIN,

    /** Блок не пускает в режим программирования: на охране, включено зажигание или сервисный режим. */
    PROGRAM_MODE_REFUSED,

    /** Блок не поддерживает команду. */
    UNSUPPORTED,

    /** Блок не подтвердил команду. */
    NOT_CONFIRMED,
    UNKNOWN,
}

class PeripheralException(val failure: PeripheralFailure, cause: Throwable? = null) : Exception(failure.name, cause)

fun Throwable.toPeripheralFailure(): PeripheralFailure =
    (this as? PeripheralException)?.failure ?: PeripheralFailure.UNKNOWN
