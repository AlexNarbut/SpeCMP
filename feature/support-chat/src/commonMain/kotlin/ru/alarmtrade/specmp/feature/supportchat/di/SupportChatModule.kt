package ru.alarmtrade.specmp.feature.supportchat.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.feature.supportchat.data.repository.SpecialistSupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.SupportChatScreenViewModel

val supportChatModule = module {
    single { SpecialistSupportChatRepository(get(), get(), get()) } bind SupportChatRepository::class bind AuthCloseable::class
    viewModelOf(::SupportChatScreenViewModel)
}
