package ru.alarmtrade.specmp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.navigation.replaceCurrent
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.ConfigurationPickCar
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.ConfigurationPickPurpose
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.ConfigurationPickerEntry
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassLearningStep
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone.CloneStep
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step.AutostartStepChrome
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step.AutostartStepNext
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation.FirmwareBootGraph
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.rememberFirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareCheckScreen
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host.DeviceInteractionHost
import ru.alarmtrade.specmp.feature.device.presentation.core.navigation.ConfigurationPickKind
import ru.alarmtrade.specmp.feature.device.presentation.core.navigation.ConfigurationPickRequest
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation.DeviceSelectionRoute
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.CheckStateChecklist
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.CheckStateChrome
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.CheckStateFastMount
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.ChecklistStatus
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.ChecklistTask
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CheckStateEntry
import ru.alarmtrade.specmp.feature.fastmount.domain.model.AutoTaskKind
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountChecklist
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTaskStatus
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.FastMountEntry
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.FastMountSelectionRoute
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.FastMountSlots
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountFileOpener
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountStep
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatRoute
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpenResult
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectEntry
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectMode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectRequest
import ru.alarmtrade.specmp.filepicker.rememberSupportChatFileOpener

/** Куда уйти после закрытия сессии блока. */
private enum class FastMountExit { BACK, CHOOSE_ANOTHER, ADVANCED_MOUNT }

/**
 * Быстрый монтаж внутри обёртки сессии; экраны других фич — слотами ([fastMountSlots]). Выход закрывает сессию;
 * дальше — назад, снова поиск («Выбрать другой блок») или поиск расширенного монтажа (блок не поддерживает быстрый).
 */
@Composable
internal fun FastMountGraph(backStack: NavBackStack<NavKey>) {
    var exit by rememberSaveable { mutableStateOf(FastMountExit.BACK) }
    val onOpenSupportChat = { backStack.navigateSafe(SupportChatRoute) }
    DeviceInteractionHost(
        onExit = {
            when (exit) {
                FastMountExit.BACK -> backStack.popSafe()
                FastMountExit.CHOOSE_ANOTHER -> backStack.replaceCurrent(FastMountSelectionRoute)
                FastMountExit.ADVANCED_MOUNT -> backStack.replaceCurrent(DeviceSelectionRoute)
            }
        },
        bootContent = { onExit ->
            FirmwareBootGraph(
                onExit = onExit,
                onOpenSupportChat = onOpenSupportChat,
                filePicker = rememberFirmwareFilePicker(),
                vehiclePicker = { deviceName, onPick, onPickerBack ->
                    ArchiveVehiclePicker(deviceName, onPick, onPickerBack)
                },
            )
        },
    ) { _, onExit ->
        FastMountEntry(
            onExit = onExit,
            onChooseAnother = {
                exit = FastMountExit.CHOOSE_ANOTHER
                onExit()
            },
            onOpenAdvancedMount = {
                exit = FastMountExit.ADVANCED_MOUNT
                onExit()
            },
            slots = fastMountSlots(onOpenSupportChat),
        )
    }
}

