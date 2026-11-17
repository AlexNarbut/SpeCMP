package ru.alarmtrade.specmp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.ui.navigation.cmpBackTransition
import ru.alarmtrade.specmp.core.ui.navigation.cmpForwardTransition
import ru.alarmtrade.specmp.core.ui.navigation.cmpPredictiveBackTransition
import ru.alarmtrade.specmp.core.ui.navigation.currentRoute
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.navigation.replaceCurrent
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.feature.advancedmount.presentation.navigation.AdvancedMountNavGraph
import ru.alarmtrade.specmp.feature.advancedmount.presentation.navigation.AdvancedMountRoute
import ru.alarmtrade.specmp.feature.appsettings.presentation.navigation.AppSettingsEntry
import ru.alarmtrade.specmp.feature.appsettings.presentation.navigation.AppSettingsRoute
import ru.alarmtrade.specmp.feature.appupdate.presentation.navigation.AppUpdateEntry
import ru.alarmtrade.specmp.feature.appupdate.presentation.navigation.AppUpdateEntryPoint
import ru.alarmtrade.specmp.feature.appupdate.presentation.navigation.AppUpdateRoute
import ru.alarmtrade.specmp.feature.authstate.presentation.navigation.AuthStateNavGraph
import ru.alarmtrade.specmp.feature.authstate.presentation.screen.SessionCheckErrorScreen
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.ConfigurationPickerEntry
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.ConfiguratorEntry
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.ConfiguratorRoute
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassLearningEntry
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone.CloneEntry
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation.FirmwareBootGraph
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveEntry
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRoute
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host.DeviceInteractionHost
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing.PairingEntry
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.SystemDevicesEntry
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaver
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation.DeviceSelectionNavGraph
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation.DeviceSelectionRoute
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation.ConfigurationEditorEntry
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation.DeviceSettingsEntry
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CheckStateEntry
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.FastMountRoute
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.FastMountSelectionRoute
import ru.alarmtrade.specmp.feature.login.presentation.navigation.LoginNavGraph
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.navigation.MainNavGraph
import ru.alarmtrade.specmp.feature.main.presentation.navigation.MainRoute
import ru.alarmtrade.specmp.feature.productinfo.presentation.navigation.ProductInfoEntry
import ru.alarmtrade.specmp.feature.productinfo.presentation.navigation.ProductInfoRoute
import ru.alarmtrade.specmp.feature.push.ui.presentation.navigation.PushRulesEntry
import ru.alarmtrade.specmp.feature.push.ui.presentation.navigation.PushRulesRoute
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatEntry
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatRoute
import ru.alarmtrade.specmp.feature.techrequests.presentation.navigation.TechRequestPrefill
import ru.alarmtrade.specmp.feature.techrequests.presentation.navigation.TechRequestsEntry
import ru.alarmtrade.specmp.feature.techrequests.presentation.navigation.TechRequestsRoute
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType
import ru.alarmtrade.specmp.feature.vehicle.domain.model.fullName
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectEntry
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectMode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectRequest
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectRoute
import ru.alarmtrade.specmp.filepicker.rememberAppFirmwareFilePicker
import ru.alarmtrade.specmp.filepicker.rememberSettingsFilePicker
import ru.alarmtrade.specmp.filepicker.rememberSettingsFileSaver
import ru.alarmtrade.specmp.filepicker.rememberSupportChatAttachmentFiles
import ru.alarmtrade.specmp.filepicker.rememberSupportChatFileOpener
import ru.alarmtrade.specmp.filepicker.rememberSupportChatFilePicker
import ru.alarmtrade.specmp.filepicker.rememberSupportChatFileSaver

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MainRoute::class, MainRoute.serializer())
            subclass(SupportChatRoute::class, SupportChatRoute.serializer())
            subclass(TechRequestsRoute::class, TechRequestsRoute.serializer())
            subclass(ProductInfoRoute::class, ProductInfoRoute.serializer())
            subclass(AppUpdateRoute::class, AppUpdateRoute.serializer())
            subclass(DeviceSelectionRoute::class, DeviceSelectionRoute.serializer())
            subclass(AdvancedMountRoute::class, AdvancedMountRoute.serializer())
            subclass(FastMountSelectionRoute::class, FastMountSelectionRoute.serializer())
            subclass(FastMountRoute::class, FastMountRoute.serializer())
            subclass(VehicleSelectRoute::class, VehicleSelectRoute.serializer())
            subclass(ConfiguratorRoute::class, ConfiguratorRoute.serializer())
            subclass(AppSettingsRoute::class, AppSettingsRoute.serializer())
            subclass(PushRulesRoute::class, PushRulesRoute.serializer())
            subclass(FirmwareArchiveRoute::class, FirmwareArchiveRoute.serializer())
        }
    }
}

