package ru.alarmtrade.specmp.core.ui.lce

import ru.alarmtrade.specmp.core.common.Response

inline fun <T, E> Response<T>.asLCEState(toError: (Throwable) -> E): LCEState<T, E> = when (this) {
    is Response.Success -> LCEState.Content(value)
    is Response.Error -> LCEState.Error(toError(exception))
}

val <T> LCEState<T, *>.content: T?
    get() = (this as? LCEState.Content)?.content

fun <T, E> LCEState<T, E>.dismissError(): LCEState<T, E> = if (this is LCEState.Error) LCEState.None else this

fun <T, E> LCEState<T, E>.refreshing(): LCEState<T, E> =
    if (this is LCEState.Content) copy(isUpdating = true, updateError = null) else LCEState.Loading

inline fun <T, E> LCEState<T, E>.refreshedWith(response: Response<T>, toError: (Throwable) -> E): LCEState<T, E> =
    when (response) {
        is Response.Success -> LCEState.Content(response.value)

        is Response.Error -> if (this is LCEState.Content) {
            copy(isUpdating = false, updateError = toError(response.exception))
        } else {
            LCEState.Error(toError(response.exception))
        }
    }
