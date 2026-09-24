package ru.alarmtrade.specmp.feature.login.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthBody
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthLanguageSegment
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthLayoutMode
import ru.alarmtrade.specmp.feature.login.presentation.components.AuthScreenChrome
import ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword.ForgotPasswordScreen
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreen
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreenState
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreenViewModel
import ru.alarmtrade.specmp.feature.login.presentation.screen.registration.RegistrationScreen
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_common_a11y_back
import specmp.feature.login.generated.resources.login_forgot_title_screen
import specmp.feature.login.generated.resources.login_registration_title_screen

@Serializable
data object LoginRoute : NavKey

@Serializable
data object RegistrationRoute : NavKey

@Serializable
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
fun LoginNavGraph(
    onLocaleChanged: (AppLocale) -> Unit,
) {
    val viewModel = koinViewModel<LoginScreenViewModel>()
    val state = viewModel.state.collectAsState()
    val colors = cmpThemeColors()
    val backStack = rememberNavBackStack(navConfig, LoginRoute)
    val currentRoute = backStack.lastOrNull() ?: LoginRoute

    AuthScreenChrome(
        colors = colors,
        isLoading = currentRoute == LoginRoute && state.value.isAuthLoading,
        title = {
            if (currentRoute != LoginRoute) {
                CmpText(
                    text = routeTitle(currentRoute),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        navigationIcon = {
            if (backStack.size > 1) {
                CmpIconButton(
                    onClick = { backStack.removeLastOrNull() },
                    icon = CmpIcons.ArrowBack,
                    contentDescription = stringResource(Res.string.login_common_a11y_back),
                )
            }
        },
        actions = {
            AuthLanguageSegment(
                onLocaleChanged = onLocaleChanged,
                colors = colors,
            )
        },
    ) { layoutMode ->
        LoginNavDisplay(
            backStack = backStack,
            state = state,
            colors = colors,
            layoutMode = layoutMode,
            viewModel = viewModel,
        )
    }
}

@Composable
private fun LoginNavDisplay(
    backStack: NavBackStack<NavKey>,
    state: State<LoginScreenState>,
    colors: CmpThemeColors,
    layoutMode: AuthLayoutMode,
    viewModel: LoginScreenViewModel,
) {
    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryDecorators = listOf(
            // Дефолтный декоратор NavDisplay: без него не работает SavedStateHandle.
            rememberSaveableStateHolderNavEntryDecorator(),
            // Per-entry ViewModelStoreOwner (см. docs/navigation.md).
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider<NavKey> {
            entry<LoginRoute> { _ ->
                AuthBody(
                    layoutMode = layoutMode,
                    modifier = Modifier.fillMaxSize(),
                    colors = colors,
                ) {
                    LoginScreen(
                        state = state.value,
                        colors = colors,
                        layoutMode = layoutMode,
                        onLoginChanged = viewModel::onLoginChanged,
                        onPasswordChanged = viewModel::onPasswordChanged,
                        onPasswordVisibilityChanged = viewModel::onPasswordVisibilityChanged,
                        onSubmit = viewModel::submit,
                        onOpenRegistration = {
                            viewModel.resetLoginForm()
                            backStack += RegistrationRoute
                        },
                        onOpenForgotPassword = {
                            viewModel.resetLoginForm()
                            backStack += ForgotPasswordRoute
                        },
                        onResendActivationEmail = viewModel::resendActivationEmail,
                        onDismissActivationRequest = viewModel::dismissActivationRequest,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            entry<RegistrationRoute> { _ ->
                AuthBody(
                    layoutMode = layoutMode,
                    modifier = Modifier.fillMaxSize(),
                    colors = colors,
                    showCompactBrand = false,
                ) {
                    RegistrationScreen(
                        colors = colors,
                        onBackToLogin = { backStack.removeLastOrNull() },
                    )
                }
            }
            entry<ForgotPasswordRoute> { _ ->
                AuthBody(
                    layoutMode = layoutMode,
                    modifier = Modifier.fillMaxSize(),
                    colors = colors,
                    showCompactBrand = false,
                ) {
                    ForgotPasswordScreen(
                        colors = colors,
                        onBackToLogin = { backStack.removeLastOrNull() },
                    )
                }
            }
        },
    )
}

@Composable
private fun routeTitle(route: NavKey): String {
    return when (route) {
        RegistrationRoute -> stringResource(Res.string.login_registration_title_screen)
        ForgotPasswordRoute -> stringResource(Res.string.login_forgot_title_screen)
        else -> ""
    }
}
