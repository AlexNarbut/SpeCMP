package ru.alarmtrade.specmp.feature.device.autostart.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.IntWrapper
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneCalculatingError
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneProcedureState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneProcedureStatus
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneStatus
import ru.alarmtrade.specialist.domain.errors.IncorrectServicePinException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneProcedureException
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneProcedureFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage

class SpecialistCloneRepositoryTest {
    private val dataSource = FakeAutostartDataSource()
    private val repository = SpecialistCloneRepository(dataSource)

    @Test
    fun `attempts come from the sdk and unavailable interactor is an error`() = runTest {
        dataSource.attempts = SpecialistResponse.Success(IntWrapper(3))
        assertEquals(Response.Success(3), repository.attempts())

        dataSource.attempts = null
        assertIs<Response.Error>(repository.attempts())
    }

    @Test
    fun `completed after writing is a fresh clone`() = runTest {
        dataSource.clone = flowOf(
            SpecialistResponse.Success(state(CloneProcedureStatus.CALCULATING_WORK)),
            SpecialistResponse.Success(state(CloneProcedureStatus.CALCULATED)),
            SpecialistResponse.Success(state(CloneProcedureStatus.COMPLETED, CloneStatus.COMPLETED)),
        )

        val events = repository.run().toList().map { assertIs<Response.Success<CloneEvent>>(it).value }

        assertEquals(
            listOf(
                CloneEvent.Stage(CloneStage.CALCULATING),
                CloneEvent.Stage(CloneStage.WRITING),
                CloneEvent.Finished(CloneResult.Completed(already = false)),
            ),
            events,
        )
    }

    @Test
    fun `completed without writing means the clone was already done`() = runTest {
        dataSource.clone = flowOf(SpecialistResponse.Success(state(CloneProcedureStatus.COMPLETED, CloneStatus.OK)))

        val event = assertIs<Response.Success<CloneEvent>>(repository.run().toList().single()).value

        assertEquals(CloneEvent.Finished(CloneResult.Completed(already = true)), event)
    }

    @Test
    fun `wrong pin in the middle ends the flow with a wrong pin failure`() = runTest {
        val wrongPin = IncorrectServicePinException()
        dataSource.clone = flowOf(
            SpecialistResponse.Success(state(CloneProcedureStatus.READING_FROM_ALARM)),
            SpecialistResponse.Error.General(wrongPin),
            SpecialistResponse.Success(state(CloneProcedureStatus.COMPLETED, CloneStatus.COMPLETED)),
        )

        val values = repository.run().toList()

        assertEquals(2, values.size)
        val error = assertIs<CloneProcedureException>(assertIs<Response.Error>(values[1]).exception)
        assertEquals(CloneProcedureFailure.WRONG_PIN, error.failure)
        assertSame(wrongPin, error.cause)
    }

    @Test
    fun `other sdk error is an unknown failure`() = runTest {
        dataSource.clone = flowOf(SpecialistResponse.Error.General(IllegalStateException("link lost")))

        val error = assertIs<Response.Error>(repository.run().toList().single())
        assertEquals(CloneProcedureFailure.UNKNOWN, assertIs<CloneProcedureException>(error.exception).failure)
    }

    @Test
    fun `unavailable interactor is an unknown failure`() = runTest {
        dataSource.clone = null

        val error = assertIs<Response.Error>(repository.run().toList().single())
        assertEquals(CloneProcedureFailure.UNKNOWN, assertIs<CloneProcedureException>(error.exception).failure)
    }

    private fun state(status: CloneProcedureStatus, result: CloneStatus = CloneStatus.REQUIRED) =
        CloneProcedureState(status, result, CloneCalculatingError.NO_ERROR, emptyList())
}
