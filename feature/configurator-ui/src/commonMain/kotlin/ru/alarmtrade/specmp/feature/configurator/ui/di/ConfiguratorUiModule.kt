package ru.alarmtrade.specmp.feature.configurator.ui.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.SourceKey
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud.CloudCardViewModel
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud.CloudListViewModel
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine.MineViewModel
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models.ModelsViewModel

val configuratorUiModule = module {
    viewModel { (pickDeviceName: String?) ->
        MineViewModel(
            pickDeviceName = pickDeviceName,
            configurations = get(),
            settings = get(),
            sessions = get(),
            deviceSession = get(),
            dispatchers = get(),
        )
    }
    viewModel { (file: SourceKey.File?) ->
        ModelsViewModel(file = file, models = get(), settings = get(), deviceSession = get(), dispatchers = get())
    }
    viewModel { (carId: Int, deviceName: String) ->
        CloudListViewModel(carId = carId, deviceName = deviceName, cloud = get(), dispatchers = get())
    }
    viewModel { (id: Int, firmwareCanCode: String?) ->
        CloudCardViewModel(
            id = id,
            firmwareCanCode = firmwareCanCode,
            cloud = get(),
            settings = get(),
            configurations = get(),
            deviceSession = get(),
            dispatchers = get(),
        )
    }
}
