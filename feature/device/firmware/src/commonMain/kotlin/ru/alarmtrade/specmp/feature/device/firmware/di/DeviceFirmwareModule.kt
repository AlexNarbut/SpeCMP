package ru.alarmtrade.specmp.feature.device.firmware.di

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.SpecialistFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.SpecialistFirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.ServicePinCooldown

/**
 * Koin-модуль логической фичи `feature/device/firmware`: версии прошивок, обновление, состояние перехода в загрузчик.
 * SDK-интеракторы берутся из `core/specialist`.
 */
val deviceFirmwareModule = module {
    single<FirmwareRepository> {
        // Ленивые провайдеры: недоступный на платформе интерактор не должен ронять создание репозитория.
        SpecialistFirmwareRepository(deviceInformation = { get() }, firmwareFiles = { get() })
    }
    single<FirmwareUpdateRepository> { SpecialistFirmwareUpdateRepository(bluetoothAlarmDeviceInteractor = { get() }) }
    // Состояние, привязанное к аккаунту, — чистится при логауте.
    single { FirmwareBootRequester() } bind AuthCloseable::class
    single { ServicePinCooldown() } bind AuthCloseable::class
}
