package ru.alarmtrade.specmp.feature.advancedmount.gallery

import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.UnsupportedSections
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.toSections
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root.AdvancedMountRootActions
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root.AdvancedMountRootContent
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root.AdvancedMountRootState
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root.MountContent
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root.MountDialog
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root.title
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.section.SectionStubScreen
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode

/** Все состояния главного экрана расширенного монтажа; PNG — в `build/state-gallery/advanced-mount`. */
class AdvancedMountStatesGallery {

    private val outDir = File("build/state-gallery/advanced-mount")

    private val alarm = AlarmDevice(
        systemId = 107131,
        systemName = "Pandora DX-4GS",
        firmwareVersion = "VER3.57",
        mode = DeviceOperationMode.MAIN,
        features = DeviceFeature.entries.toSet(),
        can = CanInfo(firmwareVersion = "12.05", code = "0A1F", isFactory = true)
    )
    private val module = AlarmDevice(
        systemId = 1076884189,
        systemName = "DX4G",
        firmwareVersion = "VER3.57",
        mode = DeviceOperationMode.MAIN,
        features = setOf(DeviceFeature.FIRMWARE_UPDATE, DeviceFeature.SETTINGS, DeviceFeature.DYNAMIC_DEVICES),
        can = null
    )

    private val alarmReady = AdvancedMountRootState(
        device = alarm,
        transport = ConnectionTransport.BLE,
        imei = "351756051523999",
        content = MountContent.Ready(MountSection.entries.toList(), unsupported = false),
        unreadCount = 3,
        remoteAvailable = true,
        productInfo = true
    )
    private val moduleBase = AdvancedMountRootState(device = module, transport = ConnectionTransport.USB)

    private fun render(name: String, state: AdvancedMountRootState) =
        StateGallery.render(outDir, name) { AdvancedMountRootContent(state, AdvancedMountRootActions()) }

    @Test
    fun loading() = render("01_loading", moduleBase)

    @Test
    fun verifyError() = render("02_verify_error", moduleBase.copy(content = MountContent.VerifyError))

    @Test
    fun denied() = render(
        "03_denied",
        moduleBase.copy(content = MountContent.Denied("Блок заблокирован для данной учётной записи монтажника."))
    )

    @Test
    fun unsupported() = render(
        "04_unsupported",
        moduleBase.copy(
            device = module.copy(features = null),
            content = MountContent.Ready(UnsupportedSections, unsupported = true)
        )
    )

    @Test
    fun modelNotInDatabase() = render(
        "04b_model_not_in_database",
        moduleBase.copy(
            device = module.copy(features = null, modelInDatabase = false),
            content = MountContent.Ready(UnsupportedSections, unsupported = true, modelNotInDatabase = true)
        )
    )

    @Test
    fun moduleReady() = render(
        "05_usb_module_ready",
        moduleBase.copy(
            imei = "351756051523999",
            content = MountContent.Ready(module.features.toSections(), unsupported = false)
        )
    )

    @Test
    fun alarmCanLocked() = render("06_ble_alarm_can_locked_unread_remote", alarmReady)

    @Test
    fun alarmCanFlashed() = render(
        "07_ble_alarm_can_flashed",
        alarmReady.copy(
            device = alarm.copy(can = CanInfo("12.05", "0A1F", isFactory = false)),
            unreadCount = 0,
            remoteAvailable = false
        )
    )

    @Test
    fun canUpdateDialog() = render("08_dialog_can_update", alarmReady.copy(dialog = MountDialog.CAN_UPDATE))

    @Test
    fun canHintDialog() = render("09_dialog_can_hint", alarmReady.copy(dialog = MountDialog.CAN_HINT))

    @Test
    fun bootMode() = render(
        "10_header_boot_mode",
        moduleBase.copy(device = module.copy(mode = DeviceOperationMode.BOOT), content = MountContent.Loading)
    )

    @Test
    fun sectionStub() = StateGallery.render(outDir, "11_section_stub") {
        SectionStubScreen(section = MountSection.VOICE_FIRMWARE, deviceTitle = module.title(), onBack = {})
    }
}
