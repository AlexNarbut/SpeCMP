package ru.alarmtrade.specmp.feature.mountpassport.gallery

import java.io.File
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportArchive
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportFailure
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportItem
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportProblem
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageUsage
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListContent
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListMode
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListScreen
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.ExportAction
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.ExportPassport
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.ExportPhase
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.ImportPhase
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.PassportTransferActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.PassportTransferSheet
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.PassportTransferState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.TransferSheet

/** Перенос архивом: лист (телефон) / диалог (шире), режим выбора и напоминание о копии в архиве. */
class MountPassportTransferGallery {

    private val outDir = File("build/state-gallery/mount-passport")
    private val listActions = PassportListActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    private val transferActions = PassportTransferActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    private val archive = PassportListState(
        mode = PassportListMode.Archive,
        list = LCEState.Content(PassportListContent("", rows.toImmutableList())),
        storage = PassportStorageUsage(128, 340_000_000),
    )
    private val passports = persistentListOf(
        ExportPassport("p1", "Kia Sportage", "А 123 ВС 777", hasSettings = true, photos = 4),
        ExportPassport("p2", "Volkswagen Polo", null, hasSettings = false, photos = 2),
        ExportPassport("p3", "Toyota RAV4", "О 456 КМ 199", hasSettings = true, photos = 6),
        ExportPassport("p4", "Kia K5", null, hasSettings = true, photos = 0),
        ExportPassport("p5", null, null, hasSettings = false, photos = 3),
    )
    private val zip = PassportArchive("/tmp/a.zip", "Паспорта_монтажа_20.11.2026_5шт.zip", 7_340_032, 5)

    private fun export(phase: ExportPhase, count: Int = 5) = TransferSheet.Export(
        ids = passports.take(count).map { it.id }.toImmutableList(),
        fullBackup = false,
        passports = passports.take(count).toImmutableList(),
        phase = phase,
    )

    private fun shot(
        name: String,
        sheet: TransferSheet?,
        list: PassportListState = archive,
        canShare: Boolean = true,
    ) = StateGallery.render(outDir, name) {
        PassportListScreen(list, listActions, CmpSnackbarHostState(), detail = {}, overlay = { wide ->
            PassportTransferSheet(PassportTransferState(canShare, sheet), transferActions, wide)
        })
    }

    @Test
    fun exportOne() = shot("80-export-one", export(ExportPhase.Form, count = 1))

    @Test
    fun exportMany() = shot("81-export-many", export(ExportPhase.Form))

    @Test
    fun exportDesktop() = shot("82-export-desktop", export(ExportPhase.Form, count = 1), canShare = false)

    @Test
    fun exportPacking() = shot("83-export-packing", export(ExportPhase.Packing(2, 5)))

    @Test
    fun exportReady() = shot("84-export-ready", export(ExportPhase.Ready(zip, busy = ExportAction.SAVE)))

    @Test
    fun exportFailed() = shot("85-export-failed", export(ExportPhase.Failed(PassportError.Unknown)))

    @Test
    fun importPick() = shot("86-import-pick", TransferSheet.Import(ImportPhase.Pick))

    @Test
    fun importSummary() = shot(
        "87-import-summary",
        TransferSheet.Import(
            ImportPhase.Summary(
                fileName = "Паспорта_монтажа_19.11.2026_5шт.zip",
                path = "/tmp/a.zip",
                items = listOf(
                    item("a", "Nissan X-Trail", "С 112 АВ 199", exists = false),
                    item("b", "Renault Duster", "У 870 ОК 750", exists = true),
                    item("c", "Kia K5", null, exists = false),
                ).toImmutableList(),
                problems = persistentListOf(
                    PassportImportProblem("d", "20261101-000001-1", PassportImportFailure.UNSUPPORTED_SCHEMA),
                ),
            ),
        ),
    )

    @Test
    fun importNotArchive() =
        shot("88-import-not-archive", TransferSheet.Import(ImportPhase.NotArchive("Фото_монтажа.zip")))

    @Test
    fun importImporting() = shot("89-import-importing", TransferSheet.Import(ImportPhase.Importing("a.zip", 3)))

    @Test
    fun importDone() = shot(
        "90-import-done",
        TransferSheet.Import(
            ImportPhase.Done(
                PassportImportResult(
                    imported = 2,
                    replaced = 1,
                    skipped = 1,
                    problems = listOf(
                        PassportImportProblem("x", "20261103-560044-1", PassportImportFailure.WRITE_FAILED),
                    ),
                ),
            ),
        ),
    )

    @Test
    fun picking() = shot(
        "91-list-picking",
        sheet = null,
        list = archive.copy(picking = true, picked = persistentSetOf("p1", "p3")),
    )

    @Test
    fun backupReminder() = shot("92-list-backup", sheet = null, list = archive.copy(backupReminder = true))

    private fun item(key: String, car: String, plate: String?, exists: Boolean) = PassportImportItem(
        key = key,
        number = "20261105-77120$key-1",
        carTitle = car,
        plate = plate,
        createdAt = AT,
        systemId = "7712040",
        unitModel = "Pandora DX-91",
        hasSettings = key != "c",
        photos = 2,
        exists = exists,
    )
}
