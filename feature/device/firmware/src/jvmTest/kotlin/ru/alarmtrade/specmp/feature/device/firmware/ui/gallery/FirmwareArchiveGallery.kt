package ru.alarmtrade.specmp.feature.device.firmware.ui.gallery

import java.io.File
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveTarget
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveExportError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveSavedUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.CanFilterUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveActions
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveContent
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveState
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.RollbackUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

/** Все состояния экрана «Архив прошивок»; PNG — в `build/state-gallery/firmware-archive`. */
class FirmwareArchiveGallery {

    private val outDir = File("build/state-gallery/firmware-archive")

    private val notes = "• Улучшена стабильность связи по Bluetooth при слабом сигнале\n" +
        "• Исправлена ошибка запуска двигателя по расписанию\n• Добавлена поддержка новых CAN-профилей"
    private val longNotes = "• Первая версия с поддержкой профилей CAN 2.0\n" +
        "• Оптимизировано энергопотребление в режиме охраны\n• Исправлены ошибки обработки датчика удара\n" +
        "• Улучшена работа автозапуска при низких температурах\n• Обновлён протокол обмена с модулем часов"

    private val rows = listOf(
        ArchiveRowUi("3.61", LocalDate(2026, 10, 14), notes, ArchiveRowKind.New),
        ArchiveRowUi("3.60", LocalDate(2026, 9, 2), "• Ускорено подключение по Bluetooth", ArchiveRowKind.New),
        ArchiveRowUi("3.58", LocalDate(2026, 7, 19), "", ArchiveRowKind.New),
        ArchiveRowUi("3.57", LocalDate(2026, 5, 30), longNotes, ArchiveRowKind.Installed),
        ArchiveRowUi(
            "3.55",
            LocalDate(2026, 4, 11),
            "• Улучшена стабильность связи по Bluetooth",
            ArchiveRowKind.Older,
        ),
        ArchiveRowUi("3.52", LocalDate(2026, 2, 3), "", ArchiveRowKind.Older),
    )
    private val base = FirmwareArchiveState(
        content = LCEState.Content(rows),
        selected = "3.61",
        installedVersion = "3.57",
        deviceTitle = "Pandora DX-4GS  107131",
    )

    private fun render(name: String, state: FirmwareArchiveState) =
        StateGallery.render(outDir, name) { FirmwareArchiveContent(state, FirmwareArchiveActions()) }

    @Test
    fun loading() = render("01_loading", base.copy(content = LCEState.Loading))

    @Test
    fun error() = render("02_error", base.copy(content = LCEState.Error(FirmwareError.Common(ApiError.NoConnection))))

    @Test
    fun empty() = render("03_empty", base.copy(content = LCEState.Content(emptyList<ArchiveRowUi>())))

    @Test
    fun list() = render("04_list", base)

    @Test
    fun selectedInstalled() = render("05_selected_installed", base.copy(selected = "3.57"))

    @Test
    fun selectedOlder() = render("06_selected_older", base.copy(selected = "3.55"))

    @Test
    fun loadingMore() = render("07_loading_more", base.copy(content = LCEState.Content(rows, isUpdating = true)))

    @Test
    fun moreError() =
        render("08_more_error", base.copy(content = LCEState.Content(rows, updateError = FirmwareError.LoadFailed)))

    @Test
    fun betaChannel() = render("09_beta_channel", base.copy(channel = FirmwareChannel.BETA))

    @Test
    fun rollbackDialog() = render("10_rollback_dialog", base.copy(rollback = RollbackUi("3.55", "3.57")))

    private val canRows = rows.take(3).map { it.copy(canCode = "0213", canVersion = "CAN 2.0 · ред. 4") }
    private val withFilter = base.copy(canFilter = CanFilterUi(canPickVehicle = true))

    @Test
    fun canFilterEmptyField() = render("11_can_filter", withFilter)

    @Test
    fun canFilterPartial() =
        render("12_can_partial", withFilter.copy(canFilter = CanFilterUi("02", canPickVehicle = true)))

    @Test
    fun canFilterFromVehicle() = render(
        "13_can_vehicle",
        withFilter.copy(
            content = LCEState.Content(canRows),
            canFilter = CanFilterUi("0213", "0213", "Toyota Camry XV70 2018–2024", canPickVehicle = true),
        ),
    )

    @Test
    fun canFilterEmpty() = render(
        "14_can_empty",
        withFilter.copy(
            content = LCEState.Content(emptyList<ArchiveRowUi>()),
            canFilter = CanFilterUi("9999", "9999", canPickVehicle = true),
        ),
    )

    @Test
    fun viewOnly() = render(
        "15_view_only",
        withFilter.copy(
            content = LCEState.Content(canRows),
            canFilter = CanFilterUi("0213", "0213", canPickVehicle = true),
            viewOnly = true,
        ),
    )

    private val deviceRows = listOf(
        ArchiveRowUi(
            "3.61",
            LocalDate(2026, 10, 14),
            notes,
            ArchiveRowKind.New,
            "2154",
            "1.03",
            canMatchesDevice = true,
        ),
        ArchiveRowUi("3.61", LocalDate(2026, 10, 14), "", ArchiveRowKind.New, "3312", "1.01"),
        ArchiveRowUi(
            "3.57",
            LocalDate(2026, 5, 30),
            longNotes,
            ArchiveRowKind.Installed,
            "2154",
            "1.03",
            canMatchesDevice = true,
        ),
        ArchiveRowUi(
            "3.57",
            LocalDate(2026, 5, 30),
            "",
            ArchiveRowKind.New,
            "2154",
            "1.02",
            canMatchesDevice = true,
            canProfileDiffers = true,
        ),
        ArchiveRowUi("3.55", LocalDate(2026, 4, 11), "", ArchiveRowKind.Older),
    )
    private val deviceFilter =
        CanFilterUi("2154", "2154", canPickVehicle = true, deviceCode = "2154", deviceProfile = "1.03")

