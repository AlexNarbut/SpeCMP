package ru.alarmtrade.specmp.feature.device.interaction.testing

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope
import ru.alarmtrade.specmp.feature.device.interaction.di.deviceInteractionModule
import ru.alarmtrade.specmp.feature.device.interaction.di.deviceSessionScopeQualifier
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession

/** Настоящая сессия из `deviceInteractionModule` поверх фейков, уже `start()`; скоуп — дочерний к `backgroundScope`. */
fun TestScope.testDeviceSession(
    sessionRepository: DeviceSessionRepository = FakeDeviceSessionRepository(),
    connectionRepository: DeviceConnectionRepository = FakeDeviceConnectionRepository(),
    dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(testScheduler),
    transport: ConnectionTransport? = null,
): DeviceSession {
    val overrides = module {
        single<DeviceSessionRepository> { sessionRepository }
        single<DeviceConnectionRepository> { connectionRepository }
        single(deviceSessionScopeQualifier) { serviceScope(dispatcher, parent = backgroundScope.coroutineContext) }
    }
    val app = koinApplication { modules(deviceInteractionModule, overrides) }
    // Экземпляры single живут в объекте модуля (общий val) — без close() следующий тест получил бы эту же сессию.
    val session = app.koin.get<DeviceSession>().also { app.close() }
    session.start()
    transport?.let(session::attach)
    return session
}
