package ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_btn_open
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_msg_copied
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_msg_deleted
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_msg_failed
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_msg_file_failed
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_msg_file_saved
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_msg_renamed
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_msg_saved_mine
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.navigation.replaceCurrent
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarDuration
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarResult
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.feature.configurator.domain.repository.toSource
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud.CloudCardActions
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud.CloudCardContent
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud.CloudCardEffect
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud.CloudCardScreen
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud.CloudCardState
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud.CloudCardViewModel
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud.CloudListScreen
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud.CloudListViewModel
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud.toSource
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine.MineActions
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine.MineEffect
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine.MineMessage
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine.MineScreen
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine.MineViewModel
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models.ModelsEffect
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models.ModelsScreen
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models.ModelsViewModel
import ru.alarmtrade.specmp.feature.device.presentation.core.navigation.ConfigurationPickKind
import ru.alarmtrade.specmp.feature.device.presentation.core.navigation.ConfigurationPickRequest
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.NoSettingsFilePicker
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFilePickResult
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFilePicker
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaver
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.fullName

/** `deviceName == null` — каталог для всех моделей блока. */
typealias ConfiguratorVehiclePicker = @Composable (
    catalog: VehicleCatalog,
    deviceName: String?,
    onPick: (CarSummary) -> Unit,
    onBack: () -> Unit,
) -> Unit

/** `onWriteToDevice == null` — «Записать в блок» не поддерживается. */
typealias ConfigurationEditor = @Composable (
    source: SettingsSource,
    fileSaver: SettingsFileSaver,
    onOpenConfiguration: (SettingsSource.LocalConfiguration) -> Unit,
    onWriteToDevice: ((SettingsSource) -> Unit)?,
    onExit: () -> Unit,
) -> Unit

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MineListRoute::class, MineListRoute.serializer())
            subclass(ModelsRoute::class, ModelsRoute.serializer())
            subclass(EditorRoute::class, EditorRoute.serializer())
            subclass(CloudCarsRoute::class, CloudCarsRoute.serializer())
            subclass(CloudListRoute::class, CloudListRoute.serializer())
            subclass(CloudCardRoute::class, CloudCardRoute.serializer())
            subclass(DeviceWriteRoute::class, DeviceWriteRoute.serializer())
        }
    }
}

/** [deviceEditor] `null` — «Записать в блок» не поддерживается; пункт виден только при связи с блоком. */
@Composable
fun ConfiguratorEntry(
    onExit: () -> Unit,
    vehiclePicker: ConfiguratorVehiclePicker,
    editor: ConfigurationEditor,
    filePicker: SettingsFilePicker,
    fileSaver: SettingsFileSaver,
    deviceEditor: (@Composable (load: SettingsSource, onBack: () -> Unit) -> Unit)? = null,
) {
    ConfiguratorGraph(
        start = MineListRoute,
        onExit = onExit,
        vehiclePicker = vehiclePicker,
        editor = editor,
        mode = GraphMode.Full(filePicker, fileSaver, deviceEditor),
    )
}

@Composable
fun ConfigurationPickerEntry(
    request: ConfigurationPickRequest,
    onPick: (SettingsSource) -> Unit,
    onOpenFirmwareUpdate: () -> Unit,
    onBack: () -> Unit,
    vehiclePicker: ConfiguratorVehiclePicker,
    editor: ConfigurationEditor,
    fileSaver: SettingsFileSaver,
    purpose: ConfigurationPickPurpose = ConfigurationPickPurpose.LOAD,
    cloudCar: ConfigurationPickCar? = null,
) {
    ConfiguratorGraph(
        start = when {
            request.kind == ConfigurationPickKind.MINE -> MineListRoute
            cloudCar != null -> CloudListRoute(cloudCar.id, cloudCar.title)
            else -> CloudCarsRoute
        },
        onExit = onBack,
        vehiclePicker = vehiclePicker,
        editor = editor,
        mode = GraphMode.Pick(request, onPick, onOpenFirmwareUpdate, purpose, fileSaver),
    )
}

enum class ConfigurationPickPurpose { LOAD, FAST_MOUNT }

