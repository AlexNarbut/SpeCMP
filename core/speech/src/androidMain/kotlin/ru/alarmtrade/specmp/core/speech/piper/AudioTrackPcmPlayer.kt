package ru.alarmtrade.specmp.core.speech.piper

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import ru.alarmtrade.specmp.core.speech.SpeechAudioFocus

internal class AudioTrackPcmPlayer(private val attributes: AudioAttributes, private val audioFocus: SpeechAudioFocus) :
    PcmPlayer {

    override suspend fun play(samples: ShortArray, sampleRate: Int) {
        if (samples.isEmpty()) return
        audioFocus.request()
        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val track = AudioTrack(
            attributes,
            format,
            samples.size * Short.SIZE_BYTES,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )
        try {
            track.write(samples, 0, samples.size)
            track.play()
            awaitPlayed(samples.size) { track.playbackHeadPosition }
        } finally {
            track.pause()
            track.flush()
            track.release()
        }
    }

    override fun release() = audioFocus.abandon()
}
