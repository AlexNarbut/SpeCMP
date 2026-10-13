package ru.alarmtrade.specmp.feature.advancedmount.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root.AdvancedMountRootViewModel

/** Koin-модуль расширенного монтажа. Сессия устройства — из `device:interaction`. */
val advancedMountModule = module {
    viewModelOf(::AdvancedMountRootViewModel)
}