@Composable
fun AppNavigation() {
    AuthStateNavGraph(
        waitingState = { AppStartLoading() },
        noAuthState = {
            LoginNavGraph()
        },
        authedState = { AuthedNavGraph() },
        errorState = { onRetry ->
            SessionCheckErrorScreen(onRetry = onRetry)
        },
    )
}

@Composable
private fun AuthedNavGraph() {
    val backStack = rememberNavBackStack(navConfig, MainRoute)
    PushOpenEffect(backStack)
    val isChatOnTop by remember(backStack) { derivedStateOf { backStack.lastOrNull() == SupportChatRoute } }
    ChatNotificationsEffect(isChatOnTop)

    NavDisplay(
        transitionSpec = cmpForwardTransition(),
        popTransitionSpec = cmpBackTransition(),
        predictivePopTransitionSpec = cmpPredictiveBackTransition(),
        backStack = backStack,
        onBack = { backStack.popSafe() },
        entryDecorators = listOf(
            // Дефолтный декоратор NavDisplay: без него не работает SavedStateHandle.
            rememberSaveableStateHolderNavEntryDecorator(),
            // Per-entry ViewModelStoreOwner: VM живёт ровно столько, сколько запись
            // бэкстека, и очищается при pop (см. docs/navigation.md).
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider<NavKey> {
            entry<MainRoute> { _ ->
                MainNavGraph(
                    onNavigateToFeature = { target ->
                        navigateFromMain(target, backStack)
                    },
                )
            }
            entry<SupportChatRoute> { _ ->
                SupportChatEntry(
                    onBack = { backStack.popSafe() },
                    filePicker = rememberSupportChatFilePicker(),
                    fileSaver = rememberSupportChatFileSaver(),
                    attachmentFiles = rememberSupportChatAttachmentFiles(),
                    fileOpener = rememberSupportChatFileOpener(),
                )
            }
            entry<TechRequestsRoute> { key ->
                TechRequestsEntry(
                    onBack = { backStack.popSafe() },
                    onConnectDevice = { backStack.navigateSafe(DeviceSelectionRoute) },
                    onOpenChat = { backStack.navigateSafe(SupportChatRoute) },
                    prefill = key.prefill,
                )
            }
            entry<ProductInfoRoute> { key ->
                ProductInfoEntry(onBack = { backStack.popSafe() }, query = key.query, connectedId = key.connectedId)
            }
            entry<AppUpdateRoute> { key ->
                AppUpdateEntry(onBack = { backStack.popSafe() }, from = key.from, install = key.install)
            }
            entry<DeviceSelectionRoute> { _ ->
                DeviceSelectionNavGraph(
                    // Сессия создана — поиск заменяется flow работы с устройством:
                    // «Назад» из монтажа не возвращает в поиск.
                    onSessionCreate = { backStack.replaceCurrent(AdvancedMountRoute) },
                    onClose = { backStack.popSafe() },
                )
            }
            entry<FastMountSelectionRoute> { _ ->
                DeviceSelectionNavGraph(
                    // Как в расширенном монтаже: «Назад» из мастера не возвращает в поиск.
                    onSessionCreate = { backStack.replaceCurrent(FastMountRoute) },
                    onClose = { backStack.popSafe() },
                    alarmsOnly = true,
                )
            }
            entry<FastMountRoute> { _ -> FastMountGraph(backStack) }
            entry<ConfiguratorRoute> { _ ->
                // С главной сессии блока нет — без «Записать в блок» (он — в разделе «Конфигуратор» монтажа).
                AppConfigurator(onExit = { backStack.popSafe() })
            }
            entry<AppSettingsRoute> { _ ->
                AppSettingsEntry(
                    onBack = { backStack.popSafe() },
                    onPushRules = { backStack.navigateSafe(PushRulesRoute) },
                )
            }
            entry<PushRulesRoute> { _ -> PushRulesEntry(onBack = { backStack.popSafe() }) }
            entry<FirmwareArchiveRoute> { _ -> FirmwareArchiveHome(onBack = { backStack.popSafe() }) }
            entry<VehicleSelectRoute> { key ->
                VehicleSelectEntry(
                    request = key.request,
                    // Только свой маршрут: повторное «Выбрать» не закроет экран под ним.
                    onPick = { if (backStack.currentRoute == key) backStack.popSafe() },
                    onBack = { backStack.popSafe() },
                )
            }
            entry<AdvancedMountRoute> { _ -> AdvancedMountFlow(backStack) }
        },
    )
}

@Composable
private fun AdvancedMountFlow(backStack: NavBackStack<NavKey>) {
    // «Обновить» периферию: устройство уходит в режим прошивки — сессия закрывается, открывается поиск.
    var reopenSearch by rememberSaveable { mutableStateOf(false) }
    DeviceInteractionHost(
        onExit = {
            if (reopenSearch) backStack.replaceCurrent(DeviceSelectionRoute) else backStack.popSafe()
        },
        bootContent = { onExit ->
            FirmwareBootGraph(
                onExit = onExit,
                onOpenSupportChat = { backStack.navigateSafe(SupportChatRoute) },
                archivePicker = koinInject(),
                filePicker = rememberAppFirmwareFilePicker(),
                vehiclePicker = { deviceName, onPick, onPickerBack ->
                    ArchiveVehiclePicker(deviceName, onPick, onPickerBack)
                },
            )
        },
    ) { device, onExit ->
        val firmwareBoot = remember { PendingFirmwareBoot() }
        AdvancedMountNavGraph(
            onExit = onExit,
            onOpenSupportChat = { backStack.navigateSafe(SupportChatRoute) },
            onOpenProductInfo = { systemId ->
                backStack.navigateSafe(ProductInfoRoute(query = systemId.toString(), connectedId = systemId))
            },
            firmwareCheck = { onBack -> AssistantFirmwareCheck(firmwareBoot, onBack) },
            settingsEditor = { onBack, onOpenFirmwareUpdate, focusSettingId, openLoad ->
                DeviceSettingsEditor(
                    onExit = onBack,
                    onOpenFirmwareUpdate = onOpenFirmwareUpdate,
                    focusSettingId = focusSettingId,
                    openLoadMenu = openLoad,
                )
            },
            configurator = { onBack, onOpenFirmwareUpdate ->
                AppConfigurator(
                    onExit = onBack,
                    deviceEditor = { load, onEditorBack ->
                        DeviceSettingsEditor(
                            onExit = onEditorBack,
                            onOpenFirmwareUpdate = onOpenFirmwareUpdate,
                            loadOnOpen = load,
                        )
                    },
                )
            },
            bypassLearning = { onBack, onOpenClone, onOpenSupportChat ->
                BypassLearningEntry(
                    onBack = onBack,
                    onOpenClone = onOpenClone,
                    onOpenSupportChat = onOpenSupportChat,
                    onOpenTechRequest = backStack.techRequestOpener(),
                )
            },
            clone = { fromBypass, onBack, onOpenSupportChat ->
                CloneEntry(
                    fromBypass = fromBypass,
                    onBack = onBack,
                    onOpenSupportChat = onOpenSupportChat,
                    onOpenTechRequest = backStack.techRequestOpener(),
                )
            },
            checkState = { onBack -> CheckStateEntry(onBack = onBack) },
            systemDevices = { onBack, onOpenPairing ->
                SystemDevicesEntry(
                    onBack = onBack,
                    onOpenPairing = onOpenPairing,
                    onOpenDeviceSearch = {
                        reopenSearch = true
                        onExit()
                    },
                )
            },
            dynamicDevices = { onBack, onOpenSystemDevices ->
                PairingEntry(onBack = onBack, onOpenSystemDevices = onOpenSystemDevices)
            },
            vehicles = { onBack -> DeviceVehicles(device, onBack) },
            // Блок здесь подключён — кнопка «Подключить блок» не показывается.
            techRequests = { onBack ->
                TechRequestsEntry(
                    onBack = onBack,
                    onConnectDevice = {},
                    onOpenChat = { backStack.navigateSafe(SupportChatRoute) },
                )
            },
            assistant = { onBack, onOpenSection, onOpenSettings ->
                AdvancedMountAssistant(
                    device = device,
                    firmwareBoot = firmwareBoot,
                    onBack = onBack,
                    onOpenSection = onOpenSection,
                    onOpenSettings = onOpenSettings,
                    onOpenProductInfo = { target ->
                        backStack.navigateSafe(ProductInfoRoute(target.query, target.connectedId))
                    },
                ) {
                    backStack.navigateSafe(TechRequestsRoute(TechRequestPrefill(origin = null, text = "")))
                }
            },
            resumeAssistantTitle = rememberAssistantResume(device),
        )
    }
}

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
                firmwareCanCode = device.can?.code,
            ),
            onPick = {},
            onBack = onBack,
            onOpenFirmwareArchive = { archiveCanCode = it },
        )
    } else {
        // Архив поверх справочника: «назад» возвращает к карточке автомобиля, а не закрывает раздел.
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = { archiveCanCode = null },
        )
        FirmwareArchiveEntry(
            request = FirmwareArchiveRequest(
                moduleId = null,
                channel = FirmwareChannel.RELEASE,
                installedVersion = device.displayFirmwareVersion,
                deviceTitle = device.title,
                profile = UpdateProfile.Main,
                deviceName = device.systemName,
                canCode = canCode,
                viewOnly = true,
                deviceCanCode = device.can?.takeUnless { it.isFactory }?.code?.takeIf { it.isNotBlank() },
                deviceCanVersion = device.can?.takeUnless { it.isFactory }?.version,
                deviceCanProfile = device.can?.takeUnless { it.isFactory }?.firmwareVersion,
            ),
            onPick = {},
            onBack = { archiveCanCode = null },
            vehiclePicker = { deviceName, onPick, onPickerBack ->
                ArchiveVehiclePicker(deviceName, onPick, onPickerBack)
            },
        )
    }
}

