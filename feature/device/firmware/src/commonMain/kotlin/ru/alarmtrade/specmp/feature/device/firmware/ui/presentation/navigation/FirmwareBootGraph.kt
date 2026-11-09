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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.NoFirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveVehiclePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveEntry
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareSources
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareUpdateScreen

@Serializable
@SerialName("device.firmware.FirmwareUpdateRoute")
internal data object FirmwareUpdateRoute : NavKey

@Serializable
@SerialName("device.firmware.FirmwareArchiveRoute")
internal data class FirmwareArchiveRoute(val request: FirmwareArchiveRequest) : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(FirmwareUpdateRoute::class, FirmwareUpdateRoute.serializer())
            subclass(FirmwareArchiveRoute::class, FirmwareArchiveRoute.serializer())
        }
    }
}

/**
 * Граф экранов режима загрузчика: «Обновление прошивки» → «Архив прошивок».
 * Слот `bootContent` хоста устройства (`composeApp`):
 * хост показывает граф, пока блок в загрузчике или ждёт перезагрузку, и сам решает, когда уйти.
 *
 * @param onExit выход из всего flow (закрывает сессию); на экране обновления вызывается после подтверждения.
 * @param onOpenSupportChat открыть чат техподдержки (фича `support-chat`, app-level маршрут).
 * @param archivePicker канал версии, выбранной в архиве, к экрану обновления (singleton Koin из корня композиции).
 * @param filePicker выбор файла прошивки в файловом менеджере (платформа, `composeApp`).
 * @param vehiclePicker выбор автомобиля для CAN-фильтра архива (фича `vehicle-ui`, `composeApp`);
 *   `null` — без каталога.
 * @param sources источники прошивки на экране обновления; [FirmwareSources.FileOnly] — только файл.
 */
@Composable
fun FirmwareBootGraph(
    onExit: () -> Unit,
    onOpenSupportChat: () -> Unit,
    archivePicker: FirmwareArchivePicker,
    filePicker: FirmwareFilePicker = NoFirmwareFilePicker,
    vehiclePicker: ArchiveVehiclePicker? = null,
    sources: FirmwareSources = FirmwareSources.All,
) {
    val backStack = rememberNavBackStack(navConfig, FirmwareUpdateRoute)

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        onBack = { backStack.popSafe() },
        entryProvider = entryProvider<NavKey> {
            entry<FirmwareUpdateRoute> { _ ->
                FirmwareUpdateScreen(
                    onExit = onExit,
                    onOpenSupportChat = onOpenSupportChat,
                    filePicker = filePicker,
                    onOpenArchive = { request -> backStack.navigateSafe(FirmwareArchiveRoute(request)) },
                    sources = sources,
                )
            }
            entry<FirmwareArchiveRoute> { key ->
                FirmwareArchiveEntry(
                    request = key.request,
                    onPick = { version ->
                        // Экран обновления ниже в стеке не отрисован — выбор доходит до его ViewModel через picker.
                        archivePicker.pick(version)
                        backStack.popSafe()
                    },
                    onBack = { backStack.popSafe() },
                    vehiclePicker = vehiclePicker,
                )
            }
        },
    )
}
