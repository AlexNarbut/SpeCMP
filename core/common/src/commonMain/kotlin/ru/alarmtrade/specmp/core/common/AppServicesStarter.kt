package ru.alarmtrade.specmp.core.common

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Запуск process-wide сервисов (`start()` доменных сервисов со своим скоупом) — один раз из корня композиции
 * приложения. Сервис — слот-лямбда с резолвом внутри: сбой одного (в том числе DI) не мешает запуску остальных.
 */
class AppServicesStarter(private val services: List<Pair<String, () -> Unit>>) {
    private val started = MutableStateFlow(false)

    fun start() {
        if (!started.compareAndSet(expect = false, update = true)) return
        services.forEach { (name, startService) ->
            safeRun { startService() }.onError { Logger.e(it) { "App service $name failed to start" } }
        }
    }
}
