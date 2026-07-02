package ru.alarmtrade.specmp.feature.login.presentation.screen.authroot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthBody
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthLanguageSegment
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthLayoutMode
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthScreenChrome
import ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword.ForgotPasswordScreen
import ru.alarmtrade.specmp.feature.login.presentation.screen.loggedinplaceholder.LoggedInPlaceholderScreen
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreen
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreenState
import ru.alarmtrade.specmp.feature.login.presentation.screen.registration.RegistrationScreen
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_authroot_msg_session_error
import specmp.feature.login.generated.resources.login_authroot_title_session_error
import specmp.feature.login.generated.resources.login_common_a11y_back
import specmp.feature.login.generated.resources.login_common_btn_retry
import specmp.feature.login.generated.resources.login_forgot_title_screen
import specmp.feature.login.generated.resources.login_loggedin_title_screen
import specmp.feature.login.generated.resources.login_login_title_screen
import specmp.feature.login.generated.resources.login_registration_title_screen

@Composable
fun AuthRootScreen(
    appLocale: AppLocale,
    onLocaleChanged: (AppLocale) -> Unit,
    onLoginSuccess: () -> Unit,
) {
    val viewModel = koinViewModel<AuthRootViewModel>()
    val state = viewModel.state.collectAsState()
    val colors = cmpThemeColors()
    var destination by remember { mutableStateOf(AuthRootDestination.Login) }

    viewModel.events.collectSideEffect { event ->
        when (event) {
            AuthRootEvent.Authorized -> onLoginSuccess()
        }
    }

    key(appLocale) {
        AuthRootContent(
            state = state,
            colors = colors,
            destination = destination,
            onDestinationChanged = { destination = it },
            onLoginChanged = viewModel::onLoginChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onPasswordVisibilityChanged = viewModel::onPasswordVisibilityChanged,
            onResetLoginForm = viewModel::resetLoginForm,
            onSubmitLogin = viewModel::submit,
            onResendActivationEmail = viewModel::resendActivationEmail,
            onDismissActivationRequest = viewModel::dismissActivationRequest,
            onRetrySessionCheck = viewModel::retrySessionCheck,
            onLogout = viewModel::logout,
            appLocale = appLocale,
            onLocaleChanged = onLocaleChanged,
        )
    }
}

