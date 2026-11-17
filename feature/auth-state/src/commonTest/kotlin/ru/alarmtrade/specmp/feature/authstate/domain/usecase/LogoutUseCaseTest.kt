package ru.alarmtrade.specmp.feature.authstate.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.InMemorySettings
import ru.alarmtrade.specmp.feature.authstate.data.repository.SettingsSessionMarkRepository
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository

class LogoutUseCaseTest {

    private val marks = SettingsSessionMarkRepository(InMemorySettings())

    private suspend fun logoutUseCase(logoutResponse: Response<Unit>, closeables: List<AuthCloseable>): LogoutUseCase {
        marks.mark()
        return LogoutUseCase(FakeAuthStateRepository(logoutResponse), ReleaseAccountStateUseCase(marks, closeables))
    }

    @Test
    fun `successful logout releases all closeables`() = runTest {
        val closeables = listOf(RecordingAuthCloseable(), RecordingAuthCloseable())
        val useCase = logoutUseCase(Response.Success(Unit), closeables)

        val result = useCase()

        assertIs<Response.Success<Unit>>(result)
        assertTrue(closeables.all { it.releaseCalls == 1 })
        assertEquals(Response.Success(false), marks.isMarked())
    }

    @Test
    fun `failed logout does not release closeables`() = runTest {
        val closeable = RecordingAuthCloseable()
        val useCase = logoutUseCase(Response.Error(IllegalStateException("logout failed")), listOf(closeable))

        val result = useCase()

        assertIs<Response.Error>(result)
        assertEquals(0, closeable.releaseCalls)
        assertEquals(Response.Success(true), marks.isMarked())
    }

    @Test
    fun `release failure does not fail logout or skip other closeables`() = runTest {
        val failing = object : AuthCloseable {
            override suspend fun release() {
                error("release failed")
            }
        }
        val next = RecordingAuthCloseable()
        val useCase = logoutUseCase(Response.Success(Unit), listOf(failing, next))

        val result = useCase()

        assertIs<Response.Success<Unit>>(result)
        assertEquals(1, next.releaseCalls)
    }

    @Test
    fun `second logout of the same session does not release again`() = runTest {
        val closeable = RecordingAuthCloseable()
        val useCase = logoutUseCase(Response.Success(Unit), listOf(closeable))

        useCase()
        useCase()

        assertEquals(1, closeable.releaseCalls)
    }

    private class FakeAuthStateRepository(private val logoutResponse: Response<Unit>) : AuthStateRepository {
        override fun observeIsLoggedIn() = throw NotImplementedError()

        override suspend fun logout(): Response<Unit> = logoutResponse
    }
}

internal class RecordingAuthCloseable : AuthCloseable {
    var releaseCalls = 0
        private set

    override suspend fun release() {
        releaseCalls += 1
    }
}
