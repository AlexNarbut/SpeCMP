package ru.alarmtrade.specmp.feature.device.selection.domain.model

/**
 * Доменное представление найденного при сканировании устройства.
 * Скрывает SDK-типы (`BleDeviceScanResult`, `AttachedUsbDeviceInformation`),
 * наружу выходят только нужные для выбора поля.
 */
sealed interface ScannedDevice {
    /** Стабильный идентификатор для выбора/подключения (BLE — uuid, USB — id). */
    val id: String
    val name: String
    val transport: DeviceTransport

    data class Ble(
        override val id: String,
        override val name: String,
        val deviceType: String,
        val mode: ScannedDeviceMode,
        val hasDeviceBond: Boolean,
        val hasPhoneBond: Boolean,
    ) : ScannedDevice {
        override val transport: DeviceTransport get() = DeviceTransport.BLE
    }

    data class Usb(
        override val id: String,
        override val name: String,
        val manufacturerName: String,
        val productId: Int,
    ) : ScannedDevice {
        override val transport: DeviceTransport get() = DeviceTransport.USB
    }
}

enum class DeviceTransport { BLE, USB }

/**
 * Режим найденного BLE-устройства. Определяет ветку подключения:
 * рабочий режим (BASE/PAIRING) → чтение информации об устройстве,
 * загрузчик (BOOT) → работа с manufacturer data / прошивкой.
 */
enum class ScannedDeviceMode { BASE, PAIRING, BOOT }
