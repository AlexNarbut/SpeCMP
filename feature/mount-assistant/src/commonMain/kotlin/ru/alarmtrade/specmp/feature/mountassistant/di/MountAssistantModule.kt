package ru.alarmtrade.specmp.feature.mountassistant.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.mountassistant.data.repository.AssistantDeviceRepositoryImpl
import ru.alarmtrade.specmp.feature.mountassistant.data.repository.AssistantSettingsRepositoryImpl
import ru.alarmtrade.specmp.feature.mountassistant.data.service.AssistantJournalImpl
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantDeviceRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantSettingsRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.service.AssistantJournal
import ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant.MountAssistantViewModel

val mountAssistantModule = module {
    singleOf(::AssistantJournalImpl) bind AssistantJournal::class
    single<AssistantDeviceRepository> { AssistantDeviceRepositoryImpl(get(), getOrNull(), get()) }
    singleOf(::AssistantSettingsRepositoryImpl) bind AssistantSettingsRepository::class
    viewModelOf(::MountAssistantViewModel)
}