@Composable
private fun AppConfigurator(
    onExit: () -> Unit,
    deviceEditor: (@Composable (load: SettingsSource, onBack: () -> Unit) -> Unit)? = null,
) {
    ConfiguratorEntry(
        onExit = onExit,
        vehiclePicker = { catalog, deviceName, onPick, onPickerBack ->
            ConfiguratorVehicles(catalog, deviceName, onPick, onPickerBack)
        },
        editor = { source, fileSaver, onOpenConfiguration, onWriteToDevice, onEditorExit ->
            ConfiguratorEditor(source, fileSaver, onOpenConfiguration, onWriteToDevice, onEditorExit)
        },
        filePicker = rememberSettingsFilePicker(),
        fileSaver = rememberSettingsFileSaver(),
        deviceEditor = deviceEditor,
    )
}

@Composable
internal fun ConfiguratorEditor(
    source: SettingsSource,
    fileSaver: SettingsFileSaver,
    onOpenConfiguration: (SettingsSource.LocalConfiguration) -> Unit,
    onWriteToDevice: ((SettingsSource) -> Unit)?,
    onExit: () -> Unit,
) {
    ConfigurationEditorEntry(
        source = source,
        onExit = onExit,
        vehiclePicker = { deviceName, onPick, onPickerBack ->
            ConfiguratorVehicles(VehicleCatalog.ALL, deviceName, onPick, onPickerBack)
        },
        fileSaver = fileSaver,
        onOpenConfiguration = onOpenConfiguration,
        onWriteToDevice = onWriteToDevice,
    )
}

