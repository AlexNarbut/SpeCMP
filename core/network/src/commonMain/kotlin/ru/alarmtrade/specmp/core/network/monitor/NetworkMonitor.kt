package ru.alarmtrade.specmp.core.network.monitor

import kotlinx.coroutines.flow.StateFlow

/** Экземпляр — синглтон процесса из корня приложения. */
interface NetworkMonitor {
    val isAvailable: StateFlow<Boolean>
}
