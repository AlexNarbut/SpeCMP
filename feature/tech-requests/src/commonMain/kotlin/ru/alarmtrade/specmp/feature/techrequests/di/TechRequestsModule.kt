package ru.alarmtrade.specmp.feature.techrequests.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.techrequests.data.datasource.SpecialistTechRequestsDataSource
import ru.alarmtrade.specmp.feature.techrequests.data.datasource.TechRequestsDataSource
import ru.alarmtrade.specmp.feature.techrequests.data.repository.SpecialistTechRequestsRepository
import ru.alarmtrade.specmp.feature.techrequests.domain.repository.TechRequestsRepository
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.TechRequestsViewModel

val techRequestsModule = module {
    singleOf(::SpecialistTechRequestsDataSource) bind TechRequestsDataSource::class
    singleOf(::SpecialistTechRequestsRepository) bind TechRequestsRepository::class
    viewModel { params ->
        TechRequestsViewModel(
            deviceSession = get(),
            repository = get(),
            accountRepository = get(),
            dispatchers = get(),
            localeStore = get(),
            prefill = params.getOrNull(),
        )
    }
}
