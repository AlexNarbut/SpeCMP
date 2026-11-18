package ru.alarmtrade.specmp.feature.device.signallog.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.files.TextFileSaver
import ru.alarmtrade.specmp.feature.device.signallog.data.datasource.SignalLogDataSource
import ru.alarmtrade.specmp.feature.device.signallog.data.datasource.SpecialistSignalLogDataSource
import ru.alarmtrade.specmp.feature.device.signallog.data.repository.SpecialistSignalLogRepository
import ru.alarmtrade.specmp.feature.device.signallog.domain.repository.SignalLogRepository
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogViewModel

val signalLogModule = module {
    singleOf(::SpecialistSignalLogDataSource) bind SignalLogDataSource::class
    singleOf(::SpecialistSignalLogRepository) bind SignalLogRepository::class
    viewModel { (fileSaver: TextFileSaver) -> SignalLogViewModel(get(), get(), fileSaver, get(), get(), get()) }
}
