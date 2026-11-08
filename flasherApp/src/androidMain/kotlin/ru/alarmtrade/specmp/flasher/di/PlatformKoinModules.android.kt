package ru.alarmtrade.specmp.flasher.di

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppRestarter
import ru.alarmtrade.specmp.core.network.monitor.AndroidNetworkMonitor
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor

@Composable
actual fun rememberPlatformKoinModules(): List<Module> {
    // Только applicationContext: синглтоны DI живут дольше Activity.
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        listOf(
            module {
                single<Context> { appContext }
                single<NetworkMonitor> { AndroidNetworkMonitor(appContext) }
                single<AppRestarter> { AppRestarter { appContext.restartApplication() } }
            },
        )
    }
}

/** Полный перезапуск процесса: новая задача с launch-Activity, текущий процесс завершается. */
private fun Context.restartApplication(): Boolean {
    val launch = packageManager.getLaunchIntentForPackage(packageName)?.component ?: return false
    startActivity(Intent.makeRestartActivityTask(launch))
    Runtime.getRuntime().exit(0)
    return true
}
