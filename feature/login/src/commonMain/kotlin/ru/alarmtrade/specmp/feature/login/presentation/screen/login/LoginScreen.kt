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
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.login_common_label_email
import ru.alarmtrade.specmp.core.localization.resources.login_common_loading_auth
import ru.alarmtrade.specmp.core.localization.resources.login_login_btn_submit
import ru.alarmtrade.specmp.core.localization.resources.login_login_link_forgot_password
import ru.alarmtrade.specmp.core.localization.resources.login_login_link_signup
import ru.alarmtrade.specmp.core.localization.resources.login_login_msg_no_account
import ru.alarmtrade.specmp.core.localization.resources.login_login_title_screen
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthLayoutMode
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthPasswordField
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthSubmitButton
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.subcomponents.ActivationRequiredDialog

@Composable
fun LoginScreen(
    state: LoginScreenState,
    colors: CmpThemeColors,
    layoutMode: AuthLayoutMode,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
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
                text = stringResource(Strings.string.login_login_title_screen),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
            )
        }

        CmpTextField(
            value = state.login,
            onValueChange = onLoginChange,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Strings.string.login_common_label_email),
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
            onValueChange = onPasswordChange,
            isPasswordVisible = state.isPasswordVisible,
            onPasswordVisibilityChange = onPasswordVisibilityChange,
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
            text = stringResource(Strings.string.login_login_link_forgot_password),
            onClick = onOpenForgotPassword,
            modifier = Modifier.align(Alignment.End),
            enabled = !state.isAuthLoading,
        )

        AuthSubmitButton(
            text = if (state.isAuthLoading) {
                stringResource(Strings.string.login_common_loading_auth)
            } else {
                stringResource(Strings.string.login_login_btn_submit)
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
                text = stringResource(Strings.string.login_login_msg_no_account),
                color = colors.onSurfaceVariant,
            )
            CmpTextButton(
                text = stringResource(Strings.string.login_login_link_signup),
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
