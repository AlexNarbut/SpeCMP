package ru.alarmtrade.specmp.feature.device.interaction.di

import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.interaction.data.repository.SpecialistAlarmDeviceRepository
import ru.alarmtrade.specmp.feature.device.interaction.data.repository.SpecialistDeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.AlarmDeviceRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository

/**
 * Koin-модуль логической фичи `feature/device/interaction`.
 * Репозитории-фасады сессии подключения и работы с alarm-устройством;
 * SDK-интеракторы берутся из `core/specialist`.
 */
val deviceInteractionModule = module {
    single<DeviceConnectionRepository> {
        // Ленивые провайдеры: недоступный транспорт на платформе не должен
        // ронять создание репозитория (см. resolveOrNull в реализации).
        SpecialistDeviceConnectionRepository(
            bluetoothConnectorInteractor = { get() },
            usbConnectorInteractor = { get() },
        )
    }
    single<AlarmDeviceRepository> {
        SpecialistAlarmDeviceRepository(
            bluetoothAlarmDeviceInteractor = { get() },
        )
    }
}
