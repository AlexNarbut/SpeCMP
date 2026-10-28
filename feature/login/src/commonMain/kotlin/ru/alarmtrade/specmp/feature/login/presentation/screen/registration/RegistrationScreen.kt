package ru.alarmtrade.specmp.feature.login.presentation.screen.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.login_common_btn_go_to_login
import ru.alarmtrade.specmp.core.localization.resources.login_common_error_email_invalid
import ru.alarmtrade.specmp.core.localization.resources.login_common_error_full_name_required
import ru.alarmtrade.specmp.core.localization.resources.login_common_error_password_short
import ru.alarmtrade.specmp.core.localization.resources.login_common_error_phone_invalid
import ru.alarmtrade.specmp.core.localization.resources.login_common_error_registration_failed
import ru.alarmtrade.specmp.core.localization.resources.login_common_label_email
import ru.alarmtrade.specmp.core.localization.resources.login_common_label_full_name
import ru.alarmtrade.specmp.core.localization.resources.login_common_label_phone
import ru.alarmtrade.specmp.core.localization.resources.login_registration_btn_back_to_login
import ru.alarmtrade.specmp.core.localization.resources.login_registration_btn_submit
import ru.alarmtrade.specmp.core.localization.resources.login_registration_label_installer_confirm
import ru.alarmtrade.specmp.core.localization.resources.login_registration_msg_success
import ru.alarmtrade.specmp.core.localization.resources.login_registration_title_success
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpCheckbox
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthBody
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthLayoutMode
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthPasswordField
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthSubmitButton
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthSuccessPanel
import ru.alarmtrade.specmp.feature.login.presentation.model.RegistrationFieldError
import ru.alarmtrade.specmp.feature.login.presentation.model.text

@Composable
fun RegistrationScreen(
    layoutMode: AuthLayoutMode,
    colors: CmpThemeColors,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = koinViewModel<RegistrationScreenViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle()

    DisposableEffect(viewModel) {
        onDispose(viewModel::reset)
    }

    AuthBody(
        layoutMode = layoutMode,
        modifier = modifier,
        colors = colors,
        showCompactBrand = false,
        error = state.value.registrationError?.text(Strings.string.login_common_error_registration_failed)
    ) {
        if (state.value.isSuccess) {
            AuthSuccessPanel(
                title = stringResource(Strings.string.login_registration_title_success),
                text = stringResource(Strings.string.login_registration_msg_success),
                icon = CmpIcons.CheckCircle,
                colors = colors,
                primaryAction = {
                    AuthSubmitButton(
                        text = stringResource(Strings.string.login_common_btn_go_to_login),
                        onClick = onBackToLogin,
                        colors = colors
                    )
                }
            )
        } else {
            RegistrationForm(
                state = state,
                colors = colors,
                onFullNameChange = viewModel::onFullNameChanged,
                onEmailChange = viewModel::onEmailChanged,
                onPhoneNumberChange = viewModel::onPhoneNumberChanged,
                onPasswordChange = viewModel::onPasswordChanged,
                onPasswordVisibilityChange = viewModel::onPasswordVisibilityChanged,
                onInstallerConfirmedChange = viewModel::onInstallerConfirmedChanged,
                onSubmit = viewModel::submit,
                onBackToLogin = onBackToLogin
            )
        }
    }
}

@Composable
private fun RegistrationForm(
    state: State<RegistrationScreenState>,
    colors: CmpThemeColors,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onInstallerConfirmedChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val value = state.value

    CmpColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CmpTextField(
            value = value.fullName,
            onValueChange = onFullNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Strings.string.login_common_label_full_name),
            singleLine = true,
            enabled = !value.isLoading,
            isError = RegistrationFieldError.FullName in value.fieldErrors,
            supportingText = fieldErrorText(
                visible = RegistrationFieldError.FullName in value.fieldErrors,
                text = Strings.string.login_common_error_full_name_required
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            leadingIcon = {
                CmpIcon(icon = CmpIcons.Person, contentDescription = null)
            }
        )

        CmpTextField(
            value = value.email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Strings.string.login_common_label_email),
            singleLine = true,
            enabled = !value.isLoading,
            isError = RegistrationFieldError.Email in value.fieldErrors,
            supportingText = fieldErrorText(
                visible = RegistrationFieldError.Email in value.fieldErrors,
                text = Strings.string.login_common_error_email_invalid
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            leadingIcon = {
                CmpIcon(icon = CmpIcons.Email, contentDescription = null)
            }
        )

        CmpTextField(
            value = value.phoneNumber,
            onValueChange = onPhoneNumberChange,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Strings.string.login_common_label_phone),
            singleLine = true,
            enabled = !value.isLoading,
            isError = RegistrationFieldError.Phone in value.fieldErrors,
            supportingText = fieldErrorText(
                visible = RegistrationFieldError.Phone in value.fieldErrors,
                text = Strings.string.login_common_error_phone_invalid
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            leadingIcon = {
                CmpIcon(icon = CmpIcons.Phone, contentDescription = null)
            }
        )

        AuthPasswordField(
            value = value.password,
            onValueChange = onPasswordChange,
            isPasswordVisible = value.isPasswordVisible,
            onPasswordVisibilityChange = onPasswordVisibilityChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !value.isLoading,
            isError = RegistrationFieldError.Password in value.fieldErrors,
            supportingText = fieldErrorText(
                visible = RegistrationFieldError.Password in value.fieldErrors,
                text = Strings.string.login_common_error_password_short
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (value.canSubmit) onSubmit()
                }
            )
        )

        CmpRow(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CmpCheckbox(
                checked = value.isInstallerConfirmed,
                onCheckedChange = onInstallerConfirmedChange,
                enabled = !value.isLoading
            )
            CmpText(
                text = stringResource(Strings.string.login_registration_label_installer_confirm),
                color = colors.onBackground
            )
        }

        AuthSubmitButton(
            text = stringResource(Strings.string.login_registration_btn_submit),
            onClick = onSubmit,
            enabled = value.canSubmit,
            isLoading = value.isLoading,
            colors = colors
        )

        CmpTextButton(
            text = stringResource(Strings.string.login_registration_btn_back_to_login),
            onClick = onBackToLogin,
            enabled = !value.isLoading
        )
    }
}

private fun fieldErrorText(visible: Boolean, text: StringResource): (@Composable () -> Unit)? {
    if (!visible) return null
    return { CmpText(text = stringResource(text)) }
}
