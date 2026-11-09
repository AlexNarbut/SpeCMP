package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceKind
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode

@Immutable
sealed interface DeviceUiItem {
    val id: String
    val name: String

    val address: String
    val transport: DeviceTransport
    val typeIcon: CmpIcons

    val systemId: Long?

    @Immutable
    data class Ble(
        override val id: String,
        override val name: String,
        override val address: String,
        val kind: DeviceKind,
        val mode: ScannedDeviceMode,
        val rssi: Int,
        val hasDeviceBond: Boolean,
        /** `null` — платформа не сообщает о сопряжении с телефоном (iOS). */
        val hasPhoneBond: Boolean?,
        val restricted: Boolean = false,
    ) : DeviceUiItem {
        override val transport: DeviceTransport get() = DeviceTransport.BLE
        override val typeIcon: CmpIcons get() = CmpIcons.Sensors
        override val systemId: Long? get() = null
    }

    @Immutable
    data class Usb(
        override val id: String,
        override val name: String,
        override val address: String,
        val manufacturerName: String,
        val vidPid: String,
        override val systemId: Long?,
        val firmwareVersion: String?,
        val isBootMode: Boolean?,
    ) : DeviceUiItem {
        override val transport: DeviceTransport get() = DeviceTransport.USB
        override val typeIcon: CmpIcons get() = CmpIcons.Memory
    }

    @Immutable
    data class Serial(
        override val id: String,
        override val name: String,
        override val address: String,
        val vidPid: String?,
    ) : DeviceUiItem {
        override val transport: DeviceTransport get() = DeviceTransport.SERIAL
        override val typeIcon: CmpIcons get() = CmpIcons.Cable
        override val systemId: Long? get() = null
    }
}

/** RSSI дрожит на 1–2 dBm почти на каждый пакет: без округления каждый эмит скана перерисовывал бы список. */
private const val RSSI_STEP_DBM = 5

private fun Int.roundToStep(step: Int): Int = (toDouble() / step).roundToInt() * step

internal fun ScannedDevice.toUiItem(): DeviceUiItem = when (this) {
    is ScannedDevice.Ble -> toUiItem(restricted = false)

    is ScannedDevice.Usb -> {
        val vidPid = "${vendorId.toHex4()}:${productId.toHex4()}"
        DeviceUiItem.Usb(
            id = id,
            name = name,
            address = systemId?.toString() ?: "HID · $vidPid",
            manufacturerName = manufacturerName.ifBlank { "USB" },
            vidPid = vidPid,
            systemId = systemId,
            firmwareVersion = firmwareVersion?.toDisplayFirmware(),
            isBootMode = isBootMode,
        )
    }

    is ScannedDevice.Serial -> toUiItem()
}

internal fun ScannedDevice.Ble.toUiItem(restricted: Boolean): DeviceUiItem.Ble = DeviceUiItem.Ble(
    id = id,
    name = name,
    address = id,
    kind = kind,
    mode = mode,
    rssi = rssi.roundToStep(RSSI_STEP_DBM),
    hasDeviceBond = hasDeviceBond,
    hasPhoneBond = hasPhoneBond,
    restricted = restricted,
)

internal fun ScannedDevice.Serial.toUiItem(): DeviceUiItem.Serial {
    val vidPid = if (vendorId != null && productId != null) "${vendorId.toHex4()}:${productId.toHex4()}" else null
    return DeviceUiItem.Serial(
        id = id,
        name = name,
        address = if (name != id) id else vidPid.orEmpty(),
        vidPid = vidPid,
    )
}

/** У USB вид до подключения неизвестен — его проверяет сценарий после подключения. */
internal val DeviceUiItem.restricted: Boolean get() = this is DeviceUiItem.Ble && restricted

internal fun String.toDisplayFirmware(): String = removePrefix("VER").trim()

private fun Int.toHex4(): String = toString(radix = 16).uppercase().padStart(length = 4, padChar = '0')