/** Автомобиль, уже выбранный вызывающим: облако открывается сразу списком конфигураций для него. */
data class ConfigurationPickCar(val id: Int, val title: String)

private sealed interface GraphMode {
    val fileSaver: SettingsFileSaver

    class Full(
        val filePicker: SettingsFilePicker,
        override val fileSaver: SettingsFileSaver,
        val deviceEditor: (@Composable (load: SettingsSource, onBack: () -> Unit) -> Unit)?,
    ) : GraphMode

    class Pick(
        val request: ConfigurationPickRequest,
        val onPick: (SettingsSource) -> Unit,
        val onOpenFirmwareUpdate: () -> Unit,
        val purpose: ConfigurationPickPurpose,
        override val fileSaver: SettingsFileSaver,
    ) : GraphMode
}

// Слот выбора автомобиля — в разных записях графа (CAN-код в редакторе и каталог облака) с разными параметрами: это
// разные экраны, общее состояние им не нужно.
@Suppress("ktlint:compose:content-slot-reused")
@Composable
private fun ConfiguratorGraph(
    start: NavKey,
    onExit: () -> Unit,
    vehiclePicker: ConfiguratorVehiclePicker,
    editor: ConfigurationEditor,
    mode: GraphMode,
) {
    val backStack = rememberNavBackStack(navConfig, start)
    val back = { if (!backStack.popSafe()) onExit() }
    NavDisplay(
        backStack = backStack,
        onBack = back,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider<NavKey> {
            entry<MineListRoute> { _ -> Opaque { MineEntry(backStack, mode, back) } }
            entry<ModelsRoute> { key -> Opaque { ModelsEntry(key, backStack, back) } }
            entry<EditorRoute> { key ->
                val full = mode as? GraphMode.Full
                editor(
                    key.source.toSource(),
                    mode.fileSaver,
                    { opened -> opened.toKey()?.let { backStack.navigateSafe(EditorRoute(it)) } },
                    full?.deviceEditor?.let {
                        { source -> source.toKey()?.let { backStack.navigateSafe(DeviceWriteRoute(it)) } }
                    },
                    back,
                )
            }
            entry<CloudCarsRoute> { _ ->
                Opaque {
                    vehiclePicker(
                        VehicleCatalog.WITH_CONFIGURATIONS,
                        (mode as? GraphMode.Pick)?.request?.deviceName,
                        { car -> backStack.navigateSafe(CloudListRoute(car.id, car.fullName())) },
                        back,
                    )
                }
            }
            entry<CloudListRoute> { key -> Opaque { CloudListEntry(key, backStack, mode, back) } }
            entry<CloudCardRoute> { key ->
                Opaque {
                    val snackbarHostState = remember { CmpSnackbarHostState() }
                    val (state, actions) = cloudCard(key.id, backStack, mode, snackbarHostState)
                    CloudCardScreen(state, key.title, back, actions, snackbarHostState)
                }
            }
            entry<DeviceWriteRoute> { key ->
                (mode as? GraphMode.Full)?.deviceEditor?.invoke(key.source.toSource(), back)
            }
        },
    )
}

/** Непрозрачный фон записи: на переходе NavDisplay без него старая запись просвечивает. */
@Composable
private fun Opaque(content: @Composable () -> Unit) {
    CmpBox(modifier = Modifier.fillMaxSize().background(CmpTheme.colors.backgroundPagePrimary)) { content() }
}

