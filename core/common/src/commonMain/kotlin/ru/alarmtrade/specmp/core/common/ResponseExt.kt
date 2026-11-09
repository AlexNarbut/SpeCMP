package ru.alarmtrade.specmp.core.common

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf

/*
 * Расширения [Response] — по одному имени на операцию (docs/patterns.md, «Response»):
 * - доступ: getValueIfSuccess;
 * - трансформации: map (значение), mapIfSuccess (следующий шаг, сам возвращает Response), mapIfError;
 * - побочное действие на ошибке: onError;
 * - безопасные обёртки бросающего кода: safeRun, safeResponseRun, safeFlowRun.
 */

fun <T> Response<T>.getValueIfSuccess(): T? = if (this is Response.Success) value else null

inline fun <T, R> Response<T>.map(transform: (T) -> R): Response<R> = when (this) {
    is Response.Success -> Response.Success(transform(value))
    is Response.Error -> this
}

inline fun <T, R> Response<T>.mapIfSuccess(it: (T) -> Response<R>): Response<R> = when (this) {
    is Response.Success -> it.invoke(this.value)
    is Response.Error -> this
}

inline fun <T> Response<T>.mapIfError(it: (Response.Error) -> Response<T>): Response<T> = when (this) {
    is Response.Success -> this
    is Response.Error -> it.invoke(this)
}

inline fun <T> Response<T>.onError(action: (Throwable) -> Unit): Response<T> {
    if (this is Response.Error) action(exception)
    return this
}

inline fun <R> safeRun(block: () -> R): Response<R> = try {
    Response.Success(block())
} catch (e: Throwable) {
    if (e is CancellationException) throw e
    Logger.e(e) { "safeRun: ${e.stackTraceToString()}" }
    Response.Error(e, e.message)
}

inline fun <R> safeResponseRun(block: () -> Response<R>): Response<R> = try {
    block()
} catch (e: Throwable) {
    if (e is CancellationException) throw e
    Logger.e(e) { "safeResponseRun: ${e.stackTraceToString()}" }
    Response.Error(e, e.message)
}

// suspend нужен: block — suspend-фабрика Flow, вызывается здесь же
@Suppress("SuspendFunWithFlowReturnType")
suspend inline fun <R> safeFlowRun(noinline block: suspend () -> Flow<Response<R>>): Flow<Response<R>> = try {
    block().catch { e ->
        emit(Response.Error(e, e.message))
    }
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Logger.e(e) { "safeFlowRun: ${e.message}" }
    flowOf(Response.Error(e, e.message))
}
