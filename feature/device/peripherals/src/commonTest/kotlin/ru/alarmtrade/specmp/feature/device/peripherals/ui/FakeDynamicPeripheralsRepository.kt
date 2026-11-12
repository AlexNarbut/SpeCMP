package ru.alarmtrade.specmp.feature.device.peripherals.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.DynamicPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.domain.repository.DynamicPeripheralsRepository

/** Таблицы шлёт тест ([emit]); команды отвечают [reply], вызовы копятся в [calls]. */
internal class FakeDynamicPeripheralsRepository : DynamicPeripheralsRepository {
    var subscriptions = 0
    var reply: Response<Unit> = Response.Success(Unit)
    val calls = mutableListOf<String>()
    private var channel = Channel<Response<List<DynamicPeripheral>>>(Channel.UNLIMITED)

    override fun slots(): Flow<Response<List<DynamicPeripheral>>> {
        subscriptions++
        channel = Channel(Channel.UNLIMITED)
        return channel.receiveAsFlow()
    }

    fun emit(response: Response<List<DynamicPeripheral>>) {
        channel.trySend(response)
    }

    override suspend fun pair(slot: Int, address: String?): Response<Unit> = reply.also {
        calls += "pair $slot $address"
    }

    override suspend fun delete(slot: Int): Response<Unit> = reply.also { calls += "delete $slot" }

    override suspend fun update(slot: Int): Response<Unit> = reply.also { calls += "update $slot" }
}

internal fun slot(
    slot: Int,
    type: PeripheralType = PeripheralType.TAG,
    number: Int = slot + 1,
    paired: Boolean = false,
    connected: Boolean = paired,
) = DynamicPeripheral(slot, type, number, paired, connected, voltage = if (connected) 3.0f else null, pirAddress = null)
