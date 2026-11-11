package ru.alarmtrade.specmp.feature.login.presentation

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthBody
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthLayoutMode
import ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword.ForgotPasswordForm
import ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword.ForgotPasswordScreenState
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreen
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreenState
import ru.alarmtrade.specmp.feature.login.presentation.screen.registration.RegistrationForm
import ru.alarmtrade.specmp.feature.login.presentation.screen.registration.RegistrationScreenState

@OptIn(ExperimentalTestApi::class)
class AuthFormKeyboardTest {

    @Test
    fun `login next moves focus to password`() = runComposeUiTest {
        setLoginContent(LoginScreenState()) {}

        field(0).requestFocus()
        field(0).performImeAction()

        field(1).assertIsFocused()
    }

    @Test
    fun `login go on password submits like the button`() = runComposeUiTest {
        var submits = 0
        setLoginContent(LoginScreenState(login = "user@example.com", password = "secret")) { submits++ }

        field(1).requestFocus()
        field(1).performImeAction()

        assertEquals(1, submits)
        field(1).assertIsNotFocused()
    }

    @Test
    fun `login go does nothing while the button is disabled`() = runComposeUiTest {
        var submits = 0
        setLoginContent(LoginScreenState(login = "user@example.com")) { submits++ }

        field(1).requestFocus()
        field(1).performImeAction()

        assertEquals(0, submits)
    }

    @Test
    fun `forgot password go submits like the button`() = runComposeUiTest {
        var submits = 0
        setForgotPasswordContent(ForgotPasswordScreenState(email = "user@example.com")) { submits++ }

        field(0).requestFocus()
        field(0).performImeAction()

        assertEquals(1, submits)
    }

    @Test
    fun `forgot password go does nothing while the button is disabled`() = runComposeUiTest {
        var submits = 0
        setForgotPasswordContent(ForgotPasswordScreenState()) { submits++ }

        field(0).requestFocus()
        field(0).performImeAction()

        assertEquals(0, submits)
    }

    @Test
    fun `registration next walks the fields and done on the last one hides the keyboard without submit`() =
        runComposeUiTest {
            var submits = 0
            setRegistrationContent(filledRegistration) { submits++ }

            field(0).requestFocus()
            repeat(REGISTRATION_FIELDS - 1) { index ->
                field(index).performImeAction()
                field(index + 1).assertIsFocused()
            }
            field(REGISTRATION_FIELDS - 1).performImeAction()

            field(REGISTRATION_FIELDS - 1).assertIsNotFocused()
            assertEquals(0, submits)
        }

    @Test
    fun `focusing the last registration field scrolls the consent checkbox into view`() = runComposeUiTest {
        val confirmations = mutableListOf<Boolean>()
        setRegistrationContent(RegistrationScreenState(), onInstallerConfirmedChange = { confirmations += it }) {}

        onNode(isToggleable()).assertIsNotDisplayed()

        field(REGISTRATION_FIELDS - 1).requestFocus()
        waitForIdle()

        onNode(isToggleable()).assertIsDisplayed().performClick()
        assertEquals(listOf(true), confirmations)
    }

    private fun ComposeUiTest.field(index: Int): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[index]

    private fun ComposeUiTest.setLoginContent(state: LoginScreenState, onSubmit: () -> Unit) {
        setContent {
            LoginScreen(
                state = state,
                colors = cmpThemeColors(),
                layoutMode = AuthLayoutMode.Compact,
                onLoginChange = {},
                onPasswordChange = {},
                onPasswordVisibilityChange = {},
                onSubmit = onSubmit,
                onOpenRegistration = {},
                onOpenForgotPassword = {},
                onResendActivationEmail = {},
                onDismissActivationRequest = {},
            )
        }
    }

    private fun ComposeUiTest.setForgotPasswordContent(state: ForgotPasswordScreenState, onSubmit: () -> Unit) {
        setContent {
            ForgotPasswordForm(
                state = mutableStateOf(state),
                colors = cmpThemeColors(),
                onEmailChange = {},
                onSubmit = onSubmit,
                onBackToLogin = {},
            )
        }
    }

    private fun ComposeUiTest.setRegistrationContent(
        state: RegistrationScreenState,
        onInstallerConfirmedChange: (Boolean) -> Unit = {},
        onSubmit: () -> Unit,
    ) {
        setContent {
            AuthBody(
                layoutMode = AuthLayoutMode.Compact,
                modifier = Modifier.size(width = 360.dp, height = 320.dp),
                showCompactBrand = false,
            ) {
                RegistrationForm(
                    state = mutableStateOf(state),
                    colors = cmpThemeColors(),
                    onFullNameChange = {},
                    onEmailChange = {},
                    onPhoneNumberChange = {},
                    onPasswordChange = {},
                    onPasswordVisibilityChange = {},
                    onInstallerConfirmedChange = onInstallerConfirmedChange,
                    onSubmit = onSubmit,
                    onBackToLogin = {},
                )
            }
        }
    }

    private companion object {
        const val REGISTRATION_FIELDS = 4

        val filledRegistration = RegistrationScreenState(
            fullName = "Ivan Ivanov",
            email = "ivan@example.com",
            phoneNumber = "+79990000000",
            password = "secret123",
            isInstallerConfirmed = true,
        )
    }
}
