package ru.alarmtrade.specmp.core.specialist.sdk

import io.ktor.util.network.UnresolvedAddressException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.io.IOException
import ru.alarmtrade.common.bluetooth.core.BluetoothGattException
import ru.alarmtrade.common.bluetooth.core.BluetoothNotEnabledException
import ru.alarmtrade.common.core.exceptions.ContentParsingException
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.exceptions.NetworkTimeoutException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.network.errors.NetworkNotConnectedError
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppError
import ru.alarmtrade.specmp.core.common.PlatformError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError

class SdkErrorMapperTest {

    @Test
    fun sdkApiErrorsMapOnAnyErrorKind() {
        assertEquals(ApiError.NoConnection, general(NetworkIsNotAvailableException()).error())
        assertEquals(ApiError.NoConnection, general(NetworkNotConnectedError()).error())
        assertEquals(ApiError.Timeout, general(NetworkTimeoutException("GET", IOException("timeout"))).error())
        assertEquals(
            ApiError.ServerError,
            general(ContentParsingException("GET", IllegalStateException("json"))).error(),
        )
    }

    @Test
    fun transportErrorsMapOnlyFromNetworkKind() {
        assertEquals(ApiError.NoConnection, network(IOException("reset")).error())
        assertEquals(ApiError.NoConnection, network(UnresolvedAddressException()).error())
        // IOException у USB/serial — не «нет интернета».
        assertEquals(null, general(IOException("serial")).error())
    }

    @Test
    fun platformErrorsMap() {
        assertEquals(PlatformError.BluetoothOff, bluetooth(BluetoothNotEnabledException()).error())
        assertEquals(PlatformError.DeviceDisconnected, bluetooth(BluetoothGattException("gatt")).error())
    }

    @Test
    fun otherErrorsPassThroughAndKeepCause() {
        val domain = IllegalStateException("pin")
        assertSame(domain, (network(domain).toResponse() as Response.Error).exception)

        val io = IOException("reset")
        assertSame(io, (network(io).toResponse() as Response.Error).exception.cause)
    }

    private fun general(e: Throwable) = SpecialistResponse.Error.General(e, "", "")

    private fun network(e: Throwable) = SpecialistResponse.Error.Network(e, "", "")

    private fun bluetooth(e: Throwable) = SpecialistResponse.Error.Bluetooth(e, "", "")

    private fun SpecialistResponse.Error.error(): AppError? = (toResponse() as Response.Error).exception.appError
}
