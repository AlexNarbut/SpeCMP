package ru.alarmtrade.specmp.flasher

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Clock
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.common.time.TimeZoneSource
import ru.alarmtrade.specmp.core.localization.prepareAppLocale
import ru.alarmtrade.specmp.core.permissions.BindEffect
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.core.ui.startup.AppStartViewModel
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
    // Язык процесса — до первой композиции; SDK получает его в AppStartViewModel.
    remember { prepareAppLocale() }

    val platformModules = rememberPlatformKoinModules()

    KoinApplication(application = {
        modules(
            platformModules + flasherKoinModules(appVersionName, appVersionCode),
        )
    }) {
        // Привязка PermissionsController к жизненному циклу корневой активити — один раз на приложение.
        // moko-permissions: контроллер (app-scoped, его берут VM фич) привязывается к жизненному циклу
        // корневой активити только из composable — BindEffect, один раз на приложение.
        @Suppress("ServiceInComposable")
        val permissions = koinInject<PermissionsProvider>()
        BindEffect(permissions.getController())

        // Process-wide сервисы и настройка SDK (язык, клиент) — во VM; UI — после неё, чтобы первые запросы шли с

        // правильным языком и клиентом.

        val appStart = koinViewModel<AppStartViewModel>()

        val appStartState by appStart.state.collectAsStateWithLifecycle()

        // Значение для форматирования дат в UI, не сервис с работой.

        @Suppress("ServiceInComposable")
        val timeZoneSource = koinInject<TimeZoneSource>()

        CompositionLocalProvider(
            LocalClock provides koinInject<Clock>(),
            LocalTimeZoneSource provides timeZoneSource,
        ) {
            AppTheme(onThemeChange, themeStore = DefaultAppThemeStore) {
                if (appStartState.isReady) {
                    CmpBox(modifier = Modifier.fillMaxSize().clearFocusOnTapOutside()) { FlasherNavigation() }
                } else {
                    CmpSurface(modifier = Modifier.fillMaxSize()) {}
                }
            }
        }
    }
}
