package ru.alarmtrade.specmp.feature.device.autostart.ui.gallery

import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone.CloneActions
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone.CloneAttempts
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone.CloneContent
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone.CloneDialog
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone.CloneInterruption
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone.ClonePhase
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone.CloneState
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.pin.ServicePinDialogState

/** Все состояния экрана «Процедура Clone» (макет C1–C6, X1–X3); PNG — в `build/state-gallery/autostart`. */
class CloneGallery {

    private val outDir = File("build/state-gallery/autostart")

    private val base = CloneState(deviceName = "Pandora DX-4GS", deviceId = 107131, attempts = CloneAttempts.Known(3))

    private fun render(name: String, state: CloneState) =
        StateGallery.render(outDir, "clone_$name") { CloneContent(state, CloneActions()) }

    private fun running(stage: CloneStage, inQueue: Boolean = false) =
        base.copy(phase = ClonePhase.Running(stage, inQueue))

    private fun failed(failure: CloneFailure, tips: List<String> = emptyList()) =
        base.copy(attempts = CloneAttempts.Known(2), phase = ClonePhase.Finished(CloneResult.Failed(failure, tips)))

    @Test
    fun start() = render("01_start", base)

    @Test
    fun startZero() = render("02_start_zero", base.copy(attempts = CloneAttempts.Known(0)))

    @Test
    fun startUnknown() = render("03_start_unknown", base.copy(attempts = CloneAttempts.Unknown))

    @Test
    fun confirm() = render("04_confirm", base.copy(dialog = CloneDialog.Confirm))

    @Test
    fun pin() =
        render("05_pin", base.copy(dialog = CloneDialog.Pin(ServicePinDialogState(wrong = true, wrongCount = 2))))

    @Test
    fun stagePreparing() = render("06_stage1", running(CloneStage.PREPARING))

    @Test
    fun stageQueue() = render("07_stage3_queue", running(CloneStage.CALCULATING, inQueue = true))

    @Test
    fun stageWriting() = render("08_stage4_locked", running(CloneStage.WRITING))

    @Test
    fun exit() = render("09_exit", running(CloneStage.READING).copy(dialog = CloneDialog.Exit))

    @Test
    fun success() = render("10_success", base.copy(phase = ClonePhase.Finished(CloneResult.Completed(already = false))))

    @Test
    fun already() = render(
        "11_already",
        base.copy(fromBypass = true, phase = ClonePhase.Finished(CloneResult.Completed(already = true)))
    )

    @Test
    fun notSupported() = render("12_not_supported", base.copy(phase = ClonePhase.Finished(CloneResult.NotSupported)))

    @Test
    fun errorTips() = render(
        "13_error_tips",
        failed(
            CloneFailure.CALC_ERROR,
            listOf("Проверьте, что выбран правильный CAN код автомобиля", "Выполните бесключевой обход повторно")
        )
    )

    @Test
    fun errorDenied() = render("14_error_denied", failed(CloneFailure.DEVICE_DENIED))

    @Test
    fun errorCode() = render("15_error_code", failed(CloneFailure.DATA))

    @Test
    fun interruptedKept() = render(
        "16_interrupted_kept",
        base.copy(phase = ClonePhase.Start(CloneInterruption.ATTEMPT_KEPT))
    )

    @Test
    fun interruptedCheck() = render(
        "17_interrupted_check",
        base.copy(phase = ClonePhase.Start(CloneInterruption.CHECK_ATTEMPTS))
    )
}