@Composable
private fun DeviceSettingsEditor(
    onExit: () -> Unit,
    onOpenFirmwareUpdate: () -> Unit,
    loadOnOpen: SettingsSource? = null,
    focusSettingId: Int? = null,
    openLoadMenu: Boolean = false,
) {
    DeviceSettingsEntry(
        onExit = onExit,
        onOpenFirmwareUpdate = onOpenFirmwareUpdate,
        vehiclePicker = { deviceName, onPick, onPickerBack ->
            ConfiguratorVehicles(VehicleCatalog.ALL, deviceName, onPick, onPickerBack)
        },
        configurationPicker = { request, onPick, onOpenFirmware, onPickerBack ->
            ConfigurationPickerEntry(
                request = request,
                onPick = onPick,
                onOpenFirmwareUpdate = onOpenFirmware,
                onBack = onPickerBack,
                vehiclePicker = { catalog, deviceName, onCarPick, onCarBack ->
                    ConfiguratorVehicles(catalog, deviceName, onCarPick, onCarBack)
                },
                editor = { source, fileSaver, onOpenConfiguration, onWriteToDevice, onEditorExit ->
                    ConfiguratorEditor(source, fileSaver, onOpenConfiguration, onWriteToDevice, onEditorExit)
                },
                fileSaver = rememberSettingsFileSaver(),
            )
        },
        filePicker = rememberSettingsFilePicker(),
        fileSaver = rememberSettingsFileSaver(),
        loadOnOpen = loadOnOpen,
        focusSettingId = focusSettingId,
        openLoadMenu = openLoadMenu,
    )
}

