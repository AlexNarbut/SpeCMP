package ru.alarmtrade.specmp.feature.device.firmware.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.firmware.FirmwareUpdateState
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SerialFirmwareUpdateState
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.requests.UpdateFirmwareRequest
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.requests.UpdateSerialFirmwareRequest
import ru.alarmtrade.specmp.feature.device.firmware.data.datasource.FirmwareUpdateDataSource

internal class FakeFirmwareUpdateDataSource : FirmwareUpdateDataSource {
    var update: Flow<SpecialistResponse<FirmwareUpdateState>>? = emptyFlow()
    var serial: Flow<SpecialistResponse<SerialFirmwareUpdateState>>? = emptyFlow()

    val calls = mutableListOf<String>()
    val serialRequests = mutableListOf<UpdateSerialFirmwareRequest>()

    override fun updateMainProgram(request: UpdateFirmwareRequest): Flow<SpecialistResponse<FirmwareUpdateState>>? =
        update.also { calls += "main ${request.filePath}" }

    override fun updateBluetoothModule(
        request: UpdateFirmwareRequest,
    ): Flow<SpecialistResponse<FirmwareUpdateState>>? = update.also { calls += "bluetooth ${request.filePath}" }

    override fun updateSerial(
        request: UpdateSerialFirmwareRequest,
    ): Flow<SpecialistResponse<SerialFirmwareUpdateState>>? = serial.also { serialRequests += request }
}
