package ru.alarmtrade.specmp.core.speech.di

import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.speech.HandsFreeSettings
import ru.alarmtrade.specmp.core.speech.PersistentHandsFreeSettings
import ru.alarmtrade.specmp.core.speech.QueuedSpeaker
import ru.alarmtrade.specmp.core.speech.Speaker
import ru.alarmtrade.specmp.core.speech.SpeechEngine

private const val HANDS_FREE = "handsfree"

val speechModule: Module = module {
    single<HandsFreeSettings> { PersistentHandsFreeSettings(get<AppSettingsFactory>().create(HANDS_FREE)) }
    single<Speaker> {
        val engine = platformSpeechEngine(languageTag = get<AppLocaleStore>().current().languageTag)
        QueuedSpeaker(engine, serviceScope(get<AppDispatchers>().default, logTag = "Speaker"))
    }
}

internal expect fun Scope.platformSpeechEngine(languageTag: String): SpeechEngine
