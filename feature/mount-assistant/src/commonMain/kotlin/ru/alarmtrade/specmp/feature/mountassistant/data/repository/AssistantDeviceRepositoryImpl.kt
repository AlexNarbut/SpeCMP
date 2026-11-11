package ru.alarmtrade.specmp.feature.mountassistant.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.mountassistant.data.mapper.toFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantDeviceRepository

internal class AssistantDeviceRepositoryImpl(
    private val deviceSession: DeviceSession,
    networkMonitor: NetworkMonitor?,
) : AssistantDeviceRepository {

    // Монитор сети кладёт корень приложения; без него, как и SDK, считаем сеть доступной.
    private val online: Flow<Boolean> = networkMonitor?.isAvailable ?: flowOf(true)

    override fun facts(): Flow<Response<DeviceFacts?>> =
        combine(deviceSession.state, deviceSession.transport, online) { session, transport, online ->
            val device = (session as? DeviceSessionState.Ready)?.device
            Response.Success(if (device != null && transport != null) device.toFacts(transport, online) else null)
        }.distinctUntilChanged()
}
