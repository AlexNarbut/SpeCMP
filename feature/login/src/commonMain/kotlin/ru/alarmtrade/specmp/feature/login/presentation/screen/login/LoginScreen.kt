package ru.alarmtrade.specmp.feature.login.presentation.screen.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthErrorBanner
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthLayoutMode
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthPasswordField
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthSubmitButton
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.subcomponents.ActivationRequiredDialog
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_common_error_auth_failed
import specmp.feature.login.generated.resources.login_common_label_email
import specmp.feature.login.generated.resources.login_common_loading_auth
import specmp.feature.login.generated.resources.login_login_btn_submit
import specmp.feature.login.generated.resources.login_login_link_forgot_password
import specmp.feature.login.generated.resources.login_login_link_signup
import specmp.feature.login.generated.resources.login_login_msg_no_account
import specmp.feature.login.generated.resources.login_login_title_screen

@Composable
fun LoginScreen(
    state: LoginScreenState,
    colors: CmpThemeColors,
    layoutMode: AuthLayoutMode,
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
    val isAuthError = state.isAuthError

    CmpColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (layoutMode == AuthLayoutMode.Expanded) {
            CmpText(
                text = stringResource(Res.string.login_login_title_screen),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
            )
        }

        if (isAuthError) {
            AuthErrorBanner(
                text = stringResource(Res.string.login_common_error_auth_failed),
                colors = colors,
            )
        }

        CmpTextField(
            value = state.login,
            onValueChange = onLoginChanged,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.login_common_label_email),
            singleLine = true,
            enabled = !state.isAuthLoading,
            isError = isAuthError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            leadingIcon = {
                CmpIcon(
                    icon = CmpIcons.Email,
                    contentDescription = null,
                )
            },
        )

        AuthPasswordField(
            value = state.password,
            onValueChange = onPasswordChanged,
            isPasswordVisible = state.isPasswordVisible,
            onPasswordVisibilityChanged = onPasswordVisibilityChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isAuthLoading,
            isError = isAuthError,
            keyboardActions = KeyboardActions(
                onDone = {
                    if (state.canSubmit) onSubmit()
                },
            ),
        )

        CmpTextButton(
            text = stringResource(Res.string.login_login_link_forgot_password),
            onClick = onOpenForgotPassword,
            modifier = Modifier.align(Alignment.End),
            enabled = !state.isAuthLoading,
        )

        AuthSubmitButton(
            text = if (state.isAuthLoading) {
                stringResource(Res.string.login_common_loading_auth)
            } else {
                stringResource(Res.string.login_login_btn_submit)
            },
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = state.canSubmit,
            isLoading = state.isAuthLoading,
            colors = colors,
        )

        CmpRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpText(
                text = stringResource(Res.string.login_login_msg_no_account),
                color = colors.onSurfaceVariant,
            )
            CmpTextButton(
                text = stringResource(Res.string.login_login_link_signup),
                onClick = onOpenRegistration,
                enabled = !state.isAuthLoading,
            )
        }
    }

    if (state.isActivationRequired) {
        ActivationRequiredDialog(
            state = state,
            onResendActivationEmail = onResendActivationEmail,
            onDismissActivationRequest = onDismissActivationRequest,
        )
    }
}
