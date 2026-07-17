package ru.alarmtrade.specmp.feature.supportchat.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.supportchat.data.repository.SpecialistSupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.SupportChatScreenViewModel

val supportChatModule = module {
    singleOf(::SpecialistSupportChatRepository) bind SupportChatRepository::class
    viewModelOf(::SupportChatScreenViewModel)
}
