package ru.alarmtrade.specmp.feature.device.selection.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice

interface DeviceScanRepository {
    fun scanBluetooth(): Flow<Response<List<ScannedDevice.Ble>>>

    fun scanUsb(): Flow<Response<List<ScannedDevice.Usb>>>

    fun scanSerial(): Flow<Response<List<ScannedDevice.Serial>>>
}
