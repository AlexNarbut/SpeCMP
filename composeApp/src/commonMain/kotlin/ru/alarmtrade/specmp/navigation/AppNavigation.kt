package ru.alarmtrade.specmp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import ru.alarmtrade.specmp.feature.calculator.presentation.CalculatorScreen
import ru.alarmtrade.specmp.home.HomeScreen

@Serializable
data object HomeRoute : NavKey

@Serializable
data object CalculatorRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeRoute::class, HomeRoute.serializer())
            subclass(CalculatorRoute::class, CalculatorRoute.serializer())
        }
    }
}

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(navConfig, HomeRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider<NavKey> {
            entry<HomeRoute> { _ ->
                HomeScreen(onNavigateToCalculator = { backStack += CalculatorRoute })
            }
            entry<CalculatorRoute> { _ ->
                CalculatorScreen(onBack = { backStack.removeLastOrNull() })
            }
        }
    )
}
