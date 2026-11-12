package ru.alarmtrade.specmp.feature.main.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.main.data.cache.MainUserCache
import ru.alarmtrade.specmp.feature.main.data.cache.SettingsMainUserCache
import ru.alarmtrade.specmp.feature.main.data.datasource.MainDataSource
import ru.alarmtrade.specmp.feature.main.data.datasource.SpecialistMainDataSource
import ru.alarmtrade.specmp.feature.main.data.repository.SpecialistMainRepository
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainScreenViewModel

private const val MAIN_USER_CACHE = "main_user"

val mainModule = module {
    singleOf(::SpecialistMainDataSource) bind MainDataSource::class
    // Кэш профиля привязан к аккаунту: LogoutUseCase очищает его через AuthCloseable.
    single {
        SettingsMainUserCache(get<AppSettingsFactory>().create(MAIN_USER_CACHE))
    } bind MainUserCache::class bind AuthCloseable::class
    single<MainRepository> {
        SpecialistMainRepository(
            dataSource = get(),
            accountRepository = get(),
            appVersionProvider = get(),
            userCache = get(),
            networkMonitor = getOrNull(),
        )
    }
    viewModelOf(::MainScreenViewModel)
}
