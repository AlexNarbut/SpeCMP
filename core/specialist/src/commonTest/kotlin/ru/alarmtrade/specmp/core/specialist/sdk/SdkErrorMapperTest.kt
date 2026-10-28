package ru.alarmtrade.specmp.core.specialist.sdk

import io.ktor.util.network.UnresolvedAddressException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.io.IOException
import ru.alarmtrade.common.core.exceptions.ContentParsingException
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.exceptions.NetworkTimeoutException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.network.errors.NetworkNotConnectedError
import ru.alarmtrade.specmp.core.common.NetworkFailure
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.networkFailure

class SdkErrorMapperTest {

    @Test
    fun sdkNetworkErrorsMapOnAnyErrorKind() {
        assertEquals(NetworkFailure.NoConnection, general(NetworkIsNotAvailableException()).failure())
        assertEquals(NetworkFailure.NoConnection, general(NetworkNotConnectedError()).failure())
        assertEquals(NetworkFailure.Timeout, general(NetworkTimeoutException("GET", IOException("timeout"))).failure())
        assertEquals(
            NetworkFailure.ServerError,
            general(ContentParsingException("GET", IllegalStateException("json"))).failure()
        )
    }

    @Test
    fun transportErrorsMapOnlyFromNetworkKind() {
        assertEquals(NetworkFailure.NoConnection, network(IOException("reset")).failure())
        assertEquals(NetworkFailure.NoConnection, network(UnresolvedAddressException()).failure())
        // IOException у USB/serial — не «нет интернета».
        assertEquals(null, general(IOException("serial")).failure())
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

    private fun SpecialistResponse.Error.failure(): NetworkFailure? =
        (toResponse() as Response.Error).exception.networkFailure
}
