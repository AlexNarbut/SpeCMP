package ru.alarmtrade.specmp.feature.main.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import ru.alarmtrade.specmp.core.ui.navigation.cmpBackTransition
import ru.alarmtrade.specmp.core.ui.navigation.cmpForwardTransition
import ru.alarmtrade.specmp.core.ui.navigation.cmpPredictiveBackTransition
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.theme.LocalThemeIsDark
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainScreen

@Serializable
@SerialName("main.MainRoute")
data object MainRoute : NavKey

@Serializable
@SerialName("main.HomeRoute")
internal data object HomeRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeRoute::class, HomeRoute.serializer())
        }
    }
}

/** [onNavigateToFeature] — переход в другую фичу приложения (решает `composeApp`). */
@Composable
fun MainNavGraph(onNavigateToFeature: (MainNavigationTarget) -> Unit) {
    val backStack = rememberNavBackStack(navConfig, HomeRoute)
    val isDarkState = LocalThemeIsDark.current
    val isDark by isDarkState

    NavDisplay(
        transitionSpec = cmpForwardTransition(),
        popTransitionSpec = cmpBackTransition(),
        predictivePopTransitionSpec = cmpPredictiveBackTransition(),
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        onBack = { backStack.popSafe() },
        entryProvider = entryProvider<NavKey> {
            entry<HomeRoute> { _ ->
                MainScreen(
                    isDarkTheme = isDark,
                    onThemeChange = { isDarkState.value = it },
                    onNavigateToFeature = onNavigateToFeature,
                )
            }
        },
    )
}
