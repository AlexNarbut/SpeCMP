package ru.alarmtrade.specmp.feature.device.selection.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice

/**
 * Фасад над scan-частью Specialist SDK (bluetooth/usb connector interactors).
 * Возвращает доменные модели и `Response<T>`, наружу SDK-типы не выпускает.
 */
interface DeviceScanRepository {
    /** Транспорты, поддерживаемые платформой (источник — `core/specialist`). */
    fun supportedTransports(): Set<DeviceTransport>

    /** Непрерывный поток результатов BLE-сканирования (обновляемый список). */
    fun scanBluetooth(): Flow<Response<List<ScannedDevice.Ble>>>

    /** Непрерывный поток подключённых USB-устройств. */
    fun scanUsb(): Flow<Response<List<ScannedDevice.Usb>>>
}