@Composable
private fun AuthRootContent(
    state: State<AuthRootState>,
    colors: CmpThemeColors,
    destination: AuthRootDestination,
    onDestinationChanged: (AuthRootDestination) -> Unit,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    onResetLoginForm: () -> Unit,
    onSubmitLogin: () -> Unit,
    onResendActivationEmail: () -> Unit,
    onDismissActivationRequest: () -> Unit,
    onRetrySessionCheck: () -> Unit,
    onLogout: () -> Unit,
    appLocale: AppLocale,
    onLocaleChanged: (AppLocale) -> Unit,
) {
    val value = state.value
    val effectiveDestination = if (value.isLoggedIn) {
        AuthRootDestination.LoggedIn
    } else {
        destination
    }
    val isSessionLoading = value.sessionState is LCEState.None ||
        value.sessionState is LCEState.Loading
    val isLoginLoading = !isSessionLoading &&
        effectiveDestination == AuthRootDestination.Login &&
        value.isAuthLoading
    val showBackButton = effectiveDestination == AuthRootDestination.Registration ||
        effectiveDestination == AuthRootDestination.ForgotPassword

    AuthScreenChrome(
        colors = colors,
        isLoading = isLoginLoading,
        title = {
            if (!isSessionLoading && effectiveDestination != AuthRootDestination.Login) {
                CmpText(
                    text = authTitle(effectiveDestination),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        navigationIcon = {
            if (!isSessionLoading && showBackButton) {
                CmpIconButton(
                    onClick = {
                        onDestinationChanged(AuthRootDestination.Login)
                    },
                    icon = CmpIcons.ArrowBack,
                    contentDescription = stringResource(Res.string.login_common_a11y_back),
                )
            }
        },
        actions = {
            if (!isSessionLoading) {
                AuthLanguageSegment(
                    appLocale = appLocale,
                    onLocaleChanged = onLocaleChanged,
                    colors = colors,
                )
            }
        },
    ) {
        AuthRootBody(
            state = value,
            colors = colors,
            destination = effectiveDestination,
            layoutMode = it,
            onDestinationChanged = onDestinationChanged,
            onLoginChanged = onLoginChanged,
            onPasswordChanged = onPasswordChanged,
            onPasswordVisibilityChanged = onPasswordVisibilityChanged,
            onResetLoginForm = onResetLoginForm,
            onSubmitLogin = onSubmitLogin,
            onResendActivationEmail = onResendActivationEmail,
            onDismissActivationRequest = onDismissActivationRequest,
            onRetrySessionCheck = onRetrySessionCheck,
            onLogout = onLogout,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun AuthRootBody(
    state: AuthRootState,
    colors: CmpThemeColors,
    destination: AuthRootDestination,
    layoutMode: AuthLayoutMode,
    onDestinationChanged: (AuthRootDestination) -> Unit,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    onResetLoginForm: () -> Unit,
    onSubmitLogin: () -> Unit,
    onResendActivationEmail: () -> Unit,
    onDismissActivationRequest: () -> Unit,
    onRetrySessionCheck: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val sessionState = state.sessionState) {
        is LCEState.None,
        is LCEState.Loading -> AuthCenteredBody(modifier = modifier) {
            AuthSessionLoading(colors = colors)
        }
        is LCEState.Error -> AuthSessionError(
            colors = colors,
            onRetrySessionCheck = onRetrySessionCheck,
            modifier = modifier,
        )
        is LCEState.Content -> {
            if (sessionState.content) {
                AuthCenteredBody(modifier = modifier) {
                    LoggedInPlaceholderScreen(
                        logoutState = state.logoutState,
                        isLogoutLoading = state.isLogoutLoading,
                        colors = colors,
                        onLogout = onLogout,
                    )
                }
            } else {
                AuthGuestBody(
                    state = state,
                    colors = colors,
                    destination = destination,
                    layoutMode = layoutMode,
                    onDestinationChanged = onDestinationChanged,
                    onLoginChanged = onLoginChanged,
                    onPasswordChanged = onPasswordChanged,
                    onPasswordVisibilityChanged = onPasswordVisibilityChanged,
                    onResetLoginForm = onResetLoginForm,
                    onSubmitLogin = onSubmitLogin,
                    onResendActivationEmail = onResendActivationEmail,
                    onDismissActivationRequest = onDismissActivationRequest,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun AuthGuestBody(
    state: AuthRootState,
    colors: CmpThemeColors,
    destination: AuthRootDestination,
    layoutMode: AuthLayoutMode,
    onDestinationChanged: (AuthRootDestination) -> Unit,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    onResetLoginForm: () -> Unit,
    onSubmitLogin: () -> Unit,
    onResendActivationEmail: () -> Unit,
    onDismissActivationRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (destination) {
        AuthRootDestination.Login,
        AuthRootDestination.LoggedIn -> AuthBody(
            layoutMode = layoutMode,
            modifier = modifier,
            colors = colors,
        ) {
            LoginScreen(
                state = state.toLoginScreenState(),
                colors = colors,
                layoutMode = layoutMode,
                onLoginChanged = onLoginChanged,
                onPasswordChanged = onPasswordChanged,
                onPasswordVisibilityChanged = onPasswordVisibilityChanged,
                onSubmit = onSubmitLogin,
                onOpenRegistration = {
                    onResetLoginForm()
                    onDestinationChanged(AuthRootDestination.Registration)
                },
                onOpenForgotPassword = {
                    onResetLoginForm()
                    onDestinationChanged(AuthRootDestination.ForgotPassword)
                },
                onResendActivationEmail = onResendActivationEmail,
                onDismissActivationRequest = onDismissActivationRequest,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        AuthRootDestination.Registration -> AuthCenteredBody(modifier = modifier) {
            RegistrationScreen(
                colors = colors,
                onBackToLogin = { onDestinationChanged(AuthRootDestination.Login) },
            )
        }
        AuthRootDestination.ForgotPassword -> AuthCenteredBody(modifier = modifier) {
            ForgotPasswordScreen(
                colors = colors,
                onBackToLogin = { onDestinationChanged(AuthRootDestination.Login) },
            )
        }
    }
}

@Composable
private fun AuthCenteredBody(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    CmpColumn(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        content()
    }
}

@Composable
private fun AuthSessionLoading(
    colors: CmpThemeColors,
) {
    CmpColumn(
        modifier = Modifier.widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CmpCircularProgressIndicator(color = colors.primary)
    }
}

@Composable
private fun AuthSessionError(
    colors: CmpThemeColors,
    onRetrySessionCheck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthCenteredBody(modifier = modifier) {
        CmpColumn(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CmpSurface(
                shape = CircleShape,
                color = colors.errorContainer,
                contentColor = colors.onErrorContainer,
            ) {
                CmpIcon(
                    icon = CmpIcons.AlertCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp).size(40.dp),
                    tint = colors.onErrorContainer,
                )
            }
            CmpText(
                text = stringResource(Res.string.login_authroot_title_session_error),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground,
                textAlign = TextAlign.Center,
            )
            CmpText(
                text = stringResource(Res.string.login_authroot_msg_session_error),
                fontSize = 14.sp,
                color = colors.onBackground.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )
            CmpButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onClick = onRetrySessionCheck,
            ) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpIcon(
                        icon = CmpIcons.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    CmpText(text = stringResource(Res.string.login_common_btn_retry))
                }
            }
        }
    }
}

@Composable
private fun authTitle(destination: AuthRootDestination): String {
    return when (destination) {
        AuthRootDestination.Login -> stringResource(Res.string.login_login_title_screen)
        AuthRootDestination.Registration -> stringResource(Res.string.login_registration_title_screen)
        AuthRootDestination.ForgotPassword -> stringResource(Res.string.login_forgot_title_screen)
        AuthRootDestination.LoggedIn -> stringResource(Res.string.login_loggedin_title_screen)
    }
}

private fun AuthRootState.toLoginScreenState(): LoginScreenState {
    return LoginScreenState(
        login = login,
        password = password,
        isPasswordVisible = isPasswordVisible,
        authState = authState,
        activationResendState = activationResendState,
        isAuthLoading = isAuthLoading,
        isActivationResendLoading = isActivationResendLoading,
        isActivationRequired = isActivationRequired,
        canSubmit = canSubmit,
    )
}

private enum class AuthRootDestination {
    Login,
    Registration,
    ForgotPassword,
    LoggedIn,
}
