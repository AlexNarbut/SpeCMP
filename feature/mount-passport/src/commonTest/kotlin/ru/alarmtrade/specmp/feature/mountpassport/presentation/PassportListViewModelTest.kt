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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageUsage
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.ConnectedUnit
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListEffect
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListMode
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class PassportListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeMountPassportRepository()
    private val transfer = FakePassportTransferRepository()
    private val viewModels = mutableListOf<PassportListViewModel>()

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
        mode: PassportListMode = PassportListMode.Archive,
        connected: Boolean = false,
        deviceId: Long = UNIT_ID,
    ): Pair<PassportListViewModel, MutableList<PassportListEffect>> {
        val session = testDeviceSession(
            FakeDeviceSessionRepository(testDevice(deviceId), link = MutableStateFlow(linkState(connected))),
            dispatcher = dispatcher,
            transport = ConnectionTransport.USB.takeIf { connected },
        )
        val vm = PassportListViewModel(mode, session, repository, transfer, testDispatchers(dispatcher))
            .also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        val effects = mutableListOf<PassportListEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm to effects
    }

    @Test
    fun archiveShowsAllPassports(): TestResult = test {
        repository.all.value = Response.Success(listOf(summary("p1"), summary("p2")))
        val (vm, _) = viewModel()

        val content = requireNotNull(vm.state.value.list.content)
        assertEquals(listOf("p1", "p2"), content.items.map { it.id })
        assertEquals("", content.query)
    }

    @Test
    fun emptyArchiveIsEmptyContentNotError(): TestResult = test {
        val (vm, _) = viewModel()

        assertTrue(requireNotNull(vm.state.value.list.content).items.isEmpty())
    }

    @Test
    fun loadErrorThenRetry(): TestResult = test {
        repository.all.value = Response.Error(AppException(ApiError.ServerError, IllegalStateException("db")))
        val (vm, _) = viewModel()
        assertEquals(LCEState.Error(PassportError.Common(ApiError.ServerError)), vm.state.value.list)

        repository.all.value = Response.Success(listOf(summary()))
        vm.onRetry()
        runCurrent()

        assertEquals(1, vm.state.value.list.content?.items?.size)
    }

    @Test
    fun listUpdatesWhenSdkEmitsChange(): TestResult = test {
        val (vm, _) = viewModel()

        repository.all.value = Response.Success(listOf(summary("p3")))

        assertEquals("p3", vm.state.value.list.content?.items?.single()?.id)
    }

    @Test
    fun unitModeObservesPassportsOfThatUnit(): TestResult = test {
        repository.unit.value = Response.Success(listOf(summary("u1")))
        val (vm, _) = viewModel(mode = PassportListMode.Unit(107131))

        assertEquals(listOf("107131"), repository.unitRequests)
        assertEquals("u1", vm.state.value.list.content?.items?.single()?.id)
    }

    @Test
    fun searchWaitsForDebounceAndSendsQueryOnce(): TestResult = test {
        repository.searchResult = { Response.Success(listOf(summary("found"))) }
        val (vm, _) = viewModel()

        vm.onQueryChange("К")
        vm.onQueryChange("Ко")
        vm.onQueryChange("Ков")
        advanceTimeBy(100)
        assertTrue(repository.searchCalls.isEmpty())

        advanceTimeBy(400)
        assertEquals(listOf("Ков"), repository.searchCalls)
        val content = requireNotNull(vm.state.value.list.content)
        assertEquals("Ков", content.query)
        assertEquals("found", content.items.single().id)
    }

    @Test
    fun newSearchCancelsPrevious(): TestResult = test {
        repository.searchDelayMs = 1_000
        repository.searchResult = { query -> Response.Success(listOf(summary(query))) }
        val (vm, _) = viewModel()

        vm.onQueryChange("а1")
        advanceTimeBy(500)
        assertEquals(listOf("а1"), repository.searchCalls)

        vm.onQueryChange("а12")
        advanceTimeBy(2_000)

        assertEquals(listOf("а1", "а12"), repository.searchCalls)
        assertEquals(listOf("а12"), repository.completedSearches)
        assertEquals("а12", vm.state.value.list.content?.query)
    }

    @Test
    fun clearingQueryReturnsToFullList(): TestResult = test {
        repository.all.value = Response.Success(listOf(summary("p1"), summary("p2")))
        repository.searchResult = { Response.Success(emptyList()) }
        val (vm, _) = viewModel()
        vm.onQueryChange("Х 999")
        advanceTimeBy(500)
        assertTrue(requireNotNull(vm.state.value.list.content).items.isEmpty())

        vm.onQueryChange("")
        runCurrent()

        assertEquals(2, vm.state.value.list.content?.items?.size)
        assertEquals("", vm.state.value.list.content?.query)
    }

    @Test
    fun searchRepeatsWhenArchiveChanges(): TestResult = test {
        repository.searchResult = { Response.Success(listOf(summary("s"))) }
        val (vm, _) = viewModel()
        vm.onQueryChange("Kia")
        advanceTimeBy(500)

        repository.all.value = Response.Success(listOf(summary("new")))
        runCurrent()

        assertEquals(listOf("Kia", "Kia"), repository.searchCalls)
        assertEquals("Kia", vm.state.value.list.content?.query)
    }

    @Test
    fun archiveShowsStorageUsage(): TestResult = test {
        repository.storage = Response.Success(PassportStorageUsage(passports = 128, bytes = 340_000_000))
        val (vm, _) = viewModel()

        assertEquals(PassportStorageUsage(128, 340_000_000), vm.state.value.storage)
    }

    @Test
    fun deleteAsksConfirmationThenDeletes(): TestResult = test {
        val item = summary("p1")
        repository.all.value = Response.Success(listOf(item))
        val (vm, effects) = viewModel()

        vm.onDeleteRequest(item)
        assertEquals(item, vm.state.value.deleteCandidate)
        assertTrue(repository.deleted.isEmpty())

        vm.onDeleteConfirm()
        runCurrent()

        assertEquals(listOf("p1"), repository.deleted)
        assertNull(vm.state.value.deleteCandidate)
        assertEquals(listOf<PassportListEffect>(PassportListEffect.Deleted), effects)
    }

    @Test
    fun deleteDismissKeepsPassport(): TestResult = test {
        val (vm, _) = viewModel()

        vm.onDeleteRequest(summary("p1"))
        vm.onDeleteDismiss()

        assertNull(vm.state.value.deleteCandidate)
        assertTrue(repository.deleted.isEmpty())
    }

    @Test
    fun deleteFailureIsReported(): TestResult = test {
        repository.deleteResult = Response.Error(IllegalStateException("io"))
        val (vm, effects) = viewModel()

        vm.onDeleteRequest(summary("p1"))
        vm.onDeleteConfirm()
        runCurrent()

        assertEquals(listOf<PassportListEffect>(PassportListEffect.DeleteFailed(PassportError.Unknown)), effects)
    }

    @Test
    fun deletingSelectedClearsSelection(): TestResult = test {
        val (vm, _) = viewModel()
        vm.onSelect("p1")
        assertEquals("p1", vm.state.value.selectedId)

        vm.onDeleteRequest(summary("p1"))
        vm.onDeleteConfirm()
        runCurrent()

        assertNull(vm.state.value.selectedId)
    }

    @Test
    fun blankCanBeFinishedOnlyWithSameUnitConnected(): TestResult = test {
        val blank = summary(status = PassportStatus.BLANK)
        val (vm, _) = viewModel(connected = true)

        assertEquals(ConnectedUnit(UNIT_ID.toString(), "Pandora DX-91"), vm.state.value.connected)
        assertTrue(vm.state.value.canFinish(blank))
        assertFalse(vm.state.value.canFinish(blank.copy(systemId = "999")))
        assertFalse(vm.state.value.canFinish(blank.copy(status = PassportStatus.DRAFT)))
    }

    @Test
    fun withoutConnectionBlankCannotBeFinished(): TestResult = test {
        val (vm, _) = viewModel(connected = false)

        assertNull(vm.state.value.connected)
        assertFalse(vm.state.value.canFinish(summary(status = PassportStatus.BLANK)))
        assertIs<PassportListMode.Archive>(vm.state.value.mode)
    }

    @Test
    fun backupReminderIsShownWhenDue(): TestResult = test {
        transfer.remind = Response.Success(true)

        val (vm, _) = viewModel()

        assertTrue(vm.state.value.backupReminder)
    }

    @Test
    fun backupReminderIsHiddenWhenNotDueOrOutsideArchive(): TestResult = test {
        val (archive, _) = viewModel()
        assertFalse(archive.state.value.backupReminder)

        transfer.remind = Response.Success(true)
        val (unit, _) = viewModel(mode = PassportListMode.Unit(107131))
        assertFalse(unit.state.value.backupReminder)

        transfer.remind = Response.Error(IllegalStateException("prefs"))
        val (failed, _) = viewModel()
        assertFalse(failed.state.value.backupReminder)
    }

    @Test
    fun laterSnoozesBackupReminder(): TestResult = test {
        transfer.remind = Response.Success(true)
        val (vm, _) = viewModel()

        vm.onBackupLater()

        assertEquals(1, transfer.snoozes)
        assertFalse(vm.state.value.backupReminder)
    }

    @Test
    fun backupReminderDisappearsOnlyAfterFullBackup(): TestResult = test {
        transfer.remind = Response.Success(true)
        val (vm, _) = viewModel()

        vm.onArchiveBuilt(fullBackup = false)
        assertTrue(vm.state.value.backupReminder)

        vm.onArchiveBuilt(fullBackup = true)
        assertFalse(vm.state.value.backupReminder)
        assertEquals(0, transfer.snoozes)
    }

    @Test
    fun onlyIssuedPassportsArePickedForExport(): TestResult = test {
        repository.all.value = Response.Success(
            listOf(
                summary("p1", status = PassportStatus.ISSUED),
                summary("p2", status = PassportStatus.BLANK),
                summary("p3", status = PassportStatus.ISSUED),
                summary("p4", status = PassportStatus.DRAFT),
            ),
        )
        val (vm, _) = viewModel()

        vm.onPickStart()
        vm.onPickToggle("p1")
        vm.onPickToggle("p2")
        vm.onPickToggle("p4")
        vm.onPickToggle("p3")
        vm.onPickToggle("p3")

        assertTrue(vm.state.value.picking)
        assertEquals(setOf("p1"), vm.state.value.picked)
        assertEquals(listOf("p1", "p3"), vm.state.value.exportable)
    }

    @Test
    fun pickAllTogglesEveryExportablePassport(): TestResult = test {
        repository.all.value = Response.Success(
            listOf(
                summary("p1", status = PassportStatus.ISSUED),
                summary("p2", status = PassportStatus.BLANK),
                summary("p3", status = PassportStatus.ISSUED),
                summary("p4", status = PassportStatus.DRAFT),
            ),
        )
        val (vm, _) = viewModel()
        vm.onPickStart()

        vm.onPickAll()
        assertEquals(setOf("p1", "p3"), vm.state.value.picked)

        vm.onPickAll()
        assertTrue(vm.state.value.picked.isEmpty())
    }

    @Test
    fun pickingEndsOnCancelAndAfterArchive(): TestResult = test {
        repository.all.value = Response.Success(listOf(summary("p1")))
        val (vm, _) = viewModel()
        vm.onPickStart()
        vm.onPickToggle("p1")

        vm.onPickCancel()

        assertFalse(vm.state.value.picking)
        assertTrue(vm.state.value.picked.isEmpty())

        vm.onPickStart()
        vm.onPickToggle("p1")
        vm.onArchiveBuilt(fullBackup = false)

        assertFalse(vm.state.value.picking)
        assertTrue(vm.state.value.picked.isEmpty())
    }
}
