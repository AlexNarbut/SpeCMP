package ru.alarmtrade.specmp.core.speech.piper

import kotlin.test.Test
import kotlin.test.assertEquals

class RussianG2pTest {

    private val g2p = RussianG2p.parse(
        """
        включите	fkɭʲu"tʃʲˈitʲi
        зажигание	zʌʒyɡˈɑnʲijɪ
        дверь	dvʲˈerʲ	dvʲˈerɪ^
        открыта	ʌtkrˈyta
        в	v	vˈɛ
        в салоне	f
        салоне	sʌɭˈonʲi
        доп	dˈop
        датчик	dˈɑttʃʲik
        есть	jestʲ	jˈestʲ
        сигнал	sʲiɡnˈɑɭ
        2	dvˈɑ
        3	trʲˈi
        """.trimIndent(),
    )

    @Test
    fun `words are joined within a clause and sentence ends split sentences`() {
        assertEquals(
            Phonemized.Sentences(listOf("fkɭʲu\"tʃʲˈitʲi zʌʒyɡˈɑnʲijɪ.", "dvʲˈerʲ ʌtkrˈyta!")),
            g2p.phonemize("Включите зажигание. Дверь открыта!"),
        )
    }

    @Test
    fun `clause end uses the final form and pause marks keep a space`() {
        assertEquals(
            Phonemized.Sentences(listOf("dvʲˈerɪ^; jestʲ sʲiɡnˈɑɭ")),
            g2p.phonemize("Дверь — есть сигнал"),
        )
        assertEquals(Phonemized.Sentences(listOf("dvʲˈerɪ^, ")), g2p.phonemize("дверь,"))
    }

    @Test
    fun `form before a specific word wins over the base form`() {
        assertEquals(Phonemized.Sentences(listOf("f sʌɭˈonʲi")), g2p.phonemize("в салоне"))
    }

    @Test
    fun `punctuation without a following space does not break a clause`() {
        assertEquals(Phonemized.Sentences(listOf("dvˈɑ trʲˈi")), g2p.phonemize("2–3"))
        assertEquals(Phonemized.Sentences(listOf("dˈop dˈɑttʃʲik")), g2p.phonemize("Доп. датчик"))
    }

    @Test
    fun `unknown word or sign sends the phrase to the system voice`() {
        assertEquals(Phonemized.Unknown("капот"), g2p.phonemize("Дверь, капот"))
        assertEquals(Phonemized.Unknown("%"), g2p.phonemize("2%"))
    }
}
