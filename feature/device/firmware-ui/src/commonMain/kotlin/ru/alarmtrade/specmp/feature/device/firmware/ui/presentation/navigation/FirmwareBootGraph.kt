@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation

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
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateScreen

@Serializable
internal data object FirmwareUpdateRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(FirmwareUpdateRoute::class, FirmwareUpdateRoute.serializer())
        }
    }
}

/**
 * Граф экранов режима загрузчика: «Обновление прошивки». Слот `bootContent` хоста устройства (`composeApp`):
 * хост показывает граф, пока блок в загрузчике или ждёт перезагрузку, и сам решает, когда уйти.
 *
 * @param onExit выход из всего flow (закрывает сессию); на экране обновления вызывается после подтверждения.
 * @param onOpenSupportChat открыть чат техподдержки (фича `support-chat`, app-level маршрут).
 */
@Composable
fun FirmwareBootGraph(onExit: () -> Unit, onOpenSupportChat: () -> Unit) {
    val backStack = rememberNavBackStack(navConfig, FirmwareUpdateRoute)

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = { backStack.popSafe() },
        entryProvider = entryProvider<NavKey> {
            entry<FirmwareUpdateRoute> { _ ->
                FirmwareUpdateScreen(
                    onExit = onExit,
                    onOpenSupportChat = onOpenSupportChat,
                    onOpenArchive = {}
                )
            }
        }
    )
}
