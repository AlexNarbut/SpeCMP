package ru.alarmtrade.specmp.core.common

/** Временный мост на время миграции фич на свои ошибки с [AppError]; удаляется вместе с `RequestError`. */
enum class NetworkFailure { NoConnection, Timeout, ServerError }

/** Временный мост: [ApiError] из [AppException] как [NetworkFailure]. */
val Throwable.networkFailure: NetworkFailure?
    get() = when (appError) {
        ApiError.NoConnection -> NetworkFailure.NoConnection
        ApiError.Timeout -> NetworkFailure.Timeout
        ApiError.ServerError -> NetworkFailure.ServerError
        is PlatformError, null -> null
    }
