package ru.alarmtrade.specmp.core.common.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.russhwolf.settings.Settings
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory

/** Qualifier для application-scoped [CoroutineScope]: переживает ViewModel и экраны. */
val appScopeQualifier = named("appScope")

val commonModule = module {
    single { AppDispatchers() }

    // Именованные key-value хранилища поверх одного платформенного Settings (no-arg).
    single { AppSettingsFactory(Settings()) }

    // Скоуп уровня приложения: для работы, которая должна завершиться даже после
    // смерти инициатора (например, закрытие чат-сессии из ViewModel.onCleared).
    single(appScopeQualifier) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}
