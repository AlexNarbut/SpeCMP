package ru.alarmtrade.specmp.feature.authstate.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.auth.domain.AuthState as SdkAuthState
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.errors.TokenIsExpired
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.authstate.data.datasource.AuthStateDataSource

class SpecialistAuthStateRepositoryTest {
    private var states: List<SpecialistResponse<SdkAuthState>> = emptyList()
    private var logoutResult: SpecialistResponse<Unit> = SpecialistResponse.Success(Unit)
    private var logoutCalls = 0
    private val repository = SpecialistAuthStateRepository(
        object : AuthStateDataSource {
            override fun authStates(): Flow<SpecialistResponse<SdkAuthState>> = flowOf(*states.toTypedArray())

            override suspend fun logout(): SpecialistResponse<Unit> = logoutResult.also { logoutCalls++ }
        },
    )

    @Test
    fun `session states map to logged in flags and waiting is skipped`() = runTest {
        states = listOf(
            SpecialistResponse.Success(SdkAuthState.Waiting),
            SpecialistResponse.Success(SdkAuthState.NoAuthed),
            SpecialistResponse.Success(SdkAuthState.Authed("token")),
        )

        assertEquals(
            listOf<Response<Boolean>>(Response.Success(false), Response.Success(true)),
            repository.observeIsLoggedIn().toList(),
        )
    }

    @Test
    fun `failed session check becomes a common error in the flow`() = runTest {
        states = listOf(SpecialistResponse.Error.Network(NetworkIsNotAvailableException()))

        val result = assertIs<Response.Error>(repository.observeIsLoggedIn().toList().single())

        assertEquals(ApiError.NoConnection, result.exception.appError)
    }

    @Test
    fun `logout goes to the SDK and keeps its error`() = runTest {
        assertEquals(Response.Success(Unit), repository.logout())

        val failure = TokenIsExpired("expired")
        logoutResult = SpecialistResponse.Error.General(failure)

        assertSame(failure, assertIs<Response.Error>(repository.logout()).exception)
        assertEquals(2, logoutCalls)
    }
}
