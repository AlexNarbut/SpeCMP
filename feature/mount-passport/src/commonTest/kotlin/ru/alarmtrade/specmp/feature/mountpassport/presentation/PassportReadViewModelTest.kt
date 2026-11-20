package ru.alarmtrade.specmp.feature.mountpassport.presentation

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadCommand
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadPhase
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStep
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportTransport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnavailableReason
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read.PassportReadArgs
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read.PassportReadEffect
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read.PassportReadViewModel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read.ReadStop

@OptIn(ExperimentalCoroutinesApi::class)
class PassportReadViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val reading = FakePassportReadingRepository()
    private val installers = FakePassportInstallerRepository()
    private val viewModels = mutableListOf<PassportReadViewModel>()

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
        source: PassportReadSource = PassportReadSource.AdvancedMount,
    ): Pair<PassportReadViewModel, MutableList<PassportReadEffect>> {
        val vm = PassportReadViewModel(
            args = PassportReadArgs(source),
            reading = reading,
            installers = installers,
            dispatchers = testDispatchers(dispatcher),
        ).also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        val effects = mutableListOf<PassportReadEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm to effects
    }

    private fun state(vararg statuses: PassportStepStatus, phase: PassportReadPhase = PassportReadPhase.Reading) =
        PassportReadState(
            steps = PassportReadStep.entries.mapIndexed { index, step ->
                PassportStepState(step, statuses.getOrElse(index) { PassportStepStatus.Waiting })
            },
            phase = phase,
            transport = PassportTransport.BLUETOOTH,
            unitName = "Pandora DX-91",
        )

    private suspend fun emit(state: PassportReadState) = reading.states.emit(Response.Success(state))

    private val block = PassportStepStatus.Done(PassportStepSummary.Block("Pandora DX-91", "1234567", "3.57"))

    @Test
    fun readingStartsOnCreateWithInstallerFromProfile(): TestResult = test {
        viewModel(PassportReadSource.Complete("p1"))

        val (source, installer) = reading.reads.single()
        assertEquals(PassportReadSource.Complete("p1"), source)
        assertEquals("Алексей Смирнов", installer.name)
        assertEquals("+79123456789", installer.contact)
    }

    @Test
    fun withoutProfileReadsWithEmptyInstaller(): TestResult = test {
        installers.result = Response.Error(IllegalStateException("no profile"))

        viewModel()

        assertEquals(PassportInstaller(), reading.reads.single().second)
    }

    @Test
    fun stepsFollowSdk(): TestResult = test {
        val (vm, _) = viewModel()

        emit(state(block, PassportStepStatus.NotRead(connectionLost = false), PassportStepStatus.Reading))

        assertEquals(block, vm.state.value.read.status(PassportReadStep.BLOCK))
        assertEquals(PassportStepStatus.Reading, vm.state.value.read.status(PassportReadStep.DEVICES))
        assertEquals("Pandora DX-91", vm.state.value.read.unitName)
        assertEquals(0.5f, vm.state.value.progress)
        assertNull(vm.state.value.stop)
    }

    @Test
    fun settingsTimerRunsOnlyWhileSettingsAreRead(): TestResult = test {
        val (vm, _) = viewModel()
        val done = PassportStepStatus.Done(PassportStepSummary.Devices(2, PassportTransport.BLUETOOTH))

        emit(state(block, block, done, PassportStepStatus.Reading))
        advanceTimeBy(3_100)
        assertEquals(3, vm.state.value.settingsSeconds)

        emit(state(block, block, done, PassportStepStatus.Skipped, PassportStepStatus.Reading))
        advanceTimeBy(5_000)
        assertEquals(3, vm.state.value.settingsSeconds)
    }

    @Test
    fun skipSendsSkip(): TestResult = test {
        val (vm, _) = viewModel()

        vm.onSkipSettings()

        assertEquals(listOf(PassportReadCommand.SKIP), reading.commands)
    }

    @Test
    fun unitFailureStopsWithRetry(): TestResult = test {
        val (vm, _) = viewModel()

        emit(state(PassportStepStatus.NotRead(false), phase = PassportReadPhase.UnitFailed(connectionLost = false)))
        vm.onRetry()

        assertEquals(ReadStop.UnitFailed(connectionLost = false), vm.state.value.stop)
        assertEquals(listOf(PassportReadCommand.RETRY), reading.commands)
        assertEquals(1, reading.reads.size)
    }

    @Test
    fun lostConnectionContinuesWithoutData(): TestResult = test {
        val (vm, _) = viewModel()

        emit(
            state(
                block,
                block,
                PassportStepStatus.NotRead(connectionLost = true),
                phase = PassportReadPhase.ConnectionLost(PassportReadStep.DEVICES),
            ),
        )
        vm.onContinueWithout()

        assertEquals(ReadStop.ConnectionLost(PassportReadStep.DEVICES), vm.state.value.stop)
        assertEquals(listOf(PassportReadCommand.CONTINUE_WITHOUT), reading.commands)
    }

    @Test
    fun offlineAndOtherStopsAreShown(): TestResult = test {
        val (vm, _) = viewModel()

        emit(state(phase = PassportReadPhase.Offline(canContinueWithout = true)))
        assertEquals(ReadStop.Offline(canContinueWithout = true), vm.state.value.stop)

        emit(state(phase = PassportReadPhase.WrongUnit("1234567", "7654321")))
        assertEquals(ReadStop.WrongUnit("1234567", "7654321"), vm.state.value.stop)

        emit(state(phase = PassportReadPhase.SaveFailed))
        assertEquals(ReadStop.SaveFailed, vm.state.value.stop)
    }

    @Test
    fun unavailableSettingsDoNotStop(): TestResult = test {
        val (vm, _) = viewModel()

        emit(state(block, block, block, PassportStepStatus.Unavailable(PassportUnavailableReason.FACTORY_CAN)))

        assertNull(vm.state.value.stop)
        assertEquals(0.8f, vm.state.value.progress)
    }

    @Test
    fun doneOpensEditorOnceAfterPause(): TestResult = test {
        val (vm, effects) = viewModel()
        val finished = PassportStepStatus.Done(PassportStepSummary.Ready("p9", "20261119-234567-1"))

        emit(state(block, block, block, block, finished, phase = PassportReadPhase.Done("p9")))
        assertTrue(effects.isEmpty())
        assertEquals(1f, vm.state.value.progress)

        advanceTimeBy(1_000)
        emit(state(block, block, block, block, finished, phase = PassportReadPhase.Done("p9")))
        advanceTimeBy(1_000)

        assertEquals(listOf<PassportReadEffect>(PassportReadEffect.Done("p9")), effects)
    }

    @Test
    fun cancelStopsReadingWithoutRecord(): TestResult = test {
        val (vm, effects) = viewModel()
        emit(state(PassportStepStatus.Reading))
        assertEquals(1, reading.states.subscriptionCount.value)

        vm.onCancel()
        runCurrent()

        assertEquals(0, reading.states.subscriptionCount.value)
        assertEquals(listOf<PassportReadEffect>(PassportReadEffect.Cancelled), effects)
        assertTrue(reading.commands.isEmpty())
    }

    @Test
    fun sdkErrorStopsAndRetryStartsAgain(): TestResult = test {
        val (vm, _) = viewModel()

        reading.states.emit(Response.Error(IllegalStateException("broken")))
        assertEquals(ReadStop.Failed(PassportError.Unknown), vm.state.value.stop)

        vm.onRetry()
        runCurrent()

        assertNull(vm.state.value.stop)
        assertEquals(2, reading.reads.size)
        assertTrue(reading.commands.isEmpty())
    }
}
