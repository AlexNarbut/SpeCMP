package ru.alarmtrade.specmp.feature.device.autostart.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.BypassLearningStatus
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneBypassState
import ru.alarmtrade.specialist.domain.alarmDevice.bypass.CloneStatus
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.AutostartState
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneCondition

class SpecialistAutostartStatusRepositoryTest {
    private val dataSource = FakeAutostartDataSource()
    private val repository = SpecialistAutostartStatusRepository(dataSource)

    private fun state(bypass: BypassLearningStatus, clone: CloneStatus) =
        SpecialistResponse.Success(CloneBypassState(clone, BypassLearningState(bypass)))

    @Test
    fun `finished learning counts as learned`() = runTest {
        dataSource.state = state(BypassLearningStatus.FINISH_SUCCESS, CloneStatus.OK)
        assertEquals(Response.Success(AutostartState(true, CloneCondition.COMPLETED)), repository.status())

        dataSource.state = state(BypassLearningStatus.FINISH_CLONE_REQUIRED, CloneStatus.REQUIRED)
        assertEquals(Response.Success(AutostartState(true, CloneCondition.REQUIRED)), repository.status())
    }

    @Test
    fun `other learning states are not learned, clone errors are failed`() = runTest {
        dataSource.state = state(BypassLearningStatus.FINISH_ERROR, CloneStatus.ERROR_DATA)
        assertEquals(Response.Success(AutostartState(false, CloneCondition.FAILED)), repository.status())

        dataSource.state = state(BypassLearningStatus.WAIT, CloneStatus.NOT_SUPPORTED)
        assertEquals(Response.Success(AutostartState(false, CloneCondition.NOT_SUPPORTED)), repository.status())
    }

    @Test
    fun `sdk error and unavailable interactor are errors`() = runTest {
        val failure = IllegalStateException("link")
        dataSource.state = SpecialistResponse.Error.General(failure)
        assertSame(failure, assertIs<Response.Error>(repository.status()).exception)

        dataSource.state = null
        assertIs<Response.Error>(repository.status())
    }
}
