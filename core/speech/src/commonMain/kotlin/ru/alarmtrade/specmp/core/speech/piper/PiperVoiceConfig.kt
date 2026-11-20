package ru.alarmtrade.specmp.core.speech.piper

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/** `.onnx.json` голоса Piper: частота, параметры вывода и таблица «фонема → ID». */
internal class PiperVoiceConfig(
    val sampleRate: Int,
    val scales: FloatArray,
    val phonemeIds: Map<String, LongArray>,
    val vowelClusters: Set<String>,
) {
    companion object {
        fun parse(json: String): PiperVoiceConfig {
            val root = Json.parseToJsonElement(json).jsonObject
            val inference = root.getValue("inference").jsonObject
            return PiperVoiceConfig(
                sampleRate = root.getValue("audio").jsonObject.getValue("sample_rate").jsonPrimitive.int,
                scales = floatArrayOf(
                    inference.float("noise_scale"),
                    inference.float("length_scale"),
                    inference.float("noise_w"),
                ),
                phonemeIds = root.getValue("phoneme_id_map").jsonObject.mapValues { (_, ids) ->
                    ids.jsonArray.map { it.jsonPrimitive.long }.toLongArray()
                },
                vowelClusters = (root["vowel_clusters"] as? JsonArray).orEmpty()
                    .map { cluster -> cluster.jsonArray.joinToString("") { it.jsonPrimitive.content } }
                    .toSet(),
            )
        }

        private fun JsonObject.float(key: String): Float = getValue(key).jsonPrimitive.float
    }
}
