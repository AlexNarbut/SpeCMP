package ru.alarmtrade.specmp.core.common.di

import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.core.common.time.TimeZoneSource

/** Qualifier для application-scoped [CoroutineScope]: переживает ViewModel и экраны. */
val appScopeQualifier = named("appScope")

val commonModule = module {
    single { AppDispatchers() }
    single<Clock> { Clock.System }
    single<TimeZoneSource> { TimeZoneSource.System }

    // Именованные key-value хранилища поверх одного платформенного Settings (no-arg).
    single { AppSettingsFactory(Settings()) }

    // Скоуп уровня приложения: для работы, которая должна завершиться даже после
    // смерти инициатора (например, закрытие чат-сессии из ViewModel.onCleared).
    single(appScopeQualifier) { CoroutineScope(SupervisorJob() + get<AppDispatchers>().default) }
}
