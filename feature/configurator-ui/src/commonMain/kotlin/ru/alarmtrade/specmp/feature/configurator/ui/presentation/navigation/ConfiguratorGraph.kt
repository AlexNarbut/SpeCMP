package ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
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
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation.ConfigurationEditorEntry
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation.ConfigurationPickKind
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation.ConfigurationPickRequest
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.NoSettingsFilePicker
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFilePickResult
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFilePicker
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFileSaveResult
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFileSaver
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog

/**
 * Выбор автомобиля из каталога (фича `vehicle-ui`, подключается в `composeApp`): [VehicleCatalog.WITH_CONFIGURATIONS] —
 * облачные конфигурации (K4), [VehicleCatalog.ALL] — CAN-код в редакторе. `deviceName == null` — каталог для всех
 * моделей блока.
 */
typealias ConfiguratorVehiclePicker = @Composable (
    catalog: VehicleCatalog,
    deviceName: String?,
    onPick: (CarSummary) -> Unit,
    onBack: () -> Unit
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

/**
 * Конфигуратор (точка A — плитка на главной): «Мои конфигурации» (K1), выбор модели (K2) и файла (K3), облачные
 * конфигурации (K4), редактор конфигурации (K5).
 *
 * @param deviceEditor «Записать в блок»: редактор подключённого блока с загрузкой конфигурации (подключается в
 *   `composeApp` вместе с обёрткой сессии устройства); `null` — не поддерживается. Пункт виден только при связи
 *   с блоком.
 */
@Composable
fun ConfiguratorEntry(
    onExit: () -> Unit,
    vehiclePicker: ConfiguratorVehiclePicker,
    filePicker: SettingsFilePicker,
    fileSaver: SettingsFileSaver,
    deviceEditor: (@Composable (load: SettingsSource, onBack: () -> Unit) -> Unit)? = null
) {
    ConfiguratorGraph(
        start = MineListRoute,
        onExit = onExit,
        vehiclePicker = vehiclePicker,
        mode = GraphMode.Full(filePicker, fileSaver, deviceEditor)
    )
}

/**
 * Выбор конфигурации для «Загрузить из…» в редакторе блока (K7): «Мои» под модель блока или облако (каталог под модель
 * блока → список → карточка «Загрузить в редактор»). Подключается в `composeApp` в слот `configurationPicker`.
 */
@Composable
fun ConfigurationPickerEntry(
    request: ConfigurationPickRequest,
    onPick: (SettingsSource) -> Unit,
    onOpenFirmwareUpdate: () -> Unit,
    onBack: () -> Unit,
    vehiclePicker: ConfiguratorVehiclePicker
) {
    ConfiguratorGraph(
        start = if (request.kind == ConfigurationPickKind.MINE) MineListRoute else CloudCarsRoute,
        onExit = onBack,
        vehiclePicker = vehiclePicker,
        mode = GraphMode.Pick(request, onPick, onOpenFirmwareUpdate)
    )
}

/** Конфигуратор целиком или выбор конфигурации для загрузки в блок. */
private sealed interface GraphMode {
    class Full(
        val filePicker: SettingsFilePicker,
        val fileSaver: SettingsFileSaver,
        val deviceEditor: (@Composable (load: SettingsSource, onBack: () -> Unit) -> Unit)?
    ) : GraphMode

    class Pick(
        val request: ConfigurationPickRequest,
        val onPick: (SettingsSource) -> Unit,
        val onOpenFirmwareUpdate: () -> Unit
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
    mode: GraphMode
) {
    val backStack = rememberNavBackStack(navConfig, start)
    val back = { if (!backStack.popSafe()) onExit() }
    NavDisplay(
        backStack = backStack,
        onBack = back,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider<NavKey> {
            entry<MineListRoute> { _ -> Opaque { MineEntry(backStack, mode, back) } }
            entry<ModelsRoute> { key -> Opaque { ModelsEntry(key, backStack, back) } }
            entry<EditorRoute> { key ->
                val full = mode as? GraphMode.Full
                ConfigurationEditorEntry(
                    source = key.source.toSource(),
                    onExit = back,
                    vehiclePicker = { deviceName, onPick, onPickerBack ->
                        vehiclePicker(VehicleCatalog.ALL, deviceName, onPick, onPickerBack)
                    },
                    fileSaver = full?.fileSaver ?: NoSaver,
                    onOpenConfiguration = { opened -> opened.toKey()?.let { backStack.navigateSafe(EditorRoute(it)) } },
                    onWriteToDevice = full?.deviceEditor?.let {
                        { source -> source.toKey()?.let { backStack.navigateSafe(DeviceWriteRoute(it)) } }
                    }
                )
            }
            entry<CloudCarsRoute> { _ ->
                Opaque {
                    vehiclePicker(
                        VehicleCatalog.WITH_CONFIGURATIONS,
                        (mode as? GraphMode.Pick)?.request?.deviceName,
                        { car -> backStack.navigateSafe(CloudListRoute(car.id, car.title())) },
                        back
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
        }
    )
}

/**
 * Непрозрачный фон записи: на переходе NavDisplay новая запись едет поверх старой, и без фона старая просвечивает.
 */
@Composable
private fun Opaque(content: @Composable () -> Unit) {
    CmpBox(modifier = Modifier.fillMaxSize().background(cmpThemeColors().background)) { content() }
}

@Composable
private fun MineEntry(backStack: NavBackStack<NavKey>, mode: GraphMode, onBack: () -> Unit) {
    val pick = mode as? GraphMode.Pick
    val full = mode as? GraphMode.Full
    val viewModel = koinViewModel<MineViewModel>(parameters = { parametersOf(pick?.request?.deviceName) })
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { CmpSnackbarHostState() }
    val scope = rememberCoroutineScope()
    val filePicker = full?.filePicker ?: NoSettingsFilePicker
    val fileFailed = stringResource(Strings.string.configurator_common_msg_file_failed)

    LaunchedEffect(filePicker) {
        filePicker.results.collect { result ->
            when (result) {
                is SettingsFilePickResult.Picked -> backStack.navigateSafe(
                    ModelsRoute(SourceKey.File(result.path, "", result.name, result.inPlace))
                )

                SettingsFilePickResult.Failed -> snackbarHostState.showSnackbar(fileFailed)

                SettingsFilePickResult.Cancelled -> Unit
            }
        }
    }
    viewModel.effect.collectSideEffect { effect ->
        when (effect) {
            is MineEffect.Message -> snackbarHostState.showSnackbar(getString(effect.message.text()))

            is MineEffect.Copied -> {
                val result = snackbarHostState.showSnackbar(
                    getString(Strings.string.configurator_common_msg_copied),
                    actionLabel = getString(Strings.string.configurator_common_btn_open),
                    duration = CmpSnackbarDuration.Long
                )
                if (result == CmpSnackbarResult.ActionPerformed) {
                    effect.configuration.toKey()?.let { backStack.navigateSafe(EditorRoute(it)) }
                }
            }

            is MineEffect.ExportFile -> scope.launch {
                viewModel.onFileExported((full?.fileSaver ?: NoSaver).save(effect.path, effect.fileName))
            }

            is MineEffect.FileSaved -> snackbarHostState.showSnackbar(
                getString(Strings.string.configurator_common_msg_file_saved, effect.name)
            )
        }
    }
    val actions = remember(viewModel, backStack) { mineActions(viewModel, backStack, mode, filePicker, onBack) }
    MineScreen(
        state = if (full?.deviceEditor == null) state.copy(connectedDeviceName = null) else state,
        actions = actions,
        snackbarHostState = snackbarHostState
    )
}

private fun mineActions(
    viewModel: MineViewModel,
    backStack: NavBackStack<NavKey>,
    mode: GraphMode,
    filePicker: SettingsFilePicker,
    onBack: () -> Unit
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
        onDialogDismiss = viewModel::onDialogDismiss
    )
}

@Composable
private fun ModelsEntry(key: ModelsRoute, backStack: NavBackStack<NavKey>, onBack: () -> Unit) {
    val viewModel = koinViewModel<ModelsViewModel>(parameters = { parametersOf(key.file) })
    val state by viewModel.state.collectAsState()
    viewModel.effect.collectSideEffect { effect ->
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
        onDialogDismiss = viewModel::onDialogDismiss
    )
}

@Composable
private fun CloudListEntry(key: CloudListRoute, backStack: NavBackStack<NavKey>, mode: GraphMode, onBack: () -> Unit) {
    val deviceName = (mode as? GraphMode.Pick)?.request?.deviceName.orEmpty()
    val viewModel = koinViewModel<CloudListViewModel>(parameters = { parametersOf(key.carId, deviceName) })
    val state by viewModel.state.collectAsState()
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
        snackbarHostState = snackbarHostState
    ) { id ->
        val (cardState, actions) = cloudCard(id, backStack, mode, snackbarHostState)
        CloudCardContent(cardState, actions)
    }
}

/** VM карточки облачной конфигурации [id] (своя на id — правая панель меняет выбранную) и её действия. */
@Composable
private fun cloudCard(
    id: Int,
    backStack: NavBackStack<NavKey>,
    mode: GraphMode,
    snackbarHostState: CmpSnackbarHostState
): Pair<CloudCardState, CloudCardActions> {
    val pick = mode as? GraphMode.Pick
    val full = mode as? GraphMode.Full
    val viewModel = koinViewModel<CloudCardViewModel>(
        key = "cloud_card_$id",
        parameters = { parametersOf(id, pick?.request?.firmwareCanCode) }
    )
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    viewModel.effect.collectSideEffect { effect ->
        when (effect) {
            is CloudCardEffect.OpenUrl -> safeRun { uriHandler.openUri(effect.url) }

            is CloudCardEffect.SavedToMine -> {
                val result = snackbarHostState.showSnackbar(
                    getString(Strings.string.configurator_common_msg_saved_mine),
                    actionLabel = if (full != null) getString(Strings.string.configurator_common_btn_open) else null,
                    duration = CmpSnackbarDuration.Long
                )
                if (result == CmpSnackbarResult.ActionPerformed) {
                    effect.configuration.toKey()?.let { backStack.navigateSafe(EditorRoute(it)) }
                }
            }

            CloudCardEffect.SaveFailed -> snackbarHostState.showSnackbar(
                getString(Strings.string.configurator_common_msg_failed)
            )
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
            onOpenFirmwareUpdate = pick?.onOpenFirmwareUpdate
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

private fun CarSummary.title(): String = listOf(brand, model, name).filter { it.isNotBlank() }.joinToString(" ")

/** Сохранение файла недоступно (режим выбора). */
private object NoSaver : SettingsFileSaver {
    override suspend fun save(sourcePath: String, fileName: String) = SettingsFileSaveResult.Cancelled
}
