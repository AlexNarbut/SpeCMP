package ru.alarmtrade.specmp.feature.device.firmware.ui

import kotlinx.coroutines.delay
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionPage
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

internal class FakeFirmwareRepository : FirmwareRepository {
    var modulesResult: Response<List<FirmwareModule>> = Response.Success(emptyList())
    val latest = mutableMapOf<Pair<Long, FirmwareChannel>, Response<FirmwareVersion>>()
    val latestCalls = mutableListOf<Pair<Long, FirmwareChannel>>()

    override suspend fun modules(deviceName: String, transport: ConnectionTransport) = modulesResult

    /** CAN-фильтр каждого запроса последней версии (по порядку). */
    val latestCans = mutableListOf<FirmwareCan?>()

    override suspend fun latest(
        moduleId: Long,
        channel: FirmwareChannel,
        can: FirmwareCan?,
    ): Response<FirmwareVersion> {
        latestCalls += moduleId to channel
        latestCans += can
        return latest[moduleId to channel] ?: Response.Error(IllegalStateException("no version"))
    }

    /** Основной модуль модели блока (архив без moduleId). */
    var mainModule: Response<Long?> = Response.Success(null)
    val mainModuleCalls = mutableListOf<String>()

    /** Bluetooth-модуль модели (архив без блока). */
    var bluetoothModule: Response<Long?> = Response.Success(null)
    val moduleCalls = mutableListOf<Pair<String, FirmwareModuleKind>>()

    override suspend fun moduleId(deviceName: String, kind: FirmwareModuleKind): Response<Long?> {
        moduleCalls += deviceName to kind
        return when (kind) {
            FirmwareModuleKind.AlarmMain -> {
                mainModuleCalls += deviceName
                mainModule
            }

            FirmwareModuleKind.BluetoothModule -> bluetoothModule

            else -> Response.Success(null)
        }
    }

    /** Системы базы устройств. */
    var systemsResult: Response<List<FirmwareSystem>> = Response.Success(emptyList())

    override suspend fun systems(): Response<List<FirmwareSystem>> = systemsResult

    /** Файл версии по ссылке; нет — ошибка сети. */
    val files = mutableMapOf<String, Response<ByteArray>>()
    val downloads = mutableListOf<String>()

    override suspend fun download(url: String): Response<ByteArray> {
        downloads += url
        return files[url] ?: Response.Error(IllegalStateException("no file"))
    }

    /** Страницы архива по номеру; нет страницы — ошибка сервера. */
    val archivePages = mutableMapOf<Int, Response<FirmwareVersionPage>>()
    val archiveCalls = mutableListOf<Int>()

    /** Запросы архива целиком: модуль, страница и CAN-фильтр. */
    val archiveRequests = mutableListOf<Triple<Long, Int, String?>>()

    /** CAN-фильтр запросов архива целиком (код и версия CAN-профиля). */
    val archiveCans = mutableListOf<FirmwareCan?>()

    /** Сколько «идёт» запрос страницы — для ответа, пришедшего после обновления списка. */
    val archiveDelayMillis = mutableMapOf<Int, Long>()

    override suspend fun archivePage(
        moduleId: Long,
        channel: FirmwareChannel,
        page: Int,
        pageSize: Int,
        can: FirmwareCan?,
    ): Response<FirmwareVersionPage> {
        archiveCalls += page
        archiveRequests += Triple(moduleId, page, can?.code)
        archiveCans += can
        archiveDelayMillis[page]?.let { delay(it) }
        return archivePages[page] ?: Response.Error(IllegalStateException("no page"))
    }
}
