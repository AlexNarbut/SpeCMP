package ru.alarmtrade.specmp.feature.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.locale.AppLocale
import ru.alarmtrade.specmp.feature.login.presentation.locale.applyAppLocale
import ru.alarmtrade.specmp.feature.login.presentation.locale.currentAppLocale
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_authroot_subtitle_specialist
import specmp.feature.login.generated.resources.login_authroot_title_brand
import specmp.feature.login.generated.resources.login_common_a11y_back
import specmp.feature.login.generated.resources.login_common_btn_done
import specmp.feature.login.generated.resources.login_common_btn_language_en
import specmp.feature.login.generated.resources.login_common_btn_language_ru
import specmp.feature.login.generated.resources.login_common_btn_later
import specmp.feature.login.generated.resources.login_common_btn_logout
import specmp.feature.login.generated.resources.login_common_btn_retry
import specmp.feature.login.generated.resources.login_common_btn_send_email
import specmp.feature.login.generated.resources.login_common_error_activation_resend_failed
import specmp.feature.login.generated.resources.login_common_error_auth_failed
import specmp.feature.login.generated.resources.login_common_error_logout_failed
import specmp.feature.login.generated.resources.login_common_error_session_check_failed
import specmp.feature.login.generated.resources.login_common_label_login_email
import specmp.feature.login.generated.resources.login_common_label_password
import specmp.feature.login.generated.resources.login_common_loading_logout
import specmp.feature.login.generated.resources.login_common_loading_send_email
import specmp.feature.login.generated.resources.login_loggedin_msg_placeholder
import specmp.feature.login.generated.resources.login_loggedin_title_screen
import specmp.feature.login.generated.resources.login_login_btn_hide_password
import specmp.feature.login.generated.resources.login_login_btn_show_password
import specmp.feature.login.generated.resources.login_login_btn_submit
import specmp.feature.login.generated.resources.login_login_msg_activation_email_sent
import specmp.feature.login.generated.resources.login_login_msg_activation_required
import specmp.feature.login.generated.resources.login_login_title_confirm_email
import specmp.feature.login.generated.resources.login_login_title_screen

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit,
) {
    val viewModel = koinViewModel<LoginViewModel>()
    val state = viewModel.state.collectAsState()
    val colors = cmpThemeColors()
    var appLocale by remember { mutableStateOf(currentAppLocale()) }

    viewModel.events.collectSideEffect { event ->
        when (event) {
            LoginEvent.Authorized -> onLoginSuccess()
        }
    }

    key(appLocale) {
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
            onRetrySessionCheck = viewModel::retrySessionCheck,
            onLogout = viewModel::logout,
            appLocale = appLocale,
            onLocaleChanged = { locale ->
                applyAppLocale(locale)
                appLocale = locale
            },
        )
    }
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
    onRetrySessionCheck: () -> Unit,
    onLogout: () -> Unit,
    appLocale: AppLocale,
    onLocaleChanged: (AppLocale) -> Unit,
) {
    val value = state.value

    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = {
                    CmpText(
                        text = if (value.isLoggedIn) {
                            stringResource(Res.string.login_loggedin_title_screen)
                        } else {
                            stringResource(Res.string.login_login_title_screen)
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    CmpIconButton(
                        onClick = onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.login_common_a11y_back),
                    )
                },
                actions = {
                    AppLocaleSwitcher(
                        appLocale = appLocale,
                        onLocaleChanged = onLocaleChanged,
                    )
                },
            )
        },
    ) { paddingValues ->
        CmpBox(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(paddingValues),
        ) {
            CmpColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                CmpColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    AuthSessionContent(
                        state = value,
                        colors = colors,
                        onLoginChanged = onLoginChanged,
                        onPasswordChanged = onPasswordChanged,
                        onPasswordVisibilityChanged = onPasswordVisibilityChanged,
                        onSubmit = onSubmit,
                        onRetrySessionCheck = onRetrySessionCheck,
                        onLogout = onLogout,
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
}

@Composable
private fun AppLocaleSwitcher(
    appLocale: AppLocale,
    onLocaleChanged: (AppLocale) -> Unit,
) {
    CmpTextButton(
        text = stringResource(Res.string.login_common_btn_language_en),
        onClick = { onLocaleChanged(AppLocale.English) },
        enabled = appLocale != AppLocale.English,
    )
    CmpTextButton(
        text = stringResource(Res.string.login_common_btn_language_ru),
        onClick = { onLocaleChanged(AppLocale.Russian) },
        enabled = appLocale != AppLocale.Russian,
    )
}

@Composable
private fun AuthSessionContent(
    state: LoginState,
    colors: CmpThemeColors,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    onSubmit: () -> Unit,
    onRetrySessionCheck: () -> Unit,
    onLogout: () -> Unit,
) {
    when (val sessionState = state.sessionState) {
        is LCEState.None,
        is LCEState.Loading -> LoginForm(
            state = state,
            colors = colors,
            onLoginChanged = onLoginChanged,
            onPasswordChanged = onPasswordChanged,
            onPasswordVisibilityChanged = onPasswordVisibilityChanged,
            onSubmit = onSubmit,
        )
        is LCEState.Error -> SessionError(
            colors = colors,
            onRetrySessionCheck = onRetrySessionCheck,
        )
        is LCEState.Content -> {
            if (sessionState.content) {
                LoggedInPlaceholder(
                    state = state,
                    colors = colors,
                    onLogout = onLogout,
                )
            } else {
                LoginForm(
                    state = state,
                    colors = colors,
                    onLoginChanged = onLoginChanged,
                    onPasswordChanged = onPasswordChanged,
                    onPasswordVisibilityChanged = onPasswordVisibilityChanged,
                    onSubmit = onSubmit,
                )
            }
        }
    }
}

@Composable
private fun SessionError(
    colors: CmpThemeColors,
    onRetrySessionCheck: () -> Unit,
) {
    CmpColumn(
        modifier = Modifier.widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CmpSurface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.error,
            contentColor = colors.onError,
        ) {
            CmpText(
                text = stringResource(Res.string.login_common_error_session_check_failed),
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
            )
        }
        CmpButton(
            text = stringResource(Res.string.login_common_btn_retry),
            onClick = onRetrySessionCheck,
        )
    }
}

@Composable
private fun LoggedInPlaceholder(
    state: LoginState,
    colors: CmpThemeColors,
    onLogout: () -> Unit,
) {
    CmpColumn(
        modifier = Modifier.widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CmpText(
            text = stringResource(Res.string.login_loggedin_title_screen),
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )

        CmpText(
            text = stringResource(Res.string.login_loggedin_msg_placeholder),
            fontSize = 16.sp,
            color = colors.onBackground.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
        )

        LogoutErrorMessage(
            logoutState = state.logoutState,
            colors = colors,
        )

        CmpButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !state.isLogoutLoading,
        ) {
            if (state.isLogoutLoading) {
                CmpRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CmpCircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.onPrimary,
                    )
                    CmpText(text = stringResource(Res.string.login_common_loading_logout))
                }
            } else {
                CmpText(text = stringResource(Res.string.login_common_btn_logout))
            }
        }
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
            text = stringResource(Res.string.login_authroot_title_brand),
            fontSize = 34.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )

        CmpText(
            text = stringResource(Res.string.login_authroot_subtitle_specialist),
            fontSize = 16.sp,
            color = colors.onBackground.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 10.dp),
        )

        CmpTextField(
            value = state.login,
            onValueChange = onLoginChanged,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.login_common_label_login_email),
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
            label = stringResource(Res.string.login_common_label_password),
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
                    text = if (state.isPasswordVisible) {
                        stringResource(Res.string.login_login_btn_hide_password)
                    } else {
                        stringResource(Res.string.login_login_btn_show_password)
                    },
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
                CmpText(text = stringResource(Res.string.login_login_btn_submit))
            }
        }
    }
}

