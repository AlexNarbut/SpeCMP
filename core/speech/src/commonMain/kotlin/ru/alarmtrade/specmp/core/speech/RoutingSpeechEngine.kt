package ru.alarmtrade.specmp.core.speech

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.alarmtrade.specmp.core.speech.piper.PiperSpeech
import ru.alarmtrade.specmp.core.speech.piper.PiperState

/**
 * Русский — нейросетевой голос Piper; не поднялся или во фразе незнакомое слово — системный голос (фраза целиком,
 * голоса внутри фразы не смешиваются). Готовый Piper — `Ready`, даже если системного голоса языка нет.
 */
internal class RoutingSpeechEngine(
    private val piper: PiperSpeech,
    private val system: SpeechEngine,
    scope: CoroutineScope,
) : SpeechEngine {

    override val availability: StateFlow<SpeechAvailability> =
        combine(piper.state, system.availability) { neural, fallback ->
            if (neural == PiperState.READY) SpeechAvailability.Ready else fallback
        }.stateIn(scope, SharingStarted.WhileSubscribed(), system.availability.value)

    override fun prepare() {
        piper.prepare()
        system.prepare()
    }

    override suspend fun speak(text: String) {
        if (!piper.speak(text)) system.speak(text)
    }

    override fun release() {
        piper.release()
        system.release()
    }

    override fun reset() {
        piper.release()
        system.reset()
    }

    override fun openVoiceInstall() = system.openVoiceInstall()
}
