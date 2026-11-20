package ru.alarmtrade.specmp.core.testing

import kotlinx.coroutines.flow.MutableStateFlow
import ru.alarmtrade.specmp.core.speech.Speaker
import ru.alarmtrade.specmp.core.speech.SpeechAvailability
import ru.alarmtrade.specmp.core.speech.SpeechMode

class FakeSpeaker(availability: SpeechAvailability = SpeechAvailability.Ready) : Speaker {
    override val availability = MutableStateFlow(availability)
    override val speaking = MutableStateFlow(false)
    val phrases = mutableListOf<Pair<String, SpeechMode>>()
    var stops = 0
    var voiceInstalls = 0

    val spoken: List<String> get() = phrases.map { it.first }

    override fun speak(text: String, mode: SpeechMode) {
        phrases += text to mode
    }

    override fun stop() {
        stops++
    }

    override fun openVoiceInstall() {
        voiceInstalls++
    }
}
