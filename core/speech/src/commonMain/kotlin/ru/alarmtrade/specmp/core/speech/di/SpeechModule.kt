package ru.alarmtrade.specmp.core.speech.di

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.core.lifecycle.AppLifecycle
import ru.alarmtrade.specmp.core.lifecycle.AppLifecycleEvent
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.speech.HandsFreeSettings
import ru.alarmtrade.specmp.core.speech.PersistentHandsFreeSettings
import ru.alarmtrade.specmp.core.speech.QueuedSpeaker
import ru.alarmtrade.specmp.core.speech.RoutingSpeechEngine
import ru.alarmtrade.specmp.core.speech.Speaker
import ru.alarmtrade.specmp.core.speech.SpeechEngine
import ru.alarmtrade.specmp.core.speech.piper.PcmPlayer
import ru.alarmtrade.specmp.core.speech.piper.PiperAssets
import ru.alarmtrade.specmp.core.speech.piper.PiperSpeech

private const val HANDS_FREE = "handsfree"

val speechModule: Module = module {
    single<HandsFreeSettings> { PersistentHandsFreeSettings(get<AppSettingsFactory>().create(HANDS_FREE)) }
    single<Speaker> {
        val engine = platformSpeechEngine(languageTag = get<AppLocaleStore>().current().languageTag)
        val backgrounds = get<AppLifecycle>().events.filter { event -> event == AppLifecycleEvent.BACKGROUND }.map { }
        QueuedSpeaker(
            engine,
            serviceScope(get<AppDispatchers>().default, logTag = "Speaker"),
            stopRequests = backgrounds,
        )
    }
}

internal expect fun Scope.platformSpeechEngine(languageTag: String): SpeechEngine

/** Русский — нейросетевой голос Piper с системным как запасным; остальные языки — системный голос. */
internal fun Scope.withNeuralVoice(
    languageTag: String,
    system: SpeechEngine,
    player: () -> PcmPlayer,
    loader: () -> PiperAssets,
): SpeechEngine {
    if (languageTag != AppLocale.Russian.languageTag) return system
    val dispatchers = get<AppDispatchers>()
    val scope = serviceScope(dispatchers.io, logTag = "PiperSpeech")
    val piper = PiperSpeech(loader, player(), dispatchers.io, dispatchers.default, scope)
    return RoutingSpeechEngine(piper, system, scope)
}
