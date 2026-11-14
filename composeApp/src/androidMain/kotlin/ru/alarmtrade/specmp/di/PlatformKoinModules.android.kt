package ru.alarmtrade.specmp.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.appupdate.StoreAppUpdateInstaller
import ru.alarmtrade.specmp.core.network.monitor.AndroidNetworkMonitor
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor
import ru.alarmtrade.specmp.core.specialist.AndroidSilentLocationProvider
import ru.alarmtrade.specmp.core.specialist.SilentLocationProvider
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePlatform
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateInstaller

// Карточка в Google Play — старого приложения «Pandora Specialist»: обновления публикуются в неё.
private const val PLAY_APP_ID = "ru.alarmtrade.pandoraspecialist"

@Composable
actual fun rememberPlatformKoinModules(): List<Module> {
    // Только applicationContext: синглтоны DI живут дольше Activity.
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        listOf(
            module {
                single<Context> { appContext }
                single<NetworkMonitor> { AndroidNetworkMonitor(appContext) }
                single<SilentLocationProvider> { AndroidSilentLocationProvider(appContext) }
                single<AppUpdateInstaller> {
                    StoreAppUpdateInstaller(
                        platform = AppUpdatePlatform.ANDROID,
                        storeUrls = listOf(
                            "market://details?id=$PLAY_APP_ID",
                            "https://play.google.com/store/apps/details?id=$PLAY_APP_ID",
                        ),
                    )
                }
            },
        )
    }
}
