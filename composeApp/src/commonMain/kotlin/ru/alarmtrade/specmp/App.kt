package ru.alarmtrade.specmp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ru.alarmtrade.specmp.core.ui.view.image.ProvideCmpImageLoader
import ru.alarmtrade.specmp.core.ui.view.image.rememberCmpImageLoader
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.di.appKoinModules
import ru.alarmtrade.specmp.di.rememberPlatformKoinModules
import ru.alarmtrade.specmp.navigation.AppNavigation

/**
 * Точка входа платформенных обёрток. Версию передаёт платформа (Android — `PackageInfo`, iOS — Info.plist,
 * desktop — сгенерированный `version.properties`); примитивами — обёртки не зависят от `core/specialist`.
 */
@Composable
fun App(appVersionName: String, appVersionCode: Int, onThemeChange: @Composable (isDark: Boolean) -> Unit = {}) {
    // Язык применяется на старте процесса (docs/localization.md, «Язык приложения»); смена — через перезапуск.
    val appLocale = remember { prepareAppLocale() }

    val platformModules = rememberPlatformKoinModules()

    KoinApplication(application = {
        modules(
            platformModules + appKoinModules(appVersionName, appVersionCode),
        )
    }) {
        // Привязка PermissionsController к жизненному циклу корневой активити — один раз
        // на всё приложение (контроллер app-scoped, инжектится во ViewModel фич).
        BindEffect(koinInject<PermissionsProvider>().getController())

        // Process-wide сервисы (сессия устройства и т. п.): start() идемпотентен, работа — в их собственных скоупах.
        val appServicesStarter = koinInject<AppServicesStarter>()
        LaunchedEffect(Unit) { appServicesStarter.start() }

        // Начальная настройка SDK (клиент, язык, язык конфигураций): app-scoped, идемпотентна; UI — после неё,
        // чтобы первые запросы шли с правильным языком. Эффект только запускает её (работает в своём скоупе).
        val specialistInitializer = koinInject<SpecialistInitializer>()
        LaunchedEffect(Unit) { specialistInitializer.start(appLocale.languageTag) }
        val specialistReady by specialistInitializer.ready.collectAsStateWithLifecycle()

        // Единый ImageLoader приложения: memory/disk-кэш Coil общий для всех экранов (U-1).
        CompositionLocalProvider(
            LocalClock provides koinInject<Clock>(),
            LocalTimeZoneSource provides koinInject<TimeZoneSource>(),
        ) {
            ProvideCmpImageLoader(rememberCmpImageLoader()) {
                AppTheme(onThemeChange, themeStore = DefaultAppThemeStore) {
                    if (specialistReady) {
                        AppNavigation()
                    } else {
                        CmpSurface(modifier = Modifier.fillMaxSize()) {}
                    }
                }
            }
        }
    }
}
