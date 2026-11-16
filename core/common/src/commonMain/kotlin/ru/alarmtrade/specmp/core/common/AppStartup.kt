package ru.alarmtrade.specmp.core.common

import kotlinx.coroutines.flow.StateFlow

/**
 * Начальная настройка приложения до показа UI (реализация — `SpecialistInitializer`): пока [ready] = `false`, корень
 * не рисует экраны. [start] идемпотентен.
 */
interface AppStartup {
    val ready: StateFlow<Boolean>

    fun start(languageTag: String)
}
