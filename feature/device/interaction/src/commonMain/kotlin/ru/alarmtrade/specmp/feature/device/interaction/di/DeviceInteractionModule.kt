package ru.alarmtrade.specmp.feature.device.interaction.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope
import ru.alarmtrade.specmp.feature.device.interaction.data.datasource.DeviceConnectionDataSource
import ru.alarmtrade.specmp.feature.device.interaction.data.datasource.DeviceSessionDataSource
import ru.alarmtrade.specmp.feature.device.interaction.data.datasource.ProgramModeDataSource
import ru.alarmtrade.specmp.feature.device.interaction.data.datasource.SpecialistDeviceConnectionDataSource
import ru.alarmtrade.specmp.feature.device.interaction.data.datasource.SpecialistDeviceSessionDataSource
import ru.alarmtrade.specmp.feature.device.interaction.data.datasource.SpecialistProgramModeDataSource
import ru.alarmtrade.specmp.feature.device.interaction.data.repository.SpecialistDeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.data.repository.SpecialistDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.data.repository.SpecialistProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.data.service.DeviceSessionImpl
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.ProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown

// Скоуп сессии — отдельной записью: тесты других модулей (`interaction:testing`) подменяют его на дочерний к
// `backgroundScope`, реализация сессии остаётся internal.
val deviceSessionScopeQualifier = named("deviceSessionScope")

/**
 * Koin-модуль логической фичи `feature/device/interaction`: подключение, сессия и устройство.
 * SDK-интеракторы берутся из `core/specialist`.
 */
val deviceInteractionModule = module {
    // Ленивые провайдеры: недоступный на платформе интерактор не должен ронять создание источника.
    single<DeviceConnectionDataSource> {
        SpecialistDeviceConnectionDataSource(bluetooth = { get() }, usb = { get() }, serial = { get() })
    }
    single<DeviceSessionDataSource> {
        SpecialistDeviceSessionDataSource(alarmDevice = { get() }, appUpdates = { get() })
    }
    single<ProgramModeDataSource> { SpecialistProgramModeDataSource(alarmDevice = { get() }) }
    singleOf(::SpecialistDeviceConnectionRepository) bind DeviceConnectionRepository::class
    singleOf(::SpecialistDeviceSessionRepository) bind DeviceSessionRepository::class
    single(deviceSessionScopeQualifier) { serviceScope(get<AppDispatchers>().io, logTag = "DeviceSession") }
    single<DeviceSession> { DeviceSessionImpl(get(), get(), get(deviceSessionScopeQualifier)) }
    // Блокировка ввода PIN программирования (по блоку): общая для прошивки и режима программирования.
    // Состояние аккаунта — чистится при логауте.
    single { ServicePinCooldown() } bind AuthCloseable::class
    // Режим программирования (PIN по BLE + кулдаун): обход, проверка состояния.
    singleOf(::SpecialistProgramModeRepository) bind ProgramModeRepository::class
    factory { ProgramModeEntry(get(), get(), get()) }
}
