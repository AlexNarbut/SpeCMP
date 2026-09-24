package ru.alarmtrade.specmp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.ui.theme.LocalThemeIsDark
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.feature.authstate.presentation.navigation.AuthStateNavGraph
import ru.alarmtrade.specmp.feature.authstate.presentation.screen.SessionCheckErrorScreen
import ru.alarmtrade.specmp.feature.login.presentation.navigation.LoginNavGraph
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainScreen
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatEntry
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatRoute
import ru.alarmtrade.specmp.filepicker.rememberSupportChatFilePicker
import ru.alarmtrade.specmp.filepicker.rememberSupportChatFileSaver

@Serializable
data object MainRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MainRoute::class, MainRoute.serializer())
            subclass(SupportChatRoute::class, SupportChatRoute.serializer())
        }
    }
}

@Composable
fun AppNavigation(
    onLocaleChanged: (AppLocale) -> Unit,
) {
    AuthStateNavGraph(
        waitingState = { AppStartLoading() },
        noAuthState = {
            LoginNavGraph(onLocaleChanged = onLocaleChanged)
        },
        authedState = { MainNavGraph() },
        errorState = { onRetry ->
            SessionCheckErrorScreen(onRetry = onRetry)
        },
    )
}

@Composable
private fun MainNavGraph() {
    val backStack = rememberNavBackStack(navConfig, MainRoute)
    val isDarkState = LocalThemeIsDark.current
    val isDark by isDarkState

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryDecorators = listOf(
            // Дефолтный декоратор NavDisplay: без него не работает SavedStateHandle.
            rememberSaveableStateHolderNavEntryDecorator(),
            // Per-entry ViewModelStoreOwner: VM живёт ровно столько, сколько запись
            // бэкстека (см. docs/navigation.md).
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider<NavKey> {
            entry<MainRoute> { _ ->
                MainScreen(
                    isDarkTheme = isDark,
                    onThemeChanged = { isDarkState.value = it },
                    onNavigateToFeature = { target ->
                        navigateFromMain(target, backStack)
                    },
                )
            }
            entry<SupportChatRoute> { _ ->
                SupportChatEntry(
                    onBack = { backStack.removeLastOrNull() },
                    filePicker = rememberSupportChatFilePicker(),
                    fileSaver = rememberSupportChatFileSaver(),
                )
            }
        }
    )
}

private fun navigateFromMain(target: MainNavigationTarget, backStack: NavBackStack<NavKey>) {
    when (target) {
        MainNavigationTarget.QuickMount -> Unit
        MainNavigationTarget.AdvancedMount -> Unit
        MainNavigationTarget.Configurator -> Unit
        MainNavigationTarget.FirmwareArchive -> Unit
        MainNavigationTarget.Cars -> Unit
        MainNavigationTarget.CanScanner -> Unit
        MainNavigationTarget.Requests -> backStack += SupportChatRoute
        MainNavigationTarget.Feedback -> Unit
        MainNavigationTarget.ProductInfo -> Unit
        MainNavigationTarget.TechSupportChat -> backStack += SupportChatRoute
        MainNavigationTarget.Warehouse1C -> Unit
        MainNavigationTarget.Settings -> Unit
        MainNavigationTarget.AppUpdate -> Unit
    }
}

@Composable
private fun AppStartLoading() {
    CmpBox(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CmpCircularProgressIndicator(color = cmpThemeColors().primary)
    }
}
