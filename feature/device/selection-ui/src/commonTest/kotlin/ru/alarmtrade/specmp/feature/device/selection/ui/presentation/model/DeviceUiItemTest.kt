package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice

class DeviceUiItemTest {

    private fun usb(systemId: Long?, firmwareVersion: String?) = ScannedDevice.Usb(
        id = "usb-1",
        name = "DX4G",
        manufacturerName = "NPO Telemetry",
        vendorId = 0x0483,
        productId = 0x5710,
        systemId = systemId,
        firmwareVersion = firmwareVersion,
        isBootMode = false
    )

    @Test
    fun `usb address is systemId when the unit answered`() {
        val item = assertIs<DeviceUiItem.Usb>(usb(systemId = 1076884189, firmwareVersion = "VER3.57").toUiItem())

        assertEquals("1076884189", item.address)
        assertEquals("0483:5710", item.vidPid)
        assertEquals("3.57", item.firmwareVersion)
    }

    @Test
    fun `usb address falls back to HID and VID PID without systemId`() {
        val item = assertIs<DeviceUiItem.Usb>(usb(systemId = null, firmwareVersion = null).toUiItem())

        assertEquals("HID · 0483:5710", item.address)
        assertNull(item.firmwareVersion)
    }

    @Test
    fun `firmware without VER prefix is kept as is`() {
        assertEquals("3.57", "3.57".toDisplayFirmware())
        assertEquals("3.57", "VER 3.57".toDisplayFirmware())
    }
}
