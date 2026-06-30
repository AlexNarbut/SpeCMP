package ru.alarmtrade.specmp.feature.login.presentation.screen.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.input.CmpCheckbox
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthErrorMessage
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthPasswordField
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthSuccessMessage
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_common_btn_done
import specmp.feature.login.generated.resources.login_common_error_registration_failed
import specmp.feature.login.generated.resources.login_common_error_required_fields
import specmp.feature.login.generated.resources.login_common_label_email
import specmp.feature.login.generated.resources.login_common_label_full_name
import specmp.feature.login.generated.resources.login_common_label_phone
import specmp.feature.login.generated.resources.login_common_loading_registration
import specmp.feature.login.generated.resources.login_registration_btn_submit
import specmp.feature.login.generated.resources.login_registration_label_installer_confirm
import specmp.feature.login.generated.resources.login_registration_msg_success

@Composable
fun RegistrationScreen(
    colors: CmpThemeColors,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<RegistrationScreenViewModel>()
    val state = viewModel.state.collectAsState()

    DisposableEffect(viewModel) {
        onDispose(viewModel::reset)
    }

    RegistrationContent(
        state = state,
        colors = colors,
        onFullNameChanged = viewModel::onFullNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onPhoneNumberChanged = viewModel::onPhoneNumberChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onPasswordVisibilityChanged = viewModel::onPasswordVisibilityChanged,
        onInstallerConfirmedChanged = viewModel::onInstallerConfirmedChanged,
        onSubmit = viewModel::submit,
        onBackToLogin = onBackToLogin,
        modifier = modifier,
    )
}

@Composable
private fun RegistrationContent(
    state: State<RegistrationScreenState>,
    colors: CmpThemeColors,
    onFullNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneNumberChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    onInstallerConfirmedChanged: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val value = state.value

    CmpColumn(
        modifier = modifier.widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CmpTextField(
            value = value.fullName,
            onValueChange = onFullNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.login_common_label_full_name),
            singleLine = true,
            enabled = !value.isLoading && !value.isSuccess,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        CmpTextField(
            value = value.email,
            onValueChange = onEmailChanged,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.login_common_label_email),
            singleLine = true,
            enabled = !value.isLoading && !value.isSuccess,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )

        CmpTextField(
            value = value.phoneNumber,
            onValueChange = onPhoneNumberChanged,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.login_common_label_phone),
            singleLine = true,
            enabled = !value.isLoading && !value.isSuccess,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next,
            ),
        )

        AuthPasswordField(
            value = value.password,
            onValueChange = onPasswordChanged,
            isPasswordVisible = value.isPasswordVisible,
            onPasswordVisibilityChanged = onPasswordVisibilityChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !value.isLoading && !value.isSuccess,
            keyboardActions = KeyboardActions(
                onDone = {
                    if (value.canSubmit) onSubmit()
                },
            ),
        )

        CmpRow(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CmpCheckbox(
                checked = value.isInstallerConfirmed,
                onCheckedChange = onInstallerConfirmedChanged,
                enabled = !value.isLoading && !value.isSuccess,
            )
            CmpText(
                text = stringResource(Res.string.login_registration_label_installer_confirm),
                color = colors.onBackground,
            )
        }

        RegistrationMessage(
            state = value.registrationState,
            colors = colors,
        )

        CmpButton(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = value.canSubmit,
        ) {
            if (value.isLoading) {
                CmpRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CmpCircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.onPrimary,
                    )
                    CmpText(text = stringResource(Res.string.login_common_loading_registration))
                }
            } else {
                CmpText(text = stringResource(Res.string.login_registration_btn_submit))
            }
        }

        if (value.isSuccess) {
            CmpTextButton(
                text = stringResource(Res.string.login_common_btn_done),
                onClick = onBackToLogin,
            )
        }
    }
}

@Composable
private fun RegistrationMessage(
    state: LCEState<Unit>,
    colors: CmpThemeColors,
) {
    when (state) {
        is LCEState.Content -> AuthSuccessMessage(
            text = stringResource(Res.string.login_registration_msg_success),
            colors = colors,
        )
        is LCEState.Error -> AuthErrorMessage(
            text = if (state.error == REQUIRED_FIELDS_ERROR) {
                stringResource(Res.string.login_common_error_required_fields)
            } else {
                stringResource(Res.string.login_common_error_registration_failed)
            },
            colors = colors,
        )
        is LCEState.Loading,
        is LCEState.None -> Unit
    }
}

private const val REQUIRED_FIELDS_ERROR = "required_fields"
