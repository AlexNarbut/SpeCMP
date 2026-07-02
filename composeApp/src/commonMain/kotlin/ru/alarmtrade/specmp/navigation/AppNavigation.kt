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
import kotlinx.serialization.modules.subclass
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.feature.login.presentation.LoginScreen

@Serializable
data object LoginRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(LoginRoute::class, LoginRoute.serializer())
        }
    }
}

@Composable
fun AppNavigation(
    appLocale: AppLocale,
    onLocaleChanged: (AppLocale) -> Unit,
) {
    val backStack = rememberNavBackStack(navConfig, LoginRoute)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = entryProvider<NavKey> {
            entry<LoginRoute> { _ ->
                LoginScreen(
                    appLocale = appLocale,
                    onLocaleChanged = onLocaleChanged,
                    onLoginSuccess = {},
                )
            }
        }
    )
}
