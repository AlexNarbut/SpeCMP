package ru.alarmtrade.specmp.feature.device.firmware.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FrmFileInfo
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareProgress

/**
 * Прошивка блока по COM-порту (serial-обновление SDK) в текущей сессии устройства: сессию порта открывает поиск
 * (`DeviceConnectionRepository.connectSerial`) и держит `DeviceSession`. Состояния репозиторий не держит.
 */
interface SerialFirmwareRepository {
    /** Для какого блока файл [path] (заголовок `.frm`); ошибка — файл не прочитан или это не `.frm`. */
    suspend fun readFileInfo(path: String): Response<FrmFileInfo>

    /**
     * Прошить файл [path] ([fileName] — имя для журнала SDK). Поток кончается [SerialFirmwareProgress.Finished] или
     * `Response.Error` с `SerialFirmwareException` (причина — `failure`).
     */
    fun update(fileName: String, path: String): Flow<Response<SerialFirmwareProgress>>
}
