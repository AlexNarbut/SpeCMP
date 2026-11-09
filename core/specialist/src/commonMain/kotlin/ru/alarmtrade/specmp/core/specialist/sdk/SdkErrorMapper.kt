package ru.alarmtrade.specmp.core.specialist.sdk

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.io.IOException
import ru.alarmtrade.bluetoothControl.domain.errors.InvalidBluetoothSessionState
import ru.alarmtrade.bluetoothControl.domain.errors.PandoraBleDeviceNotFoundException
import ru.alarmtrade.common.bluetooth.core.BluetoothConnectPermissionException
import ru.alarmtrade.common.bluetooth.core.BluetoothGattException
import ru.alarmtrade.common.bluetooth.core.BluetoothNotEnabledException
import ru.alarmtrade.common.bluetooth.core.BluetoothResettingException
import ru.alarmtrade.common.bluetooth.core.BluetoothScanPermissionException
import ru.alarmtrade.common.bluetooth.core.BluetoothUnsupportedException
import ru.alarmtrade.common.bluetooth.core.LocationNotEnableForScanException
import ru.alarmtrade.common.bluetooth.core.LocationPermissionException
import ru.alarmtrade.common.core.exceptions.ContentParsingException
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.exceptions.NetworkTimeoutException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.usb.UsbDeviceNotFoundException
import ru.alarmtrade.specialist.network.errors.NetworkNotConnectedError
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.AppException
import ru.alarmtrade.specmp.core.common.PlatformError

/**
 * Прочие ошибки возвращает как есть. Сырые исключения ktor/платформы распознаются только в
 * [SpecialistResponse.Error.Network]: `IOException` бывает и у USB/serial, это не «нет интернета».
 */
internal fun SpecialistResponse.Error.toAppException(): Throwable {
    val error = exception.sdkAppError()
        ?: (exception.transportApiError().takeIf { this is SpecialistResponse.Error.Network })
    return error?.let { AppException(it, exception) } ?: exception
}

fun Throwable.sdkAppError(): AppError? = when (this) {
    is NetworkIsNotAvailableException, is NetworkNotConnectedError -> ApiError.NoConnection
    is NetworkTimeoutException -> ApiError.Timeout
    is ContentParsingException -> ApiError.ServerError
    else -> platformError()
}

@Suppress("CyclomaticComplexMethod") // таблица-маппер
private fun Throwable.platformError(): PlatformError? = when (this) {
    is BluetoothNotEnabledException, is BluetoothResettingException -> PlatformError.BluetoothOff

    is BluetoothUnsupportedException -> PlatformError.BluetoothUnsupported

    is LocationNotEnableForScanException -> PlatformError.LocationOff

    is BluetoothScanPermissionException, is BluetoothConnectPermissionException, is LocationPermissionException ->
        PlatformError.PermissionDenied

    is PandoraBleDeviceNotFoundException, is UsbDeviceNotFoundException -> PlatformError.DeviceNotFound

    is InvalidBluetoothSessionState, is BluetoothGattException -> PlatformError.DeviceDisconnected

    else -> null
}

// Таймауты ktor — тоже IOException, поэтому проверяются раньше.
private fun Throwable.transportApiError(): ApiError? = when (this) {
    is HttpRequestTimeoutException, is ConnectTimeoutException, is SocketTimeoutException -> ApiError.Timeout
    is ServerResponseException -> ApiError.ServerError
    is UnresolvedAddressException, is IOException -> ApiError.NoConnection
    else -> null
}
