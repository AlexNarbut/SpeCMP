package ru.alarmtrade.specmp.feature.mountpassport.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.MountPassportDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.PassportDocumentDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.PassportReadingDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.SpecialistMountPassportDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.SpecialistPassportDocumentDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.SpecialistPassportReadingDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.repository.SpecialistMountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.data.repository.SpecialistPassportDocumentRepository
import ru.alarmtrade.specmp.feature.mountpassport.data.repository.SpecialistPassportReadingRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportDocumentRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportReadingRepository
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportDocumentLocalizer
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.ResourcePassportDocumentLocalizer
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorViewModel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListViewModel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup.PassportLookupViewModel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read.PassportReadViewModel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyViewModel

/**
 * Корень приложения регистрирует порты: `PassportConfigurationsRepository` («Мои конфигурации») и
 * `PassportInstallerRepository` (исполнитель из профиля).
 */
val mountPassportModule = module {
    singleOf(::SpecialistMountPassportDataSource) bind MountPassportDataSource::class
    singleOf(::SpecialistMountPassportRepository) bind MountPassportRepository::class
    single<PassportReadingDataSource> {
        SpecialistPassportReadingDataSource(interactor = get(), deviceInformation = { get() })
    }
    singleOf(::SpecialistPassportReadingRepository) bind PassportReadingRepository::class
    singleOf(::SpecialistPassportDocumentDataSource) bind PassportDocumentDataSource::class
    singleOf(::SpecialistPassportDocumentRepository) bind PassportDocumentRepository::class
    singleOf(::ResourcePassportDocumentLocalizer) bind PassportDocumentLocalizer::class
    viewModel { params ->
        PassportListViewModel(mode = params.get(), deviceSession = get(), repository = get(), dispatchers = get())
    }
    viewModel { params ->
        PassportEditorViewModel(
            args = params.get(),
            deviceSession = get(),
            repository = get(),
            reading = get(),
            configurations = get(),
            permissions = get(),
            imagePicker = params.get(),
            settingsFileSaver = params.get(),
            dispatchers = get(),
        )
    }
    viewModel { params ->
        PassportReadViewModel(args = params.get(), reading = get(), installers = get(), dispatchers = get())
    }
    viewModel {
        PassportLookupViewModel(reading = get(), installers = get(), permissions = get(), dispatchers = get())
    }
    viewModel { params ->
        PassportReadyViewModel(
            args = params.get(),
            repository = get(),
            documents = get(),
            configurations = get(),
            localizer = get(),
            renderer = params.get(),
            rasterizer = params.get(),
            exporter = params.get(),
            settingsFileSaver = params.get(),
            dispatchers = get(),
        )
    }
}
