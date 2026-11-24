package ru.alarmtrade.specmp.feature.device.firmware.ui.gallery

import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial.SerialFileProblem
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial.SerialFirmwareActions
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial.SerialFirmwareContent
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial.SerialFirmwareState
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial.SerialPhase
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial.SerialSourceError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial.SystemNameMismatch
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareSources
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode

/** Фазы экрана прошивки по COM-порту; PNG — в `build/state-gallery/firmware-serial`. */
class SerialFirmwareGallery {

    private val outDir = File("build/state-gallery/firmware-serial")

    private val device = AlarmDevice(
        systemId = 67305985,
        systemName = "grc-key",
        firmwareVersion = "VER1.00",
        mode = DeviceOperationMode.MAIN,
        features = null,
        can = null,
    )

    private val base = SerialFirmwareState(device = device)

    private fun render(name: String, state: SerialFirmwareState, sources: FirmwareSources = FirmwareSources.All) =
        StateGallery.render(outDir, name) { SerialFirmwareContent(state, SerialFirmwareActions(), sources) }

    private fun phase(phase: SerialPhase) = base.copy(phase = phase)

    @Test
    fun ready() = render("01_ready", base)

    @Test
    fun readyFileOnly() = render("02_ready_file_only", base, FirmwareSources.FileOnly)

    @Test
    fun readyInBoot() = render(
        "03_ready_boot_beta",
        base.copy(deviceInBoot = true, channel = FirmwareChannel.BETA),
    )

    @Test
    fun readyNoModule() = render("04_ready_no_module", base.copy(sourceError = SerialSourceError.NoModule))

    @Test
    fun readyServerError() = render(
        "05_ready_server_error",
        base.copy(sourceError = SerialSourceError.Server(FirmwareError.Common(ApiError.Timeout))),
    )

    @Test
    fun readyServerNotFrm() = render("06_ready_server_not_frm", base.copy(fileProblem = SerialFileProblem.ServerNotFrm))

    @Test
    fun download() = render("07_download", phase(SerialPhase.Download))

    @Test
    fun confirmServer() = render(
        "08_confirm_server",
        phase(
            SerialPhase.Confirm(
                fileName = "1.01.frm",
                path = "",
                fileVersion = "VER1.01",
                older = false,
                description = "• Исправлена ошибка обучения брелока",
                fromServer = true,
            ),
        ),
    )

    @Test
    fun confirmFileMismatch() = render(
        "09_confirm_file_mismatch",
        phase(
            SerialPhase.Confirm(
                fileName = "dx.frm",
                path = "",
                fileVersion = "VER0.90",
                older = true,
                nameMismatch = SystemNameMismatch("DX-9X", "grc-key"),
            ),
        ),
    )

    @Test
    fun waitingBoot() = render("10_waiting_boot", phase(SerialPhase.WaitingBoot))

    @Test
    fun transfer() = render("11_transfer", phase(SerialPhase.Transfer(64)))

    @Test
    fun success() = render("12_success", phase(SerialPhase.Success("VER1.01")))

    @Test
    fun successInBoot() = render("13_success_boot", phase(SerialPhase.Success("VER1.01")).copy(deviceInBoot = true))

    @Test
    fun error() = render("14_error_boot_timeout", phase(SerialPhase.Error(SerialFirmwareFailure.BootTimeout)))
}
