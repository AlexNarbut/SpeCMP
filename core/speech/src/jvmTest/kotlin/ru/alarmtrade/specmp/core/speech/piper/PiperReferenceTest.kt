package ru.alarmtrade.specmp.core.speech.piper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PiperReferenceTest {

    private val assets by lazy { OrtPiperModel.loadAssets() }
    private val config by lazy { PiperVoiceConfig.parse(assets.config) }
    private val g2p by lazy { RussianG2p.parse(assets.lexicon) }

    @Test
    fun `phoneme ids match piper for every reference phrase`() {
        val ids = PiperPhonemeIds(config)
        val reference = resourceText("/piper/reference.tsv").lineSequence().filter { it.isNotBlank() }.toList()
        assertTrue(reference.size > 100)

        reference.forEach { line ->
            val (text, expected) = line.split('\t')
            val sentences = assertIs<Phonemized.Sentences>(g2p.phonemize(text), text).sentences
            val actual = sentences.joinToString("|") { sentence -> ids.of(sentence).joinToString(",") }
            assertEquals(expected, actual, text)
        }
    }

    @Test
    fun `domain exceptions are stressed by hand`() {
        assertEquals(Phonemized.Sentences(listOf("ʌʃˈypka ɪmmʌbʲiɭˈɑjʑira")), g2p.phonemize("Ошибка иммобилайзера"))
        assertEquals(Phonemized.Sentences(listOf("prʌvʲˈerɪ^tʲi ʃˈynu kˈɑn")), g2p.phonemize("Проверьте шину CAN"))
    }

    @Test
    fun `model synthesizes speech from our phoneme ids`() {
        val sentences = assertIs<Phonemized.Sentences>(g2p.phonemize("Включите зажигание")).sentences
        val audio = assets.model.synthesize(PiperPhonemeIds(config).of(sentences.single()), config.scales)
        val seconds = audio.size.toDouble() / config.sampleRate

        assertTrue(seconds in 0.5..3.0, "duration $seconds s")
        assertTrue(audio.maxOf { kotlin.math.abs(it) } > 0.1f)
    }

    private fun resourceText(path: String): String =
        requireNotNull(PiperReferenceTest::class.java.getResource(path)) { path }.readText()
}
