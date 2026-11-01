@file:Suppress("MatchingDeclarationName")

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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan.DeviceSelectionScreen

/** Внутренний роут графа фичи — экран поиска устройства. */
@Serializable
@SerialName("device.selection.ScanDeviceRoute")
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
 * @param onSessionCreate сессия с устройством создана — навигация приложения решает, куда вести.
 * @param onClose выход из графа поиска; `null` — поиск — корневой экран (без «Назад»).
 * @param serialEnabled искать и COM-порты (приложение прошивки); сессия по порту создаётся как по USB.
 */
@Composable
fun DeviceSelectionNavGraph(onSessionCreate: () -> Unit, onClose: (() -> Unit)?, serialEnabled: Boolean = false) {
    val backStack = rememberNavBackStack(navConfig, ScanDeviceRoute)

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = {
            if (!backStack.popSafe()) onClose?.invoke()
        },
        entryProvider = entryProvider<NavKey> {
            entry<ScanDeviceRoute> { _ ->
                DeviceSelectionScreen(
                    onSessionCreate = onSessionCreate,
                    onClose = onClose,
                    serialEnabled = serialEnabled
                )
            }
        }
    )
}
