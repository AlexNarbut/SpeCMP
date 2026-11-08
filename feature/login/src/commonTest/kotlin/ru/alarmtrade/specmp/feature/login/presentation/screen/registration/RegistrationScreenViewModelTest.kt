package ru.alarmtrade.specmp.feature.login.presentation.screen.registration

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError
import ru.alarmtrade.specmp.feature.login.presentation.model.RegistrationFieldError
import ru.alarmtrade.specmp.feature.login.presentation.screen.FakeLoginRepository

class RegistrationScreenViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeLoginRepository()

    private fun TestScope.viewModel() =
        RegistrationScreenViewModel(repository, AppDispatchers(dispatcher, dispatcher, dispatcher))
            .also { vm -> backgroundScope.launch { vm.state.collect {} } }

    private fun RegistrationScreenViewModel.fillValidForm() {
        onFullNameChanged("Ivan Petrov")
        onEmailChanged("ivan@example.com")
        onPhoneNumberChanged("+7 900 000-00-00")
        onPasswordChanged("secret1")
        onInstallerConfirmedChanged(true)
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `invalid fields are marked before the request and editing clears the mark`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onEmailChanged("not-an-email")

        vm.onSubmit()

        assertEquals(RegistrationFieldError.entries.toSet(), vm.state.value.fieldErrors)
        assertTrue(repository.calls.isEmpty())

        vm.onEmailChanged("ivan@example.com")
        assertEquals(RegistrationFieldError.entries.toSet() - RegistrationFieldError.Email, vm.state.value.fieldErrors)
    }

    @Test
    fun `valid form signs up`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.fillValidForm()

        vm.onSubmit()

        assertEquals(1, repository.calls.size)
        assertTrue(vm.state.value.isSuccess)
    }

    @Test
    fun `failed sign up shows the error`() = runTest(dispatcher) {
        repository.signUpResponse = Response.Error(IllegalStateException())
        val vm = viewModel()
        vm.fillValidForm()

        vm.onSubmit()

        assertEquals(AuthFormError.Rejected, vm.state.value.registrationError)
    }
}
