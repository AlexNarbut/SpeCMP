package ru.alarmtrade.specmp.feature.push.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.push.data.datasource.PushDataSource
import ru.alarmtrade.specmp.feature.push.data.datasource.SpecialistPushDataSource
import ru.alarmtrade.specmp.feature.push.data.repository.SettingsSentPushTokenRepository
import ru.alarmtrade.specmp.feature.push.data.repository.SpecialistPushRulesRepository
import ru.alarmtrade.specmp.feature.push.data.repository.SpecialistPushTokenRepository
import ru.alarmtrade.specmp.feature.push.domain.repository.PushRulesRepository
import ru.alarmtrade.specmp.feature.push.domain.repository.PushTokenRepository
import ru.alarmtrade.specmp.feature.push.domain.repository.SentPushTokenRepository
import ru.alarmtrade.specmp.feature.push.domain.usecase.RegisterPushTokenUseCase

private const val PUSH_TOKEN_PREFERENCES = "push_token"

val pushModule = module {
    singleOf(::SpecialistPushDataSource) bind PushDataSource::class
    singleOf(::SpecialistPushTokenRepository) bind PushTokenRepository::class
    singleOf(::SpecialistPushRulesRepository) bind PushRulesRepository::class
    single {
        SettingsSentPushTokenRepository(get<AppSettingsFactory>().create(PUSH_TOKEN_PREFERENCES))
    } bind SentPushTokenRepository::class bind AuthCloseable::class
    single {
        RegisterPushTokenUseCase(
            tokenSource = get(),
            pushTokenRepository = get(),
            sentPushTokenRepository = get(),
            localeStore = get(),
            clock = get(),
        )
    }
}
