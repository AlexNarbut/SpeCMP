package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.errors.IncorrectServicePinException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ProgramModeResult

class SpecialistProgramModeRepositoryTest {
    private val dataSource = FakeProgramModeDataSource()
    private val repository = SpecialistProgramModeRepository(dataSource)

    @Test
    fun `entering program mode succeeds or reports a wrong pin as an outcome`() = runTest {
        assertEquals(Response.Success(ProgramModeResult.ENTERED), repository.enter())

        dataSource.enter = SpecialistResponse.Error.General(IncorrectServicePinException())
        assertEquals(Response.Success(ProgramModeResult.WRONG_PIN), repository.enter())
    }

    @Test
    fun `other failures stay errors`() = runTest {
        val failure = IllegalStateException("no answer")
        dataSource.enter = SpecialistResponse.Error.General(failure)
        assertSame(failure, assertIs<Response.Error>(repository.enter()).exception)

        dataSource.exit = SpecialistResponse.Error.General(failure)
        assertSame(failure, assertIs<Response.Error>(repository.exit()).exception)
    }

    @Test
    fun `unavailable interactor is an error`() = runTest {
        dataSource.enter = null
        dataSource.exit = null

        assertIs<Response.Error>(repository.enter())
        assertIs<Response.Error>(repository.exit())
    }
}
