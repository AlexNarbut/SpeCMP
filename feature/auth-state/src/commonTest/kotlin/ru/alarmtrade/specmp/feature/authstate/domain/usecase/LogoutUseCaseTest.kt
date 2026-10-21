package ru.alarmtrade.specmp.feature.authstate.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository

class LogoutUseCaseTest {

    @Test
    fun `successful logout releases all closeables`() = runTest {
        val closeables = listOf(RecordingAuthCloseable(), RecordingAuthCloseable())
        val useCase = LogoutUseCase(FakeAuthStateRepository(Response.Success(Unit)), closeables)

        val result = useCase.execute()

        assertIs<Response.Success<Unit>>(result)
        assertTrue(closeables.all { it.releaseCalls == 1 })
    }

    @Test
    fun `failed logout does not release closeables`() = runTest {
        val closeable = RecordingAuthCloseable()
        val useCase = LogoutUseCase(
            FakeAuthStateRepository(Response.Error(IllegalStateException("logout failed"))),
            listOf(closeable)
        )

        val result = useCase.execute()

        assertIs<Response.Error>(result)
        assertEquals(0, closeable.releaseCalls)
    }

    @Test
    fun `release failure does not fail logout`() = runTest {
        val failing = object : AuthCloseable {
            override suspend fun release() {
                error("release failed")
            }
        }
        val useCase = LogoutUseCase(FakeAuthStateRepository(Response.Success(Unit)), listOf(failing))

        val result = useCase.execute()

        assertIs<Response.Success<Unit>>(result)
    }

    private class RecordingAuthCloseable : AuthCloseable {
        var releaseCalls = 0
            private set

        override suspend fun release() {
            releaseCalls += 1
        }
    }

    private class FakeAuthStateRepository(private val logoutResponse: Response<Unit>) : AuthStateRepository {
        override fun observeIsLoggedIn() = throw NotImplementedError()

        override suspend fun logout(): Response<Unit> = logoutResponse
    }
}