@Composable
internal fun ConfiguratorVehicles(
    catalog: VehicleCatalog,
    deviceName: String?,
    onPick: (CarSummary) -> Unit,
    onBack: () -> Unit,
) {
    VehicleSelectEntry(
        request = VehicleSelectRequest(
            mode = VehicleSelectMode.PICK,
            catalog = catalog,
            device = deviceName?.let { VehicleDeviceFilter.Device(it) }
                ?: VehicleDeviceFilter.Type(VehicleDeviceType.ALARM).takeIf {
                    catalog == VehicleCatalog.WITH_CONFIGURATIONS
                },
        ),
        onPick = onPick,
        onBack = onBack,
    )
}

@Composable
internal fun ArchiveVehiclePicker(
    deviceName: String?,
    onPick: (canCode: String, carTitle: String) -> Unit,
    onBack: () -> Unit,
) {
    VehicleSelectEntry(
        request = VehicleSelectRequest(
            mode = VehicleSelectMode.PICK,
            catalog = VehicleCatalog.ALL,
            device = deviceName?.let { VehicleDeviceFilter.Device(it) },
        ),
        onPick = { car ->
            onPick(car.canCode, car.fullName())
        },
        onBack = onBack,
    )
}

private fun navigateFromMain(target: MainNavigationTarget, backStack: NavBackStack<NavKey>) {
    when (target) {
        MainNavigationTarget.QuickMount -> backStack.navigateSafe(FastMountSelectionRoute)

        MainNavigationTarget.AdvancedMount -> backStack.navigateSafe(DeviceSelectionRoute)

        MainNavigationTarget.Configurator -> backStack.navigateSafe(ConfiguratorRoute)

        MainNavigationTarget.FirmwareArchive -> backStack.navigateSafe(FirmwareArchiveRoute)

        MainNavigationTarget.Cars -> backStack.navigateSafe(
            VehicleSelectRoute(VehicleSelectRequest(VehicleSelectMode.VIEW, VehicleCatalog.ALL, device = null)),
        )

        MainNavigationTarget.TechSupportChat -> backStack.navigateSafe(SupportChatRoute)

        MainNavigationTarget.TechRequests -> backStack.navigateSafe(TechRequestsRoute())

        MainNavigationTarget.ProductInfo -> backStack.navigateSafe(ProductInfoRoute())

        MainNavigationTarget.Settings -> backStack.navigateSafe(AppSettingsRoute)

        MainNavigationTarget.AppUpdate -> backStack.navigateSafe(AppUpdateRoute(AppUpdateEntryPoint.MENU))

        MainNavigationTarget.AppUpdatePrompt -> backStack.navigateSafe(AppUpdateRoute(AppUpdateEntryPoint.PROMPT))

        MainNavigationTarget.AppUpdateInstall ->
            backStack.navigateSafe(AppUpdateRoute(AppUpdateEntryPoint.PROMPT, install = true))

        MainNavigationTarget.Feedback,
        MainNavigationTarget.Warehouse1C,
        -> Unit
    }
}

@Composable
private fun AppStartLoading() {
    CmpBox(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CmpCircularProgressIndicator()
    }
}

/** «Заявка на техпомощь» из сценария (обход, Clone): экран заявок поверх, сессия с блоком остаётся открытой. */
internal fun NavBackStack<NavKey>.techRequestOpener(): (origin: String, text: String) -> Unit = { origin, text ->
    navigateSafe(TechRequestsRoute(TechRequestPrefill(origin, text)))
}
