package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlinx.coroutines.CancellationException

/**
 * Резолвит зависимость провайдера, возвращая null, если она недоступна на платформе
 * (SDK бросает при обращении к неподдерживаемому транспорту). Отмена пробрасывается.
 */
internal fun <T> (() -> T).resolveOrNull(): T? = try {
    invoke()
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    null
}
