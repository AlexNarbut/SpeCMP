package ru.alarmtrade.specmp.core.speech

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

internal class RecordingSpeechEngine : SpeechEngine {
    override val availability = MutableStateFlow<SpeechAvailability>(SpeechAvailability.Ready)
    val started = mutableListOf<String>()
    val completed = mutableListOf<String>()
    val interrupted = mutableListOf<String>()
    var prepares = 0
    var releases = 0
    private var current: CompletableDeferred<Unit>? = null

    fun finishCurrent() {
        current?.complete(Unit)
    }

    override fun prepare() {
        prepares++
    }

    override suspend fun speak(text: String) {
        started += text
        val gate = CompletableDeferred<Unit>()
        current = gate
        try {
            gate.await()
        } finally {
            if (gate.isCompleted) completed += text else interrupted += text
        }
    }

    override fun release() {
        releases++
    }

    override fun openVoiceInstall() = Unit
}
