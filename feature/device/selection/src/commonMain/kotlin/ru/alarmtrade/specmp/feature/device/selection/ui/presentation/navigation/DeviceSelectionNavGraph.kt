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
import ru.alarmtrade.specmp.core.ui.navigation.cmpBackTransition
import ru.alarmtrade.specmp.core.ui.navigation.cmpForwardTransition
import ru.alarmtrade.specmp.core.ui.navigation.cmpPredictiveBackTransition
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan.DeviceSelectionScreen

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

@Composable
fun DeviceSelectionNavGraph(
    onSessionCreate: () -> Unit,
    onClose: (() -> Unit)?,
    serialEnabled: Boolean = false,
    alarmsOnly: Boolean = false,
) {
    val backStack = rememberNavBackStack(navConfig, ScanDeviceRoute)

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
        onBack = {
            if (!backStack.popSafe()) onClose?.invoke()
        },
        entryProvider = entryProvider<NavKey> {
            entry<ScanDeviceRoute> { _ ->
                DeviceSelectionScreen(
                    onSessionCreate = onSessionCreate,
                    onClose = onClose,
                    serialEnabled = serialEnabled,
                    alarmsOnly = alarmsOnly,
                )
            }
        },
    )
}
