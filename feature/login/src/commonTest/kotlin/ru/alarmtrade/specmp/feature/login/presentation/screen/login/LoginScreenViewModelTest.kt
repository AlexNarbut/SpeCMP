package ru.alarmtrade.specmp.feature.login.presentation.screen.login

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError
import ru.alarmtrade.specmp.feature.login.presentation.screen.FakeLoginRepository

class LoginScreenViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeLoginRepository()

    private fun TestScope.viewModel() =
        LoginScreenViewModel(repository, AppDispatchers(dispatcher, dispatcher, dispatcher))
            .also { vm -> backgroundScope.launch { vm.state.collect {} } }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `empty form is rejected before the request`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onSubmit()

        assertEquals(AuthFormError.RequiredFields, vm.state.value.authError)
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun `successful login resets the form`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onLoginChanged(" user ")
        vm.onPasswordChanged("secret")

        vm.onSubmit()

        assertEquals(listOf<Any>(LoginCredentials(" user ", "secret")), repository.calls)
        assertEquals(LoginScreenState(), vm.state.value)
    }

    @Test
    fun `rejected login shows an error that editing dismisses`() = runTest(dispatcher) {
        repository.loginResponse = Response.Error(IllegalStateException())
        val vm = viewModel()
        vm.onLoginChanged("user")
        vm.onPasswordChanged("wrong")

        vm.onSubmit()
        assertTrue(vm.state.value.isAuthError)

        vm.onPasswordChanged("right")
        assertFalse(vm.state.value.isAuthError)
    }

    @Test
    fun `activation request resends the email and can be dismissed`() = runTest(dispatcher) {
        repository.loginResponse = Response.Success(LoginResult.ActivationRequired)
        val vm = viewModel()
        vm.onLoginChanged("user")
        vm.onPasswordChanged("secret")

        vm.onSubmit()
        assertTrue(vm.state.value.isActivationRequired)

        vm.onResendActivationEmail()
        assertIs<LCEState.Content<Unit, AuthFormError>>(vm.state.value.activationResendState)
        assertEquals(FakeLoginRepository.RESEND to LoginCredentials("user", "secret"), repository.calls.last())

        vm.onDismissActivationRequest()
        assertFalse(vm.state.value.isActivationRequired)
        assertEquals(LCEState.None, vm.state.value.activationResendState)
    }

    @Test
    fun `reset clears the form`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onLoginChanged("user")
        vm.onPasswordVisibilityChanged()

        vm.onResetForm()

        assertEquals(LoginScreenState(), vm.state.value)
    }
}
