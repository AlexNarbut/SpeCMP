package ru.alarmtrade.specmp.core.common

/** Результат операции domain/data-слоя; в UI — `asLCEState` (docs/patterns.md, «Response»). */
sealed class Response<out T> {
    data class Success<out T>(val value: T) : Response<T>()

    class Error(val exception: Throwable, val message: String? = exception.message) : Response<Nothing>()
}
