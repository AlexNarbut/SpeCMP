package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceKind
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode

/**
 * Элемент списка найденных устройств для UI. Собирается из доменной [ScannedDevice].
 * Фото устройств в SDK нет — иконка типа выбирается по транспорту.
 */
@Immutable
sealed interface DeviceUiItem {
    val id: String
    val name: String
    /** Вторая строка: MAC для BLE, «производитель · VID:PID» для USB. */
    val address: String
    val transport: DeviceTransport
    val typeIcon: CmpIcons
    /** systemId блока, если известен при поиске (только USB). */
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
        /** VID:PID USB-дескриптора, hex (`0483:5710`). */
        val vidPid: String,
        override val systemId: Long?,
        val firmwareVersion: String?,
        val isBootMode: Boolean?,
    ) : DeviceUiItem {
        override val transport: DeviceTransport get() = DeviceTransport.USB
        override val typeIcon: CmpIcons get() = CmpIcons.Memory
    }
}

internal fun ScannedDevice.toUiItem(): DeviceUiItem = when (this) {
    is ScannedDevice.Ble -> DeviceUiItem.Ble(
        id = id,
        name = name,
        address = id,
        kind = kind,
        mode = mode,
        rssi = rssi,
        hasDeviceBond = hasDeviceBond,
        hasPhoneBond = hasPhoneBond,
    )

    is ScannedDevice.Usb -> {
        val vidPid = "${vendorId.toHex4()}:${productId.toHex4()}"
        val manufacturer = manufacturerName.ifBlank { "USB" }
        DeviceUiItem.Usb(
            id = id,
            name = name,
            address = "$manufacturer · $vidPid",
            manufacturerName = manufacturer,
            vidPid = vidPid,
            systemId = systemId,
            firmwareVersion = firmwareVersion,
            isBootMode = isBootMode,
        )
    }
}

private fun Int.toHex4(): String = toString(16).uppercase().padStart(4, '0')
