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

interface FirmwareRepository {
    /** [transport] определяет, какие модули приложение умеет обновлять. */
    suspend fun modules(deviceName: String, transport: ConnectionTransport): Response<List<FirmwareModule>>

    /** Последняя версия модуля в канале; [can] — под этот CAN-код и версию CAN-профиля (единая CAN-прошивка). */
    suspend fun latest(moduleId: Long, channel: FirmwareChannel, can: FirmwareCan? = null): Response<FirmwareVersion>

    /** `null` — у устройства модуля [kind] нет. Без загрузчика: архив из карточки автомобиля. */
    suspend fun moduleId(deviceName: String, kind: FirmwareModuleKind): Response<Long?>

    /**
     * Модуль для прошивки по COM — без учёта типа: единственная подсистема модели, иначе та, чьё имя системы
     * совпадает с [deviceName], иначе первая. `null` — подсистем у модели нет.
     */
    suspend fun serialModuleId(deviceName: String): Response<Long?>

    suspend fun systems(): Response<List<FirmwareSystem>>

    suspend fun download(url: String): Response<ByteArray>

    /** Страницы с 1; [can] — только версии под этот CAN-код (и версию CAN-профиля, если задана). */
    suspend fun archivePage(
        moduleId: Long,
        channel: FirmwareChannel,
        page: Int,
        pageSize: Int,
        can: FirmwareCan? = null,
    ): Response<FirmwareVersionPage>
}
