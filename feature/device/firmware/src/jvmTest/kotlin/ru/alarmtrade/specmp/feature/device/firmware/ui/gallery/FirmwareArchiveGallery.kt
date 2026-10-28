package ru.alarmtrade.specmp.feature.device.firmware.ui.gallery

import java.io.File
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.NetworkFailure
import ru.alarmtrade.specmp.core.ui.error.RequestError
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveContent
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.CanFilterUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveActions
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveContent
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveState
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.RollbackUi

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
            ArchiveRowKind.Older
        ),
        ArchiveRowUi("3.52", LocalDate(2026, 2, 3), "", ArchiveRowKind.Older)
    )
    private val base = FirmwareArchiveState(
        content = ArchiveContent.Ready(rows),
        selected = "3.61",
        installedVersion = "3.57",
        deviceTitle = "Pandora DX-4GS  107131"
    )

    private fun render(name: String, state: FirmwareArchiveState) =
        StateGallery.render(outDir, name) { FirmwareArchiveContent(state, FirmwareArchiveActions()) }

    @Test
    fun loading() = render("01_loading", base.copy(content = ArchiveContent.Loading))

    @Test
    fun error() =
        render("02_error", base.copy(content = ArchiveContent.Error(RequestError.Network(NetworkFailure.NoConnection))))

    @Test
    fun empty() = render("03_empty", base.copy(content = ArchiveContent.Empty))

    @Test
    fun list() = render("04_list", base)

    @Test
    fun selectedInstalled() = render("05_selected_installed", base.copy(selected = "3.57"))

    @Test
    fun selectedOlder() = render("06_selected_older", base.copy(selected = "3.55"))

    @Test
    fun loadingMore() = render("07_loading_more", base.copy(content = ArchiveContent.Ready(rows, loadingMore = true)))

    @Test
    fun moreError() =
        render("08_more_error", base.copy(content = ArchiveContent.Ready(rows, moreError = RequestError.Failed)))

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
            content = ArchiveContent.Ready(canRows),
            canFilter = CanFilterUi("0213", "0213", "Toyota Camry XV70 2018–2024", canPickVehicle = true)
        )
    )

    @Test
    fun canFilterEmpty() = render(
        "14_can_empty",
        withFilter.copy(content = ArchiveContent.Empty, canFilter = CanFilterUi("9999", "9999", canPickVehicle = true))
    )

    @Test
    fun viewOnly() = render(
        "15_view_only",
        withFilter.copy(
            content = ArchiveContent.Ready(canRows),
            canFilter = CanFilterUi("0213", "0213", canPickVehicle = true),
            viewOnly = true
        )
    )
}
