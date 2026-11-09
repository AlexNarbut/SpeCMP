package ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

internal val testDevice = AlarmDevice(1, "DX4G", "3.57", DeviceOperationMode.MAIN, emptySet(), can = null)

internal fun TestScope.testDeviceSession(link: MutableStateFlow<DeviceLinkState>, dispatcher: TestDispatcher) =
    DeviceSession(
        FakeDeviceSessionRepository(link),
        FakeDeviceConnectionRepository,
        AppDispatchers(dispatcher, dispatcher, dispatcher),
        backgroundScope.coroutineContext,
    )

private class FakeDeviceSessionRepository(private val link: Flow<DeviceLinkState>) : DeviceSessionRepository {
    override fun linkState(): Flow<Response<DeviceLinkState>> = link.map { Response.Success(it) }
    override suspend fun readBluetoothModule(): Response<BluetoothModule> =
        Response.Error(IllegalStateException("usb only"))
    override suspend fun readDevice(): Response<AlarmDevice> = Response.Success(testDevice)
    override suspend fun readImei(): Response<String> = Response.Success("")
    override suspend fun syncDeviceDatabase(): Response<Unit> = Response.Success(Unit)
    override suspend fun reconnect(): Response<Unit> = Response.Success(Unit)
    override suspend fun disconnect(): Response<Unit> = Response.Success(Unit)
    override suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> =
        Response.Success(BootModeResult.Entered)
    override suspend fun exitBootMode(target: BootModeTarget): Response<Unit> = Response.Success(Unit)
    override suspend fun setServicePin(pin: ServicePin): Response<Unit> = Response.Success(Unit)
}

private object FakeDeviceConnectionRepository : DeviceConnectionRepository {
    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectSerial(portId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override suspend fun closeSessions(): Response<Unit> = Response.Success(Unit)
}
