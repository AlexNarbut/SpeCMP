package ru.alarmtrade.specmp.core.network.monitor

import kotlinx.coroutines.flow.StateFlow

/**
 * Доступность сети для процесса: `true` — есть сеть по умолчанию и ОС её не блокирует.
 *
 * Реализации платформенные и push-based ([AndroidNetworkMonitor], [IosNetworkMonitor]); экземпляр —
 * синглтон процесса, его кладёт в Koin корень приложения (`composeApp`). На JVM реализации нет.
 */
interface NetworkMonitor {
    val isAvailable: StateFlow<Boolean>
}
