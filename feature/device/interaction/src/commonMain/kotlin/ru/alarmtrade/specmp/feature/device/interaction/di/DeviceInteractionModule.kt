package ru.alarmtrade.specmp.feature.device.interaction.di

import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.feature.device.interaction.data.repository.SpecialistDeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.data.repository.SpecialistDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.data.repository.SpecialistProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.ProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown

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
            serialConnectorInteractor = { get() },
        )
    }
    single<DeviceSessionRepository> {
        SpecialistDeviceSessionRepository(bluetoothAlarmDeviceInteractor = { get() }, appUpdatesInteractor = { get() })
    }
    single { DeviceSession(get(), get(), get()) }
    // Блокировка ввода PIN программирования (по блоку): общая для прошивки и режима программирования.
    // Состояние аккаунта — чистится при логауте.
    single { ServicePinCooldown() } bind AuthCloseable::class
    // Режим программирования (PIN по BLE + кулдаун): обход, проверка состояния.
    single<ProgramModeRepository> { SpecialistProgramModeRepository(bluetoothAlarmDeviceInteractor = { get() }) }
    factory { ProgramModeEntry(get(), get(), get()) }
}
