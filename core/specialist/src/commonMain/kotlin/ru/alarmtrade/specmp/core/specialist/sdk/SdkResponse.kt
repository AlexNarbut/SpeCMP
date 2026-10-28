package ru.alarmtrade.specmp.core.specialist.sdk

import kotlinx.coroutines.CancellationException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response

/**
 * Резолвит зависимость провайдера, возвращая null, если она недоступна на платформе
 * (SDK бросает при обращении к неподдерживаемому транспорту). Отмена пробрасывается.
 */
fun <T> (() -> T).resolveOrNull(): T? = try {
    invoke()
} catch (e: CancellationException) {
    throw e
} catch (ignored: Throwable) {
    null
}

/**
 * Переводит ответ SDK в проектный [Response]; без сообщения SDK берётся сообщение исключения.
 * Общая ошибка (связь, Bluetooth, разрешения…) приходит как [ru.alarmtrade.specmp.core.common.AppException]
 * (см. [toAppException]).
 */
fun <T> SpecialistResponse<T>.toResponse(): Response<T> = when (this) {
    is SpecialistResponse.Success -> Response.Success(value)
    is SpecialistResponse.Error -> Response.Error(toAppException(), message ?: exception.message)
}

/** Ошибка «интерактор SDK недоступен на этой платформе» — результат неудачного [resolveOrNull]. */
fun interactorUnavailable(): Response.Error =
    Response.Error(IllegalStateException("Specialist SDK interactor is not available on this platform"))
