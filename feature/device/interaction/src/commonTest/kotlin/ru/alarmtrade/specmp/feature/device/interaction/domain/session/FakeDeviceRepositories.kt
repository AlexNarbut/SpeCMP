package ru.alarmtrade.specmp.feature.device.interaction.domain.session

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import ru.alarmtrade.specmp.core.common.AppDispatchers
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
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository

val testDevice = AlarmDevice(
    systemId = 1076884189,
    systemName = "DX4G",
    firmwareVersion = "3.57",
    mode = DeviceOperationMode.MAIN,
    features = emptySet(),
    can = null
)

/** Сессия на тестовом планировщике; скоуп сессии — дочерний к `backgroundScope` (гасится в конце теста). */
fun TestScope.testSession(
    repository: DeviceSessionRepository = FakeDeviceSessionRepository(),
    connection: DeviceConnectionRepository = FakeDeviceConnectionRepository()
): DeviceSession {
    val dispatcher = UnconfinedTestDispatcher(testScheduler)
    return DeviceSession(
        repository,
        connection,
        AppDispatchers(dispatcher, dispatcher, dispatcher),
        backgroundScope.coroutineContext
    )
}

val testManufacturer = BluetoothManufacturer(
    serialNumber = "123456",
    firmwareName = "BT-DX4G",
    deviceName = "DX4G",
    bootFirmwareVersion = "1.02",
    mainFirmwareVersion = "2.15"
)

class FakeDeviceSessionRepository(initial: DeviceLinkState = DeviceLinkState.DISCONNECTED) : DeviceSessionRepository {
    val link = MutableStateFlow(initial)

    /** Ответы readDevice по порядку; последний повторяется. */
    var readResults: List<Response<AlarmDevice>> = listOf(Response.Success(testDevice))
    var readCount = 0
        private set

    override fun linkState(): Flow<DeviceLinkState> = link

    /** Ответ readBluetoothModule (BLE). */
    var bluetoothModule: Response<BluetoothModule> = Response.Success(
        BluetoothModule(BluetoothModuleMode.MAIN, testManufacturer)
    )
    var bluetoothReadCount = 0
        private set

    override suspend fun readBluetoothModule(): Response<BluetoothModule> {
        bluetoothReadCount++
        return bluetoothModule
    }

    override suspend fun readDevice(): Response<AlarmDevice> {
        if (readHangs) {
            readCount++
            awaitCancellation()
        }
        return readResults[minOf(readCount++, readResults.lastIndex)]
    }

    var imei: Response<String> = Response.Success("351756051523999")
    var bootModeCount = 0
        private set

    override suspend fun readImei(): Response<String> = imei

    var syncResult: Response<Unit> = Response.Success(Unit)
    var syncCount = 0
        private set

    override suspend fun syncDeviceDatabase(): Response<Unit> {
        syncCount++
        return syncResult
    }

    var reconnectCount = 0
        private set

    var disconnectCount = 0
        private set

    /** true — readDevice «зависает» (блок не ответил). */
    var readHangs = false

    override suspend fun disconnect(): Response<Unit> {
        disconnectCount++
        link.value = DeviceLinkState.DISCONNECTED
        return Response.Success(Unit)
    }

    /** Не null — reconnect «висит», пока его не завершат. */
    var reconnectGate: CompletableDeferred<Unit>? = null

    override suspend fun reconnect(): Response<Unit> {
        reconnectCount++
        reconnectGate?.await()
        return Response.Success(Unit)
    }

    /** Ответы setBootMode по порядку; последний повторяется. */
    var bootModeResults: List<Response<BootModeResult>> = listOf(Response.Success(BootModeResult.Entered))
    val bootModeTargets = mutableListOf<BootModeTarget>()
    val savedPins = mutableListOf<ServicePin>()

    override suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> {
        bootModeTargets += target
        return bootModeResults[minOf(bootModeCount++, bootModeResults.lastIndex)]
    }

    val exitTargets = mutableListOf<BootModeTarget>()

    override suspend fun exitBootMode(target: BootModeTarget): Response<Unit> {
        exitTargets += target
        return Response.Success(Unit)
    }

    override suspend fun setServicePin(pin: ServicePin): Response<Unit> {
        savedPins += pin
        return Response.Success(Unit)
    }
}

class FakeDeviceConnectionRepository : DeviceConnectionRepository {
    var closeCount = 0
        private set

    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override suspend fun closeSessions(): Response<Unit> {
        closeCount++
        return Response.Success(Unit)
    }
}
