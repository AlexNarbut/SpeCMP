package ru.alarmtrade.specmp.feature.device.selection.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice

/**
 * Фасад над scan-частью Specialist SDK (bluetooth/usb/serial connector interactors).
 * Возвращает доменные модели и `Response<T>`, наружу SDK-типы не выпускает.
 */
interface DeviceScanRepository {
    /** Непрерывный поток результатов BLE-сканирования (обновляемый список). */
    fun scanBluetooth(): Flow<Response<List<ScannedDevice.Ble>>>

    /** Непрерывный поток подключённых USB-устройств. */
    fun scanUsb(): Flow<Response<List<ScannedDevice.Usb>>>

    /** Непрерывный поток COM-портов (платформа без COM — пустой список). */
    fun scanSerial(): Flow<Response<List<ScannedDevice.Serial>>>
}
