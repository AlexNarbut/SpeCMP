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
        val kind: DeviceKind,
        val mode: ScannedDeviceMode,
        /** Уровень сигнала, dBm. */
        val rssi: Int,
        val hasDeviceBond: Boolean,
        /** Сопряжено ли устройство с телефоном; `null` — платформа этого не сообщает (iOS). */
        val hasPhoneBond: Boolean?
    ) : ScannedDevice {
        override val transport: DeviceTransport get() = DeviceTransport.BLE
    }

    data class Usb(
        override val id: String,
        override val name: String,
        val manufacturerName: String,
        val vendorId: Int,
        val productId: Int,
        /** systemId блока из опроса SDK; null — блок не опрошен (на Android тогда id — путь устройства). */
        val systemId: Long?,
        /** Версия прошивки из опроса; null — неизвестна. */
        val firmwareVersion: String?,
        /** Блок в загрузчике; null — неизвестно. */
        val isBootMode: Boolean?
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

/** Вид BLE-устройства Pandora (группа типов SDK `DeviceType`) — для подписи в списке. */
enum class DeviceKind { ALARM, TAG, RELAY, IMMOBILIZER, REMOTE_CONTROL, NAVIGATION, MODULE, SENSOR, OTHER }
