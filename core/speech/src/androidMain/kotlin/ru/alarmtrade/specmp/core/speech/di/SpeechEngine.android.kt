package ru.alarmtrade.specmp.core.speech.di

import android.content.Context
import org.koin.core.scope.Scope
import ru.alarmtrade.specmp.core.speech.AndroidSpeechEngine
import ru.alarmtrade.specmp.core.speech.SpeechAudioFocus
import ru.alarmtrade.specmp.core.speech.SpeechEngine
import ru.alarmtrade.specmp.core.speech.piper.AudioTrackPcmPlayer
import ru.alarmtrade.specmp.core.speech.piper.OrtPiperModel
import ru.alarmtrade.specmp.core.speech.speechAudioAttributes

internal actual fun Scope.platformSpeechEngine(languageTag: String): SpeechEngine {
    val context = get<Context>()
    val attributes = speechAudioAttributes()
    val audioFocus = SpeechAudioFocus(context, attributes)
    return withNeuralVoice(
        languageTag = languageTag,
        system = AndroidSpeechEngine(context, languageTag, attributes, audioFocus),
        player = { AudioTrackPcmPlayer(attributes, audioFocus) },
        loader = OrtPiperModel::loadAssets,
    )
}
