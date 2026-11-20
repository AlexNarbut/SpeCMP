package ru.alarmtrade.specmp.core.speech.piper

import kotlin.test.Test
import kotlin.test.assertEquals

class PiperPhonemeIdsTest {

    private val config = PiperVoiceConfig.parse(
        """
        {"audio": {"sample_rate": 22050},
         "inference": {"noise_scale": 0.5, "length_scale": 1.0, "noise_w": 0.333},
         "phoneme_id_map": {"_": [0], "^": [1], "$": [2], " ": [3], "a": [14], "ɪ": [74], "k": [23], "aɪ": [161]},
         "vowel_clusters": [["a", "ɪ"]]}
        """.trimIndent(),
    )

    @Test
    fun `ids are framed by bos and eos with pad after every phoneme`() {
        assertEquals(listOf(1L, 0, 23, 0, 14, 0, 2), PiperPhonemeIds(config).of("ka").toList())
    }

    @Test
    fun `vowel cluster is one token and unknown phonemes are skipped`() {
        assertEquals(listOf(1L, 0, 161, 0, 3, 0, 23, 0, 2), PiperPhonemeIds(config).of("aɪ kʒ").toList())
    }

    @Test
    fun `config carries inference scales and sample rate`() {
        assertEquals(listOf(0.5f, 1.0f, 0.333f), config.scales.toList())
        assertEquals(22050, config.sampleRate)
    }
}
