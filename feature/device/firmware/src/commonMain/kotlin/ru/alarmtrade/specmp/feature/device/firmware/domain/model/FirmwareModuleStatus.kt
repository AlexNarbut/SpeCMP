package ru.alarmtrade.specmp.feature.device.firmware.domain.model

/**
 * [installed] `null` — версию на блоке не прочитать; [latest] `null` — модуль не обновить на этом подключении или
 * сервер версию не дал.
 */
data class FirmwareModuleStatus(val module: FirmwareModule, val installed: String?, val latest: FirmwareVersion?) {
    val updateAvailable: Boolean get() = latest?.isUpdateFor(installed) == true
}