@Composable
private fun LogoutErrorMessage(
    logoutState: LCEState<*>,
    colors: CmpThemeColors,
) {
    if (logoutState !is LCEState.Error) return

    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.error,
        contentColor = colors.onError,
    ) {
        CmpText(
            text = stringResource(Res.string.login_common_error_logout_failed),
            modifier = Modifier.padding(12.dp),
            fontSize = 14.sp,
        )
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
            text = stringResource(Res.string.login_common_error_auth_failed),
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
                text = stringResource(Res.string.login_login_title_confirm_email),
                fontWeight = FontWeight.Medium,
            )
        },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CmpText(
                    text = stringResource(Res.string.login_login_msg_activation_required),
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
                        CmpText(text = stringResource(Res.string.login_common_loading_send_email))
                    }
                }
                if (resendSucceeded) {
                    CmpText(text = stringResource(Res.string.login_login_msg_activation_email_sent))
                }
                if (resendError != null) {
                    CmpText(text = stringResource(Res.string.login_common_error_activation_resend_failed))
                }
            }
        },
        confirmButton = {
            CmpButton(
                text = if (resendSucceeded) {
                    stringResource(Res.string.login_common_btn_done)
                } else {
                    stringResource(Res.string.login_common_btn_send_email)
                },
                onClick = if (resendSucceeded) onDismissActivationRequest else onResendActivationEmail,
                enabled = !state.isActivationResendLoading,
            )
        },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Res.string.login_common_btn_later),
                onClick = onDismissActivationRequest,
                enabled = !state.isActivationResendLoading,
            )
        },
    )
}
