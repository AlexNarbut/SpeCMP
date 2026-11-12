package ru.alarmtrade.specmp.feature.device.peripherals.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.peripherals.data.datasource.PeripheralsDataSource
import ru.alarmtrade.specmp.feature.device.peripherals.data.datasource.SpecialistPeripheralsDataSource
import ru.alarmtrade.specmp.feature.device.peripherals.data.repository.SpecialistDynamicPeripheralsRepository
import ru.alarmtrade.specmp.feature.device.peripherals.data.repository.SpecialistSystemPeripheralsRepository
import ru.alarmtrade.specmp.feature.device.peripherals.domain.repository.DynamicPeripheralsRepository
import ru.alarmtrade.specmp.feature.device.peripherals.domain.repository.SystemPeripheralsRepository
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing.PairingViewModel
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.SystemDevicesViewModel

val devicePeripheralsModule = module {
    // Ленивый провайдер: недоступный на платформе интерактор не должен ронять создание источника.
    single<PeripheralsDataSource> { SpecialistPeripheralsDataSource(alarmDevice = { get() }) }
    single<SystemPeripheralsRepository> { SpecialistSystemPeripheralsRepository(get()) }
    singleOf(::SpecialistDynamicPeripheralsRepository) bind DynamicPeripheralsRepository::class
    viewModelOf(::SystemDevicesViewModel)
    viewModelOf(::PairingViewModel)
}
