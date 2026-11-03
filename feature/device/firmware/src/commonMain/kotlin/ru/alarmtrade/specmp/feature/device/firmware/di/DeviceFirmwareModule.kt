package ru.alarmtrade.specmp.feature.device.firmware.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.device.firmware.data.preferences.SettingsFirmwareArchivePreferences
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.SpecialistFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.SpecialistFirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.SpecialistSerialFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareArchivePreferences
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SerialFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation.FirmwareArchivePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareCheckViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.file.FirmwareFileViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial.SerialFirmwareViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.systems.FirmwareSystemsViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateViewModel

/**
 * Koin-модуль фичи `feature/device/firmware`: версии прошивок, обновление (в том числе по COM-порту), состояние
 * перехода в загрузчик и ViewModel экранов. SDK-интеракторы берутся из `core/specialist`.
 */

private const val FIRMWARE_ARCHIVE_PREFERENCES = "firmwarearchive"

val deviceFirmwareModule = module {
    single<FirmwareRepository> {
        // Ленивые провайдеры: недоступный на платформе интерактор не должен ронять создание репозитория.
        SpecialistFirmwareRepository(deviceInformation = { get() }, firmwareFiles = { get() }, files = { get() })
    }
    single<FirmwareUpdateRepository> { SpecialistFirmwareUpdateRepository(bluetoothAlarmDeviceInteractor = { get() }) }
    // COM-порт (приложение прошивки): на iOS интерактор SDK недоступен — провайдеры ленивые.
    single<SerialFirmwareRepository> {
        SpecialistSerialFirmwareRepository(alarmDevice = { get() })
    }
    // Состояние, привязанное к аккаунту, — чистится при логауте.
    single { FirmwareBootRequester() } bind AuthCloseable::class
    single { FirmwareArchivePicker() }

    viewModelOf(::FirmwareCheckViewModel)
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
