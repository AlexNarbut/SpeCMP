package ru.alarmtrade.specmp.feature.device.peripherals.ui.gallery

import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralDetails
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralLink
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheralTypes
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.DeviceGroup
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.FirmwareFailure
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.FirmwareStep
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.SystemDevicesActions
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.SystemDevicesContent
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.SystemDevicesState
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.key
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.summaryOf

/** Состояния «Устройства системы» (макет U1–U7); PNG — в `build/state-gallery/peripherals`. */
class SystemDevicesGallery {

    private val outDir = File("build/state-gallery/peripherals")

    private val devices = listOf(
        device(PeripheralType.TAG, 1, "A1B2C3D4E5F6", PeripheralLink.NEAR, -58, 3.0f),
        device(
            PeripheralType.TAG,
            2,
            "A1B2C3D4E5F7",
            PeripheralLink.FAR,
            -87,
            2.41f,
            low = true,
            details = PeripheralDetails.Tag(pressed = false, blinking = true),
        ),
        device(
            PeripheralType.RELAY,
            1,
            "0C7D19A4B2E1",
            PeripheralLink.ONLINE,
            -67,
            2.95f,
            details = PeripheralDetails.Relay(blocked = true, serviceMode = false),
        ),
        device(
            PeripheralType.HOOD_LOCK,
            1,
            "5E21AA0F9C34",
            PeripheralLink.ONLINE,
            -72,
            null,
            details = PeripheralDetails.HoodLock(
                locked = true,
                hoodOpened = false,
                serviceMode = false,
                temperature = 18,
                triggerCount = 124,
            ),
        ),
        device(PeripheralType.DOOR_SENSOR, 1, "7F3302C1D8E0", PeripheralLink.OFFLINE, null, null),
        device(
            PeripheralType.TPMS,
            1,
            "3B0019F2A1C4",
            PeripheralLink.PAIRED,
            null,
            3.0f,
            details = PeripheralDetails.Tpms(pressureBar = 2.3f, temperature = 21),
        ),
        device(
            PeripheralType.FUEL_SENSOR,
            1,
            "1A9E44B07C25",
            PeripheralLink.ONLINE,
            -63,
            3.4f,
            details = PeripheralDetails.FuelSensor(liters = 42, percent = 64, temperature = 19),
        ),
    )

    private fun SystemDevicesState.withDevices(list: List<SystemPeripheral>): SystemDevicesState {
        val groups = SystemPeripheralTypes.mapNotNull { type ->
            list.filter { it.type == type }.takeIf { it.isNotEmpty() }?.let { DeviceGroup(type, it) }
        }
        val flat = groups.flatMap { it.devices }
        return copy(groups = groups, devices = flat, summary = summaryOf(flat))
    }

    private val base = SystemDevicesState(
        deviceName = "Pandora DX-4GS",
        systemId = 107131,
        firmwareVersion = "3.57",
        bluetoothVersion = "4.35",
        canOpenPairing = true,
    )

    private val done = base.withDevices(devices).copy(
        polled = SystemPeripheralTypes.size,
        firstPassDone = true,
    )

    private fun render(name: String, state: SystemDevicesState) = StateGallery.render(outDir, "system_$name") {
        SystemDevicesContent(state = state, actions = SystemDevicesActions())
    }

    @Test
    fun firstPoll() = render("u1_first_poll", base.copy(pollingType = PeripheralType.TAG))

    @Test
    fun filling() = render(
        "u2_filling",
        base.withDevices(devices.take(3)).copy(
            polled = 6,
            pollingType = PeripheralType.HOOD_LOCK,
        ),
    )

    @Test
    fun fullList() = render("u3_full", done)

    @Test
    fun detailsTag() = render("u4_details_tag", done.copy(selected = devices[1].key, detailsOpen = true))

    @Test
    fun detailsTpms() = render("u4_details_tpms", done.copy(selected = devices[5].key, detailsOpen = true))

    @Test
    fun empty() = render("u5_empty", base.copy(polled = SystemPeripheralTypes.size, firstPassDone = true))

    @Test
    fun allOnline() = render("u3_all_online", done.withDevices(listOf(devices[0], devices[2])))

    @Test
    fun firmwareConfirm() = render("u7_confirm", done.copy(firmware = FirmwareStep.Confirm(devices[1].key)))

    @Test
    fun firmwareWaiting() = render("u7_waiting", done.copy(firmware = FirmwareStep.Waiting(devices[1].key)))

    @Test
    fun firmwareFailed() = render(
        "u7_failed",
        done.copy(firmware = FirmwareStep.Failed(devices[1].key, FirmwareFailure.NO_ANSWER)),
    )

    @Test
    fun pollFailed() = render("poll_failed", base.copy(pollFailed = true))

    private fun device(
        type: PeripheralType,
        number: Int,
        address: String,
        link: PeripheralLink,
        signal: Int?,
        voltage: Float?,
        low: Boolean = false,
        details: PeripheralDetails? = null,
    ) = SystemPeripheral(type, number, address, link, signal, voltage, low, details)
}
