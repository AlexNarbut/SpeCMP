package ru.alarmtrade.specmp.feature.device.firmware.ui.gallery

import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.BlockCanUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.ConfirmCanUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.ConfirmUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FailureKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateActions
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateContent
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateState
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdatePhase
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

/** Все фазы экрана «Обновление прошивки»; PNG — в `build/state-gallery/firmware-update`. */
class FirmwareUpdateGallery {

    private val outDir = File("build/state-gallery/firmware-update")

    private val notes = "• Улучшена стабильность связи по Bluetooth при слабом сигнале\n" +
        "• Исправлена ошибка запуска двигателя по расписанию\n• Добавлена поддержка новых CAN-профилей"

    private val base = FirmwareUpdateState(
        phase = UpdatePhase.Source,
        deviceTitle = "Pandora DX-4GS  107131",
        moduleTitles = listOf("Pandora DX 4G (DX4G)"),
        currentVersion = "3.57",
        confirm = ConfirmUi("3.57", "3.61", notes, older = false),
    )

    private fun render(name: String, state: FirmwareUpdateState) =
        StateGallery.render(outDir, name) { FirmwareUpdateContent(state, FirmwareUpdateActions()) }

    private fun phase(phase: UpdatePhase) = base.copy(phase = phase)

    @Test
    fun loading() = render("01_loading", phase(UpdatePhase.Loading))

    @Test
    fun entering() = render("02_entering", phase(UpdatePhase.Entering))

    @Test
    fun source() = render("03_source_single", base)

    @Test
    fun sourceMulti() = render(
        "04_source_multi",
        base.copy(
            moduleTitles = listOf("Pandora DX 4G (DX4G)", "Pandora Main (MAIN)", "CAN-модуль (CAN2)"),
            selectedModule = 1,
            channel = FirmwareChannel.BETA,
        ),
    )

    @Test
    fun sourceBluetooth() = render(
        "05_source_bluetooth",
        base.copy(
            profile = UpdateProfile.Bluetooth,
            deviceTitle = "Pandora BT (BTM2)",
            moduleTitles = listOf("Pandora BT (BTM2)"),
            currentVersion = "1.14",
            sourceError = FirmwareError.Common(ApiError.Timeout),
        ),
    )

    @Test
    fun dataError() = render("06_data_error", phase(UpdatePhase.DataError))

    @Test
    fun download() = render("07_download", phase(UpdatePhase.Download))

    @Test
    fun confirm() = render("08_confirm", phase(UpdatePhase.Confirm))

    @Test
    fun confirmOlderBeta() = render(
        "09_confirm_older_beta",
        base.copy(
            phase = UpdatePhase.Confirm,
            channel = FirmwareChannel.BETA,
            confirm = ConfirmUi("3.57", "3.55", "", older = true),
        ),
    )

    @Test
    fun prepare() = render("10_prepare", phase(UpdatePhase.Prepare))

    @Test
    fun transfer() = render("11_transfer", phase(UpdatePhase.Transfer(64)))

    @Test
    fun transferUnknownPercent() = render("12_transfer_no_percent", phase(UpdatePhase.Transfer(null)))

    @Test
    fun transferReconnecting() =
        render("12b_transfer_reconnecting", phase(UpdatePhase.Transfer(64, reconnecting = true)))

    @Test
    fun verify() = render("13_verify", phase(UpdatePhase.Verify))

    @Test
    fun apply() = render("14_apply", phase(UpdatePhase.Apply))

    @Test
    fun success() = render("15_success", phase(UpdatePhase.Success).copy(inBoot = true, canExitBoot = true))

    @Test
    fun successUsb() = render("15b_success_usb", phase(UpdatePhase.Success).copy(inBoot = true))

    @Test
    fun successBlockBack() = render("15c_success_block_back", phase(UpdatePhase.Success))

    @Test
    fun sourceWithExit() = render("03b_source_exit_bluetooth", base.copy(canExitBoot = true, exitError = true))

    @Test
    fun finishing() = render("16_finishing", phase(UpdatePhase.Finishing))

    @Test
    fun error() = render("17_error_crc", phase(UpdatePhase.Error(FailureKind.Crc)))

    @Test
    fun errorPreparation() = render("18_error_preparation", phase(UpdatePhase.Error(FailureKind.Preparation)))

    @Test
    fun lostUpdating() = render("19_lost_updating", phase(UpdatePhase.Lost(updating = true)))

    @Test
    fun lostIdle() = render("20_lost_idle", phase(UpdatePhase.Lost(updating = false)))

    @Test
    fun exitDialog() = render("21_exit_dialog", base.copy(exitDialog = true))

    private val withCan = base.copy(blockCan = BlockCanUi("2154", "1.03"))

    @Test
    fun sourceCan() = render("22_source_can", withCan)

    @Test
    fun sourceFactoryCan() = render("23_source_factory_can", base.copy(blockCan = BlockCanUi(null, null)))

    @Test
    fun confirmSameCan() = render(
        "24_confirm_can_same",
        withCan.copy(
            phase = UpdatePhase.Confirm,
            confirm = base.confirm?.copy(can = ConfirmCanUi("2154", "2154", "1.03")),
        ),
    )

    @Test
    fun confirmOtherCan() = render(
        "25_confirm_can_other",
        withCan.copy(
            phase = UpdatePhase.Confirm,
            confirm = base.confirm?.copy(can = ConfirmCanUi("2154", "3312", "1.01")),
        ),
    )
}
