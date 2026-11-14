package ru.alarmtrade.specmp.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.appupdate.StoreAppUpdateInstaller
import ru.alarmtrade.specmp.core.network.monitor.IosNetworkMonitor
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdatePlatform
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateInstaller

// Apple ID приложения в App Store (bundle ru.alarmtrade.specialist).
private const val APP_STORE_ID = "1563735964"

@Composable
actual fun rememberPlatformKoinModules(): List<Module> = remember {
    listOf(
        module {
            single<NetworkMonitor> { IosNetworkMonitor() }
            single<AppUpdateInstaller> {
                StoreAppUpdateInstaller(
                    platform = AppUpdatePlatform.IOS,
                    storeUrls = listOf(
                        "itms-apps://apps.apple.com/app/id$APP_STORE_ID",
                        "https://apps.apple.com/app/id$APP_STORE_ID",
                    ),
                )
            }
        },
    )
}
