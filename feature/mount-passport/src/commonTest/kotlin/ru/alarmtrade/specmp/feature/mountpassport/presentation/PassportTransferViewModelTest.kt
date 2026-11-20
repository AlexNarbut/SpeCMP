package ru.alarmtrade.specmp.feature.mountpassport.presentation

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.files.BinaryFilePickResult
import ru.alarmtrade.specmp.core.files.BinaryFileSaveResult
import ru.alarmtrade.specmp.core.files.BinaryFileType
import ru.alarmtrade.specmp.core.files.PdfRenderResult
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportExportOptions
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportDecision
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportFailure
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportInspection
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportItem
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportProblem
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportNotArchiveException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPhoto
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportSettingsSnapshot
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehicle
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.ExportPhase
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.ImportPhase
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.PassportTransferEffect
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.PassportTransferViewModel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.TransferSheet

@OptIn(ExperimentalCoroutinesApi::class)
class PassportTransferViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeMountPassportRepository()
    private val transfer = FakePassportTransferRepository()
    private val documents = FakePassportDocumentRepository(repository)
    private val renderer = FakeHtmlPdfRenderer()
    private val filePicker = FakeBinaryFilePicker()
    private val imagePicker = FakeImagePicker()
    private val viewModels = mutableListOf<PassportTransferViewModel>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val photo = PassportPhoto("ph1", "/p/1.jpg", "/t/1.jpg", null)
        val snapshot = PassportSettingsSnapshot("Pandora DX-91", CREATED, canFactory = false)
        repository.passportsById = mapOf(
            "p1" to testPassport().copy(photos = listOf(photo, photo), settingsSnapshot = snapshot),
            "p2" to testPassport().copy(id = "p2", vehicle = PassportVehicle(customTitle = "Kia K5")),
        )
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun test(block: suspend TestScope.() -> Unit): TestResult = runTest(dispatcher) {
        try {
            block()
        } finally {
            viewModels.forEach { it.viewModelScope.cancel() }
        }
    }

    private fun TestScope.viewModel(
        exporter: FakeBinaryFileExporter = FakeBinaryFileExporter(),
    ): Pair<PassportTransferViewModel, MutableList<PassportTransferEffect>> {
        val vm = PassportTransferViewModel(
            repository = repository,
            transfer = transfer,
            documents = documents,
            localizer = FakePassportDocumentLocalizer(),
            renderer = renderer,
            exporter = exporter,
            filePicker = filePicker,
            imagePicker = imagePicker,
            dispatchers = testDispatchers(dispatcher),
        ).also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        val effects = mutableListOf<PassportTransferEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm to effects
    }

    private val PassportTransferViewModel.export: TransferSheet.Export
        get() = assertIs<TransferSheet.Export>(state.value.sheet)

    private val PassportTransferViewModel.import: TransferSheet.Import
        get() = assertIs<TransferSheet.Import>(state.value.sheet)

    @Test
    fun exportOfOnePassportShowsItsContents(): TestResult = test {
        val (vm, _) = viewModel()

        vm.onExport(listOf("p1"))

        val sheet = vm.export
        assertEquals(listOf("p1"), sheet.ids)
        assertFalse(sheet.fullBackup)
        assertEquals(ExportPhase.Form, sheet.phase)
        assertTrue(sheet.includeSettings)
        assertFalse(sheet.includePhotos)
        assertEquals(1, sheet.withSettings)
        assertEquals(2, sheet.photos)
        assertEquals("Toyota RAV4", sheet.passports.single().carTitle)
    }

    @Test
    fun sharingOnePassportBuildsArchiveWithItsPdf(): TestResult = test {
        val exporter = FakeBinaryFileExporter()
        val (vm, effects) = viewModel(exporter)
        vm.onExport(listOf("p1"))

        vm.onShare()

        val export = transfer.exports.single()
        assertEquals(listOf("p1"), export.ids)
        assertEquals(PassportExportOptions(includeSettings = true, includePhotos = false), export.options)
        assertFalse(export.fullBackup)
        assertEquals("Паспорт_20261119-234567-1_Toyota_RAV4", renderer.renders.single().second)
        assertEquals(
            Triple(TEST_ARCHIVE.fileName, BinaryFileType.Zip, TEST_ARCHIVE.path),
            exporter.sharedFiles.single(),
        )
        assertNull(vm.state.value.sheet)
        assertEquals(
            listOf(PassportTransferEffect.ArchiveBuilt(fullBackup = false), PassportTransferEffect.Shared),
            effects,
        )
        assertEquals(0, documents.issuedCalls)
    }

    @Test
    fun selectedPassportsAreExportedWithProgress(): TestResult = test {
        val (vm, _) = viewModel()
        val progress = mutableListOf<ExportPhase>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.state.collect { state ->
                val phase = (state.sheet as? TransferSheet.Export)?.phase
                if (phase is ExportPhase.Packing) progress += phase
            }
        }
        vm.onExport(listOf("p1", "p2"))

        vm.onSave()

        assertEquals(listOf("p1", "p2"), transfer.exports.single().ids)
        assertEquals(2, renderer.renders.size)
        assertEquals(listOf<ExportPhase>(ExportPhase.Packing(0, 2), ExportPhase.Packing(1, 2)), progress.take(2))
    }

    @Test
    fun exportAllTakesIssuedPassportsAsFullBackup(): TestResult = test {
        repository.all.value = Response.Success(
            listOf(
                summary("p1"),
                summary("p2", status = PassportStatus.ISSUED),
                summary("p3", status = PassportStatus.BLANK),
            ),
        )
        val (vm, effects) = viewModel()

        vm.onExportAll()
        vm.onSave()

        val export = transfer.exports.single()
        assertEquals(listOf("p1", "p2"), export.ids)
        assertTrue(export.fullBackup)
        assertEquals(PassportTransferEffect.ArchiveBuilt(fullBackup = true), effects.first())
    }

    @Test
    fun switchesChangeArchiveContents(): TestResult = test {
        val (vm, _) = viewModel()
        vm.onExport(listOf("p1"))

        vm.onIncludeSettingsChange(false)
        vm.onIncludePhotosChange(true)
        vm.onSave()

        assertEquals(
            PassportExportOptions(includeSettings = false, includePhotos = true),
            transfer.exports.single().options,
        )
    }

    @Test
    fun failedExportCanBeRetried(): TestResult = test {
        transfer.exportResult = Response.Error(IllegalStateException("disk"))
        val exporter = FakeBinaryFileExporter()
        val (vm, effects) = viewModel(exporter)
        vm.onExport(listOf("p1"))

        vm.onShare()

        assertIs<ExportPhase.Failed>(vm.export.phase)
        assertTrue(exporter.sharedFiles.isEmpty())
        assertTrue(effects.isEmpty())

        vm.onRetry()

        assertEquals(ExportPhase.Form, vm.export.phase)
    }

    @Test
    fun failedPdfFailsExport(): TestResult = test {
        renderer.result = PdfRenderResult.Failed
        val (vm, _) = viewModel()
        vm.onExport(listOf("p1"))

        vm.onSave()

        assertIs<ExportPhase.Failed>(vm.export.phase)
    }

    @Test
    fun withoutShareOnDesktopOnlySaving(): TestResult = test {
        val exporter = FakeBinaryFileExporter(canShare = false)
        val (vm, _) = viewModel(exporter)
        vm.onExport(listOf("p1"))

        vm.onShare()

        assertFalse(vm.state.value.canShare)
        assertTrue(transfer.exports.isEmpty())
        assertEquals(ExportPhase.Form, vm.export.phase)
    }

    @Test
    fun savedArchiveClosesSheet(): TestResult = test {
        val exporter = FakeBinaryFileExporter().apply { saveResult = BinaryFileSaveResult.Saved("Архив.zip") }
        val (vm, effects) = viewModel(exporter)
        vm.onExport(listOf("p1"))

        vm.onSave()

        assertEquals(Triple(TEST_ARCHIVE.fileName, BinaryFileType.Zip, TEST_ARCHIVE.path), exporter.savedFiles.single())
        assertNull(vm.state.value.sheet)
        assertEquals(PassportTransferEffect.Saved("Архив.zip"), effects.last())
    }

    @Test
    fun cancelledSaveKeepsReadyArchiveForAnotherTry(): TestResult = test {
        val exporter = FakeBinaryFileExporter().apply { saveResult = BinaryFileSaveResult.Cancelled }
        val (vm, effects) = viewModel(exporter)
        vm.onExport(listOf("p1"))

        vm.onSave()

        assertEquals(ExportPhase.Ready(TEST_ARCHIVE), vm.export.phase)
        assertEquals(listOf<PassportTransferEffect>(PassportTransferEffect.ArchiveBuilt(false)), effects)

        vm.onShare()

        assertEquals(1, transfer.exports.size)
        assertEquals(1, exporter.sharedFiles.size)
    }

    @Test
    fun failedSaveAndShareKeepArchive(): TestResult = test {
        val exporter = FakeBinaryFileExporter().apply {
            saveResult = BinaryFileSaveResult.Failed
            shareResult = false
        }
        val (vm, effects) = viewModel(exporter)
        vm.onExport(listOf("p1"))

        vm.onSave()
        vm.onShare()

        assertEquals(ExportPhase.Ready(TEST_ARCHIVE), vm.export.phase)
        assertEquals(
            listOf(
                PassportTransferEffect.ArchiveBuilt(false),
                PassportTransferEffect.SaveFailed,
                PassportTransferEffect.ShareFailed,
            ),
            effects,
        )
    }

    @Test
    fun sheetClosesExceptWhilePacking(): TestResult = test {
        val (vm, _) = viewModel()
        transfer.afterPdf = { vm.onClose() }
        vm.onExport(listOf("p1"))

        vm.onSave()

        assertNull(vm.state.value.sheet)
        assertEquals(1, transfer.exports.size)

        vm.onImport()
        vm.onClose()

        assertNull(vm.state.value.sheet)
    }

    @Test
    fun cancelledFilePickKeepsPickStep(): TestResult = test {
        filePicker.result = BinaryFilePickResult.Cancelled
        val (vm, effects) = viewModel()
        vm.onImport()

        vm.onPickFile()

        assertEquals(BinaryFileType.Zip, filePicker.requests.single())
        assertEquals(ImportPhase.Pick, vm.import.phase)
        assertTrue(transfer.inspected.isEmpty())
        assertTrue(effects.isEmpty())
    }

    @Test
    fun failedFilePickIsReported(): TestResult = test {
        filePicker.result = BinaryFilePickResult.Failed
        val (vm, effects) = viewModel()
        vm.onImport()

        vm.onPickFile()

        assertEquals(ImportPhase.Pick, vm.import.phase)
        assertEquals(listOf<PassportTransferEffect>(PassportTransferEffect.PickFailed), effects)
    }

    @Test
    fun notArchiveFileIsShown(): TestResult = test {
        val (vm, _) = viewModel()
        vm.onImport()

        vm.onPickFile()

        assertEquals("/cache/file-import/Паспорта.zip", transfer.inspected.single())
        assertEquals(ImportPhase.NotArchive("Паспорта.zip"), vm.import.phase)

        vm.onPickAnother()

        assertEquals(ImportPhase.Pick, vm.import.phase)
    }

    @Test
    fun archiveWithoutReadablePassportsHasNothingToImport(): TestResult = test {
        val problems = listOf(PassportImportProblem("a", "N-1", PassportImportFailure.UNSUPPORTED_SCHEMA))
        transfer.inspection = Response.Success(PassportImportInspection.NothingToImport(problems))
        val (vm, _) = viewModel()
        vm.onImport()

        vm.onPickFile()

        assertEquals(ImportPhase.NothingToImport("Паспорта.zip", problems), vm.import.phase)
    }

    @Test
    fun summaryCountsDuplicatesAndSkipsThemByDefault(): TestResult = test {
        transfer.inspection = Response.Success(
            PassportImportInspection.Ready(listOf(item("a"), item("b", exists = true), item("c")), emptyList()),
        )
        val (vm, _) = viewModel()
        vm.onImport()
        vm.onPickFile()

        val summary = assertIs<ImportPhase.Summary>(vm.import.phase)
        assertEquals(1, summary.existing)
        assertFalse(summary.replace)
        assertEquals(2, summary.toImport)

        vm.onImportConfirm()

        val import = transfer.imports.single()
        assertEquals("/cache/file-import/Паспорта.zip", import.path)
        assertEquals(mapOf("b" to PassportImportDecision.SKIP), import.decisions)
    }

    @Test
    fun replaceTakesDuplicatesFromFile(): TestResult = test {
        transfer.inspection = Response.Success(
            PassportImportInspection.Ready(listOf(item("a", exists = true), item("b", exists = true)), emptyList()),
        )
        val (vm, _) = viewModel()
        vm.onImport()
        vm.onPickFile()

        val skipped = assertIs<ImportPhase.Summary>(vm.import.phase)
        assertEquals(0, skipped.toImport)
        vm.onImportConfirm()
        assertTrue(transfer.imports.isEmpty())

        vm.onReplaceChange(true)

        assertEquals(2, assertIs<ImportPhase.Summary>(vm.import.phase).toImport)

        vm.onImportConfirm()

        assertEquals(
            mapOf("a" to PassportImportDecision.REPLACE, "b" to PassportImportDecision.REPLACE),
            transfer.imports.single().decisions,
        )
    }

    @Test
    fun importResultWithProblemsIsShown(): TestResult = test {
        val problems = listOf(PassportImportProblem("c", "N-3", PassportImportFailure.WRITE_FAILED))
        transfer.inspection = Response.Success(PassportImportInspection.Ready(listOf(item("a")), emptyList()))
        transfer.importResult = Response.Success(PassportImportResult(1, 1, 2, problems))
        val (vm, _) = viewModel()
        vm.onImport()
        vm.onPickFile()

        vm.onImportConfirm()

        assertEquals(ImportPhase.Done(PassportImportResult(1, 1, 2, problems)), vm.import.phase)
    }

    @Test
    fun missingThumbnailsAreMadeBy320pxImage(): TestResult = test {
        transfer.inspection = Response.Success(PassportImportInspection.Ready(listOf(item("a")), emptyList()))
        transfer.thumbnailJpeg = byteArrayOf(5, 5)
        val (vm, _) = viewModel()
        vm.onImport()
        vm.onPickFile()

        vm.onImportConfirm()

        assertEquals(listOf(320), imagePicker.preparedSides)
        assertEquals(THUMB_BYTES.toList(), transfer.thumbnails.single().toList())
    }

    @Test
    fun importFailuresAreShown(): TestResult = test {
        transfer.inspection = Response.Success(PassportImportInspection.Ready(listOf(item("a")), emptyList()))
        transfer.importResult = Response.Error(PassportNotArchiveException())
        val (vm, _) = viewModel()
        vm.onImport()
        vm.onPickFile()

        vm.onImportConfirm()

        assertEquals(ImportPhase.NotArchive("Паспорта.zip"), vm.import.phase)

        transfer.importResult = Response.Error(IllegalStateException("disk"))
        vm.onPickAnother()
        vm.onPickFile()
        vm.onImportConfirm()

        assertIs<ImportPhase.Failed>(vm.import.phase)
    }

    @Test
    fun failedInspectionIsShown(): TestResult = test {
        transfer.inspection = Response.Error(IllegalStateException("io"))
        val (vm, _) = viewModel()
        vm.onImport()

        vm.onPickFile()

        assertEquals("Паспорта.zip", assertIs<ImportPhase.Failed>(vm.import.phase).fileName)
    }

    private fun item(key: String, exists: Boolean = false) = PassportImportItem(
        key = key,
        number = "N-$key",
        carTitle = "Nissan X-Trail",
        plate = null,
        createdAt = CREATED,
        systemId = "7712040",
        unitModel = "Pandora DX-91",
        hasSettings = true,
        photos = 0,
        exists = exists,
    )
}
