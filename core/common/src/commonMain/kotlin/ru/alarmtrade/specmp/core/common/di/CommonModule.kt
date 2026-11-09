package ru.alarmtrade.specmp.core.common.di

import kotlinx.datetime.Clock
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.core.common.time.TimeZoneSource

val appScopeQualifier = named("appScope")

val commonModule = module {
    single { AppDispatchers() }
    single<Clock> { Clock.System }
    single<TimeZoneSource> { TimeZoneSource.System }

    single { AppSettingsFactory(platformSettings) }

    // Скоуп уровня приложения: для работы, которая должна завершиться даже после
    // смерти инициатора (например, закрытие чат-сессии из ViewModel.onCleared).
    single(appScopeQualifier) { serviceScope(get<AppDispatchers>().default, logTag = "AppScope") }
}
