package ru.alarmtrade.specmp.feature.vehicle.ui.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectRequest
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectViewModel

/** Koin-модуль экранов выбора автомобиля. Каталог и недавние — из `feature/vehicle`. */
val vehicleUiModule = module {
    viewModel { (request: VehicleSelectRequest) ->
        VehicleSelectViewModel(
            request = request,
            repository = get(),
            recentStore = get(),
            dispatchers = get()
        )
    }
}
