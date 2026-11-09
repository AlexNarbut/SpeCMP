package ru.alarmtrade.specmp.flasher

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.common.di.commonModule
import ru.alarmtrade.specmp.core.lifecycle.di.appLifecycleModule
import ru.alarmtrade.specmp.core.localization.di.localizationModule
import ru.alarmtrade.specmp.core.localization.prepareAppLocale
import ru.alarmtrade.specmp.core.permissions.BindEffect
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.core.permissions.permissionsModule
import ru.alarmtrade.specmp.core.specialist.SpecialistInitializer
import ru.alarmtrade.specmp.core.specialist.di.specialistModules
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.core.ui.theme.DefaultAppThemeStore
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.feature.authstate.di.authStateModule
import ru.alarmtrade.specmp.feature.device.firmware.di.deviceFirmwareModule
import ru.alarmtrade.specmp.feature.device.interaction.di.deviceInteractionModule
import ru.alarmtrade.specmp.feature.device.interaction.ui.di.deviceInteractionUiModule
import ru.alarmtrade.specmp.feature.device.selection.di.deviceSelectionModule
import ru.alarmtrade.specmp.feature.login.di.loginModule
import ru.alarmtrade.specmp.flasher.di.rememberPlatformKoinModules
import ru.alarmtrade.specmp.flasher.navigation.FlasherNavigation

/**
 * Точка входа приложения прошивки (обёртки `flasherAndroidApp`, `flasherDesktopApp`): тот же старт, что у `App()`
 * основного приложения (язык, Specialist SDK, разрешения), но только фичи авторизации и прошивки.
 */
@Composable
fun FlasherApp(appVersionName: String, appVersionCode: Int, onThemeChange: @Composable (isDark: Boolean) -> Unit = {}) {
    val appLocale = remember { prepareAppLocale() }

    val platformModules = rememberPlatformKoinModules()

    KoinApplication(application = {
        modules(
            buildList {
                addAll(platformModules)
                add(commonModule)
                addAll(specialistModules(appVersionName, appVersionCode))
                add(localizationModule)
                add(permissionsModule())
                add(appLifecycleModule)
                add(authStateModule)
                add(loginModule)
                add(deviceSelectionModule)
                add(deviceInteractionModule)
                add(deviceInteractionUiModule)
                add(deviceFirmwareModule)
            },
        )
    }) {
        // Привязка PermissionsController к жизненному циклу корневой активити — один раз на приложение.
        BindEffect(koinInject<PermissionsProvider>().getController())

        // Начальная настройка SDK: app-scoped и идемпотентна; UI — после неё (см. App() основного приложения).
        val specialistInitializer = koinInject<SpecialistInitializer>()
        LaunchedEffect(Unit) { specialistInitializer.start(appLocale.languageTag) }
        val specialistReady by specialistInitializer.ready.collectAsStateWithLifecycle()

        AppTheme(onThemeChange, themeStore = DefaultAppThemeStore) {
            if (specialistReady) {
                FlasherNavigation()
            } else {
                CmpSurface(modifier = Modifier.fillMaxSize()) {}
            }
        }
    }
}
