package ru.alarmtrade.specmp.core.network.monitor

import kotlinx.coroutines.flow.StateFlow

/** Экземпляр — синглтон процесса из корня приложения; на JVM реализации нет. */
interface NetworkMonitor {
    val isAvailable: StateFlow<Boolean>
}
