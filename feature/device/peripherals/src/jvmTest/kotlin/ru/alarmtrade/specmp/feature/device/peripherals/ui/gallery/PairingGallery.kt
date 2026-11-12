package ru.alarmtrade.specmp.feature.device.peripherals.ui.gallery

import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.DynamicPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing.PairingActions
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing.PairingContent
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing.PairingDialog
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing.PairingOutcome
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing.PairingPhase
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing.PairingSession
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing.PairingState
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing.SlotSheet
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState

/** Состояния «Приживление устройств» (макет D1–D13); PNG — в `build/state-gallery/peripherals`. */
class PairingGallery {

    private val outDir = File("build/state-gallery/peripherals")

    private val slots = listOf(
        slot(0, PeripheralType.TAG, 1, paired = true, connected = true),
        slot(1, PeripheralType.TAG, 2),
        slot(2, PeripheralType.TAG, 3),
        slot(3, PeripheralType.RELAY, 1, paired = true, connected = true),
        slot(4, PeripheralType.RELAY, 2),
        slot(5, PeripheralType.DOOR_SENSOR, 1, paired = true, connected = false),
        slot(6, PeripheralType.PIR_SENSOR, 1, paired = true, connected = true, pir = 4),
        slot(7, PeripheralType.TPMS, 1, paired = true, connected = false),
        slot(8, PeripheralType.TPMS, 2),
    )

    private val ready =
        PairingState(deviceName = "Pandora DX-4GS", systemId = 107131, phase = PairingPhase.READY, slots = slots)

    private fun render(name: String, state: PairingState) = StateGallery.render(outDir, "pairing_$name") {
        PairingContent(state = state, actions = PairingActions())
    }

    @Test
    fun loading() = render("d1_loading", ready.copy(phase = PairingPhase.ENTERING, slots = emptyList()))

    @Test
    fun list() = render("d2_list", ready)

    @Test
    fun allFree() = render("d3_all_free", ready.copy(slots = slots.map { it.copy(paired = false, connected = false) }))

    @Test
    fun actionsFree() = render("d4_free", ready.copy(selected = 1, sheet = SlotSheet.ACTIONS))

    @Test
    fun actionsPaired() = render("d4_paired", ready.copy(selected = 0, sheet = SlotSheet.ACTIONS))

    @Test
    fun address() = render(
        "d5_address",
        ready.copy(selected = 8, sheet = SlotSheet.ADDRESS, address = "3B0Z19", addressError = true),
    )

    @Test
    fun pairingWait() =
        render("d6_waiting", ready.copy(pairing = PairingSession(4, 104, PairingOutcome.WAITING), selected = 4))

    @Test
    fun pairingInstruction() = render(
        "d6_instruction",
        ready.copy(pairing = PairingSession(4, 104, PairingOutcome.WAITING), selected = 4, instructionOpen = true),
    )

    @Test
    fun pairingSuccess() = render(
        "d7_success",
        ready.copy(
            slots = slots.map { if (it.slot == 4) it.copy(paired = true, connected = true, voltage = 2.97f) else it },
            pairing = PairingSession(4, 60, PairingOutcome.SUCCESS),
        ),
    )

    @Test
    fun pairingFailed() = render("d8_failed", ready.copy(pairing = PairingSession(4, 0, PairingOutcome.FAILED)))

    @Test
    fun deleteDialog() = render("d9_delete", ready.copy(dialog = PairingDialog.Delete(0)))

    @Test
    fun deleting() = render("d9_deleting", ready.copy(deleting = setOf(0)))

    @Test
    fun pin() =
        render("d10_pin", ready.copy(phase = PairingPhase.ENTERING, slots = emptyList(), pin = ServicePinDialogState()))

    @Test
    fun refused() = render("d11_refused", ready.copy(phase = PairingPhase.REFUSED, slots = emptyList()))

    @Test
    fun readFailed() = render("d12_read_failed", ready.copy(phase = PairingPhase.READ_FAILED, slots = emptyList()))

    @Test
    fun unsupported() = render("d13_unsupported", ready.copy(dialog = PairingDialog.Unsupported))

    @Test
    fun usb() = render("d2_usb", ready.copy(bluetooth = false))

    @Test
    fun instructionSheet() = render("instruction_relay", ready.copy(selected = 3, sheet = SlotSheet.INSTRUCTION))

    private fun slot(
        slot: Int,
        type: PeripheralType,
        number: Int,
        paired: Boolean = false,
        connected: Boolean = false,
        pir: Int? = null,
    ) = DynamicPeripheral(slot, type, number, paired, connected, if (connected) 3.0f else null, pir)
}