@Composable
private fun MineEntry(backStack: NavBackStack<NavKey>, mode: GraphMode, onBack: () -> Unit) {
    val pick = mode as? GraphMode.Pick
    val full = mode as? GraphMode.Full
    val viewModel = koinViewModel<MineViewModel>(parameters = { parametersOf(pick?.request?.deviceName) })
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { CmpSnackbarHostState() }
    val filePicker = full?.filePicker ?: NoSettingsFilePicker
    val fileFailed = stringResource(Strings.string.configurator_common_msg_file_failed)

    LaunchedEffect(filePicker) {
        filePicker.results.collect { result ->
            when (result) {
                is SettingsFilePickResult.Picked -> backStack.navigateSafe(
                    ModelsRoute(SourceKey.File(result.path, "", result.name, result.inPlace)),
                )

                SettingsFilePickResult.Failed -> snackbarHostState.showSnackbar(fileFailed)

                SettingsFilePickResult.Cancelled -> Unit
            }
        }
    }
    val scope = rememberCoroutineScope()
    viewModel.sideEffect.collectSideEffect { effect ->
        scope.launch {
            when (effect) {
                is MineEffect.Message -> snackbarHostState.showSnackbar(getString(effect.message.text()))

                is MineEffect.Copied -> {
                    val result = snackbarHostState.showSnackbar(
                        getString(Strings.string.configurator_common_msg_copied),
                        actionLabel = getString(Strings.string.configurator_common_btn_open),
                        duration = CmpSnackbarDuration.Long,
                    )
                    if (result == CmpSnackbarResult.ActionPerformed) {
                        effect.configuration.toKey()?.let { backStack.navigateSafe(EditorRoute(it)) }
                    }
                }

                // Системное «Сохранить как» живёт в композиции (launcher), поэтому сохраняет UI, а результат уходит
                // во VM — как в редакторе настроек.
                is MineEffect.ExportFile ->
                    viewModel.onFileExported(mode.fileSaver.save(effect.path, effect.fileName))

                is MineEffect.FileSaved -> snackbarHostState.showSnackbar(
                    getString(Strings.string.configurator_common_msg_file_saved, effect.name),
                )
            }
        }
    }
    val actions = remember(viewModel, backStack) { mineActions(viewModel, backStack, mode, filePicker, onBack) }
    MineScreen(
        state = if (full?.deviceEditor == null) state.copy(connectedDeviceName = null) else state,
        actions = actions,
        snackbarHostState = snackbarHostState,
    )
}

private fun mineActions(
    viewModel: MineViewModel,
    backStack: NavBackStack<NavKey>,
    mode: GraphMode,
    filePicker: SettingsFilePicker,
    onBack: () -> Unit,
): MineActions {
    val pick = mode as? GraphMode.Pick
    val full = mode as? GraphMode.Full
    return MineActions(
        onBack = onBack,
        onOpen = { item ->
            if (pick != null) {
                pick.onPick(item.toSource())
            } else {
                item.toSource().toKey()?.let { backStack.navigateSafe(EditorRoute(it)) }
            }
        },
        onSelect = viewModel::onSelect,
        onQueryChange = viewModel::onQueryChange,
        onModelFilter = viewModel::onModelFilter,
        onResetFilter = viewModel::onResetFilter,
        onLoadMore = viewModel::onLoadMore,
        onRetry = viewModel::onRetry,
        onMenu = viewModel::onMenu,
        onCreateRequest = viewModel::onCreateRequest,
        onCreateScratch = {
            viewModel.onDialogDismiss()
            backStack.navigateSafe(ModelsRoute())
        },
        onCreateCloud = {
            viewModel.onDialogDismiss()
            backStack.navigateSafe(CloudCarsRoute)
        },
        onCreateFile = {
            viewModel.onDialogDismiss()
            filePicker.launch()
        },
        onRenameRequest = viewModel::onRenameRequest,
        onRenameChange = viewModel::onRenameChange,
        onRenameSubmit = viewModel::onRenameSubmit,
        onCopy = viewModel::onCopy,
        onExport = viewModel::onExport,
        onWrite = { item ->
            viewModel.onDialogDismiss()
            if (full?.deviceEditor != null) {
                item.toSource().toKey()?.let { backStack.navigateSafe(DeviceWriteRoute(it)) }
            }
        },
        onDeleteRequest = viewModel::onDeleteRequest,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDialogDismiss = viewModel::onDialogDismiss,
        onListWidthChange = viewModel::onListWidthChange,
    )
}

@Composable
private fun ModelsEntry(key: ModelsRoute, backStack: NavBackStack<NavKey>, onBack: () -> Unit) {
    val viewModel = koinViewModel<ModelsViewModel>(parameters = { parametersOf(key.file) })
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            // Модель выбрана — вместо выбора модели открывается редактор: «назад» из него ведёт к списку.
            is ModelsEffect.Open -> backStack.replaceCurrent(EditorRoute(effect.source))
        }
    }
    ModelsScreen(
        state = state,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onPick = viewModel::onPick,
        onRecentRemove = viewModel::onRecentRemove,
        onRetry = viewModel::onRetry,
        onDialogDismiss = viewModel::onDialogDismiss,
    )
}

