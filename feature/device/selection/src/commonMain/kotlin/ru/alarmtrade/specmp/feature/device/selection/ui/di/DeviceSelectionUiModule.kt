package ru.alarmtrade.specmp.feature.device.selection.ui.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan.ScanDeviceViewModel

/**
 * Koin-модуль UI-фичи поиска устройства. Регистрирует только ViewModel;
 * репозитории приходят из логических модулей `device:selection` и `device:interaction`.
 */
val deviceSelectionUiModule = module {
    viewModelOf(::ScanDeviceViewModel)
}
