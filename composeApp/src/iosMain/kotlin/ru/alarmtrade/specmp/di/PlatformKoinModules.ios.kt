package ru.alarmtrade.specmp.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.network.monitor.IosNetworkMonitor
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor

@Composable
actual fun rememberPlatformKoinModules(): List<Module> = remember {
    listOf(
        module {
            single<NetworkMonitor> { IosNetworkMonitor() }
        },
    )
}
