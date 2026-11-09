package ru.alarmtrade.specmp.feature.device.firmware.data.repository

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmware
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceInformation
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceInformationRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceListInformationRequest
import ru.alarmtrade.specialist.interactors.files.requests.DownloadFileRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwareRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwaresRequest
import ru.alarmtrade.specmp.feature.device.firmware.data.datasource.FirmwareDataSource

internal class FakeFirmwareDataSource : FirmwareDataSource {
    var information: SpecialistResponse<DeviceInformation>? = null
    var devices: SpecialistResponse<List<DeviceInformation>>? = null
    var latest: SpecialistResponse<SubsystemFirmware>? = null
    var firmwares: SpecialistResponse<List<SubsystemFirmware>>? = null
    var file: SpecialistResponse<ByteArray>? = null

    val deviceNames = mutableListOf<String>()
    val listRequests = mutableListOf<GetDeviceListInformationRequest>()
    val requests = mutableListOf<Any>()

    override suspend fun deviceInformation(
        request: GetDeviceInformationRequest,
    ): SpecialistResponse<DeviceInformation>? = information.also { deviceNames += request.name }

    override suspend fun deviceList(
        request: GetDeviceListInformationRequest,
    ): SpecialistResponse<List<DeviceInformation>>? = devices.also { listRequests += request }

    override suspend fun latestFirmware(request: SubsystemFirmwareRequest): SpecialistResponse<SubsystemFirmware>? =
        latest.also { requests += request }

    override suspend fun firmwares(request: SubsystemFirmwaresRequest): SpecialistResponse<List<SubsystemFirmware>>? =
        firmwares.also { requests += request }

    override suspend fun download(request: DownloadFileRequest): SpecialistResponse<ByteArray>? =
        file.also { requests += request }
}
