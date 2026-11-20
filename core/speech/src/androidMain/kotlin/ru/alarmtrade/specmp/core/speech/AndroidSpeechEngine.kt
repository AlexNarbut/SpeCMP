package ru.alarmtrade.specmp.core.speech

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.alarmtrade.specmp.core.common.safeRun

internal class AndroidSpeechEngine(context: Context, private val languageTag: String) : SpeechEngine {

    private val appContext = context.applicationContext
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
    private val audioFocus = SpeechAudioFocus(appContext, attributes)
    private val state = MutableStateFlow<SpeechAvailability>(SpeechAvailability.Ready)
    override val availability: StateFlow<SpeechAvailability> = state.asStateFlow()
    private val initialized = CompletableDeferred<Boolean>()
    private val utterances = ConcurrentHashMap<String, CancellableContinuation<Unit>>()
    private val tts = lazy { TextToSpeech(appContext, ::onInit) }

    override fun prepare() {
        tts.value
    }

    override suspend fun speak(text: String) {
        if (!initialized.await()) return
        val engine = tts.value
        if (state.value != SpeechAvailability.Ready && !applyLanguage(engine)) return
        audioFocus.request()
        suspendCancellableCoroutine { continuation ->
            val id = UUID.randomUUID().toString()
            utterances[id] = continuation
            continuation.invokeOnCancellation {
                utterances.remove(id)
                engine.stop()
            }
            if (engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) == TextToSpeech.ERROR) {
                utterances.remove(id)?.resume(Unit)
            }
        }
    }

    override fun release() = audioFocus.abandon()

    override fun openVoiceInstall() {
        val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        safeRun { appContext.startActivity(intent) }
    }

    // Ошибку инициализации TextToSpeech сообщает прямо из конструктора — до этого tts не трогаем.
    private fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            state.update { SpeechAvailability.Unsupported }
            initialized.complete(false)
            return
        }
        val engine = tts.value
        engine.setAudioAttributes(attributes)
        engine.setOnUtteranceProgressListener(UtteranceListener())
        applyLanguage(engine)
        initialized.complete(true)
    }

    private fun applyLanguage(engine: TextToSpeech): Boolean {
        val result = engine.setLanguage(Locale.forLanguageTag(languageTag))
        val ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        state.update { if (ready) SpeechAvailability.Ready else SpeechAvailability.NoVoice(canInstall = true) }
        return ready
    }

    private fun finish(utteranceId: String?) {
        utteranceId?.let(utterances::remove)?.resume(Unit)
    }

    private inner class UtteranceListener : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit

        override fun onDone(utteranceId: String?) = finish(utteranceId)

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) = finish(utteranceId)

        override fun onError(utteranceId: String?, errorCode: Int) = finish(utteranceId)

        override fun onStop(utteranceId: String?, interrupted: Boolean) = finish(utteranceId)
    }
}
