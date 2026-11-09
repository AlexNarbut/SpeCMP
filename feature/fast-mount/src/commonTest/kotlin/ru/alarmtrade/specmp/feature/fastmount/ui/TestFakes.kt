package ru.alarmtrade.specmp.feature.fastmount.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountFile
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountQuestion
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionAnswer
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState
import ru.alarmtrade.specmp.feature.fastmount.domain.model.UnitAccess
import ru.alarmtrade.specmp.feature.fastmount.domain.repository.FastMountRepository

internal fun testDevice(
    systemId: Long = 77,
    modelInDatabase: Boolean = true,
    firmware: String = "VER3.57",
    can: CanInfo? = CanInfo(firmwareVersion = "1.2", code = "0213", isFactory = false),
) = AlarmDevice(
    systemId = systemId,
    systemName = "DX-9X",
    firmwareVersion = firmware,
    mode = DeviceOperationMode.MAIN,
    features = emptySet(),
    can = can,
    modelInDatabase = modelInDatabase,
)

internal fun question(id: Int, automatic: Boolean = false) = MountQuestion(
    id = id,
    group = "",
    task = "Задание $id",
    expected = "",
    automatic = automatic,
    answers = if (automatic) emptyList() else listOf(QuestionAnswer("Да", 1), QuestionAnswer("Нет", 2)),
)

internal fun testConfiguration(
    keyBypass: Boolean = true,
    slave: Boolean = true,
    mountingMap: MountFile? = null,
    questions: List<MountQuestion> = listOf(question(5, automatic = true), question(100), question(101)),
) = FastMountConfiguration(
    id = 42,
    name = "Polo VI",
    shortDescription = "",
    fullDescription = "<p>Описание</p>",
    deviceName = "DX-9X",
    author = "Автор",
    rating = 5,
    mountingMap = mountingMap,
    images = emptyList(),
    keyBypass = keyBypass,
    slave = slave,
    questions = questions,
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
    var answerResponse: Response<Unit> = Response.Success(Unit)
    var downloadResponse: Response<ByteArray> = Response.Success(ByteArray(10))
    var onNext: (FastMountStep?) -> FastMountStep? = { it }
    var onPrevious: (FastMountStep?) -> FastMountStep? = { it }
    val step = MutableStateFlow<FastMountStep?>(null)
    val questionsState = MutableStateFlow<QuestionsState?>(null)

    /** Сколько «идёт» вызов SDK (вперёд/назад/ответ) — для проверки повторных нажатий во время вызова. */
    var callMillis = 0L
    var classRequests = 0
    val configurationRequests = mutableListOf<Int>()
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
    override suspend fun latestFirmware(systemName: String, canCode: String?, canVersion: Int?) = latest
    override suspend fun configuration(id: Int): Response<FastMountConfiguration> {
        configurationRequests += id
        return configuration
    }

    override suspend fun download(url: String): Response<ByteArray> = downloadResponse

    override suspend fun start(configurationId: Int): Response<Unit> {
        if (startResponse is Response.Success) step.value = FastMountStep.WRITING_SETTINGS
        return startResponse
    }

    override fun steps(): Flow<Response<FastMountStep>> = step.filterNotNull().map { Response.Success(it) }

    override suspend fun next(): Response<Unit> {
        nextCalls++
        delay(callMillis)
        if (nextResponse is Response.Success) step.value = onNext(step.value)
        return nextResponse
    }

    override suspend fun previous(): Response<Unit> {
        previousCalls++
        delay(callMillis)
        step.value = onPrevious(step.value)
        return Response.Success(Unit)
    }

    override fun questions(): Flow<Response<QuestionsState>> = questionsState.filterNotNull().map {
        Response.Success(it)
    }

    override suspend fun answer(answer: QuestionAnswer): Response<Unit> {
        answers += answer
        delay(callMillis)
        return answerResponse
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
