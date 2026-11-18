package ru.alarmtrade.specmp.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.appupdate.StoreAppUpdateInstaller
import ru.alarmtrade.specmp.core.common.AppStore
import ru.alarmtrade.specmp.core.network.monitor.AndroidNetworkMonitor
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor
import ru.alarmtrade.specmp.core.specialist.AndroidSilentLocationProvider
import ru.alarmtrade.specmp.core.specialist.SilentLocationProvider
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePlatform
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateInstaller

// Карточки в Google Play и AppGallery — старого приложения «Pandora Specialist»: обновления публикуются в них.
private const val STORE_APP_ID = "ru.alarmtrade.pandoraspecialist"
private const val APP_GALLERY_ID = "C103462811"

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
                        storeUrls = storeUrls(get()),
                    )
                }
            },
        )
    }
}

private fun storeUrls(store: AppStore): List<String> = when (store) {
    AppStore.GOOGLE_PLAY -> listOf(
        "market://details?id=$STORE_APP_ID",
        "https://play.google.com/store/apps/details?id=$STORE_APP_ID",
    )

    AppStore.APP_GALLERY -> listOf(
        "appmarket://details?id=$STORE_APP_ID",
        "https://appgallery.huawei.com/app/$APP_GALLERY_ID",
    )
}
