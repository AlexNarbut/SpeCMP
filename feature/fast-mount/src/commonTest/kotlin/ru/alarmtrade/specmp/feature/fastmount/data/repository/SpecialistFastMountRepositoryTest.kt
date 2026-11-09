package ru.alarmtrade.specmp.feature.fastmount.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmware
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmwareType
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceInformation
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceType
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemInformation
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemType
import ru.alarmtrade.specialist.domain.configuration.ConfigurationQuestion
import ru.alarmtrade.specialist.domain.configuration.ConfigurationQuestionAnswer
import ru.alarmtrade.specialist.domain.errors.FastMountSessionNotStartedException
import ru.alarmtrade.specialist.domain.fastMount.StateTitle
import ru.alarmtrade.specialist.domain.fastMount.questions.FastMountQuestionTestState
import ru.alarmtrade.specialist.domain.fastMount.questions.FastMountQuestionTestStatus
import ru.alarmtrade.specialist.domain.verification.VerifyDeviceState
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationRequest
import ru.alarmtrade.specialist.interactors.deviceVerification.request.VerifyDeviceRequest
import ru.alarmtrade.specialist.interactors.fastMount.requests.AddMountFeedbackRequest
import ru.alarmtrade.specialist.interactors.fastMount.requests.AnswerCurrentQuestionRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwareRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountException
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountFailure
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionAnswer
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState
import ru.alarmtrade.specmp.feature.fastmount.domain.model.UnitAccess

class SpecialistFastMountRepositoryTest {
    private val dataSource = FakeFastMountDataSource()
    private val repository = SpecialistFastMountRepository(dataSource)

    @Test
    fun `verify sends the unit and maps the access`() = runTest {
        dataSource.verification = SpecialistResponse.Success(VerifyDeviceState(false, "Нет доступа"))

        assertEquals(Response.Success(UnitAccess(false, "Нет доступа")), repository.verify(42, "DX-9X", "3.57"))
        assertEquals(listOf<Any>(VerifyDeviceRequest(42, "DX-9X", "3.57")), dataSource.calls)
    }

    @Test
    fun `latest firmware is asked for the main subsystem of the unit`() = runTest {
        dataSource.information = SpecialistResponse.Success(device(MAIN))
        dataSource.firmware = SpecialistResponse.Success(firmware("3.61"))

        assertEquals(Response.Success("3.61"), repository.latestFirmware("DX-9X", "C1", 4))
        assertEquals(
            listOf<Any>(
                "information DX-9X",
                SubsystemFirmwareRequest(7, SubsystemFirmwareType.RELEASE, canCode = "C1", canCodeVersion = 4),
            ),
            dataSource.calls,
        )
    }

    @Test
    fun `unit without main subsystem has no firmware and information error stops the request`() = runTest {
        dataSource.information = SpecialistResponse.Success(device(MICROBOT))
        assertEquals(Response.Success(null), repository.latestFirmware("DX-9X", null, null))

        val failure = IllegalStateException("db")
        dataSource.information = SpecialistResponse.Error.General(failure)
        assertSame(failure, assertIs<Response.Error>(repository.latestFirmware("DX-9X", null, null)).exception)
        assertEquals(listOf<Any>("information DX-9X", "information DX-9X"), dataSource.calls)
    }

    @Test
    fun `device class comes from the subsystems`() = runTest {
        dataSource.information = SpecialistResponse.Success(device(MICROBOT))

        assertEquals(Response.Success(MountDeviceClass.MICROBOT), repository.deviceClass("DX-9X"))
    }

    @Test
    fun `failed configuration does not start the mount`() = runTest {
        dataSource.configuration = SpecialistResponse.Error.General(IllegalStateException("404"))

        assertIs<Response.Error>(repository.start(5))
        assertEquals(listOf<Any>(ConfigurationRequest(5)), dataSource.calls)
    }

