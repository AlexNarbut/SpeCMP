package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionResult
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan.DeviceSelectionScreen

/** Внутренний роут графа фичи — экран поиска устройства. */
@Serializable
internal data object ScanDeviceRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ScanDeviceRoute::class, ScanDeviceRoute.serializer())
        }
    }
}

/**
 * Nav3-граф фичи поиска устройства (публичный вход для `composeApp`).
 * Собственный `NavDisplay` с per-entry `ViewModelStore` (VM очищается при уходе с экрана)
 * и собственный back stack — граф расширяется новыми экранами (напр. инструкции подключения)
 * без изменений в `composeApp`.
 *
 * @param onDeviceConnected устройство подключено — навигация приложения решает, куда вести.
 * @param onClose выход из графа поиска.
 */
@Composable
fun DeviceSelectionNavGraph(
    onDeviceConnected: (DeviceConnectionResult) -> Unit,
    onClose: () -> Unit,
) {
    val backStack = rememberNavBackStack(navConfig, ScanDeviceRoute)

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        onBack = {
            if (!backStack.popSafe()) onClose()
        },
        entryProvider = entryProvider<NavKey> {
            entry<ScanDeviceRoute> { _ ->
                DeviceSelectionScreen(
                    onDeviceConnected = onDeviceConnected,
                    onClose = onClose,
                )
            }
        },
    )
}
