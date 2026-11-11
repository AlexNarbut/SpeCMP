package ru.alarmtrade.specmp.feature.device.firmware.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion

/** [preselect] — что предложить сразу после входа в загрузчик; `null` — выбор источника. */
data class FirmwareBootRequest(
    val moduleId: Long,
    val channel: FirmwareChannel,
    val preselect: FirmwarePreselect? = null,
)

sealed interface FirmwarePreselect {
    data object Latest : FirmwarePreselect

    data class Version(val version: FirmwareVersion) : FirmwarePreselect
}

/** Передаёт запрос через перезагрузку блока: экран обновления создаётся уже после перехода в загрузчик. */
class FirmwareBootRequester : AuthCloseable {

    private val pending = MutableStateFlow<FirmwareBootRequest?>(null)

    fun request(request: FirmwareBootRequest) {
        pending.update { request }
    }

    /** Забрать запрос (атомарно: второй вызов получит `null`). */
    fun take(): FirmwareBootRequest? = pending.getAndUpdate { null }

    override suspend fun release() {
        pending.update { null }
    }
}
