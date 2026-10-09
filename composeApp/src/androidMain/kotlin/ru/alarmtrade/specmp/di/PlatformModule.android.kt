package ru.alarmtrade.specmp.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.network.monitor.AndroidNetworkMonitor
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor

@Composable
actual fun rememberPlatformModule(): Module {
    // Только applicationContext: синглтоны DI живут дольше Activity.
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        module {
            single<NetworkMonitor> { AndroidNetworkMonitor(appContext) }
        }
    }
}
