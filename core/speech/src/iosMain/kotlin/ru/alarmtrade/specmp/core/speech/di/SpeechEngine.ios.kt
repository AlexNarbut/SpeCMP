package ru.alarmtrade.specmp.core.speech.di

import org.koin.core.scope.Scope
import ru.alarmtrade.specmp.core.speech.IosSpeechEngine
import ru.alarmtrade.specmp.core.speech.SpeechEngine

internal actual fun Scope.platformSpeechEngine(languageTag: String): SpeechEngine = IosSpeechEngine(languageTag)
