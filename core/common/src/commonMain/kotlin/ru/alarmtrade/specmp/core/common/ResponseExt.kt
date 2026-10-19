package ru.alarmtrade.specmp.core.common

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transform

/**
 * Extension functions for Response class.
 *
 * These functions provide a convenient API for working with Response objects:
 * - Properties for checking state: isSuccess, isError
 * - Safe accessors: getOrNull(), getOrThrow(), getOrElse()
 * - Transformations: map(), mapIfSuccess(), mapValueIfSuccess()
 * - Callback chains: onSuccess(), onError(), alsoIfSuccess(), alsoIfError()
 * - Safe wrappers: safeRun(), safeResponseRun(), safeFlowRun()
 * - Flow operations: executeFlowIfSuccess(), safeFlowResponseRun()
 * - Utilities: retry(), asResponse()
 *
 * @see Response
 */

// ============================================
// State Checking Properties
// ============================================

val <T> Response<T>.isSuccess: Boolean get() = this is Response.Success
val <T> Response<T>.isError: Boolean get() = this is Response.Error

// ============================================
// Safe Accessors
// ============================================

fun <T> Response<T>.getOrNull(): T? = when (this) {
    is Response.Success -> value
    is Response.Error -> null
}

fun <T> Response<T>.getOrThrow(): T = when (this) {
    is Response.Success -> value
    is Response.Error -> throw exception
}

fun <T> Response<T>.getOrElse(default: T): T = when (this) {
    is Response.Success -> value
    is Response.Error -> default
}

fun <T> Response<T>.getValueIfSuccess(): T? = if (this is Response.Success) value else null

fun <T> Response<T>.getExceptionIfError(): Throwable? = if (this is Response.Error) exception else null

// ============================================
// Transformations
// ============================================

inline fun <T, R> Response<T>.map(transform: (T) -> R): Response<R> = when (this) {
    is Response.Success -> Response.Success(transform(value))
    is Response.Error -> this
}

inline fun <T, R> Response<T>.mapIfSuccess(it: (T) -> Response<R>): Response<R> = when (this) {
    is Response.Success -> it.invoke(this.value)
    is Response.Error -> this
}

inline fun <T, R> Response<T>.safeMapIfSuccess(it: (T) -> Response<R>): Response<R> = when (this) {
    is Response.Success -> safeResponseRun { it.invoke(this.value) }
    is Response.Error -> this
}

inline fun <T, R> Response<T>.mapValueIfSuccess(it: (T) -> R): Response<R> = when (this) {
    is Response.Success -> Response.Success(it.invoke(this.value))
    is Response.Error -> this
}

inline fun <T, R> Response<T>.safeMapValueIfSuccess(it: (T) -> R): Response<R> = when (this) {
    is Response.Success -> safeRun { it.invoke(this.value) }
    is Response.Error -> this
}

inline fun <T> Response<T>.mapIfError(it: (Response.Error) -> Response<T>): Response<T> = when (this) {
    is Response.Success -> this
    is Response.Error -> it.invoke(this)
}

// ============================================
// Callback Chains
// ============================================

inline fun <T> Response<T>.onSuccess(action: (T) -> Unit): Response<T> {
    if (this is Response.Success) action(value)
    return this
}

inline fun <T> Response<T>.onError(action: (Throwable) -> Unit): Response<T> {
    if (this is Response.Error) action(exception)
    return this
}

inline fun <T> Response<T>.alsoIfSuccess(it: (T) -> Unit): Response<T> {
    if (this is Response.Success) {
        it.invoke(this.value)
    }
    return this
}

inline fun <T> Response<T>.alsoIfError(it: (Response.Error) -> Unit): Response<T> {
    if (this is Response.Error) {
        it.invoke(this)
    }
    return this
}

// ============================================
// Flow Operations
// ============================================

inline fun <T, R> Response<T>.executeIfSuccess(block: (T) -> Response<R>): Response<R> = when (this) {
    is Response.Success -> block(this.value)
    is Response.Error -> this
}

inline fun <T, R> Response<T>.executeFlowIfSuccess(block: (T) -> Flow<Response<R>>): Flow<Response<R>> = when (this) {
    is Response.Success -> block(this.value)
    is Response.Error -> flowOf(this)
}

inline fun <T, R> Response<T>.executeStateFlowIfSuccess(block: (T) -> StateFlow<Response<R>>): StateFlow<Response<R>> =
    when (this) {
        is Response.Success -> block(this.value)
        is Response.Error -> MutableStateFlow(this)
    }

// ============================================
// Safe Wrappers
// ============================================

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

suspend inline fun <R> safeFlowResponseRun(noinline block: suspend () -> Flow<R>): Flow<Response<R>> = try {
    block()
        .transform<R, Response<R>> { emit(Response.Success(it)) }
        .catch { e -> emit(Response.Error(e, e.message)) }
} catch (e: Throwable) {
    if (e is CancellationException) throw e
    Logger.e(e) { "safeFlowResponseRun: ${e.stackTraceToString()}" }
    flowOf(Response.Error(e, e.message))
}

suspend inline fun <R> safeFlowRun(noinline block: suspend () -> Flow<Response<R>>): Flow<Response<R>> = try {
    block().catch { e ->
        emit(Response.Error(e, e.message))
    }
} catch (e: Throwable) {
    if (e is CancellationException) throw e
    Logger.e(e) { "safeFlowRun: ${e.message}" }
    flowOf(Response.Error(e, e.message))
}

// ============================================
// Utilities
// ============================================

/**
 * Retries the block up to retryCount times, with optional delay between attempts.
 * Returns the first successful Response, or the last error if all attempts fail.
 */
suspend inline fun <T> retry(retryCount: Int = 5, delay: Long = 0, block: suspend () -> Response<T>): Response<T> {
    for (currentTry in 0 until retryCount - 1) {
        val response = block()
        if (response is Response.Success) return response
        if (delay > 0) delay(delay)
    }
    return block()
}

/**
 * Wraps value in Response.Success.
 */
fun <T> T.asResponse() = Response.Success(this)
