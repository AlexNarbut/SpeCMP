package ru.alarmtrade.specmp.feature.authstate.data.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.InMemorySettings
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.authstate.data.repository.SettingsSessionMarkRepository
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.LogoutUseCase
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.RecordingAuthCloseable
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.ReleaseAccountStateUseCase

class AuthSessionControllerImplTest {

    private val settings = InMemorySettings()
    private val closeable = RecordingAuthCloseable()

    private inner class App(scope: TestScope, initial: Response<Boolean>, sdkEndsSessionOnLogout: Boolean = true) {
        val repository = FakeAuthStateRepository(initial, sdkEndsSessionOnLogout)
        private val marks = SettingsSessionMarkRepository(settings)
        private val release = ReleaseAccountStateUseCase(marks, listOf(closeable))
        val logout = LogoutUseCase(repository, release)
        val controller = AuthSessionControllerImpl(
            authStateRepository = repository,
            sessionMarkRepository = marks,
            releaseAccountState = release,
            scope = scope.backgroundScope,
            dispatchers = testDispatchers(UnconfinedTestDispatcher(scope.testScheduler)),
        )
    }

    private fun TestScope.launchApp(initial: Response<Boolean>, sdkEndsSessionOnLogout: Boolean = true): App =
        App(this, initial, sdkEndsSessionOnLogout).also { it.controller.start() }

    @Test
    fun `logout button releases once`() = runTest {
        val app = launchApp(Response.Success(true))

        app.logout()

        assertEquals(Response.Success(false), app.repository.loggedIn.value)
        assertEquals(1, closeable.releaseCalls)
    }

    @Test
    fun `logout button releases once when the session ends after logout returns`() = runTest {
        val app = launchApp(Response.Success(true), sdkEndsSessionOnLogout = false)

        app.logout()
        app.repository.loggedIn.value = Response.Success(false)

        assertEquals(1, closeable.releaseCalls)
    }

    @Test
    fun `session ended by itself releases once`() = runTest {
        val app = launchApp(Response.Success(true))

        app.repository.loggedIn.value = Response.Success(false)

        assertEquals(1, closeable.releaseCalls)
    }

    @Test
    fun `start without a session does not release`() = runTest {
        launchApp(Response.Success(false))

        assertEquals(0, closeable.releaseCalls)
    }

    @Test
    fun `failed session check does not release`() = runTest {
        val app = launchApp(Response.Success(true))

        app.repository.loggedIn.value = Response.Error(IllegalStateException("check failed"))

        assertEquals(0, closeable.releaseCalls)
    }

    @Test
    fun `login after logout and a new logout release again`() = runTest {
        val app = launchApp(Response.Success(true))
        app.logout()

        app.repository.loggedIn.value = Response.Success(true)
        app.logout()

        assertEquals(2, closeable.releaseCalls)
    }

    @Test
    fun `session that ended while the app was unloaded is released on start`() = runTest {
        launchApp(Response.Success(true))

        launchApp(Response.Success(false))

        assertEquals(1, closeable.releaseCalls)
    }

    @Test
    fun `restart with a live session does not release`() = runTest {
        launchApp(Response.Success(true))

        launchApp(Response.Success(true))

        assertEquals(0, closeable.releaseCalls)
    }

    @Test
    fun `repeated start does not subscribe twice`() = runTest {
        val app = launchApp(Response.Success(true))

        app.controller.start()

        assertEquals(1, app.repository.observeCalls)
    }

    class FakeAuthStateRepository(initial: Response<Boolean>, private val sdkEndsSessionOnLogout: Boolean) :
        AuthStateRepository {
        val loggedIn = MutableStateFlow(initial)
        var observeCalls = 0
            private set

        override fun observeIsLoggedIn(): Flow<Response<Boolean>> {
            observeCalls++
            return loggedIn
        }

        override suspend fun logout(): Response<Unit> {
            if (sdkEndsSessionOnLogout) loggedIn.value = Response.Success(false)
            return Response.Success(Unit)
        }
    }
}
