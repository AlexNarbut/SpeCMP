package ru.alarmtrade.specmp.feature.device.firmware.data.repository

import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemInformation
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceInformationRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceListInformationRequest
import ru.alarmtrade.specialist.interactors.files.requests.DownloadFileRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwareRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwaresRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.specialist.sdk.toResponseOrUnavailable
import ru.alarmtrade.specmp.feature.device.firmware.data.datasource.FirmwareDataSource
import ru.alarmtrade.specmp.feature.device.firmware.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.device.firmware.data.mapper.toKind
import ru.alarmtrade.specmp.feature.device.firmware.data.mapper.toModule
import ru.alarmtrade.specmp.feature.device.firmware.data.mapper.toSdk
import ru.alarmtrade.specmp.feature.device.firmware.data.mapper.toSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionPage
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

internal class SpecialistFirmwareRepository(private val dataSource: FirmwareDataSource) : FirmwareRepository {

    override suspend fun modules(deviceName: String, transport: ConnectionTransport): Response<List<FirmwareModule>> =
        dataSource.deviceInformation(GetDeviceInformationRequest(deviceName)).toResponseOrUnavailable()
            .map { information -> information.subsystems.map { it.toModule(transport) } }

    override suspend fun latest(
        moduleId: Long,
        channel: FirmwareChannel,
        can: FirmwareCan?,
    ): Response<FirmwareVersion> = dataSource
        .latestFirmware(
            SubsystemFirmwareRequest(
                subsystemId = moduleId,
                type = channel.toSdk(),
                canCode = can?.code,
                canCodeVersion = can?.version,
            ),
        )
        .toResponseOrUnavailable()
        .map { it.toDomain() }

    override suspend fun moduleId(deviceName: String, kind: FirmwareModuleKind): Response<Long?> =
        dataSource.deviceInformation(GetDeviceInformationRequest(deviceName)).toResponseOrUnavailable()
            .map { information -> information.subsystems.firstOrNull { it.type.toKind() == kind }?.id }

    override suspend fun serialModuleId(deviceName: String): Response<Long?> =
        dataSource.deviceInformation(GetDeviceInformationRequest(deviceName)).toResponseOrUnavailable()
            .map { information -> information.subsystems.serialModule(deviceName)?.id }

    override suspend fun systems(): Response<List<FirmwareSystem>> {
        val request =
            GetDeviceListInformationRequest(name = null, types = emptyList(), pageNumber = 0, pageSize = ALL_SYSTEMS)
        return dataSource.deviceList(request).toResponseOrUnavailable()
            .map { devices -> devices.map { it.toSystem() }.filter { it.subsystems.isNotEmpty() } }
    }

    override suspend fun download(url: String): Response<ByteArray> =
        dataSource.download(DownloadFileRequest(url)).toResponseOrUnavailable()

    override suspend fun archivePage(
        moduleId: Long,
        channel: FirmwareChannel,
        page: Int,
        pageSize: Int,
        can: FirmwareCan?,
    ): Response<FirmwareVersionPage> = dataSource
        .firmwares(
            SubsystemFirmwaresRequest(
                subsystemId = moduleId,
                type = channel.toSdk(),
                pageNumber = page,
                pageSize = pageSize,
                canCode = can?.code,
                canCodeVersion = can?.version,
            ),
        )
        .toResponseOrUnavailable()
        .map { list -> FirmwareVersionPage(items = list.map { it.toDomain() }, pageNumber = page) }
}

/** Все системы одной страницей: база на телефоне, страницы — фильтр в памяти. */
private const val ALL_SYSTEMS = 10_000

internal fun List<SubsystemInformation>.serialModule(deviceName: String): SubsystemInformation? =
    singleOrNull() ?: firstOrNull { it.systemName.trim().equals(deviceName.trim(), ignoreCase = true) } ?: firstOrNull()
