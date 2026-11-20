package ru.alarmtrade.specmp.core.speech.di

import org.koin.core.scope.Scope
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope
import ru.alarmtrade.specmp.core.speech.DesktopOs
import ru.alarmtrade.specmp.core.speech.DesktopSpeechEngine
import ru.alarmtrade.specmp.core.speech.SpeechEngine

internal actual fun Scope.platformSpeechEngine(languageTag: String): SpeechEngine {
    val io = get<AppDispatchers>().io
    return DesktopSpeechEngine(
        os = DesktopOs.of(System.getProperty("os.name").orEmpty()),
        languageTag = languageTag,
        io = io,
        scope = serviceScope(io, logTag = "SpeechEngine"),
    )
}
