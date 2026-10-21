package ru.alarmtrade.specmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.di.commonModule
import ru.alarmtrade.specmp.core.lifecycle.appLifecycleModule
import ru.alarmtrade.specmp.core.localization.LocalAppLocale
import ru.alarmtrade.specmp.core.localization.applyAppLocale
import ru.alarmtrade.specmp.core.localization.prepareAppLocale
import ru.alarmtrade.specmp.core.network.di.networkModule
import ru.alarmtrade.specmp.core.permissions.BindEffect
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.core.permissions.permissionsModule
import ru.alarmtrade.specmp.core.specialist.SpecialistAppVersion
import ru.alarmtrade.specmp.core.specialist.SpecialistAppVersionProvider
import ru.alarmtrade.specmp.core.specialist.specialistModule
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.core.ui.view.image.ProvideCmpImageLoader
import ru.alarmtrade.specmp.core.ui.view.image.rememberCmpImageLoader
import ru.alarmtrade.specmp.di.rememberPlatformKoinModules
import ru.alarmtrade.specmp.feature.advancedmount.di.advancedMountModule
import ru.alarmtrade.specmp.feature.authstate.di.authStateModule
import ru.alarmtrade.specmp.feature.device.firmware.di.deviceFirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.ui.di.deviceFirmwareUiModule
import ru.alarmtrade.specmp.feature.device.interaction.di.deviceInteractionModule
import ru.alarmtrade.specmp.feature.device.interaction.ui.di.deviceInteractionUiModule
import ru.alarmtrade.specmp.feature.device.selection.di.deviceSelectionModule
import ru.alarmtrade.specmp.feature.device.selection.ui.di.deviceSelectionUiModule
import ru.alarmtrade.specmp.feature.login.di.loginModule
import ru.alarmtrade.specmp.feature.main.di.mainModule
import ru.alarmtrade.specmp.feature.supportchat.di.supportChatModule
import ru.alarmtrade.specmp.navigation.AppNavigation

// App — публичная точка входа платформенных обёрток и одновременно превью
@Suppress("ktlint:compose:preview-public-check")
@Preview
@Composable
fun App(
    appVersionName: String? = null,
    appVersionCode: Int? = null,
    onThemeChange: @Composable (isDark: Boolean) -> Unit = {}
) {
    var appLocale by remember { mutableStateOf(prepareAppLocale()) }

    val overrideAppVersionModule = remember(appVersionName, appVersionCode) {
        if (appVersionName != null && appVersionCode != null) {
            module {
                single<SpecialistAppVersionProvider> {
                    SpecialistAppVersionProvider {
                        SpecialistAppVersion(
                            name = appVersionName,
                            code = appVersionCode
                        )
                    }
                }
            }
        } else {
            null
        }
    }

    val platformModules = rememberPlatformKoinModules()

    KoinApplication(application = {
        modules(
            buildList {
                addAll(platformModules)
                add(commonModule)
                add(networkModule)
                add(specialistModule)
                overrideAppVersionModule?.let { add(it) }
                add(permissionsModule())
                add(appLifecycleModule)
                add(authStateModule)
                add(loginModule)
                add(mainModule)
                add(supportChatModule)
                add(deviceSelectionModule)
                add(deviceInteractionModule)
                add(deviceFirmwareModule)
                add(deviceFirmwareUiModule)
                add(deviceSelectionUiModule)
                add(deviceInteractionUiModule)
                add(advancedMountModule)
            }
        )
    }) {
        // Привязка PermissionsController к жизненному циклу корневой активити — один раз
        // на всё приложение (контроллер app-scoped, инжектится во ViewModel фич).
        BindEffect(koinInject<PermissionsProvider>().getController())

        CompositionLocalProvider(LocalAppLocale provides appLocale) {
            // Единый ImageLoader приложения: memory/disk-кэш Coil общий для
            // всех экранов (U-1).
            ProvideCmpImageLoader(rememberCmpImageLoader()) {
                AppTheme(onThemeChange) {
                    AppNavigation(
                        onLocaleChange = { locale ->
                            applyAppLocale(locale)
                            appLocale = locale
                        }
                    )
                }
            }
        }
    }
}
