package ru.alarmtrade.specmp.feature.device.interaction.ui.gallery

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host.DeviceStatus
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host.DeviceStatusContent
import java.io.File
import kotlin.test.Test

/** Состояния обёртки поверх дочернего flow; PNG — в `build/state-gallery/interaction`. */
class DeviceStatusGallery {

    private val outDir = File("build/state-gallery/interaction")

    private fun render(name: String, status: DeviceStatus, transport: ConnectionTransport?) =
        StateGallery.render(outDir, name) { DeviceStatusContent(status, transport, deviceName = "DX4G", onExit = {}) }

    @Test
    fun lostUsb() = render("01_lost_usb", DeviceStatus.Disconnected, ConnectionTransport.USB)

    @Test
    fun lostBle() = render("02_lost_ble", DeviceStatus.Disconnected, ConnectionTransport.BLE)

    @Test
    fun lostUnknown() = render("03_lost_unknown_transport", DeviceStatus.Disconnected, null)

    @Test
    fun connecting() = render("04_connecting", DeviceStatus.Connecting, ConnectionTransport.USB)

    @Test
    fun bluetoothBoot() = render("05_bluetooth_boot", DeviceStatus.BluetoothBoot, ConnectionTransport.BLE)

    @Test
    fun firmwareBoot() = render("06_firmware_boot", DeviceStatus.FirmwareBoot, ConnectionTransport.USB)
}
