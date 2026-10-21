package ru.alarmtrade.specmp.feature.device.firmware.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel

/** Что выбрали на экране проверки перед переходом в загрузчик: модуль и канал. */
data class FirmwareBootRequest(val moduleId: Long, val channel: FirmwareChannel)

/**
 * Передаёт [FirmwareBootRequest] экрану обновления через перезагрузку блока: экран проверки живёт в рабочем режиме,
 * экран обновления создаётся уже после перехода в загрузчик. App-scoped; чистится при смене аккаунта.
 */
class FirmwareBootRequester : AuthCloseable {

    private val pending = MutableStateFlow<FirmwareBootRequest?>(null)

    fun request(request: FirmwareBootRequest) {
        pending.update { request }
    }

    /** Забрать запрос (атомарно: второй вызов получит `null`). */
    fun take(): FirmwareBootRequest? {
        while (true) {
            val current = pending.value
            if (pending.compareAndSet(current, null)) return current
        }
    }

    override suspend fun release() {
        pending.update { null }
    }
}
