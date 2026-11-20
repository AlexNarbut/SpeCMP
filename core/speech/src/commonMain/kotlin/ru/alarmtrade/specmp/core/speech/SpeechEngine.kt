package ru.alarmtrade.specmp.core.speech

import kotlinx.coroutines.flow.StateFlow

internal interface SpeechEngine {
    val availability: StateFlow<SpeechAvailability>

    fun prepare()

    /** Возвращается, когда фраза договорена или говорить нечем; отмена корутины обрывает фразу. */
    suspend fun speak(text: String)

    /** Очередь опустела: отпустить аудиофокус/сессию. */
    fun release()

    /** Фраза не завершилась за [SpeechTimeout]: движок завис, пересоздать его. */
    fun reset()

    fun openVoiceInstall()
}
