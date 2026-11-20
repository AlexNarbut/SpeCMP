@file:OptIn(ExperimentalForeignApi::class)

package ru.alarmtrade.specmp.core.speech

import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDuckOthers
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.setActive
import platform.darwin.NSObject

internal class IosSpeechEngine(private val languageTag: String) : SpeechEngine {

    private val state = MutableStateFlow<SpeechAvailability>(SpeechAvailability.Ready)
    override val availability: StateFlow<SpeechAvailability> = state.asStateFlow()
    private val voice by lazy { findVoice(languageTag) }
    private val delegate = FinishDelegate()
    private val synthesizer by lazy { AVSpeechSynthesizer().also { it.delegate = delegate } }
    private val sessionActive = MutableStateFlow(false)

    override fun prepare() {
        val available = voice != null
        state.update { if (available) SpeechAvailability.Ready else SpeechAvailability.NoVoice(canInstall = false) }
    }

    override suspend fun speak(text: String) {
        val voice = voice ?: return
        withContext(Dispatchers.Main) {
            activateSession()
            suspendCancellableCoroutine { continuation ->
                val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
                utterance.voice = voice
                delegate.await(utterance, continuation)
                continuation.invokeOnCancellation {
                    synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
                }
                synthesizer.speakUtterance(utterance)
            }
        }
    }

    override fun release() {
        if (!sessionActive.compareAndSet(expect = true, update = false)) return
        AVAudioSession.sharedInstance().setActive(false, AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, null)
    }

    override fun openVoiceInstall() = Unit

    // playback + duckOthers: говорим и при беззвучном режиме, музыку только приглушаем.
    private fun activateSession() {
        if (!sessionActive.compareAndSet(expect = false, update = true)) return
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, AVAudioSessionCategoryOptionDuckOthers, null)
        session.setActive(true, null)
    }

    private fun findVoice(languageTag: String): AVSpeechSynthesisVoice? =
        AVSpeechSynthesisVoice.voiceWithLanguage(languageTag)
            ?: AVSpeechSynthesisVoice.speechVoices()
                .filterIsInstance<AVSpeechSynthesisVoice>()
                .firstOrNull { it.language.startsWith(languageTag) }
}

private class FinishDelegate :
    NSObject(),
    AVSpeechSynthesizerDelegateProtocol {

    private val waiting = MutableStateFlow<Pair<AVSpeechUtterance, CancellableContinuation<Unit>>?>(null)

    fun await(utterance: AVSpeechUtterance, continuation: CancellableContinuation<Unit>) {
        waiting.update { utterance to continuation }
    }

    override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didFinishSpeechUtterance: AVSpeechUtterance) {
        val current = waiting.value ?: return
        if (current.first !== didFinishSpeechUtterance) return
        if (waiting.compareAndSet(current, null)) current.second.resume(Unit)
    }
}
