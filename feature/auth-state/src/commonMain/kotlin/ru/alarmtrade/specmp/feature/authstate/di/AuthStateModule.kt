package ru.alarmtrade.specmp.feature.authstate.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.authstate.data.datasource.AuthStateDataSource
import ru.alarmtrade.specmp.feature.authstate.data.datasource.SpecialistAuthStateDataSource
import ru.alarmtrade.specmp.feature.authstate.data.repository.SettingsSessionMarkRepository
import ru.alarmtrade.specmp.feature.authstate.data.repository.SpecialistAuthStateRepository
import ru.alarmtrade.specmp.feature.authstate.data.service.AuthSessionControllerImpl
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository
import ru.alarmtrade.specmp.feature.authstate.domain.repository.SessionMarkRepository
import ru.alarmtrade.specmp.feature.authstate.domain.service.AuthSessionController
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.LogoutUseCase
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.ReleaseAccountStateUseCase
import ru.alarmtrade.specmp.feature.authstate.presentation.logic.AuthStateViewModel

private const val AUTH_SESSION_PREFERENCES = "auth_session"

val authStateModule = module {
    singleOf(::SpecialistAuthStateDataSource) bind AuthStateDataSource::class
    singleOf(::SpecialistAuthStateRepository) bind AuthStateRepository::class
    single<SessionMarkRepository> {
        SettingsSessionMarkRepository(get<AppSettingsFactory>().create(AUTH_SESSION_PREFERENCES))
    }
    // Один экземпляр на процесс: его Mutex не даёт кнопке «Выйти» и наблюдателю сессии сбросить состояние дважды.
    single { ReleaseAccountStateUseCase(get(), getAll()) }
    single<AuthSessionController> {
        AuthSessionControllerImpl(get(), get(), get(), get(appScopeQualifier), get())
    }
    singleOf(::LogoutUseCase)
    viewModelOf(::AuthStateViewModel)
}
