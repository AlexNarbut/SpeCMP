package ru.alarmtrade.specmp.feature.supportchat.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.supportchat.data.outbox.SettingsSupportChatOutbox
import ru.alarmtrade.specmp.feature.supportchat.data.repository.SpecialistSupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatOutbox
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.SupportChatScreenViewModel

private const val SUPPORT_CHAT_SETTINGS = "supportchat"

val supportChatModule = module {
    single { SpecialistSupportChatRepository(get(), get()) } bind SupportChatRepository::class bind AuthCloseable::class
    // Outbox дополнительно регистрируется как AuthCloseable: LogoutUseCase
    // очищает очередь при смене аккаунта.
    single {
        SettingsSupportChatOutbox(get<AppSettingsFactory>().create(SUPPORT_CHAT_SETTINGS))
    } bind SupportChatOutbox::class bind AuthCloseable::class
    viewModel { params ->
        SupportChatScreenViewModel(
            repository = get(),
            dispatchers = get(),
            fileSaver = params.get(),
            attachmentFiles = params.get(),
            fileOpener = params.get(),
            outbox = get(),
        )
    }
}
