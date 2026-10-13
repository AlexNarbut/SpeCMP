package ru.alarmtrade.specmp.feature.device.interaction.ui.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host.DeviceInteractionViewModel

/** Koin-модуль обёртки flow работы с устройством. Сессия — из `device:interaction`. */
val deviceInteractionUiModule = module {
    viewModel { DeviceInteractionViewModel(get(), get(appScopeQualifier)) }
}
