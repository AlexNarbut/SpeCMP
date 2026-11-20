package ru.alarmtrade.specmp.feature.device.state.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.state.data.datasource.DeviceStateDataSource
import ru.alarmtrade.specmp.feature.device.state.data.datasource.SpecialistDeviceStateDataSource
import ru.alarmtrade.specmp.feature.device.state.data.repository.SpecialistDeviceStateRepository
import ru.alarmtrade.specmp.feature.device.state.domain.repository.DeviceStateRepository
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CheckStateViewModel

val deviceStateModule = module {
    // Ленивый провайдер: недоступный на платформе интерактор не должен ронять создание источника.
    single<DeviceStateDataSource> { SpecialistDeviceStateDataSource(alarmDevice = { get() }) }
    singleOf(::SpecialistDeviceStateRepository) bind DeviceStateRepository::class
    viewModel { (fastMount: Boolean) ->
        CheckStateViewModel(get(), get(), get(), get(), get(), get(), get(), get(), fastMount)
    }
}
