package ru.alarmtrade.specmp.feature.device.firmware.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget

interface FirmwareUpdateRepository {
    /**
     * Итог — [FirmwareUpdateProgress.Finished] или `Response.Error` с `FirmwareUpdateException`; без итога поток
     * кончается
     * только при обрыве на [FirmwareUpdateProgress.Applying] (блок перезагрузился) — итог тогда даёт возврат блока.
     */
    fun update(target: BootModeTarget, firmware: FirmwareVersion): Flow<Response<FirmwareUpdateProgress>>

    /** Локальный файл не скачивается и не сверяется с сервером: корректность версии и модуля проверяет только блок. */
    fun updateFromFile(target: BootModeTarget, path: String): Flow<Response<FirmwareUpdateProgress>>
}
