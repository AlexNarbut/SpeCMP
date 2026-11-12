package ru.alarmtrade.specmp.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope
import ru.alarmtrade.specmp.core.network.monitor.JvmNetworkMonitor
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor

// Перезапуск JVM-приложения изнутри не делаем — пользователь перезапускает сам.
@Composable
actual fun rememberPlatformKoinModules(): List<Module> = remember {
    listOf(
        module {
            single<NetworkMonitor> {
                JvmNetworkMonitor(serviceScope(get<AppDispatchers>().io, logTag = "JvmNetworkMonitor"))
            }
        },
    )
}