    @Test
    fun canDevice() =
        render("16_can_device", withFilter.copy(content = LCEState.Content(deviceRows), canFilter = deviceFilter))

    @Test
    fun canOtherThanDevice() = render(
        "17_can_other",
        withFilter.copy(
            content = LCEState.Content(deviceRows.subList(1, 2)),
            selected = deviceRows[1].key,
            canFilter = deviceFilter.copy(input = "3312", applied = "3312"),
        ),
    )

    @Test
    fun canFactory() = render(
        "18_can_factory",
        withFilter.copy(canFilter = CanFilterUi(canPickVehicle = true, deviceFactory = true)),
    )

    @Test
    fun canCleared() = render(
        "19_can_cleared",
        withFilter.copy(
            content = LCEState.Content(deviceRows),
            canFilter = deviceFilter.copy(input = "", applied = null),
        ),
    )

    @Test
    fun canPartialDevice() = render(
        "20_can_partial_device",
        withFilter.copy(content = LCEState.Content(deviceRows), canFilter = deviceFilter.copy(input = "21")),
    )

    @Test
    fun canFromVehicleDevice() = render(
        "21_can_vehicle_device",
        withFilter.copy(
            content = LCEState.Content(deviceRows.subList(1, 2)),
            canFilter = deviceFilter.copy(input = "0188", applied = "0188", carTitle = "Kia Rio 2021"),
        ),
    )

    @Test
    fun canEmptyDevice() = render(
        "22_can_empty_device",
        withFilter.copy(content = LCEState.Content(emptyList()), canFilter = deviceFilter),
    )

    @Test
    fun canEmptyOther() = render(
        "23_can_empty_other",
        withFilter.copy(
            content = LCEState.Content(emptyList()),
            canFilter = deviceFilter.copy(input = "0342", applied = "0342"),
        ),
    )

    @Test
    fun canDetailProfileDiffers() = render(
        "24_can_detail_profile",
        withFilter.copy(content = LCEState.Content(deviceRows), selected = deviceRows[3].key, canFilter = deviceFilter),
    )

    private val noUnitRows = listOf(
        ArchiveRowUi("3.61", LocalDate(2026, 10, 14), notes, ArchiveRowKind.New, "2154", "1.03"),
        ArchiveRowUi("3.61", LocalDate(2026, 10, 14), "", ArchiveRowKind.New, "3312", "1.01"),
        ArchiveRowUi("3.58", LocalDate(2026, 7, 19), "", ArchiveRowKind.New),
    )
    private val noUnit = base.copy(
        content = LCEState.Content(noUnitRows),
        selected = noUnitRows[0].key,
        installedVersion = null,
        deviceTitle = "Основная программа",
        deviceName = "Pandora DX-4GS",
        moduleName = "DX4GS",
        viewOnly = true,
        noUnit = true,
        canShare = true,
        canFilter = CanFilterUi(canPickVehicle = true, noUnit = true),
    )

    @Test
    fun noUnitList() = render("25_no_unit_list", noUnit)

    @Test
    fun noUnitVehicle() = render(
        "26_no_unit_vehicle",
        noUnit.copy(
            content = LCEState.Content(noUnitRows.take(1)),
            canFilter = noUnit.canFilter?.copy(input = "2154", applied = "2154", carTitle = "Kia Rio 2021"),
            busy = noUnitRows[0].key,
        ),
    )

    @Test
    fun noUnitBluetoothBeta() = render(
        "27_no_unit_bluetooth_beta",
        noUnit.copy(
            content = LCEState.Content(listOf(ArchiveRowUi("1.16", LocalDate(2026, 10, 1), "", ArchiveRowKind.New))),
            selected = "1.16",
            profile = UpdateProfile.Bluetooth,
            deviceTitle = "Bluetooth-модуль",
            channel = FirmwareChannel.BETA,
            canFilter = null,
        ),
    )

    @Test
    fun noUnitNoFirmware() = render(
        "28_no_unit_no_firmware",
        noUnit.copy(content = LCEState.Content(emptyList()), noModelFirmware = true, deviceTitle = "Метка BT-760"),
    )

    @Test
    fun noUnitSavedPhone() = render(
        "29_no_unit_saved_phone",
        noUnit.copy(
            saved = ArchiveSavedUi(
                noUnitRows[0].key,
                "DX-4GS_3.61_CAN2154-1.03.pld",
                "Download/Pandora Spec/DX-4GS_3.61_CAN2154-1.03.pld",
                "Download/Pandora Spec",
            ),
        ),
    )

    @Test
    fun noUnitSavedDesktop() = render(
        "30_no_unit_saved_desktop",
        noUnit.copy(
            canShare = false,
            canOpenFolder = true,
            saveTarget = FirmwareSaveTarget.Folder,
            saved = ArchiveSavedUi(
                noUnitRows[0].key,
                "DX-4GS_3.61_CAN2154-1.03.pld",
                "/Users/master/Downloads/Pandora Spec/DX-4GS_3.61_CAN2154-1.03.pld",
                "/Users/master/Downloads/Pandora Spec",
            ),
        ),
    )

    @Test
    fun noUnitExportError() = render("31_no_unit_export_error", noUnit.copy(exportError = ArchiveExportError.Save))
}