@Composable
private fun CloudListEntry(key: CloudListRoute, backStack: NavBackStack<NavKey>, mode: GraphMode, onBack: () -> Unit) {
    val deviceName = (mode as? GraphMode.Pick)?.request?.deviceName.orEmpty()
    val viewModel = koinViewModel<CloudListViewModel>(parameters = { parametersOf(key.carId, deviceName) })
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { CmpSnackbarHostState() }
    CloudListScreen(
        state = state,
        carTitle = key.carTitle,
        onBack = onBack,
        onOrder = viewModel::onOrder,
        onModelFilter = viewModel::onModelFilter,
        onRetry = viewModel::onRetry,
        onOpen = { item -> backStack.navigateSafe(CloudCardRoute(item.id, item.name)) },
        onSelect = viewModel::onSelect,
        onListWidthChange = viewModel::onListWidthChange,
        snackbarHostState = snackbarHostState,
    ) { id ->
        val (cardState, actions) = cloudCard(id, backStack, mode, snackbarHostState)
        CloudCardContent(cardState, actions)
    }
}

/** Своя VM на [id]: правая панель меняет выбранную. */
@Composable
private fun cloudCard(
    id: Int,
    backStack: NavBackStack<NavKey>,
    mode: GraphMode,
    snackbarHostState: CmpSnackbarHostState,
): Pair<CloudCardState, CloudCardActions> {
    val pick = mode as? GraphMode.Pick
    val full = mode as? GraphMode.Full
    val viewModel = koinViewModel<CloudCardViewModel>(
        key = "cloud_card_$id",
        parameters = { parametersOf(id, pick?.request?.firmwareCanCode) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            is CloudCardEffect.OpenUrl -> safeRun { uriHandler.openUri(effect.url) }

            is CloudCardEffect.SavedToMine -> scope.launch {
                val result = snackbarHostState.showSnackbar(
                    getString(Strings.string.configurator_common_msg_saved_mine),
                    actionLabel = if (full != null) getString(Strings.string.configurator_common_btn_open) else null,
                    duration = CmpSnackbarDuration.Long,
                )
                if (result == CmpSnackbarResult.ActionPerformed) {
                    effect.configuration.toKey()?.let { backStack.navigateSafe(EditorRoute(it)) }
                }
            }

            CloudCardEffect.SaveFailed -> scope.launch {
                snackbarHostState.showSnackbar(getString(Strings.string.configurator_common_msg_failed))
            }
        }
    }
    val actions = remember(viewModel, backStack) {
        CloudCardActions(
            onRetry = viewModel::onRetry,
            onDescriptionToggle = viewModel::onDescriptionToggle,
            onAttachmentOpen = viewModel::onAttachmentOpen,
            onViewerClose = viewModel::onViewerClose,
            onSaveRequest = viewModel::onSaveRequest,
            onSaveChange = viewModel::onSaveChange,
            onSaveSubmit = viewModel::onSaveSubmit,
            onSaveDismiss = viewModel::onSaveDismiss,
            onLoad = pick?.let { { card -> it.onPick(card.toSource()) } },
            onOpenSettings = full?.let {
                { card -> card.toSource().toKey()?.let { key -> backStack.navigateSafe(EditorRoute(key)) } }
            },
            onWrite = full?.deviceEditor?.let {
                { card -> card.toSource().toKey()?.let { key -> backStack.navigateSafe(DeviceWriteRoute(key)) } }
            },
            onOpenFirmwareUpdate = pick?.onOpenFirmwareUpdate,
            selectForMount = pick?.purpose == ConfigurationPickPurpose.FAST_MOUNT,
        )
    }
    return state to actions
}

private fun MineMessage.text() = when (this) {
    MineMessage.COPIED -> Strings.string.configurator_common_msg_copied
    MineMessage.DELETED -> Strings.string.configurator_common_msg_deleted
    MineMessage.RENAMED -> Strings.string.configurator_common_msg_renamed
    MineMessage.FAILED -> Strings.string.configurator_common_msg_failed
}
