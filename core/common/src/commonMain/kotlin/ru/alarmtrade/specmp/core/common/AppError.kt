package ru.alarmtrade.specmp.core.common

/**
 * Общая ошибка приложения — то, что одинаково для всех фич и одинаково показывается пользователю.
 * Источник (SDK, ktor, платформа) приводится к ней на границе data-слоя ([AppException]); фича оборачивает её
 * своим подтипом (`data class Common(val error: AppError)`) рядом с собственными случаями — см. docs/patterns.md,
 * «Ошибки фичи».
 */
sealed interface AppError

/** Связь с сервером. */
sealed interface ApiError : AppError {
    /** Сети нет: устройство офлайн, адрес не разрешился, соединение отклонено или оборвалось. */
    data object NoConnection : ApiError

    /** Сервер не ответил вовремя. */
    data object Timeout : ApiError

    /** Сервер ответил ошибкой или ответ не разобрался. */
    data object ServerError : ApiError
}

/** Платформа и связь с блоком: Bluetooth, USB, разрешения. */
sealed interface PlatformError : AppError {
    data object BluetoothOff : PlatformError

    data object BluetoothUnsupported : PlatformError

    /** Для поиска BLE-устройств нужна включённая геолокация (Android). */
    data object LocationOff : PlatformError

    /** Нет разрешения на Bluetooth / геолокацию. */
    data object PermissionDenied : PlatformError

    /** Блок не найден: не виден по BLE или не подключён по USB. */
    data object DeviceNotFound : PlatformError

    /** Сессия с блоком оборвалась или не открыта. */
    data object DeviceDisconnected : PlatformError
}

/** Носитель [AppError] в `Response.Error.exception`; исходная ошибка — в [cause]. */
class AppException(val error: AppError, cause: Throwable) : Exception("App error: $error", cause)

/** Общая ошибка, если исключение — [AppException]; иначе null (ошибка фичи или неизвестная). */
val Throwable.appError: AppError?
    get() = (this as? AppException)?.error
