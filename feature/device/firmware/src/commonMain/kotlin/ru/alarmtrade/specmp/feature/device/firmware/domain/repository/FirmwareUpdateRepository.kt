package ru.alarmtrade.specmp.feature.device.firmware.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget

interface FirmwareUpdateRepository {
    /**
     * Процесс обновления [target] версией [firmware]. Завершается [FirmwareUpdateProgress.Finished] или
     * `Response.Error` с `FirmwareUpdateException` (причина — `failure`, прочие исключения —
     * `FirmwareFailure.Unknown`); итога нет, только если поток оборвался на стадии
     * [FirmwareUpdateProgress.Applying] (блок перезагрузился до результата): тогда он просто заканчивается,
     * и итог определяет возврат блока в рабочий режим.
     */
    fun update(target: BootModeTarget, firmware: FirmwareVersion): Flow<Response<FirmwareUpdateProgress>>

    /**
     * То же обновление, но из локального файла [path] (выбран пользователем в файловом менеджере): файл не скачивается
     * и не сверяется с сервером, поэтому корректность версии и модуля проверяет только блок.
     */
    fun updateFromFile(target: BootModeTarget, path: String): Flow<Response<FirmwareUpdateProgress>>
}
