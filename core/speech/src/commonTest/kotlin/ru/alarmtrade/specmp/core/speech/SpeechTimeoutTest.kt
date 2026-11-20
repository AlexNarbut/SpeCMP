package ru.alarmtrade.specmp.core.speech

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class SpeechTimeoutTest {

    @Test
    fun `short phrase gets the base reserve`() {
        assertEquals(SpeechTimeout.base, SpeechTimeout.of(""))
        assertTrue(SpeechTimeout.of("Принято") < 10.seconds)
    }

    @Test
    fun `timeout grows with the text length`() {
        assertEquals(SpeechTimeout.base + SpeechTimeout.perChar * 10, SpeechTimeout.of("0123456789"))
        assertTrue(SpeechTimeout.of("Включите зажигание") < SpeechTimeout.of("Включите зажигание и нажмите тормоз"))
    }

    @Test
    fun `timeout leaves at least twice the time a phrase is spoken`() {
        val phrase = "Этап 3 из 5: чтение ключа. Не прерывайте соединение"
        val spokenAtThirteenCharsPerSecond = (phrase.length / 13.0).seconds

        assertTrue(SpeechTimeout.of(phrase) >= spokenAtThirteenCharsPerSecond * 2)
    }
}
