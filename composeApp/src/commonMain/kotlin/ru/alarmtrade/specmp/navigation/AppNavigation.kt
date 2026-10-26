package ru.alarmtrade.specmp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
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
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation.FirmwareBootGraph
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveEntry
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareCheckScreen
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host.DeviceInteractionHost
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation.DeviceSelectionNavGraph
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation.DeviceSelectionRoute
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation.DeviceSettingsEntry
import ru.alarmtrade.specmp.feature.login.presentation.navigation.LoginNavGraph
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.navigation.MainNavGraph
import ru.alarmtrade.specmp.feature.main.presentation.navigation.MainRoute
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatEntry
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatRoute
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectEntry
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectMode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectRequest
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectRoute
import ru.alarmtrade.specmp.filepicker.rememberFirmwareFilePicker
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
            subclass(VehicleSelectRoute::class, VehicleSelectRoute.serializer())
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
            entry<VehicleSelectRoute> { key ->
                VehicleSelectEntry(
                    request = key.request,
                    onPick = { backStack.popSafe() },
                    onBack = { backStack.popSafe() }
                )
            }
            entry<AdvancedMountRoute> { _ ->
                // Обёртка interaction: дисконнект — экран поверх графа, выход — закрытие сессии.
                DeviceInteractionHost(
                    onExit = { backStack.popSafe() },
                    bootContent = { onExit ->
                        FirmwareBootGraph(
                            onExit = onExit,
                            onOpenSupportChat = { backStack.navigateSafe(SupportChatRoute) },
                            filePicker = rememberFirmwareFilePicker(),
                            vehiclePicker = { deviceName, onPick, onPickerBack ->
                                ArchiveVehiclePicker(deviceName, onPick, onPickerBack)
                            }
                        )
                    }
                ) { device, onExit ->
                    AdvancedMountNavGraph(
                        onExit = onExit,
                        onOpenSupportChat = { backStack.navigateSafe(SupportChatRoute) },
                        firmwareCheck = { onBack -> FirmwareCheckScreen(onBack = onBack) },
                        settingsEditor = { onBack, onOpenFirmwareUpdate ->
                            DeviceSettingsEntry(
                                onExit = onBack,
                                onOpenFirmwareUpdate = onOpenFirmwareUpdate,
                                // CAN-код из каталога: автомобили под модель блока, выбор подставляет код и carId.
                                vehiclePicker = { deviceName, onPick, onPickerBack ->
                                    VehicleSelectEntry(
                                        request = VehicleSelectRequest(
                                            mode = VehicleSelectMode.PICK,
                                            catalog = VehicleCatalog.ALL,
                                            device = deviceName?.let { VehicleDeviceFilter.Device(it) }
                                        ),
                                        onPick = onPick,
                                        onBack = onPickerBack
                                    )
                                }
                            )
                        },
                        vehicles = { onBack -> DeviceVehicles(device, onBack) }
                    )
                }
            }
        }
    )
}

/**
 * Справочник автомобилей под подключённый блок (подсистемы модели, CAN-код прошивки — в поиске по CAN). «Прошивки
 * для CAN» в карточке — архив основной программы блока с этим CAN-фильтром, только просмотр.
 */
@Composable
private fun DeviceVehicles(device: AlarmDevice, onBack: () -> Unit) {
    var archiveCanCode by rememberSaveable { mutableStateOf<String?>(null) }
    val canCode = archiveCanCode
    if (canCode == null) {
        VehicleSelectEntry(
            request = VehicleSelectRequest(
                mode = VehicleSelectMode.VIEW,
                catalog = VehicleCatalog.ALL,
                device = VehicleDeviceFilter.Device(device.systemName),
                firmwareCanCode = device.can?.code
            ),
            onPick = {},
            onBack = onBack,
            onOpenFirmwareArchive = { archiveCanCode = it }
        )
    } else {
        // Архив поверх справочника: «назад» возвращает к карточке автомобиля, а не закрывает раздел.
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = { archiveCanCode = null }
        )
        FirmwareArchiveEntry(
            request = FirmwareArchiveRequest(
                moduleId = null,
                channel = FirmwareChannel.RELEASE,
                installedVersion = device.displayFirmwareVersion,
                deviceTitle = "${device.systemName}  ${device.systemId}",
                profile = UpdateProfile.Main,
                deviceName = device.systemName,
                canCode = canCode,
                viewOnly = true
            ),
            onPick = {},
            onBack = { archiveCanCode = null },
            vehiclePicker = { deviceName, onPick, onPickerBack ->
                ArchiveVehiclePicker(deviceName, onPick, onPickerBack)
            }
        )
    }
}

/** Выбор автомобиля для CAN-фильтра архива прошивок: каталог под модель блока, результат — CAN-код и название. */
@Composable
private fun ArchiveVehiclePicker(
    deviceName: String?,
    onPick: (canCode: String, carTitle: String) -> Unit,
    onBack: () -> Unit
) {
    VehicleSelectEntry(
        request = VehicleSelectRequest(
            mode = VehicleSelectMode.PICK,
            catalog = VehicleCatalog.ALL,
            device = deviceName?.let { VehicleDeviceFilter.Device(it) }
        ),
        onPick = { car ->
            onPick(car.canCode, listOf(car.brand, car.model, car.name).filter { it.isNotBlank() }.joinToString(" "))
        },
        onBack = onBack
    )
}

private fun navigateFromMain(target: MainNavigationTarget, backStack: NavBackStack<NavKey>) {
    when (target) {
        MainNavigationTarget.QuickMount -> Unit

        MainNavigationTarget.AdvancedMount -> backStack.navigateSafe(DeviceSelectionRoute)

        MainNavigationTarget.Configurator -> Unit

        MainNavigationTarget.FirmwareArchive -> Unit

        // Справочник автомобилей: блок не подключён — сначала выбор типа устройства.
        MainNavigationTarget.Cars -> backStack.navigateSafe(
            VehicleSelectRoute(VehicleSelectRequest(VehicleSelectMode.VIEW, VehicleCatalog.ALL, device = null))
        )

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
