package ru.alarmtrade.specmp.feature.device.autostart.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningError
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningStatus
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassLearningException
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult

class SpecialistBypassLearningRepositoryTest {
    private val dataSource = FakeAutostartDataSource()
    private val repository = SpecialistBypassLearningRepository(dataSource)

    @Test
    fun `learning maps sdk states to steps and the result`() = runTest {
        dataSource.bypass = flowOf(
            SpecialistResponse.Success(state(BypassLearningStatus.OPEN_HOOD_REQUEST)),
            SpecialistResponse.Success(state(BypassLearningStatus.OPEN_HOOD_SUCCESS)),
            SpecialistResponse.Success(state(BypassLearningStatus.FINISH_SUCCESS)),
        )

        val events = repository.learn().toList().map { assertIs<Response.Success<BypassEvent>>(it).value }

        assertEquals(3, events.size)
        assertIs<BypassEvent.Step>(events[0])
        assertIs<BypassEvent.Step>(events[1])
        assertIs<BypassResult.Success>(assertIs<BypassEvent.Finished>(events[2]).result)
    }

    @Test
    fun `sdk error in the middle ends the flow with a learning exception`() = runTest {
        val failure = IllegalStateException("link lost")
        dataSource.bypass = flowOf(
            SpecialistResponse.Success(state(BypassLearningStatus.OPEN_HOOD_REQUEST)),
            SpecialistResponse.Error.General(failure),
            SpecialistResponse.Success(state(BypassLearningStatus.FINISH_SUCCESS)),
        )

        val values = repository.learn().toList()

        assertEquals(2, values.size)
        assertIs<Response.Success<BypassEvent>>(values[0])
        val error = assertIs<BypassLearningException>(assertIs<Response.Error>(values[1]).exception)
        assertSame(failure, error.cause)
    }

    @Test
    fun `broken sdk flow ends with a learning exception`() = runTest {
        dataSource.bypass = flow { throw IllegalStateException("parse") }

        assertIs<BypassLearningException>(assertIs<Response.Error>(repository.learn().toList().single()).exception)
    }

    @Test
    fun `unavailable interactor is a learning exception without cause`() = runTest {
        dataSource.bypass = null

        val error = assertIs<Response.Error>(repository.learn().toList().single())
        assertNull(assertIs<BypassLearningException>(error.exception).cause)
    }

    private fun state(status: BypassLearningStatus) = BypassLearningState(status, BypassLearningError.NO_ERROR, 0)
}
