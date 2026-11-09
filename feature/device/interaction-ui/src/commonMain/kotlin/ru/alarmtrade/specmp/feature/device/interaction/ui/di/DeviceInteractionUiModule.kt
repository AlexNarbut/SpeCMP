package ru.alarmtrade.specmp.feature.device.interaction.ui.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host.DeviceInteractionViewModel

val deviceInteractionUiModule = module {
    viewModel { DeviceInteractionViewModel(get()) }
}
