package ru.alarmtrade.specmp.feature.login.presentation.screen.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthBrandHeader
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthErrorMessage
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthPasswordField
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.subcomponents.ActivationRequiredDialog
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_common_error_auth_failed
import specmp.feature.login.generated.resources.login_common_label_email
import specmp.feature.login.generated.resources.login_login_btn_submit
import specmp.feature.login.generated.resources.login_login_link_forgot_password
import specmp.feature.login.generated.resources.login_login_link_registration

@Composable
fun LoginScreen(
    state: LoginScreenState,
    colors: CmpThemeColors,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    onSubmit: () -> Unit,
    onOpenRegistration: () -> Unit,
    onOpenForgotPassword: () -> Unit,
    onResendActivationEmail: () -> Unit,
    onDismissActivationRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CmpColumn(
        modifier = modifier.widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AuthBrandHeader(
            colors = colors,
            modifier = Modifier.padding(bottom = 10.dp),
        )

        CmpTextField(
            value = state.login,
            onValueChange = onLoginChanged,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.login_common_label_email),
            singleLine = true,
            enabled = !state.isAuthLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )

        AuthPasswordField(
            value = state.password,
            onValueChange = onPasswordChanged,
            isPasswordVisible = state.isPasswordVisible,
            onPasswordVisibilityChanged = onPasswordVisibilityChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isAuthLoading,
            keyboardActions = KeyboardActions(
                onDone = {
                    if (state.canSubmit) onSubmit()
                },
            ),
        )

        CmpTextButton(
            text = stringResource(Res.string.login_login_link_forgot_password),
            onClick = onOpenForgotPassword,
            enabled = !state.isAuthLoading,
        )

        if (state.authState is LCEState.Error) {
            AuthErrorMessage(
                text = stringResource(Res.string.login_common_error_auth_failed),
                colors = colors,
            )
        }

        CmpButton(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = state.canSubmit,
        ) {
            if (state.isAuthLoading) {
                CmpCircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = colors.onPrimary,
                )
            } else {
                CmpText(text = stringResource(Res.string.login_login_btn_submit))
            }
        }

        CmpTextButton(
            text = stringResource(Res.string.login_login_link_registration),
            onClick = onOpenRegistration,
            enabled = !state.isAuthLoading,
        )
    }

    if (state.isActivationRequired) {
        ActivationRequiredDialog(
            state = state,
            onResendActivationEmail = onResendActivationEmail,
            onDismissActivationRequest = onDismissActivationRequest,
        )
    }
}
