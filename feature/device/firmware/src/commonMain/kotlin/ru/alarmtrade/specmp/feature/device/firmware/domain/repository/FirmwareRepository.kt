package ru.alarmtrade.specmp.feature.device.firmware.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionPage
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

/** Модули блока и версии прошивок на сервере. Тонкий: повторы и сбросы — в ViewModel. */
interface FirmwareRepository {
    /** Модули блока с именем [deviceName]; [transport] определяет, какие из них приложение умеет обновлять. */
    suspend fun modules(deviceName: String, transport: ConnectionTransport): Response<List<FirmwareModule>>

    /** Последняя версия модуля в канале; [can] — под этот CAN-код и версию CAN-профиля (единая CAN-прошивка). */
    suspend fun latest(moduleId: Long, channel: FirmwareChannel, can: FirmwareCan? = null): Response<FirmwareVersion>

    /**
     * Модуль вида [kind] устройства [deviceName] (основная программа, Bluetooth-модуль); `null` — у устройства его
     * нет. Без загрузчика: архив из карточки автомобиля.
     */
    suspend fun moduleId(deviceName: String, kind: FirmwareModuleKind): Response<Long?>

    /** Все системы базы устройств с подсистемами (архив без блока: выбор подсистемы). */
    suspend fun systems(): Response<List<FirmwareSystem>>

    /** Файл версии целиком в память (архив без блока: «Скачать», «Поделиться»). */
    suspend fun download(url: String): Response<ByteArray>

    /**
     * Страница архива версий (страницы с 1); [can] — только версии под этот CAN-код (и версию CAN-профиля,
     * если задана).
     */
    suspend fun archivePage(
        moduleId: Long,
        channel: FirmwareChannel,
        page: Int,
        pageSize: Int,
        can: FirmwareCan? = null,
    ): Response<FirmwareVersionPage>
}
