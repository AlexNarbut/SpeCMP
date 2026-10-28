package ru.alarmtrade.specmp.core.common

/** Категория сбоя связи с сервером — то, что различает пользователь. */
enum class NetworkFailure {
    /** Сети нет: устройство офлайн, адрес не разрешился, соединение отклонено или оборвалось. */
    NoConnection,

    /** Сервер не ответил вовремя. */
    Timeout,

    /** Сервер ответил ошибкой или ответ не разобрался. */
    ServerError
}

/**
 * Сбой связи с сервером, общий для всех фич. Источник (SDK, ktor, платформа) приводится к нему
 * на границе data-слоя; исходная ошибка — в [cause].
 */
class NetworkException(val failure: NetworkFailure, cause: Throwable) : Exception("Network failure: $failure", cause)

/** Категория сбоя связи, если ошибка — [NetworkException]; иначе null. */
val Throwable.networkFailure: NetworkFailure?
    get() = (this as? NetworkException)?.failure
