package ru.alarmtrade.specmp.feature.device.selection.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SerialPortFilterTest {

    private fun port(id: String, vendorId: Int? = null) =
        ScannedDevice.Serial(id, id.substringAfterLast('/'), vendorId, null)

    @Test
    fun `macOS - only usb adapters, call-out port instead of its dial-in pair`() {
        val ports = listOf(
            port("/dev/cu.debug-console"),
            port("/dev/tty.debug-console"),
            port("/dev/cu.Bluetooth-Incoming-Port"),
            port("/dev/cu.usbmodem5AAF2961781", vendorId = 0x1A86),
            port("/dev/tty.usbmodem5AAF2961781", vendorId = 0x1A86)
        )

        assertEquals(listOf("/dev/cu.usbmodem5AAF2961781"), ports.connectableAdapters().map { it.id })
    }

    @Test
    fun `android and windows usb ports are kept as is`() {
        val ports =
            listOf(port("/dev/bus/usb/002/002", vendorId = 0x1A86), port("COM3", vendorId = 0x10C4), port("COM1"))

        assertEquals(listOf("/dev/bus/usb/002/002", "COM3"), ports.connectableAdapters().map { it.id })
    }
}
