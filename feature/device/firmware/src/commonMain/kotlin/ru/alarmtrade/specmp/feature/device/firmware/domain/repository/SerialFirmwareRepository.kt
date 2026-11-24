package ru.alarmtrade.specmp.feature.device.firmware.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FrmFileInfo
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareProgress

/** Сессию порта открывает поиск (`DeviceConnectionRepository.connectSerial`) и держит `DeviceSession`. */
interface SerialFirmwareRepository {
    suspend fun readFileInfo(path: String): Response<FrmFileInfo>

    /** Скачивает версию с сервера во временный файл и отдаёт его путь — дальше как у выбранного файла. */
    suspend fun download(url: String): Response<String>

    /** [fileName] — имя для журнала SDK. */
    fun update(fileName: String, path: String): Flow<Response<SerialFirmwareProgress>>
}
