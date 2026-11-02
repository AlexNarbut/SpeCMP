package ru.alarmtrade.specmp.feature.device.autostart.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.ProgramModeResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.BypassLearningRepository
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.CloneRepository
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.ProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothManufacturer
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModuleMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository

internal val testDevice = AlarmDevice(
    systemId = 77,
    systemName = "DX4G",
    firmwareVersion = "VER3.57",
    mode = DeviceOperationMode.MAIN,
    features = emptySet(),
    can = null
)

internal class FakeSessionRepository(var device: AlarmDevice = testDevice) : DeviceSessionRepository {
    val link = MutableStateFlow(DeviceLinkState.READY)
    val pins = mutableListOf<ServicePin>()

    override fun linkState(): Flow<DeviceLinkState> = link
    override suspend fun readBluetoothModule(): Response<BluetoothModule> = Response.Success(
        BluetoothModule(BluetoothModuleMode.MAIN, BluetoothManufacturer("1", "BT", "DX4G", "1.0", "1.14"))
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

/** Clone: события процедуры шлёт тест ([events]); каждый запуск — новый канал. */
internal class FakeCloneRepository : CloneRepository {
    var attempts: Int? = 3
    var attemptsCalls = 0
    var runs = 0
    var events = Channel<CloneEvent>(Channel.UNLIMITED)
        private set

    override suspend fun attempts(): Int? {
        attemptsCalls++
        return attempts
    }

    override fun run(): Flow<CloneEvent> {
        runs++
        events = Channel(Channel.UNLIMITED)
        return events.receiveAsFlow()
    }

    fun send(vararg event: CloneEvent) = event.forEach { events.trySend(it) }

    fun finish() = events.close()
}

/** Обход: шаги шлёт тест ([events]); каждый запуск — новый канал. */
internal class FakeBypassRepository : BypassLearningRepository {
    var runs = 0
    var events = Channel<BypassEvent>(Channel.UNLIMITED)
        private set

    override fun learn(): Flow<BypassEvent> {
        runs++
        events = Channel(Channel.UNLIMITED)
        return events.receiveAsFlow()
    }

    fun send(vararg event: BypassEvent) = event.forEach { events.trySend(it) }
}

internal class FakeProgramModeRepository : ProgramModeRepository {
    /** Ответы enter по порядку; последний повторяется. */
    var results: List<Response<ProgramModeResult>> = listOf(Response.Success(ProgramModeResult.ENTERED))
    var calls = 0

    override suspend fun enter(): Response<ProgramModeResult> = results[minOf(calls++, results.lastIndex)]
}
