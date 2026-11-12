package ru.alarmtrade.specmp.feature.device.peripherals.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralDetails
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralLink
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralPollStep
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.repository.SystemPeripheralsRepository

/** Шаги опроса шлёт тест ([emit]); команды отвечают [blinkReply] / [firmwareReply]. */
internal class FakeSystemPeripheralsRepository : SystemPeripheralsRepository {
    var polls = 0
    var types: List<PeripheralType> = emptyList()
    var blinkReply: Response<Unit> = Response.Success(Unit)
    var firmwareReply: Response<Unit> = Response.Success(Unit)
    val calls = mutableListOf<String>()
    private var channel = Channel<Response<PeripheralPollStep>>(Channel.UNLIMITED)

    override fun poll(types: List<PeripheralType>): Flow<Response<PeripheralPollStep>> {
        polls++
        this.types = types
        channel = Channel(Channel.UNLIMITED)
        return channel.receiveAsFlow()
    }

    fun emit(response: Response<PeripheralPollStep>) {
        channel.trySend(response)
    }

    fun close() {
        channel.close()
    }

    override suspend fun blink(address: String): Response<Unit> = blinkReply.also { calls += "blink $address" }

    override suspend fun enterFirmwareMode(address: String): Response<Unit> =
        firmwareReply.also { calls += "firmware $address" }
}

internal fun peripheral(
    type: PeripheralType = PeripheralType.TAG,
    number: Int = 1,
    address: String = "A1B2C3D4E5F$number",
    link: PeripheralLink = PeripheralLink.NEAR,
    signalDbm: Int? = -58,
    voltage: Float? = 2.95f,
    lowBattery: Boolean = false,
    details: PeripheralDetails? = null,
) = SystemPeripheral(type, number, address, link, signalDbm, voltage, lowBattery, details)
