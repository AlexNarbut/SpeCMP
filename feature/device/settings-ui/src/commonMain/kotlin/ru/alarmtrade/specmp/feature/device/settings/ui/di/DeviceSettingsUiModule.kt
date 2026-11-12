package ru.alarmtrade.specmp.feature.device.settings.ui.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsViewModel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SettingsEditorOptions

val deviceSettingsUiModule = module {
    viewModel { (source: SettingsSource, options: SettingsEditorOptions) ->
        DeviceSettingsViewModel(
            source = source,
            options = options,
            repository = get(),
            sessions = get(),
            localConfigurations = get(),
            paneWidths = get(),
            carsRepository = get(),
            deviceSession = get(),
            dispatchers = get(),
        )
    }
}
