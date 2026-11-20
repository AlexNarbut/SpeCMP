package ru.alarmtrade.specmp.feature.mountpassport.presentation

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.files.BinaryFileSaveResult
import ru.alarmtrade.specmp.core.files.PdfRenderResult
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportVehicle
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.DocumentStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyArgs
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyEffect
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class PassportReadyViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeMountPassportRepository()
    private val documents = FakePassportDocumentRepository(repository)
    private val localizer = FakePassportDocumentLocalizer()
    private val renderer = FakeHtmlPdfRenderer()
    private val rasterizer = FakePdfPageRasterizer()
    private val viewModels = mutableListOf<PassportReadyViewModel>()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

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
        preview: Boolean = false,
    ): Pair<PassportReadyViewModel, MutableList<PassportReadyEffect>> {
        val vm = PassportReadyViewModel(
            args = PassportReadyArgs("p1", preview = preview),
            repository = repository,
            documents = documents,
            localizer = localizer,
            renderer = renderer,
            rasterizer = rasterizer,
            exporter = exporter,
            dispatchers = testDispatchers(dispatcher),
        ).also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        val effects = mutableListOf<PassportReadyEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm to effects
    }

    private fun passportWith(block: MountPassport.() -> MountPassport) {
        repository.passport = Response.Success(testPassport().block())
    }

    private fun changed(at: Instant) {
        passportWith { copy(status = PassportStatus.ISSUED, updatedAt = at, remarks = "правка") }
        repository.all.value = Response.Success(listOf(summary(updatedAt = at)))
    }

    @Test
    fun documentIsBuiltOnOpen(): TestResult = test {
        val (vm, _) = viewModel()

        val (html, baseName, page) = renderer.renders.single()
        assertEquals("<html>p1</html>", html)
        assertEquals("Паспорт_20261119-234567-1_Toyota_RAV4", baseName)
        assertEquals("Паспорт № 20261119-234567-1", page.footer?.start)
        assertEquals("стр. {page} из {pages}", page.footer?.end)
        assertEquals(localizer.strings to localizer.format, documents.builds.single())
        assertEquals(DocumentStatus.Ready, vm.state.value.document)
        assertEquals(3, vm.state.value.pageCount)
        assertEquals("Паспорт_20261119-234567-1_Toyota_RAV4.pdf", vm.state.value.pdf?.fileName)
    }

    @Test
    fun withoutCarFileIsNamedByUnitId(): TestResult = test {
        passportWith { copy(vehicle = PassportVehicle()) }

        val (vm, _) = viewModel()

        assertEquals("Паспорт_20261119-234567-1_1234567.pdf", vm.state.value.pdf?.fileName)
    }

    @Test
    fun firstDocumentIssuesPassportOnce(): TestResult = test {
        val (vm, _) = viewModel()
        assertEquals(1, documents.issuedCalls)
        assertEquals(PassportStatus.ISSUED, vm.state.value.passport?.status)

        changed(Instant.fromEpochMilliseconds(ISSUED_AT.toEpochMilliseconds() + 60_000))
        advanceTimeBy(2_000)

        assertEquals(2, renderer.renders.size)
        assertEquals(1, documents.issuedCalls)
    }

    @Test
    fun livePreviewDoesNotIssuePassport(): TestResult = test {
        val (vm, _) = viewModel(preview = true)

        val edited = Instant.fromEpochMilliseconds(ISSUED_AT.toEpochMilliseconds() + 60_000)
        passportWith { copy(updatedAt = edited, remarks = "правка") }
        repository.all.value = Response.Success(listOf(summary(updatedAt = edited)))
        advanceTimeBy(2_000)

        assertEquals(DocumentStatus.Ready, vm.state.value.document)
        assertEquals(2, renderer.renders.size)
        assertEquals(0, documents.issuedCalls)
        assertEquals(PassportStatus.DRAFT, vm.state.value.passport?.status)
    }

    @Test
    fun issuedPassportIsNotIssuedAgain(): TestResult = test {
        passportWith { copy(status = PassportStatus.ISSUED) }

        viewModel()

        assertEquals(0, documents.issuedCalls)
    }

    @Test
    fun htmlErrorShowsFailureAndRetryBuildsAgain(): TestResult = test {
        documents.html = Response.Error(IllegalStateException("broken"))
        val (vm, _) = viewModel()
        assertEquals(DocumentStatus.Failed(PassportError.Unknown), vm.state.value.document)
        assertTrue(renderer.renders.isEmpty())
        assertEquals(0, documents.issuedCalls)

        documents.html = Response.Success("<html>p1</html>")
        vm.onRetry()
        runCurrent()

        assertEquals(DocumentStatus.Ready, vm.state.value.document)
        assertEquals(1, renderer.renders.size)
    }

    @Test
    fun renderErrorShowsFailure(): TestResult = test {
        renderer.result = PdfRenderResult.Failed

        val (vm, _) = viewModel()

        assertIs<DocumentStatus.Failed>(vm.state.value.document)
        assertEquals(0, documents.issuedCalls)
    }

    @Test
    fun formingIsShownWhileRendering(): TestResult = test {
        renderer.delayMs = 1_000

        val (vm, _) = viewModel()
        assertEquals(DocumentStatus.Forming, vm.state.value.document)

        advanceTimeBy(1_100)
        assertEquals(DocumentStatus.Ready, vm.state.value.document)
    }

    @Test
    fun unchangedPassportUsesCachedPdf(): TestResult = test {
        val (vm, _) = viewModel()

        repository.all.value = Response.Success(listOf(summary(updatedAt = ISSUED_AT)))
        advanceTimeBy(5_000)
        vm.onExportPdf()
        vm.onExportPdf()

        assertEquals(1, renderer.renders.size)
        assertEquals(1, documents.builds.size)
    }

    @Test
    fun editedPassportIsRebuiltAfterPause(): TestResult = test {
        val (vm, _) = viewModel()

        changed(Instant.fromEpochMilliseconds(ISSUED_AT.toEpochMilliseconds() + 1_000))
        advanceTimeBy(300)
        changed(Instant.fromEpochMilliseconds(ISSUED_AT.toEpochMilliseconds() + 2_000))
        assertEquals(1, renderer.renders.size)

        advanceTimeBy(2_000)

        assertEquals(2, renderer.renders.size)
        assertEquals(DocumentStatus.Ready, vm.state.value.document)
    }

    @Test
    fun exportPdfOnPhoneSharesDocument(): TestResult = test {
        val exporter = FakeBinaryFileExporter()
        val (vm, _) = viewModel(exporter)

        vm.onExportPdf()

        val (name, bytes) = exporter.shared.single()
        assertEquals("Паспорт_20261119-234567-1_Toyota_RAV4.pdf", name)
        assertEquals(listOf<Byte>(1, 2, 3), bytes.toList())
        assertTrue(exporter.saved.isEmpty())
    }

    @Test
    fun exportPdfOnDesktopSavesFile(): TestResult = test {
        val exporter = FakeBinaryFileExporter(canShare = false)
        val (vm, _) = viewModel(exporter)

        vm.onExportPdf()

        assertTrue(exporter.shared.isEmpty())
        assertEquals(listOf("Паспорт_20261119-234567-1_Toyota_RAV4.pdf"), exporter.saved)
    }

    @Test
    fun exportPdfReportsResults(): TestResult = test {
        val exporter = FakeBinaryFileExporter(canShare = false)
        val (vm, effects) = viewModel(exporter)

        vm.onExportPdf()
        exporter.saveResult = BinaryFileSaveResult.Failed
        vm.onExportPdf()
        exporter.saveResult = BinaryFileSaveResult.Cancelled
        vm.onExportPdf()

        assertEquals(listOf(PassportReadyEffect.Saved("Паспорт.pdf"), PassportReadyEffect.SaveFailed), effects)
        assertEquals(3, exporter.saved.size)
    }

    @Test
    fun exportPdfReportsFailedShare(): TestResult = test {
        val exporter = FakeBinaryFileExporter().apply { shareResult = false }
        val (vm, effects) = viewModel(exporter)

        vm.onExportPdf()

        assertEquals(listOf<PassportReadyEffect>(PassportReadyEffect.ShareFailed), effects)
    }

    @Test
    fun issueFromPreviewRebuildsAndIssuesDraft(): TestResult = test {
        val (vm, _) = viewModel(preview = true)
        assertEquals(1, renderer.renders.size)
        assertNull(vm.state.value.issue)

        vm.onIssue()

        assertEquals(2, renderer.renders.size)
        assertEquals(1, documents.issuedCalls)
        assertEquals(DocumentStatus.Ready, vm.state.value.issue)
        assertEquals(PassportStatus.ISSUED, vm.state.value.passport?.status)
    }

    @Test
    fun updateOfIssuedPassportRebuildsWithoutIssuingAgain(): TestResult = test {
        passportWith { copy(status = PassportStatus.ISSUED) }
        val (vm, _) = viewModel(preview = true)

        vm.onIssue()

        assertEquals(2, renderer.renders.size)
        assertEquals(0, documents.issuedCalls)
        assertEquals(DocumentStatus.Ready, vm.state.value.issue)
    }

    @Test
    fun issueShowsFormingThenReady(): TestResult = test {
        val (vm, _) = viewModel(preview = true)
        renderer.delayMs = 1_000

        vm.onIssue()
        assertEquals(DocumentStatus.Forming, vm.state.value.issue)

        advanceTimeBy(1_100)
        assertEquals(DocumentStatus.Ready, vm.state.value.issue)
    }

    @Test
    fun issueFailureIsShownInDialogAndCanBeRetried(): TestResult = test {
        val (vm, effects) = viewModel(preview = true)
        renderer.result = PdfRenderResult.Failed

        vm.onIssue()

        assertIs<DocumentStatus.Failed>(vm.state.value.issue)
        assertEquals(0, documents.issuedCalls)
        assertTrue(effects.none { it is PassportReadyEffect.RefreshFailed })

        renderer.result = PdfRenderResult.Rendered(byteArrayOf(1, 2, 3), pageCount = 3)
        vm.onIssue()

        assertEquals(DocumentStatus.Ready, vm.state.value.issue)
        assertEquals(1, documents.issuedCalls)
    }

    @Test
    fun issueSurvivesRebuildAfterEdit(): TestResult = test {
        val (vm, _) = viewModel(preview = true)
        renderer.delayMs = 1_000

        vm.onIssue()
        val edited = Instant.fromEpochMilliseconds(ISSUED_AT.toEpochMilliseconds() + 60_000)
        passportWith { copy(updatedAt = edited, remarks = "правка") }
        repository.all.value = Response.Success(listOf(summary(updatedAt = edited)))
        advanceTimeBy(5_000)

        assertEquals(DocumentStatus.Ready, vm.state.value.issue)
        assertEquals(1, documents.issuedCalls)
    }

    @Test
    fun issueDialogClosesOnDismissButStaysOpenOnExportPdf(): TestResult = test {
        val exporter = FakeBinaryFileExporter()
        val (vm, _) = viewModel(exporter, preview = true)

        vm.onIssue()
        vm.onIssueDismiss()
        assertNull(vm.state.value.issue)

        vm.onIssue()
        vm.onExportPdf()
        assertEquals(DocumentStatus.Ready, vm.state.value.issue)
        assertEquals(1, exporter.shared.size)
    }

    @Test
    fun pagesAreRasterizedOnDemandOnce(): TestResult = test {
        val (vm, _) = viewModel()
        assertTrue(rasterizer.renders.isEmpty())

        vm.onPageVisible(1, 600)
        vm.onPageVisible(1, 600)
        assertEquals(listOf(1 to 600), rasterizer.renders)
        assertTrue(1 in vm.state.value.pages)

        vm.onPageVisible(1, 800)
        assertEquals(listOf(1 to 600, 1 to 800), rasterizer.renders)
    }
}
