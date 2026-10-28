package ru.alarmtrade.specmp.core.ui.error

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.PlatformError
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.common_error_bluetooth_off
import ru.alarmtrade.specmp.core.localization.resources.common_error_bluetooth_unsupported
import ru.alarmtrade.specmp.core.localization.resources.common_error_device_disconnected
import ru.alarmtrade.specmp.core.localization.resources.common_error_device_not_found
import ru.alarmtrade.specmp.core.localization.resources.common_error_location_off
import ru.alarmtrade.specmp.core.localization.resources.common_error_no_connection
import ru.alarmtrade.specmp.core.localization.resources.common_error_permission_denied
import ru.alarmtrade.specmp.core.localization.resources.common_error_server
import ru.alarmtrade.specmp.core.localization.resources.common_error_timeout

/** Текст общей ошибки — одинаков на всех экранах. Ошибка фичи отдаёт сюда свой подтип `Common(error)`. */
@Composable
fun AppError.message(): String = stringResource(messageRes())

/** То же вне composition — для снекбаров из обработчика эффектов. */
suspend fun AppError.messageText(): String = getString(messageRes())

private fun AppError.messageRes(): StringResource = when (this) {
    ApiError.NoConnection -> Strings.string.common_error_no_connection
    ApiError.Timeout -> Strings.string.common_error_timeout
    ApiError.ServerError -> Strings.string.common_error_server
    PlatformError.BluetoothOff -> Strings.string.common_error_bluetooth_off
    PlatformError.BluetoothUnsupported -> Strings.string.common_error_bluetooth_unsupported
    PlatformError.LocationOff -> Strings.string.common_error_location_off
    PlatformError.PermissionDenied -> Strings.string.common_error_permission_denied
    PlatformError.DeviceNotFound -> Strings.string.common_error_device_not_found
    PlatformError.DeviceDisconnected -> Strings.string.common_error_device_disconnected
}
