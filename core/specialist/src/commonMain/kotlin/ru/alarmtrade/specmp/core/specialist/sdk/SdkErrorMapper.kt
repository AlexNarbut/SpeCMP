package ru.alarmtrade.specmp.core.specialist.sdk

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.io.IOException
import ru.alarmtrade.common.core.exceptions.ContentParsingException
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.exceptions.NetworkTimeoutException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.network.errors.NetworkNotConnectedError
import ru.alarmtrade.specmp.core.common.NetworkException
import ru.alarmtrade.specmp.core.common.NetworkFailure

/**
 * Приводит сбой связи из SDK к [NetworkException]; прочие ошибки возвращает как есть.
 *
 * Типизированные ошибки SDK распознаются везде. Сырые исключения ktor/платформы (DNS, connect, обрыв)
 * SDK отдаёт без обёртки — их распознаём только в [SpecialistResponse.Error.Network]: `IOException`
 * бывает и у USB/serial, это не «нет интернета».
 */
internal fun SpecialistResponse.Error.toAppException(): Throwable {
    val failure = exception.sdkNetworkFailure()
        ?: (exception.transportFailure().takeIf { this is SpecialistResponse.Error.Network })
    return failure?.let { NetworkException(it, exception) } ?: exception
}

private fun Throwable.sdkNetworkFailure(): NetworkFailure? = when (this) {
    is NetworkIsNotAvailableException, is NetworkNotConnectedError -> NetworkFailure.NoConnection
    is NetworkTimeoutException -> NetworkFailure.Timeout
    is ContentParsingException -> NetworkFailure.ServerError
    else -> null
}

// Таймауты ktor — тоже IOException, поэтому проверяются раньше.
private fun Throwable.transportFailure(): NetworkFailure? = when (this) {
    is HttpRequestTimeoutException, is ConnectTimeoutException, is SocketTimeoutException -> NetworkFailure.Timeout
    is ServerResponseException -> NetworkFailure.ServerError
    is UnresolvedAddressException, is IOException -> NetworkFailure.NoConnection
    else -> null
}
