package ru.alarmtrade.specmp.feature.device.firmware.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.device.firmware.data.datasource.FirmwareDataSource
import ru.alarmtrade.specmp.feature.device.firmware.data.datasource.FirmwareUpdateDataSource
import ru.alarmtrade.specmp.feature.device.firmware.data.datasource.SpecialistFirmwareDataSource
import ru.alarmtrade.specmp.feature.device.firmware.data.datasource.SpecialistFirmwareUpdateDataSource
import ru.alarmtrade.specmp.feature.device.firmware.data.preferences.SettingsFirmwareArchivePreferences
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.SpecialistFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.SpecialistFirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.SpecialistSerialFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareArchivePreferences
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SerialFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootEntry
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.domain.usecase.CheckFirmwareUseCase
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation.FirmwareArchivePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareCheckViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.file.FirmwareFileViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial.SerialFirmwareViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems.FirmwareSystemsViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateViewModel

private const val FIRMWARE_ARCHIVE_PREFERENCES = "firmwarearchive"

val deviceFirmwareModule = module {
    // Ленивые провайдеры: недоступный на платформе интерактор (COM-порт на iOS) не должен ронять создание источника.
    single<FirmwareDataSource> {
        SpecialistFirmwareDataSource(deviceInformation = { get() }, firmwareFiles = { get() }, files = { get() })
    }
    single<FirmwareUpdateDataSource> { SpecialistFirmwareUpdateDataSource(alarmDevice = { get() }) }
    singleOf(::SpecialistFirmwareRepository) bind FirmwareRepository::class
    factoryOf(::CheckFirmwareUseCase)
    singleOf(::SpecialistFirmwareUpdateRepository) bind FirmwareUpdateRepository::class
    singleOf(::SpecialistSerialFirmwareRepository) bind SerialFirmwareRepository::class
    // Состояние, привязанное к аккаунту, — чистится при логауте.
    single { FirmwareBootRequester() } bind AuthCloseable::class
    single { FirmwareArchivePicker() }

    factoryOf(::FirmwareBootEntry)
    viewModel { args -> FirmwareCheckViewModel(get(), get(), get(), get(), args.getOrNull()) }
    viewModelOf(::FirmwareUpdateViewModel)
    viewModelOf(::FirmwareFileViewModel)
    viewModelOf(::SerialFirmwareViewModel)
    single<FirmwareArchivePreferences> {
        SettingsFirmwareArchivePreferences(get<AppSettingsFactory>().create(FIRMWARE_ARCHIVE_PREFERENCES))
    }
    viewModel { FirmwareSystemsViewModel(get(), get(), get()) }
    viewModel { (args: FirmwareArchiveRequest, exporter: FirmwareFileExporter?) ->
        FirmwareArchiveViewModel(args, get(), get(), exporter)
    }
}
