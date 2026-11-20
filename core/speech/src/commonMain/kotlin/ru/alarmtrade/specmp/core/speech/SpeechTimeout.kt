package ru.alarmtrade.specmp.core.speech

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** Предел фразы: ~2× от темпа речи (~13 символов в секунду) плюс запас на запуск движка. */
internal object SpeechTimeout {
    val base: Duration = 8.seconds
    val perChar: Duration = 150.milliseconds

    fun of(text: String): Duration = base + perChar * text.length
}
