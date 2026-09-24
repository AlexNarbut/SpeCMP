package ru.alarmtrade.specmp.feature.supportchat.di

import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.feature.supportchat.data.outbox.SettingsSupportChatOutboxStore
import ru.alarmtrade.specmp.feature.supportchat.data.outbox.SupportChatOutboxStore
import ru.alarmtrade.specmp.feature.supportchat.data.repository.SpecialistSupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.SupportChatScreenViewModel

val supportChatModule = module {
    single { SpecialistSupportChatRepository(get(), get(), get()) } bind SupportChatRepository::class bind AuthCloseable::class
    // Outbox дополнительно регистрируется как AuthCloseable: LogoutUseCase
    // очищает очередь при смене аккаунта.
    single { SettingsSupportChatOutboxStore(Settings()) } bind SupportChatOutboxStore::class bind AuthCloseable::class
    viewModel { params ->
        SupportChatScreenViewModel(
            repository = get(),
            dispatchers = get(),
            appScope = get(appScopeQualifier),
            fileSaver = params.get(),
            outboxStore = get(),
        )
    }
}
