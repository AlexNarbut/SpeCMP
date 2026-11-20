package ru.alarmtrade.specmp.core.speech

import kotlinx.coroutines.flow.StateFlow

enum class SpeechMode {
    FLUSH,

    QUEUE,
}

sealed interface SpeechAvailability {
    data object Ready : SpeechAvailability

    data class NoVoice(val canInstall: Boolean) : SpeechAvailability

    data object Unsupported : SpeechAvailability
}

/** Голос системы под язык приложения; фраза, совпадающая с последней поставленной, не повторяется. */
interface Speaker {
    val availability: StateFlow<SpeechAvailability>

    /** Очередь не пуста: звучит фраза или ждёт следующая. */
    val speaking: StateFlow<Boolean>

    fun speak(text: String, mode: SpeechMode = SpeechMode.FLUSH)

    fun stop()

    /** Android — установка голоса (`ACTION_INSTALL_TTS_DATA`) при `NoVoice(canInstall = true)`; иначе no-op. */
    fun openVoiceInstall()
}
