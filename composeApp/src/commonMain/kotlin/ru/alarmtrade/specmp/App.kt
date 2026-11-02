package ru.alarmtrade.specmp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.di.commonModule
import ru.alarmtrade.specmp.core.lifecycle.appLifecycleModule
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.localization.DefaultAppLocaleStore
import ru.alarmtrade.specmp.core.localization.prepareAppLocale
import ru.alarmtrade.specmp.core.permissions.BindEffect
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.core.permissions.permissionsModule
import ru.alarmtrade.specmp.core.specialist.SpecialistAppVersion
import ru.alarmtrade.specmp.core.specialist.SpecialistAppVersionProvider
import ru.alarmtrade.specmp.core.specialist.SpecialistInitializer
import ru.alarmtrade.specmp.core.specialist.specialistModule
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.core.ui.view.image.ProvideCmpImageLoader
import ru.alarmtrade.specmp.core.ui.view.image.rememberCmpImageLoader
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.di.rememberPlatformKoinModules
import ru.alarmtrade.specmp.feature.account.di.accountModule
import ru.alarmtrade.specmp.feature.advancedmount.di.advancedMountModule
import ru.alarmtrade.specmp.feature.appsettings.di.appSettingsModule
import ru.alarmtrade.specmp.feature.authstate.di.authStateModule
import ru.alarmtrade.specmp.feature.configurator.di.configuratorModule
import ru.alarmtrade.specmp.feature.configurator.ui.di.configuratorUiModule
import ru.alarmtrade.specmp.feature.device.autostart.di.deviceAutostartModule
import ru.alarmtrade.specmp.feature.device.firmware.di.deviceFirmwareModule
import ru.alarmtrade.specmp.feature.device.interaction.di.deviceInteractionModule
import ru.alarmtrade.specmp.feature.device.interaction.ui.di.deviceInteractionUiModule
import ru.alarmtrade.specmp.feature.device.selection.di.deviceSelectionModule
import ru.alarmtrade.specmp.feature.device.settings.di.deviceSettingsModule
import ru.alarmtrade.specmp.feature.device.settings.ui.di.deviceSettingsUiModule
import ru.alarmtrade.specmp.feature.login.di.loginModule
import ru.alarmtrade.specmp.feature.main.di.mainModule
import ru.alarmtrade.specmp.feature.supportchat.di.supportChatModule
import ru.alarmtrade.specmp.feature.vehicle.di.vehicleModule
import ru.alarmtrade.specmp.feature.vehicle.ui.di.vehicleUiModule
import ru.alarmtrade.specmp.navigation.AppNavigation

/**
 * Точка входа платформенных обёрток. Версию передаёт платформа (Android — `PackageInfo`, iOS — Info.plist,
 * desktop — сгенерированный `version.properties`); примитивами — обёртки не зависят от `core/specialist`.
 */
@Composable
fun App(appVersionName: String, appVersionCode: Int, onThemeChange: @Composable (isDark: Boolean) -> Unit = {}) {
    // Язык применяется на старте процесса (docs/localization.md, «Язык приложения»); смена — через перезапуск.
    val appLocale = remember { prepareAppLocale() }

    val appVersionModule = remember(appVersionName, appVersionCode) {
        val version = SpecialistAppVersion(name = appVersionName, code = appVersionCode)
        module { single<SpecialistAppVersionProvider> { SpecialistAppVersionProvider { version } } }
    }

    val platformModules = rememberPlatformKoinModules()

    KoinApplication(application = {
        modules(
            buildList {
                addAll(platformModules)
                add(commonModule)
                add(specialistModule)
                add(appVersionModule)
                add(module { single<AppLocaleStore> { DefaultAppLocaleStore } })
                add(permissionsModule())
                add(appLifecycleModule)
                add(authStateModule)
                add(loginModule)
                add(mainModule)
                add(supportChatModule)
                add(deviceSelectionModule)
                add(deviceInteractionModule)
                add(deviceFirmwareModule)
                add(deviceAutostartModule)
                add(deviceSettingsModule)
                add(vehicleModule)
                add(accountModule)
                add(vehicleUiModule)
                add(configuratorModule)
                add(configuratorUiModule)
                add(deviceSettingsUiModule)
                add(deviceInteractionUiModule)
                add(advancedMountModule)
                add(appSettingsModule)
            }
        )
    }) {
        // Привязка PermissionsController к жизненному циклу корневой активити — один раз
        // на всё приложение (контроллер app-scoped, инжектится во ViewModel фич).
        BindEffect(koinInject<PermissionsProvider>().getController())

        // Начальная настройка SDK (клиент, язык, язык конфигураций): app-scoped, идемпотентна; UI — после неё,
        // чтобы первые запросы шли с правильным языком. Эффект только запускает её (работает в своём скоупе).
        val specialistInitializer = koinInject<SpecialistInitializer>()
        LaunchedEffect(Unit) { specialistInitializer.start(appLocale.languageTag) }
        val specialistReady by specialistInitializer.ready.collectAsStateWithLifecycle()

        // Единый ImageLoader приложения: memory/disk-кэш Coil общий для всех экранов (U-1).
        ProvideCmpImageLoader(rememberCmpImageLoader()) {
            AppTheme(onThemeChange) {
                if (specialistReady) {
                    AppNavigation()
                } else {
                    CmpSurface(modifier = Modifier.fillMaxSize()) {}
                }
            }
        }
    }
}
