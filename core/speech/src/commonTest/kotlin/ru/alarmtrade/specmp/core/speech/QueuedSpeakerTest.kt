package ru.alarmtrade.specmp.core.speech

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope

class QueuedSpeakerTest {

    private val engine = RecordingSpeechEngine()

    private fun TestScope.speaker(): Speaker = QueuedSpeaker(
        engine,
        serviceScope(UnconfinedTestDispatcher(testScheduler), parent = backgroundScope.coroutineContext),
    )

    @Test
    fun `queued phrases are spoken one after another and focus is released at the end`() = runTest {
        val speaker = speaker()

        speaker.speak("A", SpeechMode.FLUSH)
        speaker.speak("B", SpeechMode.QUEUE)
        assertEquals(listOf("A"), engine.started)

        engine.finishCurrent()
        assertEquals(listOf("A", "B"), engine.started)
        assertEquals(0, engine.releases)

        engine.finishCurrent()
        assertEquals(listOf("A", "B"), engine.completed)
        assertEquals(1, engine.releases)
    }

    @Test
    fun `flush interrupts current phrase and drops the queue`() = runTest {
        val speaker = speaker()

        speaker.speak("A", SpeechMode.FLUSH)
        speaker.speak("B", SpeechMode.QUEUE)
        speaker.speak("C", SpeechMode.FLUSH)

        assertEquals(listOf("A", "C"), engine.started)
        assertEquals(listOf("A"), engine.interrupted)

        engine.finishCurrent()
        assertEquals(listOf("C"), engine.completed)
        assertEquals(listOf("A", "C"), engine.started)
    }

    @Test
    fun `same phrase in a row is not repeated in any mode`() = runTest {
        val speaker = speaker()

        speaker.speak("A", SpeechMode.FLUSH)
        speaker.speak("A", SpeechMode.FLUSH)
        speaker.speak("A", SpeechMode.QUEUE)
        engine.finishCurrent()
        speaker.speak("A", SpeechMode.FLUSH)

        assertEquals(listOf("A"), engine.started)
        assertEquals(emptyList(), engine.interrupted)
    }

    @Test
    fun `phrase repeats after a different one`() = runTest {
        val speaker = speaker()

        speaker.speak("A", SpeechMode.FLUSH)
        speaker.speak("B", SpeechMode.QUEUE)
        speaker.speak("A", SpeechMode.QUEUE)
        engine.finishCurrent()
        engine.finishCurrent()
        engine.finishCurrent()

        assertEquals(listOf("A", "B", "A"), engine.completed)
    }

    @Test
    fun `stop silences current phrase, clears the queue and releases focus`() = runTest {
        val speaker = speaker()

        speaker.speak("A", SpeechMode.FLUSH)
        speaker.speak("B", SpeechMode.QUEUE)
        speaker.stop()

        assertEquals(listOf("A"), engine.interrupted)
        assertEquals(listOf("A"), engine.started)
        assertEquals(1, engine.releases)
    }

    @Test
    fun `after stop the last phrase can be spoken again`() = runTest {
        val speaker = speaker()

        speaker.speak("A", SpeechMode.FLUSH)
        speaker.stop()
        speaker.speak("A", SpeechMode.FLUSH)

        assertEquals(listOf("A", "A"), engine.started)
    }

    @Test
    fun `blank phrase is ignored`() = runTest {
        val speaker = speaker()

        speaker.speak("  ", SpeechMode.FLUSH)

        assertEquals(emptyList(), engine.started)
        assertEquals(0, engine.prepares)
    }

    @Test
    fun `availability prepares the engine lazily`() = runTest {
        val speaker = speaker()
        assertEquals(0, engine.prepares)

        engine.availability.value = SpeechAvailability.NoVoice(canInstall = true)

        assertSame(engine.availability, speaker.availability)
        assertEquals(SpeechAvailability.NoVoice(canInstall = true), speaker.availability.value)
        assertEquals(1, engine.prepares)
    }
}
