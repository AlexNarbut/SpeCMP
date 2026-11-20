package ru.alarmtrade.specmp.core.speech

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope

class QueuedSpeakerTest {

    private val engine = RecordingSpeechEngine()

    private val backgrounds = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private fun TestScope.speaker(): Speaker = QueuedSpeaker(
        engine,
        serviceScope(UnconfinedTestDispatcher(testScheduler), parent = backgroundScope.coroutineContext),
        stopRequests = backgrounds,
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

    @Test
    fun `hung engine does not block the next phrases and is reset`() = runTest {
        val speaker = speaker()

        speaker.speak("A", SpeechMode.FLUSH)
        speaker.speak("B", SpeechMode.QUEUE)
        advanceTimeBy(SpeechTimeout.of("A") - 1.milliseconds)
        assertEquals(listOf("A"), engine.started)
        assertEquals(0, engine.resets)

        advanceTimeBy(2.milliseconds)
        assertEquals(listOf("A", "B"), engine.started)
        assertEquals(listOf("A"), engine.interrupted)
        assertEquals(1, engine.resets)

        engine.finishCurrent()
        assertEquals(listOf("B"), engine.completed)
        assertEquals(1, engine.releases)
    }

    @Test
    fun `focus is released after the last phrase hangs`() = runTest {
        val speaker = speaker()

        speaker.speak("A", SpeechMode.FLUSH)
        advanceTimeBy(SpeechTimeout.of("A") + 1.milliseconds)

        assertEquals(1, engine.resets)
        assertEquals(1, engine.releases)
        speaker.speak("B", SpeechMode.QUEUE)
        assertEquals(listOf("A", "B"), engine.started)
    }

    @Test
    fun `phrase finished in time does not reset the engine`() = runTest {
        val speaker = speaker()

        speaker.speak("A", SpeechMode.FLUSH)
        advanceTimeBy(SpeechTimeout.of("A") / 2)
        engine.finishCurrent()
        advanceTimeBy(SpeechTimeout.of("A"))

        assertEquals(listOf("A"), engine.completed)
        assertEquals(0, engine.resets)
    }

    @Test
    fun `speaking lasts until the queue is drained`() = runTest {
        val speaker = speaker()
        assertFalse(speaker.speaking.value)

        speaker.speak("A", SpeechMode.FLUSH)
        speaker.speak("B", SpeechMode.QUEUE)
        assertTrue(speaker.speaking.value)

        engine.finishCurrent()
        assertTrue(speaker.speaking.value)

        engine.finishCurrent()
        assertFalse(speaker.speaking.value)
    }

    @Test
    fun `stop ends speaking`() = runTest {
        val speaker = speaker()

        speaker.speak("A", SpeechMode.FLUSH)
        speaker.stop()

        assertFalse(speaker.speaking.value)
    }

    @Test
    fun `stop request silences the queue and forgets the last phrase`() = runTest {
        val speaker = speaker()

        speaker.speak("A", SpeechMode.FLUSH)
        speaker.speak("B", SpeechMode.QUEUE)
        backgrounds.emit(Unit)

        assertEquals(listOf("A"), engine.interrupted)
        assertFalse(speaker.speaking.value)
        speaker.speak("A", SpeechMode.FLUSH)
        assertEquals(listOf("A", "A"), engine.started)
    }
}
