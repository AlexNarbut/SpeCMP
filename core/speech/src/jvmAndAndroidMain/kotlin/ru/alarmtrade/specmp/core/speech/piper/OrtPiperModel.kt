package ru.alarmtrade.specmp.core.speech.piper

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels

internal class OrtPiperModel(private val environment: OrtEnvironment, private val session: OrtSession) : PiperModel {

    override fun synthesize(ids: LongArray, scales: FloatArray): FloatArray =
        OnnxTensor.createTensor(environment, arrayOf(ids)).use { input ->
            OnnxTensor.createTensor(environment, longArrayOf(ids.size.toLong())).use { lengths ->
                OnnxTensor.createTensor(environment, scales).use { scaleTensor ->
                    val inputs = mapOf(INPUT to input, INPUT_LENGTHS to lengths, SCALES to scaleTensor)
                    session.run(inputs).use { result ->
                        val audio = (result[0] as OnnxTensor).floatBuffer
                        FloatArray(audio.remaining()).also(audio::get)
                    }
                }
            }
        }

    companion object {
        private const val INPUT = "input"
        private const val INPUT_LENGTHS = "input_lengths"
        private const val SCALES = "scales"
        private const val RESOURCE_DIR = "/piper/"
        private const val VOICE = "ru_RU-dmitri-medium"
        private const val LEXICON = "ru_lexicon.tsv"
        private const val MODEL_BUFFER_BYTES = 64 shl 20

        /** Модель, её `.onnx.json` и словарь — ресурсы модуля (модель кладёт Gradle-задача `downloadPiperVoice`). */
        fun loadAssets(): PiperAssets {
            val environment = OrtEnvironment.getEnvironment()
            val modelBuffer = resource("$VOICE.onnx").use { it.readDirect() }
            val session = OrtSession.SessionOptions().use { environment.createSession(modelBuffer, it) }
            return PiperAssets(
                config = resource("$VOICE.onnx.json").use { it.readBytes().decodeToString() },
                lexicon = resource(LEXICON).use { it.readBytes().decodeToString() },
                model = OrtPiperModel(environment, session),
            )
        }

        private fun resource(name: String): InputStream =
            OrtPiperModel::class.java.getResourceAsStream(RESOURCE_DIR + name)
                ?: throw IOException("Piper resource $name is missing")

        // Модель — в нативную память (direct buffer), не в кучу: размер ресурса на Android заранее неизвестен.
        private fun InputStream.readDirect(): ByteBuffer {
            var buffer = ByteBuffer.allocateDirect(MODEL_BUFFER_BYTES)
            val channel = Channels.newChannel(this)
            while (channel.read(buffer) >= 0) {
                if (!buffer.hasRemaining()) {
                    val grown = ByteBuffer.allocateDirect(buffer.capacity() * 2)
                    buffer.flip()
                    grown.put(buffer)
                    buffer = grown
                }
            }
            buffer.flip()
            return buffer
        }
    }
}
