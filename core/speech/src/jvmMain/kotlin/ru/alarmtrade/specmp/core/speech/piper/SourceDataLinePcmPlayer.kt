package ru.alarmtrade.specmp.core.speech.piper

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

internal class SourceDataLinePcmPlayer(private val io: CoroutineDispatcher) : PcmPlayer {

    override suspend fun play(samples: ShortArray, sampleRate: Int) {
        if (samples.isEmpty()) return
        val format = AudioFormat(sampleRate.toFloat(), Short.SIZE_BITS, 1, true, false)
        val bytes = ByteArray(samples.size * Short.SIZE_BYTES)
        samples.forEachIndexed { index, sample ->
            bytes[index * 2] = sample.toByte()
            bytes[index * 2 + 1] = (sample.toInt() shr Byte.SIZE_BITS).toByte()
        }
        withContext(io) {
            val line = AudioSystem.getSourceDataLine(format)
            try {
                line.open(format)
                line.start()
                val chunk = line.bufferSize / 2 / format.frameSize * format.frameSize
                var offset = 0
                while (offset < bytes.size) {
                    ensureActive()
                    offset += line.write(bytes, offset, minOf(chunk, bytes.size - offset))
                }
                awaitPlayed(samples.size) { line.longFramePosition.toInt() }
            } finally {
                line.stop()
                line.flush()
                line.close()
            }
        }
    }

    override fun release() = Unit
}
