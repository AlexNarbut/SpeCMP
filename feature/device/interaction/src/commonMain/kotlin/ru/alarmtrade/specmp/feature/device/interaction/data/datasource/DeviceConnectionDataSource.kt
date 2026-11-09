package ru.alarmtrade.specmp.feature.device.interaction.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.bluetoothControl.domain.bluetooth.ConnectingStatus
import ru.alarmtrade.bluetoothControl.interactors.bluetoothConnector.requests.ConnectToBluetoothDeviceRequest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.serial.SerialPortConnectingStatus
import ru.alarmtrade.specialist.domain.usb.UsbConnectingState
import ru.alarmtrade.specialist.interactors.bluetoothConnector.BluetoothConnectorInteractor
import ru.alarmtrade.specialist.interactors.serialConnector.SerialConnectorInteractor
import ru.alarmtrade.specialist.interactors.serialConnector.requests.ConnectToSerialPortRequest
import ru.alarmtrade.specialist.interactors.usbConnector.UsbConnectorInteractor
import ru.alarmtrade.specialist.interactors.usbConnector.requests.ConnectToUsbDeviceRequest
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор транспорта недоступен на платформе.
internal interface DeviceConnectionDataSource {
    fun connectBluetooth(request: ConnectToBluetoothDeviceRequest): Flow<SpecialistResponse<ConnectingStatus>>?
    suspend fun stopBluetoothScan(): SpecialistResponse<Unit>?
    fun connectUsb(request: ConnectToUsbDeviceRequest): Flow<SpecialistResponse<UsbConnectingState>>?
    fun connectSerial(request: ConnectToSerialPortRequest): Flow<SpecialistResponse<SerialPortConnectingStatus>>?
    suspend fun closeBluetoothSession(): SpecialistResponse<Unit>?
    suspend fun closeUsbSession(): SpecialistResponse<Unit>?
    suspend fun closeSerialSession(): SpecialistResponse<Unit>?
}

internal class SpecialistDeviceConnectionDataSource(
    private val bluetooth: () -> BluetoothConnectorInteractor,
    private val usb: () -> UsbConnectorInteractor,
    private val serial: () -> SerialConnectorInteractor,
) : DeviceConnectionDataSource {
    override fun connectBluetooth(
        request: ConnectToBluetoothDeviceRequest,
    ): Flow<SpecialistResponse<ConnectingStatus>>? = bluetooth.resolveOrNull()?.let { interactor ->
        flow { emitAll(interactor.openBluetoothAlarmDeviceSessionFlowableUseCase.execute(request)) }
    }

    override suspend fun stopBluetoothScan(): SpecialistResponse<Unit>? =
        bluetooth.resolveOrNull()?.stopBluetoothScanUseCase?.execute()

    override fun connectUsb(request: ConnectToUsbDeviceRequest): Flow<SpecialistResponse<UsbConnectingState>>? =
        usb.resolveOrNull()?.let { interactor ->
            flow { emitAll(interactor.openUsbDeviceSessionFlowableUseCase.execute(request)) }
        }

    override fun connectSerial(
        request: ConnectToSerialPortRequest,
    ): Flow<SpecialistResponse<SerialPortConnectingStatus>>? = serial.resolveOrNull()?.let { interactor ->
        flow { emitAll(interactor.openSerialPortSessionFlowableUseCase.execute(request)) }
    }

    override suspend fun closeBluetoothSession(): SpecialistResponse<Unit>? =
        bluetooth.resolveOrNull()?.closeBluetoothDeviceSessionUseCase?.execute()

    override suspend fun closeUsbSession(): SpecialistResponse<Unit>? =
        usb.resolveOrNull()?.closeUsbDeviceSessionUseCase?.execute()

    override suspend fun closeSerialSession(): SpecialistResponse<Unit>? =
        serial.resolveOrNull()?.closeSerialDeviceSessionUseCase?.execute()
}
