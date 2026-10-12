package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode

/**
 * Элемент списка найденных устройств для UI. Собирается из доменной [ScannedDevice];
 * RSSI/фото в SDK-скане отсутствуют — по грациозной деградации не показываем их
 * (иконка типа выбирается по транспорту, адрес — MAC/uuid для BLE и производитель для USB).
 */
@Immutable
sealed interface DeviceUiItem {
    val id: String
    val name: String
    val address: String
    val transport: DeviceTransport
    val typeIcon: CmpIcons

    @Immutable
    data class Ble(
        override val id: String,
        override val name: String,
        override val address: String,
        val mode: ScannedDeviceMode,
        val hasDeviceBond: Boolean,
        /** `null` — платформа не сообщает о сопряжении с телефоном (iOS). */
        val hasPhoneBond: Boolean?,
    ) : DeviceUiItem {
        override val transport: DeviceTransport get() = DeviceTransport.BLE
        override val typeIcon: CmpIcons get() = CmpIcons.Sensors
    }

    @Immutable
    data class Usb(
        override val id: String,
        override val name: String,
        override val address: String,
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
        mode = mode,
        hasDeviceBond = hasDeviceBond,
        hasPhoneBond = hasPhoneBond,
    )

    is ScannedDevice.Usb -> DeviceUiItem.Usb(
        id = id,
        name = name,
        address = manufacturerName.ifBlank { "USB" },
    )
}
