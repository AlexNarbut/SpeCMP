package ru.alarmtrade.specmp.feature.device.autostart.ui.gallery

import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStep
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStepKind
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassSubject
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassActions
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassContent
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassDialog
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassPhase
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassState
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState

/** Состояния экрана «Бесключевой обход» (макет B1–B8, X1, X3); PNG — в `build/state-gallery/autostart`. */
class BypassGallery {

    private val outDir = File("build/state-gallery/autostart")

    private val base = BypassState(deviceName = "Pandora DX-4GS", deviceId = 107131, cloneAvailable = true)

    private fun render(name: String, state: BypassState) =
        StateGallery.render(outDir, "bypass_$name") { BypassContent(state, BypassActions()) }

    private fun step(kind: BypassStepKind, subject: BypassSubject) =
        base.copy(phase = BypassPhase.Running(BypassStep(kind, subject)))

    private fun result(result: BypassResult) = base.copy(phase = BypassPhase.Finished(result))

    @Test
    fun start() = render("01_start", base)

    @Test
    fun startCanUsb() = render("02_start_can_usb", base.copy(canCode = "VWPOLO20", usb = true))

    @Test
    fun interrupted() = render("03_interrupted", base.copy(phase = BypassPhase.Start(interrupted = true)))

    @Test
    fun pin() = render("04_pin", base.copy(dialog = BypassDialog.Pin(ServicePinDialogState())))

    @Test
    fun action() = render("05_action_ignition_on", step(BypassStepKind.ACTION, BypassSubject.IGNITION_ON))

    @Test
    fun accepted() = render("06_accepted_hood_open", step(BypassStepKind.ACCEPTED, BypassSubject.HOOD_OPEN))

    @Test
    fun waitEcu() = render("07_wait_ecu", step(BypassStepKind.WAIT, BypassSubject.ECU_EXCHANGE))

    @Test
    fun criticalLadaCan() = render("08_critical_lada_can", step(BypassStepKind.CRITICAL, BypassSubject.LADA_CAN))

    @Test
    fun instructionLada() = render("09_instruction_lada", step(BypassStepKind.INSTRUCTION, BypassSubject.LADA_APS))

    @Test
    fun instructionToyota() =
        render("10_instruction_toyota", step(BypassStepKind.INSTRUCTION, BypassSubject.TOYOTA_DAIHATSU))

    @Test
    fun success() = render("11_success", result(BypassResult.Success))

    @Test
    fun cloneRequired() = render("12_clone_required", result(BypassResult.CloneRequired))

    @Test
    fun notSupported() = render("13_not_supported", result(BypassResult.NotSupported))

    @Test
    fun errorDoor() = render("14_error_door", result(BypassResult.Error(BypassError.MERCEDES_DRIVER_DOOR)))

    @Test
    fun errorGeneral() = render("15_error_general", result(BypassResult.Error(BypassError.GENERAL)))

    @Test
    fun errorVagCode() = render("16_error_vag_code", result(BypassResult.Error(BypassError.VAG_ECU_EXCHANGE, 7)))

    @Test
    fun exit() = render(
        "17_exit",
        step(BypassStepKind.ACTION, BypassSubject.ENGINE_START).copy(dialog = BypassDialog.Exit),
    )
}
