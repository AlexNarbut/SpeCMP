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
import ru.alarmtrade.specmp.core.ui.navigation.replaceCurrent
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.feature.advancedmount.presentation.navigation.AdvancedMountNavGraph
import ru.alarmtrade.specmp.feature.advancedmount.presentation.navigation.AdvancedMountRoute
import ru.alarmtrade.specmp.feature.authstate.presentation.navigation.AuthStateNavGraph
import ru.alarmtrade.specmp.feature.authstate.presentation.screen.SessionCheckErrorScreen
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareCheckScreen
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host.DeviceInteractionHost
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation.DeviceSelectionNavGraph
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation.DeviceSelectionRoute
import ru.alarmtrade.specmp.feature.login.presentation.navigation.LoginNavGraph
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.navigation.MainNavGraph
import ru.alarmtrade.specmp.feature.main.presentation.navigation.MainRoute
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatEntry
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatRoute
import ru.alarmtrade.specmp.filepicker.rememberSupportChatAttachmentFiles
import ru.alarmtrade.specmp.filepicker.rememberSupportChatFileOpener
import ru.alarmtrade.specmp.filepicker.rememberSupportChatFilePicker
import ru.alarmtrade.specmp.filepicker.rememberSupportChatFileSaver

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MainRoute::class, MainRoute.serializer())
            subclass(SupportChatRoute::class, SupportChatRoute.serializer())
            subclass(DeviceSelectionRoute::class, DeviceSelectionRoute.serializer())
            subclass(AdvancedMountRoute::class, AdvancedMountRoute.serializer())
        }
    }
}

@Composable
fun AppNavigation(onLocaleChange: (AppLocale) -> Unit) {
    AuthStateNavGraph(
        waitingState = { AppStartLoading() },
        noAuthState = {
            LoginNavGraph(onLocaleChange = onLocaleChange)
        },
        authedState = { AuthedNavGraph() },
        errorState = { onRetry ->
            SessionCheckErrorScreen(onRetry = onRetry)
        }
    )
}

// Хост авторизованной зоны: держит app-level переходы между графами фич
// (main / support-chat / device-selection). Каждый entry — целый граф фичи.
@Composable
private fun AuthedNavGraph() {
    val backStack = rememberNavBackStack(navConfig, MainRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.popSafe() },
        entryDecorators = listOf(
            // Дефолтный декоратор NavDisplay: без него не работает SavedStateHandle.
            rememberSaveableStateHolderNavEntryDecorator(),
            // Per-entry ViewModelStoreOwner: VM живёт ровно столько, сколько запись
            // бэкстека, и очищается при pop (см. docs/navigation.md).
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider<NavKey> {
            entry<MainRoute> { _ ->
                MainNavGraph(
                    onNavigateToFeature = { target ->
                        navigateFromMain(target, backStack)
                    }
                )
            }
            entry<SupportChatRoute> { _ ->
                SupportChatEntry(
                    onBack = { backStack.popSafe() },
                    filePicker = rememberSupportChatFilePicker(),
                    fileSaver = rememberSupportChatFileSaver(),
                    attachmentFiles = rememberSupportChatAttachmentFiles(),
                    fileOpener = rememberSupportChatFileOpener()
                )
            }
            entry<DeviceSelectionRoute> { _ ->
                DeviceSelectionNavGraph(
                    // Сессия создана — поиск заменяется flow работы с устройством:
                    // «Назад» из монтажа не возвращает в поиск.
                    onSessionCreate = { _ -> backStack.replaceCurrent(AdvancedMountRoute) },
                    onClose = { backStack.popSafe() }
                )
            }
            entry<AdvancedMountRoute> { _ ->
                // Обёртка interaction: дисконнект — экран поверх графа, выход — закрытие сессии.
                DeviceInteractionHost(onExit = { backStack.popSafe() }) { _, onExit ->
                    AdvancedMountNavGraph(
                        onExit = onExit,
                        onOpenSupportChat = { backStack.navigateSafe(SupportChatRoute) },
                        firmwareCheck = { onBack -> FirmwareCheckScreen(onBack = onBack) }
                    )
                }
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
        contentAlignment = Alignment.Center
    ) {
        CmpCircularProgressIndicator(color = cmpThemeColors().primary)
    }
}
