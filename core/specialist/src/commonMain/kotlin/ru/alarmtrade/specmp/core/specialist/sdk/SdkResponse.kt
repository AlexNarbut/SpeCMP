package ru.alarmtrade.specmp.core.specialist.sdk

import kotlinx.coroutines.CancellationException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response

/** SDK бросает при обращении к неподдерживаемому на платформе транспорту; отмена пробрасывается. */
fun <T> (() -> T).resolveOrNull(): T? = try {
    invoke()
} catch (e: CancellationException) {
    throw e
} catch (ignored: Throwable) {
    null
}

fun <T> SpecialistResponse<T>.toResponse(): Response<T> = when (this) {
    is SpecialistResponse.Success -> Response.Success(value)
    is SpecialistResponse.Error -> Response.Error(toAppException(), message ?: exception.message)
}

fun interactorUnavailable(): Response.Error =
    Response.Error(IllegalStateException("Specialist SDK interactor is not available on this platform"))

/** [toResponse] ответа источника с ленивым интерактором: `null` — интерактор недоступен ([interactorUnavailable]). */
fun <T> SpecialistResponse<T>?.toResponseOrUnavailable(): Response<T> = this?.toResponse() ?: interactorUnavailable()
