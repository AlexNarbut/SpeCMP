package ru.alarmtrade.specmp.feature.device.presentation.core.voice

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import ru.alarmtrade.specmp.core.speech.HandsFreeSettings
import ru.alarmtrade.specmp.core.speech.Speaker
import ru.alarmtrade.specmp.core.speech.SpeechAvailability
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel

/** Кнопка «Озвучка»: флаг режима, голос и подсказки. Что и когда говорить, решают VM экранов шагов. */
class HandsFreeVoiceViewModel(private val speaker: Speaker, private val settings: HandsFreeSettings) :
    BaseViewModel<HandsFreeVoiceState, Nothing>(HandsFreeVoiceState(enabled = settings.enabled.value)) {

    override suspend fun IntentScope.onStart() {
        speaker.availability.collectIntoState { availability -> copy(availability = availability) }
        speaker.speaking.collectIntoState { speaking -> copy(speaking = speaking) }
        settings.enabled.collectIntoState { enabled -> copy(enabled = enabled) }
    }

    fun onToggle() {
        intent {
            if (currentState.availability is SpeechAvailability.NoVoice) {
                reduce { copy(noVoiceHelp = true) }
                return@intent
            }
            val enabled = !settings.enabled.value
            settings.setEnabled(enabled)
            val firstTime = enabled && !settings.tipShown.value
            if (firstTime) settings.markTipShown()
            reduce { copy(enabled = enabled, tip = firstTime) }
            if (firstTime) {
                launchReplacing(TIP) {
                    delay(TIP_VISIBLE)
                    reduce { copy(tip = false) }
                }
            } else {
                cancelLaunched(TIP)
            }
        }
    }

    fun onTipDismiss() {
        intent {
            cancelLaunched(TIP)
            reduce { copy(tip = false) }
        }
    }

    fun onNoVoiceDismiss() {
        intent { reduce { copy(noVoiceHelp = false) } }
    }

    fun onInstallVoice() {
        intent {
            speaker.openVoiceInstall()
            reduce { copy(noVoiceHelp = false) }
        }
    }

    private companion object {
        const val TIP = "tip"
        val TIP_VISIBLE = 7.seconds
    }
}
