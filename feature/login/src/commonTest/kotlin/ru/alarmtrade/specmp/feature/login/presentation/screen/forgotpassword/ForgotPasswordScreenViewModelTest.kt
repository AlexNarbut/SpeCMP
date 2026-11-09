package ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.login.domain.model.ResetPasswordCredentials
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError
import ru.alarmtrade.specmp.feature.login.presentation.screen.FakeLoginRepository

class ForgotPasswordScreenViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeLoginRepository()

    private fun TestScope.viewModel() = ForgotPasswordScreenViewModel(repository, testDispatchers(dispatcher))
        .also { vm -> backgroundScope.launch { vm.state.collect {} } }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `blank email is rejected before the request`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onEmailChanged("  ")

        vm.onSubmit()

        assertEquals(AuthFormError.RequiredFields, vm.state.value.resetError)
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun `reset request succeeds`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onEmailChanged("ivan@example.com")

        vm.onSubmit()

        assertEquals(listOf<Any>(ResetPasswordCredentials("ivan@example.com")), repository.calls)
        assertTrue(vm.state.value.isSuccess)
    }

    @Test
    fun `failed reset shows an error that editing dismisses`() = runTest(dispatcher) {
        repository.resetPasswordResponse = Response.Error(IllegalStateException())
        val vm = viewModel()
        vm.onEmailChanged("ivan@example.com")

        vm.onSubmit()
        assertEquals(AuthFormError.Rejected, vm.state.value.resetError)

        vm.onEmailChanged("petr@example.com")
        assertNull(vm.state.value.resetError)
    }
}
