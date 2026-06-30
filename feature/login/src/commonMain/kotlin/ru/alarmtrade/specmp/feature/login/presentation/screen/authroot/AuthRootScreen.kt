package ru.alarmtrade.specmp.feature.login.presentation.screen.authroot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthErrorMessage
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthLocaleSwitcher
import ru.alarmtrade.specmp.feature.login.presentation.locale.AppLocale
import ru.alarmtrade.specmp.feature.login.presentation.locale.applyAppLocale
import ru.alarmtrade.specmp.feature.login.presentation.locale.currentAppLocale
import ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword.ForgotPasswordScreen
import ru.alarmtrade.specmp.feature.login.presentation.screen.loggedinplaceholder.LoggedInPlaceholderScreen
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreen
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreenState
import ru.alarmtrade.specmp.feature.login.presentation.screen.registration.RegistrationScreen
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_common_a11y_back
import specmp.feature.login.generated.resources.login_common_btn_retry
import specmp.feature.login.generated.resources.login_common_error_session_check_failed
import specmp.feature.login.generated.resources.login_common_loading_session_check
import specmp.feature.login.generated.resources.login_forgot_title_screen
import specmp.feature.login.generated.resources.login_loggedin_title_screen
import specmp.feature.login.generated.resources.login_login_title_screen
import specmp.feature.login.generated.resources.login_registration_title_screen

@Composable
fun AuthRootScreen(
    onLoginSuccess: () -> Unit,
) {
    val viewModel = koinViewModel<AuthRootViewModel>()
    val state = viewModel.state.collectAsState()
    val colors = cmpThemeColors()
    var appLocale by remember { mutableStateOf(currentAppLocale()) }
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
            onLocaleChanged = { locale ->
                applyAppLocale(locale)
                appLocale = locale
            },
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
    val showBackButton = effectiveDestination == AuthRootDestination.Registration ||
        effectiveDestination == AuthRootDestination.ForgotPassword

    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = {
                    CmpText(
                        text = authTitle(effectiveDestination),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    if (showBackButton) {
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
                    AuthLocaleSwitcher(
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
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                AuthRootBody(
                    state = value,
                    colors = colors,
                    destination = effectiveDestination,
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
                )
            }
        }
    }
}

@Composable
private fun AuthRootBody(
    state: AuthRootState,
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
) {
    when (val sessionState = state.sessionState) {
        is LCEState.None,
        is LCEState.Loading -> AuthSessionLoading(colors = colors)
        is LCEState.Error -> AuthSessionError(
            colors = colors,
            onRetrySessionCheck = onRetrySessionCheck,
        )
        is LCEState.Content -> {
            if (sessionState.content) {
                LoggedInPlaceholderScreen(
                    logoutState = state.logoutState,
                    isLogoutLoading = state.isLogoutLoading,
                    colors = colors,
                    onLogout = onLogout,
                )
            } else {
                AuthGuestBody(
                    state = state,
                    colors = colors,
                    destination = destination,
                    onDestinationChanged = onDestinationChanged,
                    onLoginChanged = onLoginChanged,
                    onPasswordChanged = onPasswordChanged,
                    onPasswordVisibilityChanged = onPasswordVisibilityChanged,
                    onResetLoginForm = onResetLoginForm,
                    onSubmitLogin = onSubmitLogin,
                    onResendActivationEmail = onResendActivationEmail,
                    onDismissActivationRequest = onDismissActivationRequest,
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
    onDestinationChanged: (AuthRootDestination) -> Unit,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityChanged: () -> Unit,
    onResetLoginForm: () -> Unit,
    onSubmitLogin: () -> Unit,
    onResendActivationEmail: () -> Unit,
    onDismissActivationRequest: () -> Unit,
) {
    when (destination) {
        AuthRootDestination.Login,
        AuthRootDestination.LoggedIn -> LoginScreen(
            state = state.toLoginScreenState(),
            colors = colors,
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
        )
        AuthRootDestination.Registration -> RegistrationScreen(
            colors = colors,
            onBackToLogin = { onDestinationChanged(AuthRootDestination.Login) },
        )
        AuthRootDestination.ForgotPassword -> ForgotPasswordScreen(
            colors = colors,
            onBackToLogin = { onDestinationChanged(AuthRootDestination.Login) },
        )
    }
}

@Composable
private fun AuthSessionLoading(
    colors: CmpThemeColors,
) {
    CmpColumn(
        modifier = Modifier.widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CmpCircularProgressIndicator(color = colors.primary)
        CmpText(
            text = stringResource(Res.string.login_common_loading_session_check),
            color = colors.onBackground,
        )
    }
}

@Composable
private fun AuthSessionError(
    colors: CmpThemeColors,
    onRetrySessionCheck: () -> Unit,
) {
    CmpColumn(
        modifier = Modifier.widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AuthErrorMessage(
            text = stringResource(Res.string.login_common_error_session_check_failed),
            colors = colors,
        )
        CmpButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.login_common_btn_retry),
            onClick = onRetrySessionCheck,
        )
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
