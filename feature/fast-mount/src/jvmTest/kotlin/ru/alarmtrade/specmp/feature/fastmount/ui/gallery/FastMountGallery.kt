package ru.alarmtrade.specmp.feature.fastmount.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountFailure
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountFile
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountImage
import ru.alarmtrade.specmp.feature.fastmount.domain.model.fastMountPlan
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.model.FastMountError
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.FastMountContent
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.FastMountState
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.FixedDevice
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.MasterActions
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.MasterState
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.MountMapState
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.PrepActions
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.RebootWait
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.SummaryLoad
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.UnitCheck
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.UnitInfo
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.UnitVerdict
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.UnsupportedReason
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.WizardDialog
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.WizardScreen
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.WizardToast

/** Все состояния мастера быстрого монтажа (F2–F17, W1); PNG — в `build/state-gallery/fast-mount`. */
class FastMountGallery {

    private val outDir = File("build/state-gallery/fast-mount")

    private val fixed = FixedDevice(107131, "DX-4GS", "DX-4GS  107131", ConnectionTransport.BLE)
    private val unit = UnitInfo(firmware = "3.55", canCode = "0213", canVersion = "1.4", canFactory = false)
    private val car = MountCar(1, "Volkswagen", "Polo", "2020", "0213")
    private val configuration = FastMountConfiguration(
        id = 42,
        name = "Polo VI 2020 — автозапуск, обход по CAN",
        shortDescription = "",
        fullDescription =
            "Автозапуск по CAN с бесключевым обходом. Управление штатным брелоком (Slave), закрытие окон " +
                "при постановке в охрану. Питание и масса — у блока предохранителей водителя.",
        deviceName = "DX-4GS",
        author = "Алексей Морозов",
        rating = 5,
        mountingMap = MountFile("polo.pdf", "https://example.com/polo.pdf"),
        images = listOf(
            MountImage("CAN — разъём за бардачком", "", null, ""),
            MountImage("Питание +12 В — предохранитель F44", "", null, ""),
            MountImage("Масса — болт под рулевой колонкой", "", null, "")
        ),
        keyBypass = true,
        slave = true,
        questions = emptyList()
    )
    private val ready = FastMountState(
        fixed = fixed,
        unit = unit,
        check = UnitCheck.Done(UnitVerdict.Ready),
        car = car,
        configuration = configuration,
        plan = fastMountPlan(configuration, ConnectionTransport.BLE),
        summary = SummaryLoad.Ready
    )

    private fun master(step: FastMountStep?, master: MasterState = MasterState()) =
        ready.copy(screen = WizardScreen.MASTER, master = master.copy(step = step))

    private fun render(name: String, state: FastMountState) = StateGallery.render(outDir, name) { Wizard(state) }

    @Test
    fun preparation() {
        render("F3a-check-ready", ready)
        render("F3b-check-outdated", ready.copy(check = UnitCheck.Done(UnitVerdict.Outdated("3.57"))))
        render(
            "F3c-check-factory",
            ready.copy(
                check = UnitCheck.Done(UnitVerdict.FactoryCan),
                unit = unit.copy(canCode = "0000", canFactory = true)
            )
        )
        render("F3d-check-no-access", ready.copy(check = UnitCheck.Done(UnitVerdict.NoAccess(""))))
        render("F3-checking", ready.copy(check = UnitCheck.Checking))
        render("F2-unsupported", ready.copy(check = UnitCheck.Unsupported(UnsupportedReason.NOT_IN_DATABASE)))
        render("F5a-summary", ready.copy(screen = WizardScreen.SUMMARY))
        render(
            "F5b-summary-usb",
            ready.copy(
                screen = WizardScreen.SUMMARY,
                fixed = fixed.copy(transport = ConnectionTransport.USB),
                plan = fastMountPlan(configuration, ConnectionTransport.USB)
            )
        )
        render("FV-vehicle-slot", ready.copy(screen = WizardScreen.VEHICLE))
    }

    @Test
    fun master() {
        render("F6a-starting", master(null, MasterState(starting = true)))
        render("F6b-start-error", master(null, MasterState(startError = FastMountError.Common(ApiError.NoConnection))))
        render("F7a-writing", master(FastMountStep.WRITING_SETTINGS, MasterState(busy = true)))
        render("F7b-write-error", master(FastMountStep.WRITING_SETTINGS, MasterState(error = FastMountError.Unknown)))
        render(
            "F8a-mount-downloading",
            master(FastMountStep.MOUNT, MasterState(mountMap = MountMapState.Downloading))
        )
        render(
            "F8b-mount-ready-toast",
            master(
                FastMountStep.MOUNT,
                MasterState(mountMap = MountMapState.Ready(ByteArray(3_400_000)), toast = WizardToast.SETTINGS_WRITTEN)
            )
        )
        render("F8c-mount-map-error", master(FastMountStep.MOUNT, MasterState(mountMap = MountMapState.Failed)))
        render("F9-reboot", master(FastMountStep.MOUNT, MasterState(reboot = RebootWait(FastMountStep.MOUNT))))
        render("F10-bypass-slot", master(FastMountStep.KEY_BYPASS))
        render("F11-slave", master(FastMountStep.SLAVE, MasterState(slaveDone = true, cloneNeeded = false)))
        render(
            "F13a-review",
            master(FastMountStep.FEEDBACK, MasterState(cloneNeeded = false, mountMinutes = 84, rating = 4))
        )
        render(
            "F17-review-error",
            master(
                FastMountStep.FEEDBACK,
                MasterState(
                    cloneNeeded = false,
                    rating = 5,
                    feedback = "Всё по карте",
                    error = FastMountError.Step(FastMountFailure.FeedbackRequired)
                )
            )
        )
        render("F14-done", master(FastMountStep.FINISH, MasterState(cloneNeeded = false)))
    }

    @Test
    fun overlays() {
        render("F15-wrong-device", master(FastMountStep.MOUNT).copy(wrongDevice = "DX-4GS  204588"))
        render("F16a-exit", master(FastMountStep.MOUNT).copy(dialog = WizardDialog.EXIT))
        render("F16d-mount-done", master(FastMountStep.MOUNT).copy(dialog = WizardDialog.MOUNT_DONE))
    }
}

@Composable
private fun Wizard(state: FastMountState) {
    FastMountContent(
        state = state,
        prep = PrepActions(),
        master = MasterActions(),
        slotContent = { slot -> SlotStub(slot.name) },
        onDialogConfirm = {},
        onDialogDismiss = {},
        onToastHide = {},
        onExit = {},
        rebootSecondsOverride = 12
    )
}

@Composable
private fun SlotStub(name: String) {
    CmpBox(
        modifier = Modifier.fillMaxSize().background(cmpThemeColors().surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        CmpText(text = "Экран фичи: $name")
    }
}
