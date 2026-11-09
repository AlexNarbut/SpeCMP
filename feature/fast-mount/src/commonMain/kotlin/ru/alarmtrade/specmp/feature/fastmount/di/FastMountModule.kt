package ru.alarmtrade.specmp.feature.fastmount.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.fastmount.data.datasource.FastMountDataSource
import ru.alarmtrade.specmp.feature.fastmount.data.datasource.SpecialistFastMountDataSource
import ru.alarmtrade.specmp.feature.fastmount.data.repository.SpecialistFastMountRepository
import ru.alarmtrade.specmp.feature.fastmount.domain.repository.FastMountRepository
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master.FastMountViewModel

/**
 * Koin-модуль `feature/fast-mount`: мастер быстрого монтажа. SDK-интеракторы — из `core/specialist`; сессия
 * устройства и режим программирования — из `deviceInteractionModule`.
 */
val fastMountModule = module {
    // Ленивые провайдеры: недоступный на платформе интерактор не должен ронять создание источника.
    single<FastMountDataSource> {
        SpecialistFastMountDataSource(
            fastMount = { get() },
            configurations = { get() },
            deviceInformation = { get() },
            verification = { get() },
            firmwareFiles = { get() },
            files = { get() },
        )
    }
    singleOf(::SpecialistFastMountRepository) bind FastMountRepository::class

    viewModelOf(::FastMountViewModel)
}
