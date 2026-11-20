package ru.alarmtrade.specmp.feature.device.presentation.core.voice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_msg_locked
import ru.alarmtrade.specmp.core.speech.SpeechMode
import ru.alarmtrade.specmp.core.testing.FakeSpeaker

class SpeakerVoiceTest {

    private val speaker = FakeSpeaker()
    private val cancel = text(Strings.string.deviceinteraction_pin_btn_cancel)
    private val locked = text(Strings.string.deviceinteraction_pin_msg_locked, 5)

    @Test
    fun `phrase lines go first in its mode then queued`() = runTest {
        speaker.say(
            voicePhrase(
                voiceText(Strings.string.deviceinteraction_pin_btn_cancel),
                VoiceText.Raw("Задание"),
                voiceText(Strings.string.deviceinteraction_pin_msg_locked, 5),
            ),
        )
        assertEquals(
            listOf(cancel to SpeechMode.FLUSH, "Задание" to SpeechMode.QUEUE, locked to SpeechMode.QUEUE),
            speaker.phrases,
        )
    }

    @Test
    fun `follow compares each value with the previous one and stops when turned off`() = runTest {
        val enabled = MutableStateFlow(true)
        val states = MutableStateFlow(1)
        val seen = mutableListOf<Pair<Int?, Int>>()
        backgroundScope.launch {
            speaker.follow(enabled, states) { previous, current ->
                seen += previous to current
                VoicePhrase(listOf(VoiceText.Raw("$current")))
            }
        }
        runCurrent()
        states.value = 2
        runCurrent()
        assertEquals(listOf<Pair<Int?, Int>>(null to 1, 1 to 2), seen)
        assertEquals(listOf("1", "2"), speaker.spoken)

        enabled.value = false
        runCurrent()
        assertEquals(1, speaker.stops)
        states.value = 3
        runCurrent()
        assertEquals(listOf("1", "2"), speaker.spoken)

        enabled.value = true
        runCurrent()
        assertEquals(null to 3, seen.last())
        assertEquals(listOf("1", "2", "3"), speaker.spoken)
    }

    @Test
    fun `follow is silent for transitions without a phrase`() = runTest {
        val states = MutableStateFlow(1)
        backgroundScope.launch { speaker.follow(MutableStateFlow(true), states) { _, _ -> null } }
        runCurrent()
        states.value = 2
        runCurrent()
        assertEquals(emptyList(), speaker.phrases)
        assertEquals(0, speaker.stops)
    }

    private fun text(resource: StringResource, vararg args: Any) = runBlocking { getString(resource, *args) }
}
