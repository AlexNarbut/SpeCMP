package ru.alarmtrade.specmp.feature.device.firmware.ui.gallery

import java.io.File
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CardStatusUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CheckContent
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CheckDialog
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CurrentVersionUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareCheckActions
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareCheckContent
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareCheckState
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.ModuleCardUi

/** Все состояния экрана «Проверка прошивки»; PNG — в `build/state-gallery/firmware-check`. */
class FirmwareCheckGallery {

    private val outDir = File("build/state-gallery/firmware-check")

    private val notes = "• Улучшена стабильность связи по Bluetooth при слабом сигнале\n" +
        "• Исправлена ошибка запуска двигателя по расписанию\n• Добавлена поддержка новых CAN-профилей"

    private val update = ModuleCardUi(
        id = 1,
        kind = FirmwareModuleKind.AlarmMain,
        title = "Pandora DX 4G (DX4G)",
        currentVersion = CurrentVersionUi.Known("3.57"),
        status = CardStatusUi.UpdateAvailable("3.61", notes)
    )
    private val actual = ModuleCardUi(
        id = 2,
        kind = FirmwareModuleKind.BluetoothModule,
        title = "Pandora BT (BTM2)",
        currentVersion = CurrentVersionUi.Known("1.14"),
        status = CardStatusUi.Actual("1.14")
    )
    private val unsupported = ModuleCardUi(
        id = 3,
        kind = FirmwareModuleKind.WatchMain,
        title = "Pandora Watch (PW2)",
        currentVersion = CurrentVersionUi.Known("2.03"),
        status = CardStatusUi.NotSupported
    )
    private val base = FirmwareCheckState(
        deviceName = "Pandora DX-4GS",
        deviceId = 107131,
        content = CheckContent.Ready(listOf(update, actual, unsupported))
    )

    private fun render(name: String, state: FirmwareCheckState) =
        StateGallery.render(outDir, name) { FirmwareCheckContent(state, FirmwareCheckActions()) }

    @Test
    fun loading() = render("01_loading", base.copy(content = CheckContent.Loading))

    @Test
    fun listError() = render("02_list_error", base.copy(content = CheckContent.Error))

    @Test
    fun list() = render("03_list", base)

    @Test
    fun cardStates() = render(
        "04_card_states",
        base.copy(
            content = CheckContent.Ready(
                listOf(
                    update.copy(currentVersion = CurrentVersionUi.Reading, status = CardStatusUi.Searching),
                    actual.copy(status = CardStatusUi.Searching),
                    unsupported.copy(
                        kind = FirmwareModuleKind.GpsTracker,
                        title = "Pandora GPS (GPS2)",
                        currentVersion = CurrentVersionUi.Absent,
                        status = CardStatusUi.Failed
                    )
                )
            )
        )
    )

    @Test
    fun betaChannel() = render("05_beta_channel", base.copy(channel = FirmwareChannel.BETA))

    @Test
    fun bootFailed() = render("06_boot_failed", base.copy(bootFailed = true))

    @Test
    fun descriptionDialog() = render(
        "07_dialog_description",
        base.copy(dialog = CheckDialog.Description(1, "3.61", notes))
    )

    @Test
    fun descriptionDialogEmpty() = render(
        "08_dialog_description_empty",
        base.copy(dialog = CheckDialog.Description(1, "3.61", ""))
    )

    @Test
    fun confirmDialog() = render("09_dialog_confirm", base.copy(dialog = CheckDialog.ConfirmBoot(1)))

    @Test
    fun confirmDialogSending() =
        render("10_dialog_confirm_sending", base.copy(dialog = CheckDialog.ConfirmBoot(1, sending = true)))

    @Test
    fun pinDialog() = render("11_dialog_pin", base.copy(dialog = CheckDialog.Pin(2)))

    @Test
    fun pinDialogLocked() = render(
        "12_dialog_pin_locked",
        base.copy(dialog = CheckDialog.Pin(2, wrong = true, wrongCount = 1, lockedFor = 14.seconds))
    )

    @Test
    fun pinDialogSending() = render("13_dialog_pin_sending", base.copy(dialog = CheckDialog.Pin(2, sending = true)))

    @Test
    fun longNames() = render(
        "14_long_names",
        base.copy(
            deviceName = "Pandora DX-4GS Premium Long Edition 4G+",
            content = CheckContent.Ready(
                listOf(
                    update.copy(title = "Pandora DX 4G Premium Long (DX4G-LX)"),
                    actual.copy(title = "Pandora Bluetooth Module Long (BTM2-LNG)"),
                    unsupported.copy(title = "Pandora Watch Ultra Long Edition (PW2-X)")
                )
            )
        )
    )
}
