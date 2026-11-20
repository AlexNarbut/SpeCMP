package ru.alarmtrade.specmp.core.speech.di

import org.koin.core.scope.Scope
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope
import ru.alarmtrade.specmp.core.speech.DesktopOs
import ru.alarmtrade.specmp.core.speech.DesktopSpeechEngine
import ru.alarmtrade.specmp.core.speech.SpeechEngine
import ru.alarmtrade.specmp.core.speech.piper.OrtPiperModel
import ru.alarmtrade.specmp.core.speech.piper.SourceDataLinePcmPlayer

internal actual fun Scope.platformSpeechEngine(languageTag: String): SpeechEngine {
    val io = get<AppDispatchers>().io
    val os = DesktopOs.of(System.getProperty("os.name").orEmpty())
    val system = DesktopSpeechEngine(
        os = os,
        languageTag = languageTag,
        io = io,
        scope = serviceScope(io, logTag = "SpeechEngine"),
    )
    if (os == DesktopOs.OTHER) return system
    return withNeuralVoice(
        languageTag = languageTag,
        system = system,
        player = { SourceDataLinePcmPlayer(io) },
        loader = OrtPiperModel::loadAssets,
    )
}
