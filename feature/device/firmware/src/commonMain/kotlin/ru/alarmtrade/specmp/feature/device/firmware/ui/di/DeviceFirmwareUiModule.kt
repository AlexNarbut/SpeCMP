package ru.alarmtrade.specmp.feature.device.firmware.ui.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveArgs
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareCheckViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateViewModel

/** Koin-модуль экранов прошивки: ViewModel. Репозитории и состояние — из `feature/device/firmware`. */
val deviceFirmwareUiModule = module {
    viewModelOf(::FirmwareCheckViewModel)
    viewModelOf(::FirmwareUpdateViewModel)
    viewModel { (args: FirmwareArchiveArgs) -> FirmwareArchiveViewModel(args, get(), get(), get()) }
}
