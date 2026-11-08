package ru.alarmtrade.specmp.feature.device.firmware.data.repository

import ru.alarmtrade.specialist.interactors.deviceInformation.DeviceInformationInteractor
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceInformationRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceListInformationRequest
import ru.alarmtrade.specialist.interactors.files.RemoteFilesInteractor
import ru.alarmtrade.specialist.interactors.files.requests.DownloadFileRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.FirmwareFilesInteractor
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwareRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwaresRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionPage
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

/**
 * [FirmwareRepository] над `deviceInformationInteractor`, `firmwareFilesInteractor` и `remoteFilesInteractor`
 * (скачивание файла версии); интеракторы — лениво.
 */
class SpecialistFirmwareRepository(
    private val deviceInformation: () -> DeviceInformationInteractor,
    private val firmwareFiles: () -> FirmwareFilesInteractor,
    private val files: () -> RemoteFilesInteractor,
) : FirmwareRepository {

    override suspend fun modules(deviceName: String, transport: ConnectionTransport): Response<List<FirmwareModule>> {
        val interactor = deviceInformation.resolveOrNull() ?: return interactorUnavailable()
        return interactor.getDeviceInformation.execute(GetDeviceInformationRequest(deviceName)).toResponse()
            .map { information -> information.subsystems.map { it.toModule(transport) } }
    }

    override suspend fun latest(
        moduleId: Long,
        channel: FirmwareChannel,
        can: FirmwareCan?,
    ): Response<FirmwareVersion> {
        val interactor = firmwareFiles.resolveOrNull() ?: return interactorUnavailable()
        return interactor.getLatestSubsystemFirmwareUseCase
            .execute(
                SubsystemFirmwareRequest(
                    subsystemId = moduleId,
                    type = channel.toSdk(),
                    canCode = can?.code,
                    canCodeVersion = can?.version,
                ),
            )
            .toResponse()
            .map { it.toDomain() }
    }

    override suspend fun moduleId(deviceName: String, kind: FirmwareModuleKind): Response<Long?> {
        val interactor = deviceInformation.resolveOrNull() ?: return interactorUnavailable()
        return interactor.getDeviceInformation.execute(GetDeviceInformationRequest(deviceName)).toResponse()
            .map { information -> information.subsystems.firstOrNull { it.type.toKind() == kind }?.id }
    }

    override suspend fun systems(): Response<List<FirmwareSystem>> {
        val interactor = deviceInformation.resolveOrNull() ?: return interactorUnavailable()
        val request =
            GetDeviceListInformationRequest(name = null, types = emptyList(), pageNumber = 0, pageSize = ALL_SYSTEMS)
        return interactor.getDeviceListInformation.execute(request).toResponse()
            .map { devices -> devices.map { it.toSystem() }.filter { it.subsystems.isNotEmpty() } }
    }

    override suspend fun download(url: String): Response<ByteArray> {
        val interactor = files.resolveOrNull() ?: return interactorUnavailable()
        return interactor.downloadFileUseCase.execute(DownloadFileRequest(url)).toResponse()
    }

    override suspend fun archivePage(
        moduleId: Long,
        channel: FirmwareChannel,
        page: Int,
        pageSize: Int,
        can: FirmwareCan?,
    ): Response<FirmwareVersionPage> {
        val interactor = firmwareFiles.resolveOrNull() ?: return interactorUnavailable()
        return interactor.getSubsystemFirmwaresUseCase
            .execute(
                SubsystemFirmwaresRequest(
                    subsystemId = moduleId,
                    type = channel.toSdk(),
                    pageNumber = page,
                    pageSize = pageSize,
                    canCode = can?.code,
                    canCodeVersion = can?.version,
                ),
            )
            .toResponse()
            .map { list -> FirmwareVersionPage(items = list.map { it.toDomain() }, pageNumber = page) }
    }
}

/** Все системы локальной базы устройств одной страницей (база на телефоне, страницы — фильтр в памяти). */
private const val ALL_SYSTEMS = 10_000
