package ru.alarmtrade.specmp.feature.appearance.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.AppearanceViewModel

/** `CustomThemeStore` и `AppDispatchers` — из composition root, порты файлов и тема при входе — параметрами. */
val appearanceModule = module {
    viewModel { params ->
        AppearanceViewModel(
            store = get(),
            filePicker = params.get(),
            fileSaver = params.get(),
            dispatchers = get(),
            isDark = params.get(),
        )
    }
}
