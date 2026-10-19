package ru.alarmtrade.specmp.core.ui.lce

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.Response

/*
 * Extension functions for LCEState.
 */

/**
 * Maps content of LCEState to another type.
 */
fun <T, R> LCEState<T>.mapContent(mapper: (T) -> R): LCEState<R> = when (this) {
    is LCEState.Content -> LCEState.Content(mapper(content))
    is LCEState.Error -> this
    is LCEState.Loading -> this
    is LCEState.None -> this
}

/**
 * Executes a suspend function and updates the MutableStateFlow with LCEState.
 *
 * @param execute The suspend function to execute that returns Response<R>
 * @param needReload If true, always shows loading. If false, only shows loading if current state is not success
 * @param errorMapper Maps exception to user-friendly error message
 * @param dispatcher The CoroutineDispatcher to use for mapping
 * @param map Maps the Response value to the desired UI state type
 */
suspend fun <R, L> MutableStateFlow<LCEState<L>>.applyFromExecuting(
    execute: suspend () -> Response<R>,
    needReload: Boolean = true,
    errorMapper: (Throwable) -> String,
    dispatcher: CoroutineDispatcher,
    map: suspend (R) -> L
) {
    val currentState = this.value
    if (!currentState.isSuccess() || needReload) {
        update { LCEState.Loading() }
    }

    update {
        when (val response = execute()) {
            is Response.Success -> {
                val content = withContext(dispatcher) { map(response.value) }
                LCEState.Content(content)
            }

            is Response.Error -> createLCEError(errorMapper.invoke(response.exception))
        }
    }
}

/**
 * Creates an LCEState.Error with the given error message.
 */
fun createLCEError(error: String): LCEState.Error = LCEState.Error(error)

/**
 * Executes action if state is Content.
 */
inline fun <T> LCEState<T>.content(action: (T) -> Unit) {
    if (this is LCEState.Content) action(content)
}

/**
 * Executes action if state is Content (alias for content).
 */
inline fun <T> LCEState<T>.ifSuccess(action: (T) -> Unit) {
    if (this is LCEState.Content) action(content)
}

/**
 * Returns true if state is Error.
 */
fun <T> LCEState<T>.isError(): Boolean = this is LCEState.Error

/**
 * Returns true if state is Content.
 */
fun <T> LCEState<T>.isSuccess(): Boolean = this is LCEState.Content

/**
 * Returns true if state is not Loading (i.e., it's a final state).
 */
fun <T> LCEState<T>.isFinalState(): Boolean = this !is LCEState.Loading

/**
 * Returns true if state is Loading.
 */
fun <T> LCEState<T>.isLoading(): Boolean = this is LCEState.Loading

/**
 * Wraps value in LCEState.Content.
 */
fun <T> T.asLCEState() = LCEState.Content(this)

/**
 * Converts Response to LCEState.
 *
 * @param errorMapper Maps exception to user-friendly error message
 */
fun <T> Response<T>.asLCEState(errorMapper: (Throwable) -> String): LCEState<T> = when (this) {
    is Response.Success -> LCEState.Content(this.value)
    is Response.Error -> LCEState.Error(errorMapper.invoke(this.exception))
}

/**
 * Gets content if state is Content, otherwise returns null.
 */
val <T> LCEState<T>.content: T?
    get() = if (this is LCEState.Content) content else null
