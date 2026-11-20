package ru.alarmtrade.specmp.feature.device.presentation.core.voice

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.speech.SpeechAvailability

enum class VoiceButtonMode { OFF, ON, SPEAKING, NO_VOICE }

/**
 * @property availability `null` — голос ещё не проверен (кнопки нет, чтобы не мигнуть ею там, где синтеза нет).
 * @property tip подсказка первого включения у кнопки.
 * @property noVoiceHelp открыт лист (диалог) «Нет голоса».
 */
@Immutable
data class HandsFreeVoiceState(
    val availability: SpeechAvailability? = null,
    val enabled: Boolean = false,
    val speaking: Boolean = false,
    val tip: Boolean = false,
    val noVoiceHelp: Boolean = false,
) {
    /** `null` — кнопки нет (синтез речи не поддерживается). */
    val mode: VoiceButtonMode?
        get() = when (availability) {
            null, SpeechAvailability.Unsupported -> null

            is SpeechAvailability.NoVoice -> VoiceButtonMode.NO_VOICE

            SpeechAvailability.Ready -> when {
                !enabled -> VoiceButtonMode.OFF
                speaking -> VoiceButtonMode.SPEAKING
                else -> VoiceButtonMode.ON
            }
        }

    val canInstallVoice: Boolean get() = (availability as? SpeechAvailability.NoVoice)?.canInstall == true
}
