package ru.alarmtrade.specmp.core.ui.error

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.PlatformError
import specmp.core.ui.generated.resources.Res
import specmp.core.ui.generated.resources.coreui_common_error_bluetooth_off
import specmp.core.ui.generated.resources.coreui_common_error_bluetooth_unsupported
import specmp.core.ui.generated.resources.coreui_common_error_device_disconnected
import specmp.core.ui.generated.resources.coreui_common_error_device_not_found
import specmp.core.ui.generated.resources.coreui_common_error_location_off
import specmp.core.ui.generated.resources.coreui_common_error_no_connection
import specmp.core.ui.generated.resources.coreui_common_error_permission_denied
import specmp.core.ui.generated.resources.coreui_common_error_server
import specmp.core.ui.generated.resources.coreui_common_error_timeout

/** Текст общей ошибки — одинаков на всех экранах. Ошибка фичи отдаёт сюда свой подтип `Common(error)`. */
@Composable
fun AppError.message(): String = stringResource(messageRes())

private fun AppError.messageRes(): StringResource = when (this) {
    ApiError.NoConnection -> Res.string.coreui_common_error_no_connection
    ApiError.Timeout -> Res.string.coreui_common_error_timeout
    ApiError.ServerError -> Res.string.coreui_common_error_server
    PlatformError.BluetoothOff -> Res.string.coreui_common_error_bluetooth_off
    PlatformError.BluetoothUnsupported -> Res.string.coreui_common_error_bluetooth_unsupported
    PlatformError.LocationOff -> Res.string.coreui_common_error_location_off
    PlatformError.PermissionDenied -> Res.string.coreui_common_error_permission_denied
    PlatformError.DeviceNotFound -> Res.string.coreui_common_error_device_not_found
    PlatformError.DeviceDisconnected -> Res.string.coreui_common_error_device_disconnected
}
