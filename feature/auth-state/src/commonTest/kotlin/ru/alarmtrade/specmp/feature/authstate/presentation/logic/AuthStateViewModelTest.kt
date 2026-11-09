package ru.alarmtrade.specmp.feature.authstate.presentation.logic

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository

class AuthStateViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeAuthStateRepository()

    private fun TestScope.viewModel() = AuthStateViewModel(repository, testDispatchers(dispatcher))
        .also { vm -> backgroundScope.launch { vm.state.collect {} } }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `each login opens a new session`() = runTest(dispatcher) {
        val loggedIn = MutableStateFlow<Response<Boolean>>(Response.Success(true))
        repository.flows += loggedIn
        val vm = viewModel()
        assertEquals(AuthStateScreenState(AuthState.AUTHED, session = 1), vm.state.value)

        loggedIn.value = Response.Success(false)
        assertEquals(AuthStateScreenState(AuthState.NO_AUTH, session = 1), vm.state.value)

        loggedIn.value = Response.Success(true)
        assertEquals(AuthStateScreenState(AuthState.AUTHED, session = 2), vm.state.value)
    }

    @Test
    fun `failed check is retried`() = runTest(dispatcher) {
        repository.flows += flowOf(Response.Error(IllegalStateException()))
        repository.flows += flowOf(Response.Success(true))
        val vm = viewModel()
        assertEquals(AuthState.CHECK_FAILED, vm.state.value.state)

        vm.onRetry()

        assertEquals(AuthStateScreenState(AuthState.AUTHED, session = 1), vm.state.value)
    }

    @Test
    fun `resubscription keeps the session and the running check`() = runTest(dispatcher) {
        repository.flows += MutableStateFlow(Response.Success(true))
        val vm = AuthStateViewModel(repository, testDispatchers(dispatcher))
        val subscription = backgroundScope.launch { vm.state.collect {} }
        subscription.cancel()
        advanceTimeBy(10.seconds)

        backgroundScope.launch { vm.state.collect {} }

        assertEquals(AuthStateScreenState(AuthState.AUTHED, session = 1), vm.state.value)
        assertEquals(1, repository.observeCalls)
    }

    private class FakeAuthStateRepository : AuthStateRepository {
        val flows = ArrayDeque<Flow<Response<Boolean>>>()
        var observeCalls = 0
            private set

        override fun observeIsLoggedIn(): Flow<Response<Boolean>> {
            observeCalls++
            return flows.removeFirst()
        }

        override suspend fun logout(): Response<Unit> = Response.Success(Unit)
    }
}
