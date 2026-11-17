package ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.login_common_btn_go_to_login
import ru.alarmtrade.specmp.core.localization.resources.login_common_error_reset_password_failed
import ru.alarmtrade.specmp.core.localization.resources.login_common_label_email
import ru.alarmtrade.specmp.core.localization.resources.login_forgot_btn_submit
import ru.alarmtrade.specmp.core.localization.resources.login_forgot_link_back_to_login
import ru.alarmtrade.specmp.core.localization.resources.login_forgot_msg_success
import ru.alarmtrade.specmp.core.localization.resources.login_forgot_subtitle_screen
import ru.alarmtrade.specmp.core.localization.resources.login_forgot_title_success
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthBody
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthLayoutMode
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthSubmitButton
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthSuccessPanel
import ru.alarmtrade.specmp.feature.login.presentation.components.formTail
import ru.alarmtrade.specmp.feature.login.presentation.components.rememberAuthFormTail
import ru.alarmtrade.specmp.feature.login.presentation.components.revealsFormTail
import ru.alarmtrade.specmp.feature.login.presentation.components.submitKeyboardActions
import ru.alarmtrade.specmp.feature.login.presentation.model.text

@Composable
fun ForgotPasswordScreen(layoutMode: AuthLayoutMode, onBackToLogin: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<ForgotPasswordScreenViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle()

    AuthBody(
        layoutMode = layoutMode,
        modifier = modifier,
        showCompactBrand = false,
        error = state.value.resetError?.text(Strings.string.login_common_error_reset_password_failed),
    ) {
        if (state.value.isSuccess) {
            AuthSuccessPanel(
                title = stringResource(Strings.string.login_forgot_title_success),
                text = stringResource(Strings.string.login_forgot_msg_success),
                icon = CmpIcons.Email,
                primaryAction = {
                    AuthSubmitButton(
                        text = stringResource(Strings.string.login_common_btn_go_to_login),
                        onClick = onBackToLogin,
                    )
                },
            )
        } else {
            ForgotPasswordForm(
                state = state,
                onEmailChange = viewModel::onEmailChanged,
                onSubmit = viewModel::onSubmit,
                onBackToLogin = onBackToLogin,
            )
        }
    }
}

@Composable
internal fun ForgotPasswordForm(
    state: State<ForgotPasswordScreenState>,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    val value = state.value
    val formTail = rememberAuthFormTail()

    CmpColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
    ) {
        CmpText(
            text = stringResource(Strings.string.login_forgot_subtitle_screen),
            style = CmpTextStyle.Paragraph2,
            modifier = Modifier.fillMaxWidth(),
            color = colors.foregroundOnDarkSecondaryDefault,
        )

        CmpTextField(
            value = value.email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth().revealsFormTail(formTail),
            label = stringResource(Strings.string.login_common_label_email),
            singleLine = true,
            enabled = !value.isLoading,
            isError = value.resetError?.blamesInput == true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = submitKeyboardActions(enabled = value.canSubmit, onSubmit = onSubmit),
            leadingIcon = {
                CmpIcon(icon = CmpIcons.Email, contentDescription = null)
            },
        )

        AuthSubmitButton(
            text = stringResource(Strings.string.login_forgot_btn_submit),
            onClick = onSubmit,
            modifier = Modifier.formTail(formTail),
            enabled = value.canSubmit,
            isLoading = value.isLoading,
        )

        CmpButton(
            text = stringResource(Strings.string.login_forgot_link_back_to_login),
            onClick = onBackToLogin,
            enabled = !value.isLoading,
            type = CmpButtonType.Tertiary,
            size = CmpButtonSize.S,
            icon = CmpIcons.ArrowBack,
        )
    }
}
