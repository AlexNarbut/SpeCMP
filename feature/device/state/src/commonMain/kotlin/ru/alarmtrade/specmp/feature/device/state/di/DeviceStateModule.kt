package ru.alarmtrade.specmp.feature.device.state.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.state.data.repository.SpecialistDeviceStateRepository
import ru.alarmtrade.specmp.feature.device.state.domain.repository.DeviceStateRepository
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CheckStateViewModel

/**
 * Koin-модуль фичи `feature/device/state`: экран «Проверка состояния» (расширенный монтаж, шаг быстрого монтажа).
 * SDK-интерактор — из `core/specialist`; сессия, PIN и режим программирования — из `deviceInteractionModule`.
 */
val deviceStateModule = module {
    // Ленивый провайдер: недоступный на платформе интерактор не должен ронять создание репозитория.
    single<DeviceStateRepository> { SpecialistDeviceStateRepository(bluetoothAlarmDeviceInteractor = { get() }) }
    viewModel { (fastMount: Boolean) ->
        CheckStateViewModel(get(), get(), get(), get(), get(), get(), fastMount)
    }
}
