package ru.alarmtrade.specmp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
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
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainScreen

@Serializable
data object MainRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MainRoute::class, MainRoute.serializer())
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
        entryProvider = entryProvider<NavKey> {
            entry<MainRoute> { _ ->
                MainScreen(
                    isDarkTheme = isDark,
                    onThemeChanged = { isDarkState.value = it },
                )
            }
        }
    )
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
