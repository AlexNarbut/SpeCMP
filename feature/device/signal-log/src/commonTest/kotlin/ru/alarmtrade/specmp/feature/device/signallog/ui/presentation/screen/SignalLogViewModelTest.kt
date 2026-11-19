package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen

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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.time.TimeZoneSource
import ru.alarmtrade.specmp.core.files.TextFileSaveResult
import ru.alarmtrade.specmp.core.files.TextFileType
import ru.alarmtrade.specmp.core.testing.FixedClock
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testBluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSendResult

@OptIn(ExperimentalCoroutinesApi::class)
class SignalLogViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sessionRepository = FakeDeviceSessionRepository()
    private val repository = FakeSignalLogRepository()
    private val fileSaver = FakeTextFileSaver()
    private val clock = FixedClock(Instant.parse("2026-11-18T10:05:00Z"))
    private val viewModels = mutableListOf<SignalLogViewModel>()
    private val effects = mutableListOf<SignalLogEffect>()

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

    private fun TestScope.viewModel(): SignalLogViewModel {
        val session = testDeviceSession(sessionRepository, dispatcher = dispatcher, transport = ConnectionTransport.BLE)
        val vm = SignalLogViewModel(
            session,
            repository,
            fileSaver,
            testDispatchers(dispatcher),
            clock,
            TimeZoneSource.fixed(TimeZone.UTC),
        ).also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm
    }

    private val SignalLogViewModel.current: SignalLogState get() = state.value

    private fun TestScope.record(vararg ids: Long) {
        ids.forEach { repository.record(line(it)) }
        runCurrent()
    }

    @Test
    fun `opens the log by the bluetooth serial, newest first`() = test {
        repository.log = (1L..3L).map(::line)

        val vm = viewModel()

        val state = vm.current
        assertEquals("1", state.serial)
        assertEquals("DX4G", state.deviceName)
        assertEquals(77L, state.systemId)
        assertEquals(LCEState.Content(Unit), state.load)
        assertEquals(listOf(3L, 2L, 1L), state.lines.map { it.id })
        assertFalse(state.hasOlder)
        assertEquals(3, state.summary.count)
        assertEquals(listOf<Pair<Long?, Int>>(null to 100), repository.pages)
    }

    @Test
    fun `unreadable serial is an error, retry reopens`() = test {
        sessionRepository.bluetoothVersions = listOf(Response.Error(IllegalStateException("ble")))
        val vm = viewModel()
        assertEquals(LCEState.Error(SignalLogError.NO_SERIAL), vm.current.load)

        sessionRepository.bluetoothVersions = listOf(Response.Success(testModule))
        vm.onRetry()
        runCurrent()

        assertEquals("1", vm.current.serial)
        assertEquals(LCEState.Content(Unit), vm.current.load)
    }

    @Test
    fun `failed first page is a load error`() = test {
        repository.pageError = IllegalStateException("db")

        val vm = viewModel()

        assertEquals(LCEState.Error(SignalLogError.LOAD_FAILED), vm.current.load)
    }

    @Test
    fun `live lines go to the bottom and are counted, duplicates are dropped`() = test {
        repository.log = (1L..2L).map(::line)
        val vm = viewModel()

        record(3, 4)
        repository.live.tryEmit(Response.Success(line(4)))
        runCurrent()

        assertEquals(listOf(4L, 3L, 2L, 1L), vm.current.lines.map { it.id })
        assertEquals(4, vm.current.summary.count)
        assertEquals(line(4).timeMs, vm.current.summary.lastTimeMs)
    }

    @Test
    fun `older lines load by the cursor of the oldest shown`() = test {
        repository.log = (1L..150L).map(::line)
        val vm = viewModel()
        assertTrue(vm.current.hasOlder)
        assertEquals(51L, vm.current.lines.last().id)

        vm.onLoadOlder()
        runCurrent()

        assertEquals(51L to 100, repository.pages.last())
        assertEquals(150, vm.current.lines.size)
        assertEquals(1L, vm.current.lines.last().id)
        assertFalse(vm.current.hasOlder)
        assertFalse(vm.current.loadingOlder)
    }

    @Test
    fun `pause keeps the feed and counts skipped lines, resume loads them`() = test {
        repository.log = (1L..2L).map(::line)
        val vm = viewModel()

        vm.onPauseToggle()
        runCurrent()
        record(3, 4, 5)

        assertTrue(vm.current.paused)
        assertEquals(3, vm.current.pendingCount)
        assertEquals(listOf(2L, 1L), vm.current.lines.map { it.id })
        assertEquals(5, vm.current.summary.count)

        vm.onPauseToggle()
        runCurrent()

        assertFalse(vm.current.paused)
        assertEquals(0, vm.current.pendingCount)
        assertEquals(listOf(2L), repository.afterRequests)
        assertEquals(listOf(5L, 4L, 3L, 2L, 1L), vm.current.lines.map { it.id })
    }

    @Test
    fun `clear asks with the count of the whole log and empties the feed`() = test {
        repository.log = (1L..3L).map(::line)
        val vm = viewModel()

        vm.onClearRequest()
        runCurrent()
        assertEquals(SignalLogDialog.ConfirmClear(3), vm.current.dialog)

        vm.onClearConfirm()
        runCurrent()

        assertEquals(1, repository.clears)
        assertNull(vm.current.dialog)
        assertTrue(vm.current.lines.isEmpty())
        assertEquals(0, vm.current.summary.count)
        assertFalse(vm.current.hasLines)
        assertEquals(listOf<SignalLogEffect>(SignalLogEffect.Cleared), effects)
    }

    @Test
    fun `failed clear keeps the log`() = test {
        repository.log = (1L..3L).map(::line)
        repository.clearResult = Response.Error(IllegalStateException("db"))
        val vm = viewModel()

        vm.onClearRequest()
        runCurrent()
        vm.onClearConfirm()
        runCurrent()

        assertEquals(3, vm.current.lines.size)
        assertEquals(listOf<SignalLogEffect>(SignalLogEffect.Failed(SignalLogOperation.CLEAR)), effects)
    }

    @Test
    fun `search finds in the whole log after a pause in typing and steps through matches`() = test {
        repository.log = (1L..5L).map { line(it, if (it % 2 == 1L) "ARM zone $it" else "idle") }
        val vm = viewModel()

        vm.onSearchOpen()
        vm.onSearchQuery("ar")
        vm.onSearchQuery("arm")
        runCurrent()
        advanceTimeBy(SEARCH_WAIT)
        runCurrent()

        assertEquals(listOf("arm"), repository.searches)
        val search = assertIs<SignalLogSearch>(vm.current.search)
        assertEquals(listOf(1L, 3L, 5L), search.matches.map { it.id })
        assertEquals(5L, search.currentLine?.id)

        vm.onSearchStep(older = true)
        runCurrent()
        assertEquals(3L, vm.current.search?.currentLine?.id)

        vm.onOnlyMatchesToggle()
        runCurrent()
        assertEquals(true, vm.current.search?.onlyMatches)

        repository.record(line(6, "idle"))
        repository.record(line(7, "DISARM"))
        runCurrent()
        assertEquals(4, vm.current.search?.matches?.size, "a live line with the query is a new match")
    }

    @Test
    fun `match older than the loaded window loads pages up to it`() = test {
        repository.log = (1L..700L).map { line(it, if (it == 10L) "ALARM tilt" else "idle") }
        val vm = viewModel()
        assertEquals(601L, vm.current.lines.last().id)

        vm.onSearchOpen()
        vm.onSearchQuery("tilt")
        advanceTimeBy(SEARCH_WAIT)
        runCurrent()

        assertTrue(vm.current.lines.any { it.id == 10L })
        assertEquals(listOf<Pair<Long?, Int>>(null to 100, 601L to 500, 101L to 500), repository.pages)
    }

    @Test
    fun `problem is required for sending and for the file`() = test {
        repository.log = (1L..3L).map(::line)
        val vm = viewModel()
        vm.onShareOpen()
        vm.onProblemChange("   ")
        vm.onSend(PROBLEM_TITLE)
        vm.onSave(PROBLEM_TITLE)
        runCurrent()

        assertTrue(vm.current.share.problemError)
        assertTrue(repository.sends.isEmpty())
        assertTrue(fileSaver.saves.isEmpty())

        vm.onProblemChange("Тревога")
        runCurrent()
        assertFalse(vm.current.share.problemError)
    }

    @Test
    fun `sent log closes the sheet and clears the form`() = test {
        repository.log = (1L..3L).map(::line)
        val vm = viewModel()
        vm.onShareOpen()
        vm.onProblemChange(" Тревога сама ")
        vm.onSend(PROBLEM_TITLE)
        runCurrent()

        assertEquals(listOf(PROBLEM_TITLE to "Тревога сама"), repository.sends)
        assertFalse(vm.current.sheetOpen)
        assertEquals(SignalLogShareForm(), vm.current.share)
        assertEquals(listOf<SignalLogEffect>(SignalLogEffect.Sent), effects)
    }

    @Test
    fun `queued log closes the sheet with its own message`() = test {
        repository.log = (1L..3L).map(::line)
        repository.sendResult = Response.Success(SignalLogSendResult.Queued)
        val vm = viewModel()
        vm.onShareOpen()
        vm.onProblemChange("x")
        vm.onSend(PROBLEM_TITLE)
        runCurrent()

        assertFalse(vm.current.sheetOpen)
        assertEquals(listOf<SignalLogEffect>(SignalLogEffect.Queued), effects)
    }

    @Test
    fun `failed send keeps the sheet with the form and offers a retry`() = test {
        repository.log = (1L..3L).map(::line)
        repository.sendResult = Response.Error(IllegalStateException("server"))
        val vm = viewModel()
        vm.onShareOpen()
        vm.onProblemChange("x")
        vm.onSend(PROBLEM_TITLE)
        runCurrent()

        assertTrue(vm.current.sheetOpen)
        assertTrue(vm.current.share.sendFailed)
        assertNull(vm.current.share.busy)
        assertEquals("x", vm.current.share.problem)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `report is saved to a txt file named by serial and local time`() = test {
        repository.log = (1L..3L).map(::line)
        val vm = viewModel()
        vm.onShareOpen()
        vm.onProblemChange("x")
        vm.onSave(PROBLEM_TITLE)
        runCurrent()

        val (name, type, text) = fileSaver.saves.single()
        assertEquals("signal-log_1_20261118-1005.txt", name)
        assertEquals(TextFileType.Txt, type)
        assertEquals("$PROBLEM_TITLE: x", text)
        assertFalse(vm.current.sheetOpen)
        assertEquals(listOf<SignalLogEffect>(SignalLogEffect.Saved(name)), effects)
    }

    @Test
    fun `cancelled file dialog keeps the sheet silently`() = test {
        repository.log = (1L..3L).map(::line)
        fileSaver.result = TextFileSaveResult.Cancelled
        val vm = viewModel()
        vm.onShareOpen()
        vm.onProblemChange("x")
        vm.onSave(PROBLEM_TITLE)
        runCurrent()

        assertTrue(vm.current.sheetOpen)
        assertNull(vm.current.share.busy)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `empty log blocks share, clear, search and pause`() = test {
        val vm = viewModel()
        assertEquals(LCEState.Content(Unit), vm.current.load)
        assertFalse(vm.current.hasLines)

        vm.onShareOpen()
        vm.onClearRequest()
        vm.onSearchOpen()
        vm.onPauseToggle()
        runCurrent()

        assertFalse(vm.current.sheetOpen)
        assertNull(vm.current.dialog)
        assertNull(vm.current.search)
        assertFalse(vm.current.paused)
    }

    @Test
    fun `lost link keeps the log available and releases the pause, return resumes recording`() = test {
        repository.log = (1L..2L).map(::line)
        val vm = viewModel()
        vm.onPauseToggle()
        runCurrent()

        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()

        assertFalse(vm.current.connected)
        assertFalse(vm.current.paused)
        assertEquals(2, vm.current.lines.size)
        vm.onShareOpen()
        runCurrent()
        assertTrue(vm.current.sheetOpen, "sharing works without the unit")

        sessionRepository.link.value = DeviceLinkState.READY
        advanceTimeBy(RECONNECT_WAIT)
        runCurrent()
        record(3)

        assertTrue(vm.current.connected)
        assertEquals(listOf(3L, 2L, 1L), vm.current.lines.map { it.id })
    }

    private companion object {
        const val PROBLEM_TITLE = "Проблема"
        const val SEARCH_WAIT = 400L
        const val RECONNECT_WAIT = 5_000L
        val testModule = testBluetoothModule

        fun line(id: Long, text: String = "line $id") = SignalLogLine(id = id, timeMs = 1_000L * id, text = text)
    }
}
