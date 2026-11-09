package ru.alarmtrade.specmp.flasher

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Clock
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.common.AppServicesStarter
import ru.alarmtrade.specmp.core.common.time.TimeZoneSource
import ru.alarmtrade.specmp.core.localization.prepareAppLocale
import ru.alarmtrade.specmp.core.permissions.BindEffect
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.core.specialist.SpecialistInitializer
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.core.ui.theme.DefaultAppThemeStore
import ru.alarmtrade.specmp.core.ui.time.LocalClock
import ru.alarmtrade.specmp.core.ui.time.LocalTimeZoneSource
import ru.alarmtrade.specmp.core.ui.view.input.clearFocusOnTapOutside
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.flasher.di.flasherKoinModules
import ru.alarmtrade.specmp.flasher.di.rememberPlatformKoinModules
import ru.alarmtrade.specmp.flasher.navigation.FlasherNavigation

@Composable
fun FlasherApp(appVersionName: String, appVersionCode: Int, onThemeChange: @Composable (isDark: Boolean) -> Unit = {}) {
    val appLocale = remember { prepareAppLocale() }

    val platformModules = rememberPlatformKoinModules()

    KoinApplication(application = {
        modules(
            platformModules + flasherKoinModules(appVersionName, appVersionCode),
        )
    }) {
        // Привязка PermissionsController к жизненному циклу корневой активити — один раз на приложение.
        BindEffect(koinInject<PermissionsProvider>().getController())

        // Process-wide сервисы (сессия устройства и т. п.): start() идемпотентен, работа — в их собственных скоупах.
        val appServicesStarter = koinInject<AppServicesStarter>()
        LaunchedEffect(Unit) { appServicesStarter.start() }

        // UI — после начальной настройки SDK (см. App() основного приложения).
        val specialistInitializer = koinInject<SpecialistInitializer>()
        LaunchedEffect(Unit) { specialistInitializer.start(appLocale.languageTag) }
        val specialistReady by specialistInitializer.ready.collectAsStateWithLifecycle()

        CompositionLocalProvider(
            LocalClock provides koinInject<Clock>(),
            LocalTimeZoneSource provides koinInject<TimeZoneSource>(),
        ) {
            AppTheme(onThemeChange, themeStore = DefaultAppThemeStore) {
                if (specialistReady) {
                    CmpBox(modifier = Modifier.fillMaxSize().clearFocusOnTapOutside()) { FlasherNavigation() }
                } else {
                    CmpSurface(modifier = Modifier.fillMaxSize()) {}
                }
            }
        }
    }
}
