package ru.alarmtrade.specmp.feature.device.state.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.state.domain.model.Autostart
import ru.alarmtrade.specmp.feature.device.state.domain.model.CommandReply
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineStatus
import ru.alarmtrade.specmp.feature.device.state.domain.model.HandsFreeMode
import ru.alarmtrade.specmp.feature.device.state.domain.model.LinkInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.PowerInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.SirenMode
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneValue
import ru.alarmtrade.specmp.feature.device.state.domain.repository.DeviceStateRepository

/** Снимки шлёт тест ([emit]); команды отвечают [reply], запросы копятся в [sent]. */
internal class FakeDeviceStateRepository : DeviceStateRepository {
    var subscriptions = 0
    var active = 0
    val sent = mutableListOf<StateCommand>()
    var reply: Response<CommandReply> = Response.Success(CommandReply.DONE)
    private var channel = Channel<Response<VehicleState>>(Channel.UNLIMITED)

    override fun states(): Flow<Response<VehicleState>> {
        subscriptions++
        channel = Channel(Channel.UNLIMITED)
        val current = channel
        return current.receiveAsFlow()
            .onStart { active++ }
            .onCompletion { active-- }
    }

    fun emit(state: VehicleState) {
        channel.trySend(Response.Success(state))
    }

    override suspend fun send(command: StateCommand): Response<CommandReply> {
        sent += command
        return reply
    }
}

/** Снимок «всё закрыто, снято с охраны»; меняется через copy. */
internal fun vehicle(
    security: SecurityState = SecurityState.DISARMED,
    zones: Map<Zone, ZoneValue> = emptyMap(),
    serviceMode: Boolean = false,
    programMode: Boolean = false,
    engine: EngineInfo = EngineInfo(EngineStatus.STOPPED, temperature = 20, rpm = null, autostart = Autostart.Disabled),
) = VehicleState(
    security = security,
    alarm = false,
    serviceMode = serviceMode,
    programMode = programMode,
    zones = zones,
    engine = engine,
    power = PowerInfo(voltage = 12.6f, cabinTemp = 21, outsideTemp = 12, fuelPercent = 64),
    link = LinkInfo(gsmLevel = 3, gpsFix = true, speedKmh = 0, sms = true, calls = true),
    event = null,
    can = null,
    ev = null,
    heater = null,
    camera = null,
    siren = SirenMode.SIGNALS,
    handsFree = HandsFreeMode.OFF,
)
