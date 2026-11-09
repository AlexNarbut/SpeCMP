package ru.alarmtrade.specmp.feature.device.firmware.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.firmware.FirmwareUpdateState
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SerialFirmwareUpdateState
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.requests.UpdateFirmwareRequest
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.requests.UpdateSerialFirmwareRequest
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе (COM-порт на iOS в SDK не реализован).
internal interface FirmwareUpdateDataSource {
    fun updateMainProgram(request: UpdateFirmwareRequest): Flow<SpecialistResponse<FirmwareUpdateState>>?
    fun updateBluetoothModule(request: UpdateFirmwareRequest): Flow<SpecialistResponse<FirmwareUpdateState>>?
    fun updateSerial(request: UpdateSerialFirmwareRequest): Flow<SpecialistResponse<SerialFirmwareUpdateState>>?
}

internal class SpecialistFirmwareUpdateDataSource(private val alarmDevice: () -> BluetoothAlarmDeviceInteractor) :
    FirmwareUpdateDataSource {
    override fun updateMainProgram(request: UpdateFirmwareRequest): Flow<SpecialistResponse<FirmwareUpdateState>>? =
        alarmDevice.resolveOrNull()?.let { interactor ->
            flow { emitAll(interactor.updateAlarmDeviceFirmwareFlowUseCase.execute(request)) }
        }

    override fun updateBluetoothModule(
        request: UpdateFirmwareRequest,
    ): Flow<SpecialistResponse<FirmwareUpdateState>>? = alarmDevice.resolveOrNull()?.let { interactor ->
        flow { emitAll(interactor.updateBluetoothFirmwareFlowUseCase.execute(request)) }
    }

    override fun updateSerial(
        request: UpdateSerialFirmwareRequest,
    ): Flow<SpecialistResponse<SerialFirmwareUpdateState>>? = alarmDevice.resolveOrNull()?.let { interactor ->
        flow { emitAll(interactor.updateSerialDeviceFirmwareFlowUseCase.execute(request)) }
    }
}