@Composable
private fun fastMountSlots(onOpenSupportChat: () -> Unit): FastMountSlots {
    val opener = rememberSupportChatFileOpener()
    return remember(opener) {
        FastMountSlots(
            firmwareUpdate = { onBack -> FirmwareCheckScreen(onBack = onBack) },
            vehiclePicker = { request, onPick, onBack ->
                VehicleSelectEntry(
                    request = VehicleSelectRequest(
                        mode = VehicleSelectMode.PICK,
                        catalog = VehicleCatalog.WITH_CONFIGURATIONS,
                        device = VehicleDeviceFilter.Device(request.deviceName),
                        firmwareCanCode = request.canCode,
                    ),
                    onPick = { car -> onPick(car.toMountCar()) },
                    onBack = onBack,
                )
            },
            configurationPicker = { request, onPick, onOpenFirmwareUpdate, onBack ->
                ConfigurationPickerEntry(
                    request = ConfigurationPickRequest(
                        kind = ConfigurationPickKind.CLOUD,
                        deviceName = request.deviceName,
                        firmwareCanCode = request.canCode,
                    ),
                    onPick = { source -> (source as? SettingsSource.RemoteConfiguration)?.let { onPick(it.id) } },
                    onOpenFirmwareUpdate = onOpenFirmwareUpdate,
                    onBack = onBack,
                    vehiclePicker = { catalog, deviceName, onCarPick, onCarBack ->
                        ConfiguratorVehicles(catalog, deviceName, onCarPick, onCarBack)
                    },
                    editor = { source, fileSaver, onOpenConfiguration, onWriteToDevice, onEditorExit ->
                        ConfiguratorEditor(source, fileSaver, onOpenConfiguration, onWriteToDevice, onEditorExit)
                    },
                    purpose = ConfigurationPickPurpose.FAST_MOUNT,
                    cloudCar = ConfigurationPickCar(request.car.id, request.car.title),
                )
            },
            bypass = { step, onNext ->
                BypassLearningStep(
                    key = step.key,
                    chrome = step.autostartChrome(),
                    next = AutostartStepNext(step.next.busy, step.next.error),
                    onNext = onNext,
                    onBack = step.onBack,
                    onOpenSupportChat = onOpenSupportChat,
                )
            },
            clone = { step, onNext ->
                CloneStep(
                    key = step.key,
                    chrome = step.autostartChrome(),
                    next = AutostartStepNext(step.next.busy, step.next.error),
                    onNext = onNext,
                    onBack = step.onBack,
                    onOpenSupportChat = onOpenSupportChat,
                )
            },
            systemCheck = { step, checklist, actions ->
                CheckStateEntry(
                    onBack = actions.onBack,
                    fastMount = CheckStateFastMount(
                        checklist = checklist.toCheckState(),
                        onNext = actions.onNext,
                        onBack = actions.onBack,
                        onSkip = { id -> id.toIntOrNull()?.let(actions.onSkip) },
                        onAnswer = { id, option -> id.toIntOrNull()?.let { actions.onAnswer(it, option) } },
                        chrome = CheckStateChrome(
                            topBar = { onBack -> step.frame.topBar(false, onBack) },
                            dock = step.frame.dock,
                        ),
                        nextBusy = step.next.busy,
                    ),
                )
            },
            fileOpener = MountFileOpener { name, bytes ->
                when (opener.openBytes(name, bytes)) {
                    SupportChatFileOpenResult.Opened -> true
                    SupportChatFileOpenResult.NoApp -> false
                    SupportChatFileOpenResult.Failed -> null
                }
            },
        )
    }
}

private fun MountStep.autostartChrome() = AutostartStepChrome(topBar = frame.topBar, dock = frame.dock)

private fun CarSummary.toMountCar(): MountCar = MountCar(
    id = id,
    brand = brand,
    model = model,
    years = if (years.first == years.last) "${years.first}" else "${years.first}–${years.last}",
    canCode = canCode,
)

/** Чек-лист мастера → чек-лист экрана проверки (строгий порядок, зоны авто-заданий). */
private fun MountChecklist.toCheckState(): CheckStateChecklist = CheckStateChecklist(
    tasks = tasks.map { task ->
        ChecklistTask(
            id = task.id.toString(),
            text = task.text,
            status = when (task.status) {
                MountTaskStatus.PENDING -> ChecklistStatus.PENDING
                MountTaskStatus.PASSED -> ChecklistStatus.PASSED
                MountTaskStatus.FAILED -> ChecklistStatus.FAILED
                MountTaskStatus.SKIPPED -> ChecklistStatus.SKIPPED
            },
            zone = task.auto?.zone(),
            guard = task.auto == AutoTaskKind.GUARD,
            options = task.options,
            active = task.active,
        )
    },
    sequential = true,
)

private fun AutoTaskKind.zone(): Zone? = when (this) {
    AutoTaskKind.GUARD -> null
    AutoTaskKind.DOOR_FRONT_LEFT -> Zone.DOOR_FRONT_LEFT
    AutoTaskKind.DOOR_REAR_LEFT -> Zone.DOOR_REAR_LEFT
    AutoTaskKind.DOOR_FRONT_RIGHT -> Zone.DOOR_FRONT_RIGHT
    AutoTaskKind.DOOR_REAR_RIGHT -> Zone.DOOR_REAR_RIGHT
    AutoTaskKind.HOOD -> Zone.HOOD
    AutoTaskKind.TRUNK -> Zone.TRUNK
    AutoTaskKind.IGNITION -> Zone.IGNITION
    AutoTaskKind.BRAKE -> Zone.BRAKE
    AutoTaskKind.HANDBRAKE -> Zone.HANDBRAKE
}
