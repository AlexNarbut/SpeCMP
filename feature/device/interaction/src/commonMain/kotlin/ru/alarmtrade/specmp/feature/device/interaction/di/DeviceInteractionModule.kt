package ru.alarmtrade.specmp.feature.device.interaction.di

import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.interaction.data.repository.SpecialistDeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.data.repository.SpecialistDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

/**
 * Koin-модуль логической фичи `feature/device/interaction`: подключение, сессия и устройство.
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
    single<DeviceSessionRepository> {
        SpecialistDeviceSessionRepository(bluetoothAlarmDeviceInteractor = { get() })
    }
    single { DeviceSession(get(), get(), get()) }
}
