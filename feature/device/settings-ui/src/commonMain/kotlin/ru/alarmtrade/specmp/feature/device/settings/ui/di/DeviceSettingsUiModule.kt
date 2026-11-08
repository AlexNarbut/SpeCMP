package ru.alarmtrade.specmp.feature.device.settings.ui.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsViewModel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SettingsEditorOptions

/** Koin-модуль экранов настроек: ViewModel сессии редактора. Репозиторий — из `feature/device/settings`. */
val deviceSettingsUiModule = module {
    viewModel { (source: SettingsSource, options: SettingsEditorOptions) ->
        DeviceSettingsViewModel(
            source = source,
            options = options,
            repository = get(),
            localConfigurations = get(),
            preferences = get(),
            carsRepository = get(),
            deviceSession = get(),
            appScope = get(appScopeQualifier),
            dispatchers = get(),
        )
    }
}
