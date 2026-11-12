package ru.alarmtrade.specmp.core.common.di

import kotlinx.datetime.Clock
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope
import ru.alarmtrade.specmp.core.common.layout.PaneWidthStore
import ru.alarmtrade.specmp.core.common.layout.SettingsPaneWidthStore
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.core.common.time.TimeZoneSource

val appScopeQualifier = named("appScope")

private const val PANE_WIDTHS = "panewidths"

val commonModule = module {
    single { AppDispatchers() }
    single<Clock> { Clock.System }
    single<TimeZoneSource> { TimeZoneSource.System }

    single { AppSettingsFactory(platformSettings) }
    single<PaneWidthStore> { SettingsPaneWidthStore(get<AppSettingsFactory>().create(PANE_WIDTHS)) }

    // Скоуп уровня приложения: для работы, которая должна завершиться даже после
    // смерти инициатора (например, закрытие чат-сессии из ViewModel.onCleared).
    single(appScopeQualifier) { serviceScope(get<AppDispatchers>().default, logTag = "AppScope") }
}
