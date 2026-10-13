package ru.alarmtrade.specmp.feature.advancedmount.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.advancedmount.data.repository.SpecialistAdvancedMountRepository
import ru.alarmtrade.specmp.feature.advancedmount.domain.repository.AdvancedMountRepository
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root.AdvancedMountRootViewModel

/** Koin-модуль расширенного монтажа. Сессия устройства — из `device:interaction`, SDK-интеракторы — из `core:specialist`. */
val advancedMountModule = module {
    singleOf(::SpecialistAdvancedMountRepository) bind AdvancedMountRepository::class
    viewModelOf(::AdvancedMountRootViewModel)
}
