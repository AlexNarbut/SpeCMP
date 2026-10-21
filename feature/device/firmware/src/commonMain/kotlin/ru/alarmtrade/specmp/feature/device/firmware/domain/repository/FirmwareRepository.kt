package ru.alarmtrade.specmp.feature.device.firmware.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionPage
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

/** Модули блока и версии прошивок на сервере. Тонкий: повторы и сбросы — в ViewModel. */
interface FirmwareRepository {
    /** Модули блока с именем [deviceName]; [transport] определяет, какие из них приложение умеет обновлять. */
    suspend fun modules(deviceName: String, transport: ConnectionTransport): Response<List<FirmwareModule>>

    /** Модуль по id (для Bluetooth-модуля в загрузчике, когда список блока недоступен). */
    suspend fun moduleById(id: Long, transport: ConnectionTransport): Response<FirmwareModule>

    /** Последняя версия модуля в канале. */
    suspend fun latest(moduleId: Long, channel: FirmwareChannel): Response<FirmwareVersion>

    /** Страница архива версий (страницы с 1). */
    suspend fun archivePage(
        moduleId: Long,
        channel: FirmwareChannel,
        page: Int,
        pageSize: Int
    ): Response<FirmwareVersionPage>
}
