package ru.alarmtrade.specmp.core.ui.lce

import ru.alarmtrade.specmp.core.common.Response

/** `Response` → `LCEState`; [toError] приводит исключение к ошибке фичи (обычно `XxxError::from`). */
inline fun <T, E> Response<T>.asLCEState(toError: (Throwable) -> E): LCEState<T, E> = when (this) {
    is Response.Success -> LCEState.Content(value)
    is Response.Error -> LCEState.Error(toError(exception))
}

/** Преобразует контент, сохраняя признаки фоновой операции. */
inline fun <T, E, R> LCEState<T, E>.mapContent(mapper: (T) -> R): LCEState<R, E> = when (this) {
    is LCEState.Content -> LCEState.Content(mapper(content), isUpdating, updateError)
    is LCEState.Error -> this
    LCEState.Loading -> LCEState.Loading
    LCEState.None -> LCEState.None
}

/** Контент, если он показан; иначе null. */
val <T> LCEState<T, *>.content: T?
    get() = (this as? LCEState.Content)?.content

val LCEState<*, *>.isLoading: Boolean
    get() = this is LCEState.Loading

val LCEState<*, *>.isContent: Boolean
    get() = this is LCEState.Content

/** Ошибка → [LCEState.None] (пользователь начал править форму); прочие состояния — как есть. */
fun <T, E> LCEState<T, E>.dismissError(): LCEState<T, E> = if (this is LCEState.Error) LCEState.None else this
