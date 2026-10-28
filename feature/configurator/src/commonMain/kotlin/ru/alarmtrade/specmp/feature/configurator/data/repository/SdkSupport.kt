package ru.alarmtrade.specmp.feature.configurator.data.repository

import kotlinx.coroutines.CancellationException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response

/**
 * Резолвит зависимость провайдера, возвращая null, если она недоступна на платформе
 * (SDK бросает при обращении к неподдерживаемому транспорту). Отмена пробрасывается.
 */
internal fun <T> (() -> T).resolveOrNull(): T? = try {
    invoke()
} catch (e: CancellationException) {
    throw e
} catch (ignored: Throwable) {
    null
}

internal fun <T> SpecialistResponse<T>.toResponse(): Response<T> = when (this) {
    is SpecialistResponse.Success -> Response.Success(value)
    is SpecialistResponse.Error -> Response.Error(exception, message)
}

internal fun interactorUnavailable(): Response.Error =
    Response.Error(IllegalStateException("Configurations interactor is not available on this platform"))
