package ru.alarmtrade.specmp.feature.device.autostart.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.autostart.data.datasource.AutostartDataSource
import ru.alarmtrade.specmp.feature.device.autostart.data.datasource.SpecialistAutostartDataSource
import ru.alarmtrade.specmp.feature.device.autostart.data.repository.SpecialistAutostartStatusRepository
import ru.alarmtrade.specmp.feature.device.autostart.data.repository.SpecialistBypassLearningRepository
import ru.alarmtrade.specmp.feature.device.autostart.data.repository.SpecialistCloneRepository
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.AutostartStatusRepository
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.BypassLearningRepository
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.CloneRepository
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassLearningViewModel
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone.CloneViewModel

val deviceAutostartModule = module {
    // Ленивый провайдер: недоступный на платформе интерактор не должен ронять создание источника.
    single<AutostartDataSource> { SpecialistAutostartDataSource(alarmDevice = { get() }) }
    singleOf(::SpecialistBypassLearningRepository) bind BypassLearningRepository::class
    singleOf(::SpecialistCloneRepository) bind CloneRepository::class
    singleOf(::SpecialistAutostartStatusRepository) bind AutostartStatusRepository::class

    viewModelOf(::BypassLearningViewModel)
    viewModel { (fromBypass: Boolean) -> CloneViewModel(get(), get(), get(), get(), get(), get(), fromBypass) }
}
