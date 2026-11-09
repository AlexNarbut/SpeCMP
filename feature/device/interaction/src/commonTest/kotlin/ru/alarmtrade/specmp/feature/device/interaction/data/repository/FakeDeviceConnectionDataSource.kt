package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.bluetoothControl.domain.bluetooth.ConnectingStatus
import ru.alarmtrade.bluetoothControl.interactors.bluetoothConnector.requests.ConnectToBluetoothDeviceRequest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.serial.SerialPortConnectingStatus
import ru.alarmtrade.specialist.domain.usb.UsbConnectingState
import ru.alarmtrade.specialist.interactors.serialConnector.requests.ConnectToSerialPortRequest
import ru.alarmtrade.specialist.interactors.usbConnector.requests.ConnectToUsbDeviceRequest
import ru.alarmtrade.specmp.feature.device.interaction.data.datasource.DeviceConnectionDataSource

internal class FakeDeviceConnectionDataSource : DeviceConnectionDataSource {
    var bluetooth: Flow<SpecialistResponse<ConnectingStatus>>? = null
    var usb: Flow<SpecialistResponse<UsbConnectingState>>? = null
    var serial: Flow<SpecialistResponse<SerialPortConnectingStatus>>? = null
    var closeBluetooth: SpecialistResponse<Unit>? = SpecialistResponse.Success(Unit)
    var closeUsb: SpecialistResponse<Unit>? = SpecialistResponse.Success(Unit)
    var closeSerial: SpecialistResponse<Unit>? = SpecialistResponse.Success(Unit)

    val bluetoothRequests = mutableListOf<ConnectToBluetoothDeviceRequest>()
    val serialRequests = mutableListOf<ConnectToSerialPortRequest>()
    var scanStops = 0

    override fun connectBluetooth(
        request: ConnectToBluetoothDeviceRequest,
    ): Flow<SpecialistResponse<ConnectingStatus>>? {
        bluetoothRequests += request
        return bluetooth
    }

    override suspend fun stopBluetoothScan(): SpecialistResponse<Unit>? {
        scanStops++
        return SpecialistResponse.Success(Unit)
    }

    override fun connectUsb(request: ConnectToUsbDeviceRequest): Flow<SpecialistResponse<UsbConnectingState>>? = usb

    override fun connectSerial(
        request: ConnectToSerialPortRequest,
    ): Flow<SpecialistResponse<SerialPortConnectingStatus>>? {
        serialRequests += request
        return serial
    }

    override suspend fun closeBluetoothSession(): SpecialistResponse<Unit>? = closeBluetooth

    override suspend fun closeUsbSession(): SpecialistResponse<Unit>? = closeUsb

    override suspend fun closeSerialSession(): SpecialistResponse<Unit>? = closeSerial
}
