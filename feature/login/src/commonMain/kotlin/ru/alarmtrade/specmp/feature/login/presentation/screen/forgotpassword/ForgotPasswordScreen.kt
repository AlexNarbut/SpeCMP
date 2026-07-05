package ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthErrorBanner
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthSubmitButton
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthSuccessPanel
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_common_btn_go_to_login
import specmp.feature.login.generated.resources.login_common_error_required_fields
import specmp.feature.login.generated.resources.login_common_error_reset_password_failed
import specmp.feature.login.generated.resources.login_common_label_email
import specmp.feature.login.generated.resources.login_forgot_btn_submit
import specmp.feature.login.generated.resources.login_forgot_link_back_to_login
import specmp.feature.login.generated.resources.login_forgot_msg_success
import specmp.feature.login.generated.resources.login_forgot_subtitle_screen
import specmp.feature.login.generated.resources.login_forgot_title_success

@Composable
fun ForgotPasswordScreen(
    colors: CmpThemeColors,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<ForgotPasswordScreenViewModel>()
    val state = viewModel.state.collectAsState()

    DisposableEffect(viewModel) {
        onDispose(viewModel::reset)
    }

    if (state.value.isSuccess) {
        AuthSuccessPanel(
            title = stringResource(Res.string.login_forgot_title_success),
            text = stringResource(Res.string.login_forgot_msg_success),
            modifier = modifier,
            icon = CmpIcons.Email,
            colors = colors,
            primaryAction = {
                AuthSubmitButton(
                    text = stringResource(Res.string.login_common_btn_go_to_login),
                    onClick = onBackToLogin,
                    colors = colors,
                )
            },
        )
    } else {
        ForgotPasswordForm(
            state = state,
            colors = colors,
            onEmailChanged = viewModel::onEmailChanged,
            onSubmit = viewModel::submit,
            onBackToLogin = onBackToLogin,
            modifier = modifier,
        )
    }
}

@Composable
private fun ForgotPasswordForm(
    state: State<ForgotPasswordScreenState>,
    colors: CmpThemeColors,
    onEmailChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val value = state.value

    CmpColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CmpText(
            text = stringResource(Res.string.login_forgot_subtitle_screen),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            color = colors.onSurfaceVariant,
        )

        if (value.resetError != null) {
            AuthErrorBanner(
                text = when (value.resetError) {
                    AuthFormError.RequiredFields ->
                        stringResource(Res.string.login_common_error_required_fields)
                    AuthFormError.RequestFailed ->
                        stringResource(Res.string.login_common_error_reset_password_failed)
                },
                colors = colors,
            )
        }

        CmpTextField(
            value = value.email,
            onValueChange = onEmailChanged,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.login_common_label_email),
            singleLine = true,
            enabled = !value.isLoading,
            isError = value.resetError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (value.canSubmit) onSubmit()
                },
            ),
            leadingIcon = {
                CmpIcon(icon = CmpIcons.Email, contentDescription = null)
            },
        )

        AuthSubmitButton(
            text = stringResource(Res.string.login_forgot_btn_submit),
            onClick = onSubmit,
            enabled = value.canSubmit,
            isLoading = value.isLoading,
            colors = colors,
        )

        CmpTextButton(
            text = stringResource(Res.string.login_forgot_link_back_to_login),
            onClick = onBackToLogin,
            enabled = !value.isLoading,
        )
    }
}
