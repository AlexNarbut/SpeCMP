package ru.alarmtrade.specmp.feature.device.firmware.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmware
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceInformation
import ru.alarmtrade.specialist.interactors.deviceInformation.DeviceInformationInteractor
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceInformationRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceListInformationRequest
import ru.alarmtrade.specialist.interactors.files.RemoteFilesInteractor
import ru.alarmtrade.specialist.interactors.files.requests.DownloadFileRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.FirmwareFilesInteractor
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwareRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwaresRequest
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе.
internal interface FirmwareDataSource {
    suspend fun deviceInformation(request: GetDeviceInformationRequest): SpecialistResponse<DeviceInformation>?
    suspend fun deviceList(request: GetDeviceListInformationRequest): SpecialistResponse<List<DeviceInformation>>?
    suspend fun latestFirmware(request: SubsystemFirmwareRequest): SpecialistResponse<SubsystemFirmware>?
    suspend fun firmwares(request: SubsystemFirmwaresRequest): SpecialistResponse<List<SubsystemFirmware>>?
    suspend fun download(request: DownloadFileRequest): SpecialistResponse<ByteArray>?
}

internal class SpecialistFirmwareDataSource(
    private val deviceInformation: () -> DeviceInformationInteractor,
    private val firmwareFiles: () -> FirmwareFilesInteractor,
    private val files: () -> RemoteFilesInteractor,
) : FirmwareDataSource {
    override suspend fun deviceInformation(
        request: GetDeviceInformationRequest,
    ): SpecialistResponse<DeviceInformation>? =
        deviceInformation.resolveOrNull()?.getDeviceInformation?.execute(request)

    override suspend fun deviceList(
        request: GetDeviceListInformationRequest,
    ): SpecialistResponse<List<DeviceInformation>>? =
        deviceInformation.resolveOrNull()?.getDeviceListInformation?.execute(request)

    override suspend fun latestFirmware(request: SubsystemFirmwareRequest): SpecialistResponse<SubsystemFirmware>? =
        firmwareFiles.resolveOrNull()?.getLatestSubsystemFirmwareUseCase?.execute(request)

    override suspend fun firmwares(request: SubsystemFirmwaresRequest): SpecialistResponse<List<SubsystemFirmware>>? =
        firmwareFiles.resolveOrNull()?.getSubsystemFirmwaresUseCase?.execute(request)

    override suspend fun download(request: DownloadFileRequest): SpecialistResponse<ByteArray>? =
        files.resolveOrNull()?.downloadFileUseCase?.execute(request)
}
