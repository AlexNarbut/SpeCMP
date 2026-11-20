package ru.alarmtrade.specmp.feature.device.presentation.core.voice

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
import ru.alarmtrade.specmp.core.speech.SpeechAvailability
import ru.alarmtrade.specmp.core.testing.FakeHandsFreeSettings
import ru.alarmtrade.specmp.core.testing.FakeSpeaker

@OptIn(ExperimentalCoroutinesApi::class)
class HandsFreeVoiceViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val speaker = FakeSpeaker()
    private val settings = FakeHandsFreeSettings()
    private val viewModels = mutableListOf<HandsFreeVoiceViewModel>()

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

    private fun TestScope.viewModel(): HandsFreeVoiceViewModel {
        val vm = HandsFreeVoiceViewModel(speaker, settings).also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()
        return vm
    }

    private fun TestScope.state(vm: HandsFreeVoiceViewModel): HandsFreeVoiceState {
        runCurrent()
        return vm.state.value
    }

    @Test
    fun `button follows the flag, the voice and speech`() = test {
        val vm = viewModel()
        assertEquals(VoiceButtonMode.OFF, state(vm).mode)

        settings.setEnabled(true)
        assertEquals(VoiceButtonMode.ON, state(vm).mode)

        speaker.speaking.value = true
        assertEquals(VoiceButtonMode.SPEAKING, state(vm).mode)

        speaker.availability.value = SpeechAvailability.NoVoice(canInstall = true)
        assertEquals(VoiceButtonMode.NO_VOICE, state(vm).mode)

        speaker.availability.value = SpeechAvailability.Unsupported
        assertNull(state(vm).mode)
    }

    @Test
    fun `no button until the voice is known`() = test {
        assertNull(HandsFreeVoiceState().mode)
        assertEquals(VoiceButtonMode.OFF, HandsFreeVoiceState(availability = SpeechAvailability.Ready).mode)
    }

    @Test
    fun `first turn on shows the tip once and hides it after seven seconds`() = test {
        val vm = viewModel()
        vm.onToggle()
        assertTrue(state(vm).tip)
        assertTrue(settings.enabled.value)
        assertTrue(settings.tipShown.value)

        advanceTimeBy(6_900)
        assertTrue(state(vm).tip)
        advanceTimeBy(200)
        assertFalse(state(vm).tip)

        vm.onToggle()
        vm.onToggle()
        assertFalse(state(vm).tip)
        assertTrue(settings.enabled.value)
    }

    @Test
    fun `tip closes on got it and on turning off`() = test {
        val vm = viewModel()
        vm.onToggle()
        vm.onTipDismiss()
        assertFalse(state(vm).tip)

        settings.setEnabled(false)
        settings.tipShown.value = false
        vm.onToggle()
        assertTrue(state(vm).tip)
        vm.onToggle()
        assertFalse(state(vm).tip)
        assertFalse(settings.enabled.value)
        advanceTimeBy(8_000)
        assertFalse(state(vm).tip)
    }

    @Test
    fun `no voice opens the help instead of toggling`() = test {
        speaker.availability.value = SpeechAvailability.NoVoice(canInstall = true)
        val vm = viewModel()
        vm.onToggle()
        assertTrue(state(vm).noVoiceHelp)
        assertTrue(state(vm).canInstallVoice)
        assertFalse(settings.enabled.value)

        vm.onInstallVoice()
        assertFalse(state(vm).noVoiceHelp)
        assertEquals(1, speaker.voiceInstalls)

        vm.onToggle()
        vm.onNoVoiceDismiss()
        assertFalse(state(vm).noVoiceHelp)
    }
}
