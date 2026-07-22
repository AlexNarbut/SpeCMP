package ru.alarmtrade.specmp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.feature.authstate.presentation.navigation.AuthStateNavGraph
import ru.alarmtrade.specmp.feature.authstate.presentation.screen.SessionCheckErrorScreen
import ru.alarmtrade.specmp.feature.login.presentation.navigation.LoginNavGraph
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.navigation.MainNavGraph
import ru.alarmtrade.specmp.feature.main.presentation.navigation.MainRoute
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation.DeviceSelectionNavGraph
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation.DeviceSelectionRoute
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatNavGraph
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatRoute
import ru.alarmtrade.specmp.filepicker.rememberSupportChatFilePicker
import ru.alarmtrade.specmp.filepicker.rememberSupportChatFileSaver

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MainRoute::class, MainRoute.serializer())
            subclass(SupportChatRoute::class, SupportChatRoute.serializer())
            subclass(DeviceSelectionRoute::class, DeviceSelectionRoute.serializer())
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
        authedState = { AuthedNavGraph() },
        errorState = { onRetry ->
            SessionCheckErrorScreen(onRetry = onRetry)
        },
    )
}

// Хост авторизованной зоны: держит app-level переходы между графами фич
// (main / support-chat / device-selection). Каждый entry — целый граф фичи.
@Composable
private fun AuthedNavGraph() {
    val backStack = rememberNavBackStack(navConfig, MainRoute)

    NavDisplay(
        backStack = backStack,
        // ViewModel-store декоратор: у каждого entry свой ViewModelStore, поэтому при
        // уходе с экрана (pop) его ViewModel очищается (onCleared), а не живёт на уровне Activity.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        onBack = { backStack.popSafe() },
        entryProvider = entryProvider<NavKey> {
            entry<MainRoute> { _ ->
                MainNavGraph(
                    onNavigateToFeature = { target ->
                        navigateFromMain(target, backStack)
                    },
                )
            }
            entry<SupportChatRoute> { _ ->
                val filePicker = rememberSupportChatFilePicker()
                val fileSaver = rememberSupportChatFileSaver()
                SupportChatNavGraph(
                    onBack = { backStack.popSafe() },
                    filePicker = filePicker,
                    fileSaver = fileSaver,
                )
            }
            entry<DeviceSelectionRoute> { _ ->
                DeviceSelectionNavGraph(
                    // TODO(device:interaction-ui): вести в flow работы с подключённым
                    //  устройством. Пока подключение завершает поиск и возвращает на main.
                    onDeviceConnected = { _ -> backStack.popSafe() },
                    onClose = { backStack.popSafe() },
                )
            }
        }
    )
}

private fun navigateFromMain(target: MainNavigationTarget, backStack: NavBackStack<NavKey>) {
    when (target) {
        MainNavigationTarget.QuickMount -> Unit
        MainNavigationTarget.AdvancedMount -> backStack.navigateSafe(DeviceSelectionRoute)
        MainNavigationTarget.Configurator -> Unit
        MainNavigationTarget.FirmwareArchive -> Unit
        MainNavigationTarget.Cars -> Unit
        MainNavigationTarget.CanScanner -> Unit
        MainNavigationTarget.Requests -> backStack.navigateSafe(SupportChatRoute)
        MainNavigationTarget.Feedback -> Unit
        MainNavigationTarget.ProductInfo -> Unit
        MainNavigationTarget.TechSupportChat -> backStack.navigateSafe(SupportChatRoute)
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
