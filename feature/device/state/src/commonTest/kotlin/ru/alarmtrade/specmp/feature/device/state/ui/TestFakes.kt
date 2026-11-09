package ru.alarmtrade.specmp.feature.device.state.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothManufacturer
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModuleMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ProgramModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.ProgramModeRepository
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

internal val testDevice = AlarmDevice(
    systemId = 77,
    systemName = "DX4G",
    firmwareVersion = "VER3.57",
    mode = DeviceOperationMode.MAIN,
    features = emptySet(),
    can = null,
)

internal class FakeSessionRepository(var device: AlarmDevice = testDevice) : DeviceSessionRepository {
    val link = MutableStateFlow(DeviceLinkState.READY)
    val pins = mutableListOf<ServicePin>()

    override fun linkState(): Flow<Response<DeviceLinkState>> = link.map { Response.Success(it) }
    override suspend fun readBluetoothModule(): Response<BluetoothModule> = Response.Success(
        BluetoothModule(BluetoothModuleMode.MAIN, BluetoothManufacturer("1", "BT", "DX4G", "1.0", "1.14")),
    )
    override suspend fun readDevice(): Response<AlarmDevice> = Response.Success(device)
    override suspend fun readImei(): Response<String> = Response.Success("")
    override suspend fun syncDeviceDatabase(): Response<Unit> = Response.Success(Unit)
    override suspend fun reconnect(): Response<Unit> = Response.Success(Unit)
    override suspend fun disconnect(): Response<Unit> = Response.Success(Unit)
    override suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> =
        Response.Success(BootModeResult.Entered)
    override suspend fun exitBootMode(target: BootModeTarget): Response<Unit> = Response.Success(Unit)
    override suspend fun setServicePin(pin: ServicePin): Response<Unit> {
        pins += pin
        return Response.Success(Unit)
    }
}

internal class FakeConnectionRepository : DeviceConnectionRepository {
    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectSerial(portId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override suspend fun closeSessions(): Response<Unit> = Response.Success(Unit)
}

internal class FakeProgramModeRepository : ProgramModeRepository {
    /** Ответы enter по порядку; последний повторяется. */
    var results: List<Response<ProgramModeResult>> = listOf(Response.Success(ProgramModeResult.ENTERED))
    var calls = 0

    override suspend fun enter(): Response<ProgramModeResult> = results[minOf(calls++, results.lastIndex)]

    var exits = 0

    override suspend fun exit(): Response<Unit> {
        exits++
        return Response.Success(Unit)
    }
}

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
