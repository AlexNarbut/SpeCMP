package ru.alarmtrade.specmp.feature.device.firmware.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateTarget
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionPage
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothManufacturer
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModuleMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository

internal class FakeFirmwareRepository : FirmwareRepository {
    var modulesResult: Response<List<FirmwareModule>> = Response.Success(emptyList())
    val latest = mutableMapOf<Pair<Long, FirmwareChannel>, Response<FirmwareVersion>>()
    val latestCalls = mutableListOf<Pair<Long, FirmwareChannel>>()

    override suspend fun modules(deviceName: String, transport: ConnectionTransport) = modulesResult

    override suspend fun latest(moduleId: Long, channel: FirmwareChannel): Response<FirmwareVersion> {
        latestCalls += moduleId to channel
        return latest[moduleId to channel] ?: Response.Error(IllegalStateException("no version"))
    }

    /** Основной модуль модели блока (архив без moduleId). */
    var mainModule: Response<Long?> = Response.Success(null)
    val mainModuleCalls = mutableListOf<String>()

    override suspend fun mainModuleId(deviceName: String): Response<Long?> {
        mainModuleCalls += deviceName
        return mainModule
    }

    /** Страницы архива по номеру; нет страницы — ошибка сервера. */
    val archivePages = mutableMapOf<Int, Response<FirmwareVersionPage>>()
    val archiveCalls = mutableListOf<Int>()

    /** Запросы архива целиком: модуль, страница и CAN-фильтр. */
    val archiveRequests = mutableListOf<Triple<Long, Int, String?>>()

    override suspend fun archivePage(
        moduleId: Long,
        channel: FirmwareChannel,
        page: Int,
        pageSize: Int,
        canCode: String?
    ): Response<FirmwareVersionPage> {
        archiveCalls += page
        archiveRequests += Triple(moduleId, page, canCode)
        return archivePages[page] ?: Response.Error(IllegalStateException("no page"))
    }
}

internal class FakeSessionRepository(var device: AlarmDevice) : DeviceSessionRepository {
    val link = MutableStateFlow(DeviceLinkState.READY)

    /** Ответы readBluetoothModule по порядку; последний повторяется. */
    var bluetoothVersions: List<Response<BluetoothModule>> = listOf(
        Response.Success(
            BluetoothModule(
                BluetoothModuleMode.MAIN,
                BluetoothManufacturer("1", "BT", "DX4G", "1.0", "1.14")
            )
        )
    )
    var bluetoothReads = 0

    /** Ответы setBootMode по порядку; последний повторяется. */
    var bootResults: List<Response<BootModeResult>> = listOf(Response.Success(BootModeResult.Entered))
    var bootCalls = 0
    val bootTargets = mutableListOf<BootModeTarget>()
    val pins = mutableListOf<ServicePin>()

    override fun linkState(): Flow<DeviceLinkState> = link
    override suspend fun readBluetoothModule() = bluetoothVersions[minOf(bluetoothReads++, bluetoothVersions.lastIndex)]
    override suspend fun readDevice(): Response<AlarmDevice> = Response.Success(device)
    override suspend fun readImei(): Response<String> = Response.Success("")
    override suspend fun syncDeviceDatabase(): Response<Unit> = Response.Success(Unit)
    var reconnectCount = 0
    override suspend fun reconnect(): Response<Unit> {
        reconnectCount++
        return Response.Success(Unit)
    }
    override suspend fun disconnect(): Response<Unit> = Response.Success(Unit)

    override suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> {
        bootTargets += target
        return bootResults[minOf(bootCalls++, bootResults.lastIndex)]
    }

    /** Ответы exitBootMode по порядку; последний повторяется. */
    var exitResults: List<Response<Unit>> = listOf(Response.Success(Unit))
    var exitCalls = 0
    val exitTargets = mutableListOf<BootModeTarget>()

    override suspend fun exitBootMode(target: BootModeTarget): Response<Unit> {
        exitTargets += target
        return exitResults[minOf(exitCalls++, exitResults.lastIndex)]
    }

    override suspend fun setServicePin(pin: ServicePin): Response<Unit> {
        pins += pin
        return Response.Success(Unit)
    }
}

internal class FakeConnectionRepository : DeviceConnectionRepository {
    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override suspend fun closeSessions(): Response<Unit> = Response.Success(Unit)
}

/** Процесс обновления из тестов: прогресс подаётся в [progress]; параметры запуска запоминаются. */
internal class FakeUpdateRepository : FirmwareUpdateRepository {
    val progress = MutableSharedFlow<FirmwareUpdateProgress>(extraBufferCapacity = 16)
    val calls = mutableListOf<Pair<FirmwareUpdateTarget, FirmwareVersion>>()

    override fun update(target: FirmwareUpdateTarget, firmware: FirmwareVersion): Flow<FirmwareUpdateProgress> {
        calls += target to firmware
        return progress
    }

    val fileCalls = mutableListOf<Pair<FirmwareUpdateTarget, String>>()

    override fun updateFromFile(target: FirmwareUpdateTarget, path: String): Flow<FirmwareUpdateProgress> {
        fileCalls += target to path
        return progress
    }
}
