package ru.alarmtrade.specmp.feature.fastmount.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothManufacturer
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModuleMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ProgramModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.ProgramModeRepository
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountQuestion
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionAnswer
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState
import ru.alarmtrade.specmp.feature.fastmount.domain.model.UnitAccess
import ru.alarmtrade.specmp.feature.fastmount.domain.repository.FastMountRepository

internal fun testDevice(
    systemId: Long = 77,
    modelInDatabase: Boolean = true,
    firmware: String = "VER3.57",
    can: CanInfo? = CanInfo(firmwareVersion = "1.2", code = "0213", isFactory = false)
) = AlarmDevice(
    systemId = systemId,
    systemName = "DX-9X",
    firmwareVersion = firmware,
    mode = DeviceOperationMode.MAIN,
    features = emptySet(),
    can = can,
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

internal class FakeProgramModeRepository : ProgramModeRepository {
    var exits = 0
    override suspend fun enter(): Response<ProgramModeResult> = Response.Success(ProgramModeResult.ENTERED)
    override suspend fun exit(): Response<Unit> {
        exits++
        return Response.Success(Unit)
    }
}

internal fun question(id: Int, automatic: Boolean = false) = MountQuestion(
    id = id,
    group = "",
    task = "Задание $id",
    expected = "",
    automatic = automatic,
    answers = if (automatic) emptyList() else listOf(QuestionAnswer("Да", 1), QuestionAnswer("Нет", 2))
)

internal fun testConfiguration(
    keyBypass: Boolean = true,
    slave: Boolean = true,
    questions: List<MountQuestion> = listOf(question(5, automatic = true), question(100), question(101))
) = FastMountConfiguration(
    id = 42,
    name = "Polo VI",
    shortDescription = "",
    fullDescription = "<p>Описание</p>",
    deviceName = "DX-9X",
    author = "Автор",
    rating = 5,
    mountingMap = null,
    images = emptyList(),
    keyBypass = keyBypass,
    slave = slave,
    questions = questions
)

/**
 * SDK-мастер: [steps] — текущий шаг (тест двигает его сам или через [onNext]/[onPrevious]), вопросы — [questions].
 */
internal class FakeFastMountRepository : FastMountRepository {
    var deviceClass: Response<MountDeviceClass> = Response.Success(MountDeviceClass.ALARM)
    var access: Response<UnitAccess> = Response.Success(UnitAccess(granted = true, message = ""))
    var latest: Response<String?> = Response.Success("3.57")
    var configuration: Response<FastMountConfiguration> = Response.Success(testConfiguration())
    var startResponse: Response<Unit> = Response.Success(Unit)
    var nextResponse: Response<Unit> = Response.Success(Unit)
    var feedbackResponse: Response<Unit> = Response.Success(Unit)
    var onNext: (FastMountStep?) -> FastMountStep? = { it }
    var onPrevious: (FastMountStep?) -> FastMountStep? = { it }
    val step = MutableStateFlow<FastMountStep?>(null)
    val questionsState = MutableStateFlow<QuestionsState?>(null)
    var classRequests = 0
    var nextCalls = 0
    var previousCalls = 0
    var skips = 0
    val answers = mutableListOf<QuestionAnswer>()
    var feedback: Pair<String, Int>? = null

    override suspend fun deviceClass(systemName: String): Response<MountDeviceClass> {
        classRequests++
        return deviceClass
    }

    override suspend fun verify(systemId: Long, systemName: String, firmwareVersion: String) = access
    override suspend fun latestFirmware(systemName: String) = latest
    override suspend fun configuration(id: Int) = configuration
    override suspend fun download(url: String): Response<ByteArray> = Response.Success(ByteArray(10))

    override suspend fun start(configurationId: Int): Response<Unit> {
        if (startResponse is Response.Success) step.value = FastMountStep.WRITING_SETTINGS
        return startResponse
    }

    override fun steps(): Flow<Response<FastMountStep>> = step.filterNotNull().map { Response.Success(it) }

    override suspend fun next(): Response<Unit> {
        nextCalls++
        if (nextResponse is Response.Success) step.value = onNext(step.value)
        return nextResponse
    }

    override suspend fun previous(): Response<Unit> {
        previousCalls++
        step.value = onPrevious(step.value)
        return Response.Success(Unit)
    }

    override fun questions(): Flow<Response<QuestionsState>> = questionsState.filterNotNull().map {
        Response.Success(it)
    }

    override suspend fun answer(answer: QuestionAnswer): Response<Unit> {
        answers += answer
        return Response.Success(Unit)
    }

    override suspend fun skip(): Response<Unit> {
        skips++
        return Response.Success(Unit)
    }

    override suspend fun feedback(text: String, rating: Int): Response<Unit> {
        feedback = text to rating
        return feedbackResponse
    }
}
