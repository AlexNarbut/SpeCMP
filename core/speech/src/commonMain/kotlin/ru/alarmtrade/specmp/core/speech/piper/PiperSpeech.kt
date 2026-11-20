package ru.alarmtrade.specmp.core.speech.piper

import co.touchlab.kermit.Logger
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeRun

/** Модель Piper в памяти: ID фонем одного предложения → PCM (float, моно, [PiperVoiceConfig.sampleRate]). */
internal interface PiperModel {
    fun synthesize(ids: LongArray, scales: FloatArray): FloatArray
}

internal class PiperAssets(val config: String, val lexicon: String, val model: PiperModel)

internal interface PcmPlayer {
    /** Возвращается, когда звук доигран; отмена корутины обрывает звук. */
    suspend fun play(samples: ShortArray, sampleRate: Int)

    fun release()
}

internal enum class PiperState {
    IDLE,
    LOADING,
    READY,
    FAILED,
}

/** Нейросетевой голос: модель грузится лениво (первая фраза или [prepare]), синтез — на [compute]. */
internal class PiperSpeech(
    loader: () -> PiperAssets,
    private val player: PcmPlayer,
    io: CoroutineDispatcher,
    private val compute: CoroutineDispatcher,
    scope: CoroutineScope,
) {
    private val logger = Logger.withTag(this::class.simpleName.orEmpty())
    private val mutableState = MutableStateFlow(PiperState.IDLE)
    val state: StateFlow<PiperState> = mutableState.asStateFlow()

    private val voice = scope.async(io, start = CoroutineStart.LAZY) {
        mutableState.update { PiperState.LOADING }
        val started = TimeSource.Monotonic.markNow()
        when (val loaded = safeRun { LoadedVoice(loader()) }) {
            is Response.Success -> {
                logger.i { "Piper voice loaded in ${started.elapsedNow().inWholeMilliseconds} ms" }
                mutableState.update { PiperState.READY }
                loaded.value
            }

            is Response.Error -> {
                mutableState.update { PiperState.FAILED }
                null
            }
        }
    }

    fun prepare() {
        voice.start()
    }

    /** `false` — фразу не сказали (модель не поднялась, незнакомое слово, ошибка синтеза): говорить другим голосом. */
    suspend fun speak(text: String): Boolean {
        val loaded = voice.await() ?: return false
        val sentences = when (val phonemized = loaded.g2p.phonemize(text)) {
            is Phonemized.Sentences -> phonemized.sentences

            is Phonemized.Unknown -> {
                logger.i { "Unknown token '${phonemized.token}', phrase goes to the system voice" }
                return false
            }
        }
        for (sentence in sentences) {
            val pcm = withContext(compute) { safeRun { loaded.synthesize(sentence) } }
            if (pcm !is Response.Success) return false
            if (safeRun { player.play(pcm.value, loaded.config.sampleRate) } !is Response.Success) return false
        }
        return true
    }

    fun release() = player.release()

    private inner class LoadedVoice(assets: PiperAssets) {
        val config = PiperVoiceConfig.parse(assets.config)
        val g2p = RussianG2p.parse(assets.lexicon)
        private val ids = PiperPhonemeIds(config)
        private val model = assets.model

        fun synthesize(sentence: String): ShortArray {
            val started = TimeSource.Monotonic.markNow()
            val pcm = model.synthesize(ids.of(sentence), config.scales).toPcm16()
            logger.d {
                "Synthesized ${pcm.size * MILLIS_PER_SECOND / config.sampleRate} ms of speech " +
                    "in ${started.elapsedNow().inWholeMilliseconds} ms"
            }
            return pcm
        }
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1000
    }
}

internal fun FloatArray.toPcm16(): ShortArray = ShortArray(size) { index ->
    (this[index] * Short.MAX_VALUE).coerceIn(-Short.MAX_VALUE.toFloat(), Short.MAX_VALUE.toFloat()).toInt().toShort()
}
