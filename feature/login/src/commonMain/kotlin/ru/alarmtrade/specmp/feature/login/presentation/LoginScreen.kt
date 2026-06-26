package ru.alarmtrade.specmp.feature.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit,
) {
    val viewModel = koinViewModel<LoginViewModel>()
    val state = viewModel.state.collectAsState()
    val colors = cmpThemeColors()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.Authorized -> onLoginSuccess()
            }
        }
    }

    LoginContent(
        state = state,
        colors = colors,
        onBack = onBack,
        onLoginChanged = viewModel::onLoginChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onPasswordVisibilityChanged = viewModel::onPasswordVisibilityChanged,
        onSubmit = viewModel::submit,
        onResendActivationEmail = viewModel::resendActivationEmail,
        onDismissActivationRequest = viewModel::dismissActivationRequest,
    )
}

@Composable
private fun LoginContent(
    state: State<LoginState>,
    colors: CmpThemeColors,
    onBack: () -> Unit,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    onSubmit: () -> Unit,
    onResendActivationEmail: () -> Unit,
    onDismissActivationRequest: () -> Unit,
) {
    val value = state.value

    CmpBox(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        CmpColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            LoginTopBar(
                colors = colors,
                onBack = onBack,
            )

            CmpColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                LoginForm(
                    state = value,
                    colors = colors,
                    onLoginChanged = onLoginChanged,
                    onPasswordChanged = onPasswordChanged,
                    onPasswordVisibilityChanged = onPasswordVisibilityChanged,
                    onSubmit = onSubmit,
                )
            }
        }

        if (value.isActivationRequired) {
            ActivationRequiredDialog(
                state = value,
                onResendActivationEmail = onResendActivationEmail,
                onDismissActivationRequest = onDismissActivationRequest,
            )
        }
    }
}

@Composable
private fun LoginTopBar(
    colors: CmpThemeColors,
    onBack: () -> Unit,
) {
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpIconButton(onClick = onBack) {
            CmpText(text = "<", fontSize = 24.sp, color = colors.onBackground)
        }
        CmpText(
            text = "Вход",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onBackground,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun LoginForm(
    state: LoginState,
    colors: CmpThemeColors,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    onSubmit: () -> Unit,
) {
    CmpColumn(
        modifier = Modifier.widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CmpText(
            text = "SpeCMP",
            fontSize = 34.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )

        CmpText(
            text = "Авторизация специалиста",
            fontSize = 16.sp,
            color = colors.onBackground.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 10.dp),
        )

        CmpTextField(
            value = state.login,
            onValueChange = onLoginChanged,
            modifier = Modifier.fillMaxWidth(),
            label = "Логин или email",
            singleLine = true,
            enabled = !state.isAuthLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )

        CmpTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = "Пароль",
            singleLine = true,
            enabled = !state.isAuthLoading,
            visualTransformation = if (state.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (state.canSubmit) onSubmit()
                },
            ),
            trailingIcon = {
                CmpTextButton(
                    text = if (state.isPasswordVisible) "Скрыть" else "Показать",
                    onClick = onPasswordVisibilityChanged,
                    enabled = !state.isAuthLoading,
                )
            },
        )

        LoginErrorMessage(
            authState = state.authState,
            colors = colors,
        )

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
                CmpText(text = "Войти")
            }
        }
    }
}

@Composable
private fun LoginErrorMessage(
    authState: LCEState<*>,
    colors: CmpThemeColors,
) {
    if (authState !is LCEState.Error) return

    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.error,
        contentColor = colors.onError,
    ) {
        CmpText(
            text = authState.error,
            modifier = Modifier.padding(12.dp),
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun ActivationRequiredDialog(
    state: LoginState,
    onResendActivationEmail: () -> Unit,
    onDismissActivationRequest: () -> Unit,
) {
    val resendSucceeded = state.activationResendState is LCEState.Content
    val resendError = state.activationResendState as? LCEState.Error

    CmpDialog(
        onDismissRequest = onDismissActivationRequest,
        title = {
            CmpText(
                text = "Подтвердите email",
                fontWeight = FontWeight.Medium,
            )
        },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CmpText(
                    text = "Аккаунт ещё не активирован. Можно повторно отправить письмо подтверждения на указанный логин.",
                )
                if (state.isActivationResendLoading) {
                    CmpRow(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CmpCircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        CmpText(text = "Отправляем письмо...")
                    }
                }
                if (resendSucceeded) {
                    CmpText(text = "Письмо подтверждения отправлено.")
                }
                if (resendError != null) {
                    CmpText(text = resendError.error)
                }
            }
        },
        confirmButton = {
            CmpButton(
                text = if (resendSucceeded) "Готово" else "Отправить письмо",
                onClick = if (resendSucceeded) onDismissActivationRequest else onResendActivationEmail,
                enabled = !state.isActivationResendLoading,
            )
        },
        dismissButton = {
            CmpTextButton(
                text = "Позже",
                onClick = onDismissActivationRequest,
                enabled = !state.isActivationResendLoading,
            )
        },
    )
}
