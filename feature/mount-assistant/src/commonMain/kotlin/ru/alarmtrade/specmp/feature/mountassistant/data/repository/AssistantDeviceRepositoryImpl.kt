package ru.alarmtrade.specmp.feature.mountassistant.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.mountassistant.data.mapper.toFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantProduct
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantDeviceRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantProductRepository

internal class AssistantDeviceRepositoryImpl(
    private val deviceSession: DeviceSession,
    networkMonitor: NetworkMonitor?,
    private val products: AssistantProductRepository,
) : AssistantDeviceRepository {

    // Монитор сети кладёт корень приложения; без него, как и SDK, считаем сеть доступной.
    private val online: Flow<Boolean> = networkMonitor?.isAvailable ?: flowOf(true)

    override fun facts(): Flow<Response<DeviceFacts?>> {
        val productSearch = products.enabled().map { it.getValueIfSuccess() ?: false }.onStart { emit(false) }
        return combine(deviceSession.state, deviceSession.transport, online, productSearch) {
                session,
                transport,
                online,
                product,
            ->
            val device = (session as? DeviceSessionState.Ready)?.device
            val facts = if (device != null && transport != null) device.toFacts(transport, online, product) else null
            Response.Success(facts)
        }.distinctUntilChanged()
    }

    override suspend fun product(systemId: Long): Response<AssistantProduct> = products.summary(systemId)
}
