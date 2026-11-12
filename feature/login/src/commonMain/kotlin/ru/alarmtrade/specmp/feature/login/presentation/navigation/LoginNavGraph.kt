package ru.alarmtrade.specmp.feature.login.presentation.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.login_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.login_common_error_auth_failed
import ru.alarmtrade.specmp.core.localization.resources.login_forgot_title_screen
import ru.alarmtrade.specmp.core.localization.resources.login_registration_title_screen
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthBody
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthLayoutMode
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthScreenChrome
import ru.alarmtrade.specmp.feature.login.presentation.model.text
import ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword.ForgotPasswordScreen
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreen
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreenViewModel
import ru.alarmtrade.specmp.feature.login.presentation.screen.registration.RegistrationScreen

@Serializable
@SerialName("login.LoginRoute")
data object LoginRoute : NavKey

@Serializable
@SerialName("login.RegistrationRoute")
data object RegistrationRoute : NavKey

@Serializable
@SerialName("login.ForgotPasswordRoute")
data object ForgotPasswordRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(LoginRoute::class, LoginRoute.serializer())
            subclass(RegistrationRoute::class, RegistrationRoute.serializer())
            subclass(ForgotPasswordRoute::class, ForgotPasswordRoute.serializer())
        }
    }
}

@Composable
fun LoginNavGraph() {
    val viewModel = koinViewModel<LoginScreenViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle()
    val colors = cmpThemeColors()
    val backStack = rememberNavBackStack(navConfig, LoginRoute)

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = { backStack.popSafe() },
        entryDecorators = listOf(
            // Дефолтный декоратор NavDisplay: без него не работает SavedStateHandle.
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider<NavKey> {
            entry<LoginRoute> { _ ->
                AuthScreenChrome(
                    colors = colors,
                    isLoading = state.value.isAuthLoading,
                ) { layoutMode ->
                    AuthBody(
                        layoutMode = layoutMode,
                        modifier = Modifier.fillMaxSize(),
                        colors = colors,
                        error = state.value.authError?.text(Strings.string.login_common_error_auth_failed),
                    ) {
                        LoginScreen(
                            state = state.value,
                            colors = colors,
                            layoutMode = layoutMode,
                            onLoginChange = viewModel::onLoginChanged,
                            onPasswordChange = viewModel::onPasswordChanged,
                            onPasswordVisibilityChange = viewModel::onPasswordVisibilityChanged,
                            onSubmit = viewModel::onSubmit,
                            onOpenRegistration = {
                                viewModel.onResetForm()
                                backStack.navigateSafe(RegistrationRoute)
                            },
                            onOpenForgotPassword = {
                                viewModel.onResetForm()
                                backStack.navigateSafe(ForgotPasswordRoute)
                            },
                            onResendActivationEmail = viewModel::onResendActivationEmail,
                            onDismissActivationRequest = viewModel::onDismissActivationRequest,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            entry<RegistrationRoute> { _ ->
                AuthChildScreenChrome(
                    title = stringResource(Strings.string.login_registration_title_screen),
                    colors = colors,
                    onBack = { backStack.popSafe() },
                ) { layoutMode ->
                    RegistrationScreen(
                        layoutMode = layoutMode,
                        colors = colors,
                        onBackToLogin = { backStack.popSafe() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            entry<ForgotPasswordRoute> { _ ->
                AuthChildScreenChrome(
                    title = stringResource(Strings.string.login_forgot_title_screen),
                    colors = colors,
                    onBack = { backStack.popSafe() },
                ) { layoutMode ->
                    ForgotPasswordScreen(
                        layoutMode = layoutMode,
                        colors = colors,
                        onBackToLogin = { backStack.popSafe() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        },
    )
}

@Composable
private fun AuthChildScreenChrome(
    title: String,
    colors: CmpThemeColors,
    onBack: () -> Unit,
    body: @Composable ColumnScope.(AuthLayoutMode) -> Unit,
) {
    AuthScreenChrome(
        colors = colors,
        title = {
            CmpText(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
            )
        },
        navigationIcon = {
            CmpIconButton(
                onClick = onBack,
                icon = CmpIcons.ArrowBack,
                contentDescription = stringResource(Strings.string.login_common_a11y_back),
            )
        },
        body = body,
    )
}
