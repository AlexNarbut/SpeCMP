package ru.alarmtrade.specmp.core.common

/** Фича оборачивает общую ошибку своим подтипом `Common(error)` — docs/patterns.md, «Ошибки фичи». */
sealed interface AppError

sealed interface ApiError : AppError {
    /** Сети нет: устройство офлайн, адрес не разрешился, соединение отклонено или оборвалось. */
    data object NoConnection : ApiError

    data object Timeout : ApiError

    /** Сервер ответил ошибкой или ответ не разобрался. */
    data object ServerError : ApiError
}

sealed interface PlatformError : AppError {
    data object BluetoothOff : PlatformError

    data object BluetoothUnsupported : PlatformError

    /** Для поиска BLE-устройств нужна включённая геолокация (Android). */
    data object LocationOff : PlatformError

    data object PermissionDenied : PlatformError

    /** Блок не найден: не виден по BLE или не подключён по USB. */
    data object DeviceNotFound : PlatformError

    /** Сессия с блоком оборвалась или не открыта. */
    data object DeviceDisconnected : PlatformError
}

class AppException(val error: AppError, cause: Throwable) : Exception("App error: $error", cause)

val Throwable.appError: AppError?
    get() = (this as? AppException)?.error