    @Test
    fun `steps skip finished states and sdk mount errors become fast mount failures`() = runTest {
        val notStarted = FastMountSessionNotStartedException()
        dataSource.states = flowOf(
            SpecialistResponse.Success(StateTitle.INITIAL_STATE),
            SpecialistResponse.Success(StateTitle.MOUNT_FINISHED),
            SpecialistResponse.Success(StateTitle.CLONE_STARTED),
            SpecialistResponse.Error.General(notStarted),
        )

        val steps = repository.steps().toList()

        assertEquals(
            listOf(FastMountStep.WRITING_SETTINGS, FastMountStep.CLONE),
            steps.take(2).map { assertIs<Response.Success<FastMountStep>>(it).value },
        )
        val error = assertIs<FastMountException>(assertIs<Response.Error>(steps[2]).exception)
        assertEquals(FastMountFailure.SessionLost, error.failure)
        assertSame(notStarted, error.cause)
    }

    @Test
    fun `broken steps flow and unavailable interactor end with an error`() = runTest {
        dataSource.states = flow { throw IllegalStateException("parse") }
        assertIs<Response.Error>(repository.steps().toList().single())

        dataSource.states = null
        assertIs<Response.Error>(repository.steps().toList().single())
        dataSource.questionStates = null
        assertIs<Response.Error>(repository.questions().toList().single())
    }

    @Test
    fun `questions map the current question and the empty test`() = runTest {
        val question = ConfigurationQuestion(5, 1, "Двери", 0, "", "Откройте дверь", "Блок видит", false, ANSWERS)
        dataSource.questionStates = flowOf(
            SpecialistResponse.Success(FastMountQuestionTestState(FastMountQuestionTestStatus.IN_PROCESS, question)),
            SpecialistResponse.Success(
                FastMountQuestionTestState(FastMountQuestionTestStatus.COMPLETED, ConfigurationQuestion.Empty),
            ),
        )

        val states = repository.questions().toList().map { assertIs<Response.Success<QuestionsState>>(it).value }

        assertEquals("Откройте дверь", states[0].current?.task)
        assertNull(states[1].current)
        assertEquals(true, states[1].completed)
    }

    @Test
    fun `commands reach the sdk and unavailable interactor is an error`() = runTest {
        assertEquals(Response.Success(Unit), repository.next())
        assertEquals(Response.Success(Unit), repository.previous())
        assertEquals(Response.Success(Unit), repository.answer(QuestionAnswer("Да", 1)))
        assertEquals(Response.Success(Unit), repository.skip())
        assertEquals(Response.Success(Unit), repository.feedback("Отлично", 5))
        assertEquals(
            listOf<Any>(
                "next",
                "previous",
                AnswerCurrentQuestionRequest(ANSWERS[0]),
                "skip",
                AddMountFeedbackRequest(feedback = "Отлично", rating = 5),
            ),
            dataSource.calls,
        )

        dataSource.command = null
        assertIs<Response.Error>(repository.next())
        assertIs<Response.Error>(repository.download("https://example.com/map.pdf"))
    }

    private fun device(vararg subsystems: SubsystemInformation) =
        DeviceInformation(1, "DX-9X", DeviceType.ALARM, subsystems.toList(), null)

    private fun firmware(version: String) = SubsystemFirmware(
        id = 3,
        type = SubsystemFirmwareType.RELEASE,
        version = version,
        creationTime = LocalDateTime(2026, 10, 14, 23, 50),
        description = "",
        url = "https://example.com/fw.bin",
        canCode = null,
        canVersion = null,
        canVersionString = null,
    )

    private companion object {
        val MAIN = SubsystemInformation("DX4G", "Pandora", 7, SubsystemType.ALARM_MAIN)
        val MICROBOT = SubsystemInformation("MB", "Microbot", 9, SubsystemType.MICROBOT_MAIN)
        val ANSWERS = listOf(ConfigurationQuestionAnswer("Да", 1), ConfigurationQuestionAnswer("Нет", 2))
    }
}
