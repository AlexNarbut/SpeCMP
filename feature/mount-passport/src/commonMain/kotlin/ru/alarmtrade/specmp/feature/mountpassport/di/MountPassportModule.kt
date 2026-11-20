package ru.alarmtrade.specmp.feature.mountpassport.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.MountPassportDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.SpecialistMountPassportDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.repository.SpecialistMountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorViewModel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListViewModel

/** `PassportConfigurationsRepository` («Мои конфигурации») регистрирует корень приложения. */
val mountPassportModule = module {
    singleOf(::SpecialistMountPassportDataSource) bind MountPassportDataSource::class
    singleOf(::SpecialistMountPassportRepository) bind MountPassportRepository::class
    viewModel { params ->
        PassportListViewModel(mode = params.get(), deviceSession = get(), repository = get(), dispatchers = get())
    }
    viewModel { params ->
        PassportEditorViewModel(
            args = params.get(),
            deviceSession = get(),
            repository = get(),
            configurations = get(),
            permissions = get(),
            imagePicker = params.get(),
            settingsFileSaver = params.get(),
            dispatchers = get(),
        )
    }
}
