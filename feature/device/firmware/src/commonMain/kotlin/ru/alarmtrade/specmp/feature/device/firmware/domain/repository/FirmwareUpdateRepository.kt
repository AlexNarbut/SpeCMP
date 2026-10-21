package ru.alarmtrade.specmp.feature.device.firmware.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateTarget
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion

interface FirmwareUpdateRepository {
    /**
     * Процесс обновления [target] версией [firmware]. Завершается [FirmwareUpdateProgress.Finished] или
     * [FirmwareUpdateProgress.Failed]; единственное исключение — поток оборвался на стадии
     * [FirmwareUpdateProgress.Applying] (блок перезагрузился до результата): тогда он просто заканчивается,
     * и итог определяет возврат блока в рабочий режим.
     */
    fun update(target: FirmwareUpdateTarget, firmware: FirmwareVersion): Flow<FirmwareUpdateProgress>
}
