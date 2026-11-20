package ru.alarmtrade.specmp.core.speech.piper

/**
 * Предложение в фонемах (уже в NFD — так их хранит словарь) → ID модели, как `phonemes_to_ids` piper:
 * BOS `^`, pad `_` после каждой фонемы, EOS `$`; гласные кластеры (`aɪ`, `oʊ`…) — один токен.
 */
internal class PiperPhonemeIds(private val config: PiperVoiceConfig) {

    private val longestCluster = config.vowelClusters.maxOfOrNull { it.length } ?: 1

    fun of(phonemes: String): LongArray {
        val ids = ArrayList<Long>(phonemes.length * 2 + 3)
        ids += id(BOS)
        ids += id(PAD)
        var index = 0
        while (index < phonemes.length) {
            val size = (longestCluster downTo 2).firstOrNull { size ->
                index + size <= phonemes.length && phonemes.substring(index, index + size) in config.vowelClusters
            } ?: 1
            config.phonemeIds[phonemes.substring(index, index + size)]?.let { phoneme ->
                ids += phoneme.asList()
                ids += id(PAD)
            }
            index += size
        }
        ids += id(EOS)
        return ids.toLongArray()
    }

    private fun id(symbol: String): List<Long> = config.phonemeIds.getValue(symbol).asList()

    private companion object {
        const val BOS = "^"
        const val EOS = "$"
        const val PAD = "_"
    }
}
