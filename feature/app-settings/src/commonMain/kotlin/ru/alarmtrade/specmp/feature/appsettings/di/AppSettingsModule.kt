package ru.alarmtrade.specmp.feature.appsettings.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings.AppSettingsViewModel

/**
 * Koin-модуль настроек приложения. `AppLocaleStore` и `AppRestarter` — из composition root,
 * `ConfigurationLanguageStore` — из `core/specialist`.
 */
val appSettingsModule = module {
    viewModelOf(::AppSettingsViewModel)
}
