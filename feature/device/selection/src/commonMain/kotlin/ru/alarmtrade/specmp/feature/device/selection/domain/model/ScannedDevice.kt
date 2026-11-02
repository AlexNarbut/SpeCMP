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

    /**
     * COM-порт (USB-UART адаптер или порт ПК). Блок за портом до подключения не опрашивается — известны только порт
     * и VID:PID адаптера (если платформа их сообщает). Ищется, только если поиск запущен с COM-портами.
     */
    data class Serial(
        /** Идентификатор порта SDK (desktop — системный путь `/dev/tty…` / `COM3`, Android — путь USB-устройства). */
        override val id: String,
        override val name: String,
        val vendorId: Int?,
        val productId: Int?
    ) : ScannedDevice {
        override val transport: DeviceTransport get() = DeviceTransport.SERIAL
    }
}

enum class DeviceTransport { BLE, USB, SERIAL }

/**
 * Режим найденного BLE-устройства. Определяет ветку подключения:
 * рабочий режим (BASE/PAIRING) → чтение информации об устройстве,
 * загрузчик (BOOT) → работа с manufacturer data / прошивкой.
 */
enum class ScannedDeviceMode { BASE, PAIRING, BOOT }

/** Вид BLE-устройства Pandora (группа типов SDK `DeviceType`) — для подписи в списке. */
enum class DeviceKind { ALARM, MICROBOT, TAG, RELAY, IMMOBILIZER, REMOTE_CONTROL, NAVIGATION, MODULE, SENSOR, OTHER }

private const val MAC_DIAL_IN_PREFIX = "/dev/tty."
private const val MAC_CALL_OUT_PREFIX = "/dev/cu."

/**
 * COM-порты, к которым может быть подключён блок: только USB-адаптеры (у них есть VID — встроенные порты
 * `debug-console`, Bluetooth-гарнитуры и т.п. его не имеют). На macOS каждый порт виден дважды — `/dev/cu.*` и
 * `/dev/tty.*` («Dial-In», ждёт сигнал модема): оставляем `cu`, `tty` — только если пары нет.
 */
fun List<ScannedDevice.Serial>.connectableAdapters(): List<ScannedDevice.Serial> {
    val adapters = filter { it.vendorId != null }
    val callOut = adapters.mapTo(mutableSetOf()) { it.id }
    return adapters.filterNot { port ->
        port.id.startsWith(MAC_DIAL_IN_PREFIX) &&
            MAC_CALL_OUT_PREFIX + port.id.removePrefix(MAC_DIAL_IN_PREFIX) in callOut
    }
}
