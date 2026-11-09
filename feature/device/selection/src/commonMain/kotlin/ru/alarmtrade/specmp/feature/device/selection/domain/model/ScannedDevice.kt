package ru.alarmtrade.specmp.feature.device.selection.domain.model

sealed interface ScannedDevice {
    val id: String
    val name: String
    val transport: DeviceTransport

    data class Ble(
        override val id: String,
        override val name: String,
        val kind: DeviceKind,
        val mode: ScannedDeviceMode,
        val rssi: Int,
        val hasDeviceBond: Boolean,
        /** Сопряжено ли устройство с телефоном; `null` — платформа этого не сообщает (iOS). */
        val hasPhoneBond: Boolean?,
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
        val firmwareVersion: String?,
        val isBootMode: Boolean?,
    ) : ScannedDevice {
        override val transport: DeviceTransport get() = DeviceTransport.USB
    }

    /** Блок за портом до подключения не опрашивается — известны только порт и VID:PID адаптера. */
    data class Serial(
        /** Идентификатор порта SDK (desktop — системный путь `/dev/tty…` / `COM3`, Android — путь USB-устройства). */
        override val id: String,
        override val name: String,
        val vendorId: Int?,
        val productId: Int?,
    ) : ScannedDevice {
        override val transport: DeviceTransport get() = DeviceTransport.SERIAL
    }
}

enum class DeviceTransport { BLE, USB, SERIAL }

enum class ScannedDeviceMode { BASE, PAIRING, BOOT }

enum class DeviceKind { ALARM, MICROBOT, TAG, RELAY, IMMOBILIZER, REMOTE_CONTROL, NAVIGATION, MODULE, SENSOR, OTHER }

private const val MAC_DIAL_IN_PREFIX = "/dev/tty."
private const val MAC_CALL_OUT_PREFIX = "/dev/cu."

/** Только USB-адаптеры (у встроенных портов VID нет). На macOS — `/dev/cu.*`, `tty` — только если пары нет. */
fun List<ScannedDevice.Serial>.connectableAdapters(): List<ScannedDevice.Serial> {
    val adapters = filter { it.vendorId != null }
    val callOut = adapters.mapTo(mutableSetOf()) { it.id }
    return adapters.filterNot { port ->
        port.id.startsWith(MAC_DIAL_IN_PREFIX) &&
            MAC_CALL_OUT_PREFIX + port.id.removePrefix(MAC_DIAL_IN_PREFIX) in callOut
    }
}
