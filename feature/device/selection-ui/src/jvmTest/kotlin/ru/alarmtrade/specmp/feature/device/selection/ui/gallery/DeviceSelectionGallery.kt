package ru.alarmtrade.specmp.feature.device.selection.ui.gallery

import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.core.permissions.DeviceAccessRequirement
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceKind
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.toUiItem
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan.DeviceSelectionActions
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan.DeviceSelectionContent
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan.ScanDeviceState
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan.ScanStep

/** Все состояния экрана поиска устройства; PNG — в `build/state-gallery/selection`. */
class DeviceSelectionGallery {

    private val outDir = File("build/state-gallery/selection")

    private val devices = listOf(
        ScannedDevice.Ble(
            "AC:23:3F:A1:0C:7D",
            "Pandora DX-4GS",
            DeviceKind.ALARM,
            ScannedDeviceMode.BASE,
            -48,
            hasDeviceBond = true,
            hasPhoneBond = true
        ),
        ScannedDevice.Ble(
            "C8:FD:19:22:8B:04",
            "Pandora DXL-5000",
            DeviceKind.ALARM,
            ScannedDeviceMode.PAIRING,
            -67,
            hasDeviceBond = false,
            hasPhoneBond = false
        ),
        ScannedDevice.Usb(
            "usb-1",
            "DX4G",
            "NPO Telemetry",
            0x0483,
            0x5710,
            systemId = 1076884189,
            firmwareVersion = "VER3.57",
            isBootMode = false
        ),
        ScannedDevice.Usb(
            "usb-2",
            "Pandora RMD-4",
            "NPO Telemetry",
            0x0483,
            0x5751,
            systemId = null,
            firmwareVersion = "VER3.42",
            isBootMode = true
        ),
        ScannedDevice.Ble(
            "7A:11:0D:5E:C3:90",
            "Pandora D-032",
            DeviceKind.TAG,
            ScannedDeviceMode.BASE,
            -75,
            hasDeviceBond = false,
            hasPhoneBond = false
        ),
        ScannedDevice.Ble(
            "3C:2E:FF:90:11:AA",
            "Pandora VX-4G",
            DeviceKind.ALARM,
            ScannedDeviceMode.BOOT,
            -83,
            hasDeviceBond = true,
            hasPhoneBond = true
        )
    ).map { it.toUiItem() }

    private val base = ScanDeviceState(
        step = ScanStep.Scanning,
        requirements = listOf(DeviceAccessRequirement.BLUETOOTH, DeviceAccessRequirement.USB),
        supportedTransports = setOf(DeviceTransport.BLE, DeviceTransport.USB)
    )
    private val list = base.copy(devices = LCEState.Content(devices))

    private fun render(name: String, state: ScanDeviceState, infoId: String? = null) =
        StateGallery.render(outDir, name) {
            DeviceSelectionContent(state, DeviceSelectionActions(), initialInfoId = infoId)
        }

    @Test
    fun permissionCheck() = render("01_permission_check", base.copy(step = ScanStep.PermissionCheck))

    @Test
    fun deniedSettings() = render("02_denied_open_settings", base.copy(step = ScanStep.Denied(canOpenSettings = true)))

    @Test
    fun deniedRetry() = render("03_denied_retry", base.copy(step = ScanStep.Denied(canOpenSettings = false)))

    @Test
    fun adapterOff() = render("04_bluetooth_off", base.copy(step = ScanStep.AdapterOff))

    @Test
    fun locationOff() = render("05_location_off", base.copy(step = ScanStep.LocationOff))

    @Test
    fun scanning() = render("06_scanning", base)

    @Test
    fun scanningBleOnly() = render("06b_scanning_ble_only", base.copy(supportedTransports = setOf(DeviceTransport.BLE)))

    @Test
    fun scanningUsbOnly() = render("06c_scanning_usb_only", base.copy(supportedTransports = setOf(DeviceTransport.USB)))

    @Test
    fun empty() = render("07_empty", base.copy(devices = LCEState.Content(emptyList()), searchedLongEnough = true))

    @Test
    fun listNothingSelected() = render("08_list", list)

    @Test
    fun splitBleSelected() = render("09_split_ble_selected", list.copy(selectedId = "C8:FD:19:22:8B:04"))

    @Test
    fun splitUsbBootSelected() = render("10_split_usb_boot_no_system_id", list.copy(selectedId = "usb-2"))

    @Test
    fun infoDialogBle() = render("11_info_dialog_ble", list, infoId = "AC:23:3F:A1:0C:7D")

    @Test
    fun infoDialogUsb() = render("12_info_dialog_usb", list, infoId = "usb-1")

    @Test
    fun connecting() = render("13_connecting", list.copy(step = ScanStep.Connecting(devices[2])))
}
