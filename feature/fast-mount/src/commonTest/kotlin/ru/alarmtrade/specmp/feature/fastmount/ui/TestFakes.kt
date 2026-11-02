package ru.alarmtrade.specmp.feature.fastmount.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionAnswer
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState
import ru.alarmtrade.specmp.feature.fastmount.domain.repository.FastMountRepository

internal fun testDevice(systemId: Long = 77, modelInDatabase: Boolean = true) = AlarmDevice(
    systemId = systemId,
    systemName = "DX-9X",
    firmwareVersion = "VER3.57",
    mode = DeviceOperationMode.MAIN,
    features = emptySet(),
    can = null,
    modelInDatabase = modelInDatabase
)

internal class FakeSessionRepository(var device: AlarmDevice = testDevice()) : DeviceSessionRepository {
    val link = MutableStateFlow(DeviceLinkState.READY)

    override fun linkState(): Flow<DeviceLinkState> = link
    override suspend fun readBluetoothModule(): Response<BluetoothModule> = Response.Success(
        BluetoothModule(BluetoothModuleMode.MAIN, BluetoothManufacturer("1", "BT", "DX-9X", "1.0", "1.14"))
    )
    override suspend fun readDevice(): Response<AlarmDevice> = Response.Success(device)
    override suspend fun readImei(): Response<String> = Response.Success("")
    override suspend fun syncDeviceDatabase(): Response<Unit> = Response.Success(Unit)
    override suspend fun reconnect(): Response<Unit> = Response.Success(Unit)
    override suspend fun disconnect(): Response<Unit> = Response.Success(Unit)
    override suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> =
        Response.Success(BootModeResult.Entered)
    override suspend fun exitBootMode(target: BootModeTarget): Response<Unit> = Response.Success(Unit)
    override suspend fun setServicePin(pin: ServicePin): Response<Unit> = Response.Success(Unit)
}

internal class FakeConnectionRepository : DeviceConnectionRepository {
    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectSerial(portId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override suspend fun closeSessions(): Response<Unit> = Response.Success(Unit)
}

/** Класс блока отвечает [deviceClass]; шаги мастера в этих тестах не нужны. */
internal class FakeFastMountRepository : FastMountRepository {
    var deviceClass: Response<MountDeviceClass> = Response.Success(MountDeviceClass.ALARM)
    var classRequests = 0

    override suspend fun deviceClass(systemName: String): Response<MountDeviceClass> {
        classRequests++
        return deviceClass
    }

    override suspend fun configuration(id: Int): Response<FastMountConfiguration> = notUsed()
    override suspend fun start(configurationId: Int): Response<Unit> = notUsed()
    override fun steps(): Flow<Response<FastMountStep>> = emptyFlow()
    override suspend fun next(): Response<Unit> = notUsed()
    override suspend fun previous(): Response<Unit> = notUsed()
    override fun questions(): Flow<Response<QuestionsState>> = emptyFlow()
    override suspend fun answer(answer: QuestionAnswer): Response<Unit> = notUsed()
    override suspend fun skip(): Response<Unit> = notUsed()
    override suspend fun feedback(text: String, rating: Int): Response<Unit> = notUsed()

    private fun notUsed(): Response<Nothing> = Response.Error(UnsupportedOperationException())
}
