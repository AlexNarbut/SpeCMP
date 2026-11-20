package ru.alarmtrade.specmp.core.speech

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope
import ru.alarmtrade.specmp.core.speech.piper.PcmPlayer
import ru.alarmtrade.specmp.core.speech.piper.PiperAssets
import ru.alarmtrade.specmp.core.speech.piper.PiperModel
import ru.alarmtrade.specmp.core.speech.piper.PiperSpeech

class RoutingSpeechEngineTest {

    private val system = RecordingSpeechEngine()
    private val player = RecordingPlayer()
    private var loads = 0

    private fun TestScope.engine(load: () -> PiperAssets = ::assets): SpeechEngine {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val scope = serviceScope(dispatcher, parent = backgroundScope.coroutineContext)
        val piper = PiperSpeech(
            loader = {
                loads++
                load()
            },
            player = player,
            io = dispatcher,
            compute = dispatcher,
            scope = scope,
        )
        return RoutingSpeechEngine(piper, system, scope)
    }

    // Системный дублёр держит фразу до finishCurrent — говорим в фоне, чтобы тест не ждал.
    private fun TestScope.speakInBackground(engine: SpeechEngine, text: String) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { engine.speak(text) }
    }

    @Test
    fun `known phrase is spoken by piper only`() = runTest {
        val engine = engine()

        engine.speak("Включите зажигание")

        assertEquals(1, player.played.size)
        assertEquals(PCM_RATE, player.rates.single())
        assertEquals(emptyList(), system.started)
    }

    @Test
    fun `phrase with an unknown word goes to the system voice whole`() = runTest {
        val engine = engine()

        speakInBackground(engine, "Включите зажигание и фары")

        assertEquals(emptyList(), player.played)
        assertEquals(listOf("Включите зажигание и фары"), system.started)
    }

    @Test
    fun `failed model falls back to the system voice and its availability`() = runTest {
        val engine = engine(load = { error("no model") })
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { engine.availability.collect {} }
        system.availability.value = SpeechAvailability.NoVoice(canInstall = true)

        engine.prepare()
        speakInBackground(engine, "Включите зажигание")

        assertEquals(listOf("Включите зажигание"), system.started)
        assertEquals(SpeechAvailability.NoVoice(canInstall = true), engine.availability.value)
    }

    @Test
    fun `ready piper is ready even without a system voice`() = runTest {
        val engine = engine()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { engine.availability.collect {} }
        system.availability.value = SpeechAvailability.NoVoice(canInstall = true)
        assertEquals(SpeechAvailability.NoVoice(canInstall = true), engine.availability.value)

        engine.prepare()

        assertEquals(SpeechAvailability.Ready, engine.availability.value)
    }

    @Test
    fun `model is loaded lazily and once`() = runTest {
        val engine = engine()
        assertEquals(0, loads)

        engine.speak("Включите зажигание")
        engine.speak("Включите зажигание")

        assertEquals(1, loads)
    }

    @Test
    fun `release lets both voices go`() = runTest {
        val engine = engine()

        engine.release()

        assertEquals(1, player.releases)
        assertEquals(1, system.releases)
    }

    private fun assets() = PiperAssets(config = CONFIG, lexicon = LEXICON, model = SilentModel)

    private object SilentModel : PiperModel {
        override fun synthesize(ids: LongArray, scales: FloatArray): FloatArray = FloatArray(ids.size)
    }

    private class RecordingPlayer : PcmPlayer {
        val played = mutableListOf<ShortArray>()
        val rates = mutableListOf<Int>()
        var releases = 0

        override suspend fun play(samples: ShortArray, sampleRate: Int) {
            played += samples
            rates += sampleRate
        }

        override fun release() {
            releases++
        }
    }

    private companion object {
        const val PCM_RATE = 22050
        const val LEXICON = "включите\tfkɭʲu\"tʃʲˈitʲi\nзажигание\tzʌʒyɡˈɑnʲijɪ\nи\tɪ"
        val CONFIG = """
            {"audio": {"sample_rate": $PCM_RATE},
             "inference": {"noise_scale": 0.5, "length_scale": 1.0, "noise_w": 0.333},
             "phoneme_id_map": {"_": [0], "^": [1], "$": [2], " ": [3], "a": [14]},
             "vowel_clusters": [["a", "ɪ"]]}
        """.trimIndent()
    }
}
