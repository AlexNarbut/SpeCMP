package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_common_msg_loaded_same
import ru.alarmtrade.specmp.core.localization.resources.settings_common_msg_saved
import ru.alarmtrade.specmp.core.localization.resources.settings_common_msg_saved_file
import ru.alarmtrade.specmp.core.localization.resources.settings_common_msg_saved_mine
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_error_set_value
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_msg_written
import ru.alarmtrade.specmp.core.localization.resources.settings_loaded_msg
import ru.alarmtrade.specmp.core.localization.resources.settings_reset_msg_done
import ru.alarmtrade.specmp.core.localization.resources.settings_saved_btn_open
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarDuration
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarResult
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.findNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.pathTo
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.SettingsLayout
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.NoSettingsFilePicker
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFilePicker
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFileSaver
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsActions
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsContent
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsEffect
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsState
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsViewModel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.ExitTarget
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.RowMarks
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SettingsEditorOptions
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SettingsLocation
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.firstSectionId
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.EditorActions
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SearchPane
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SectionPane
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SettingRowActions
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.TransferActions
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary

/** Раздел меню; `null` — корень. */
@Serializable
@SerialName("device.settings-ui.SettingsSectionRoute")
internal data class SettingsSectionRoute(val sectionId: Int?) : NavKey

@Serializable
@SerialName("device.settings-ui.SettingsSearchRoute")
internal data object SettingsSearchRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(SettingsSectionRoute::class, SettingsSectionRoute.serializer())
            subclass(SettingsSearchRoute::class, SettingsSearchRoute.serializer())
        }
    }
}

/**
 * Расширенные настройки подключённого блока: слот раздела «Настройки» графа расширенного монтажа.
 *
 * Одна сессия на граф ([DeviceSettingsViewModel] в хранилище записи, в которой стоит слот); back stack графа —
 * путь по разделам от корня (+ поиск). «Назад» поднимается на уровень вверх, с корня — выход (с незаписанными
 * изменениями — подтверждение).
 *
 * @param onExit выход из настроек.
 * @param onOpenFirmwareUpdate выход в «Проверку прошивок»: на блоке с единой CAN-прошивкой CAN-код меняется прошивкой.
 * @param vehiclePicker выбор автомобиля из каталога для CAN-кода (фича `vehicle-ui`, подключается в `composeApp`):
 *   получает модель блока (`null` — ещё не прочитана), выбранный автомобиль и отмену. Рисуется поверх настроек.
 * @param configurationPicker «Загрузить из…» → «Мои конфигурации» / облако (фича `configurator-ui`, подключается в
 *   `composeApp`): получает запрос, выбранный источник, переход к обновлению прошивки (CAN-код облачной конфигурации
 *   не совпадает с прошивкой) и отмену. Рисуется поверх настроек.
 * @param filePicker / [fileSaver] — платформенные выбор и сохранение файла `.dxs`.
 * @param loadOnOpen конфигурация, которую сравнить и загрузить сразу после чтения блока («Записать в блок»).
 */
@Composable
fun DeviceSettingsEntry(
    onExit: () -> Unit,
    onOpenFirmwareUpdate: () -> Unit,
    vehiclePicker: @Composable (deviceName: String?, onPick: (CarSummary) -> Unit, onBack: () -> Unit) -> Unit,
    configurationPicker: @Composable (
        request: ConfigurationPickRequest,
        onPicked: (SettingsSource) -> Unit,
        onOpenFirmwareUpdate: () -> Unit,
        onBack: () -> Unit,
    ) -> Unit,
    filePicker: SettingsFilePicker,
    fileSaver: SettingsFileSaver,
    loadOnOpen: SettingsSource? = null,
) {
    SettingsEditorHost(
        source = SettingsSource.Device,
        options = SettingsEditorOptions(loadOnOpen = loadOnOpen),
        slots = EditorSlots(
            onExit = onExit,
            onOpenFirmwareUpdate = onOpenFirmwareUpdate,
            vehiclePicker = vehiclePicker,
            configurationPicker = configurationPicker,
            filePicker = filePicker,
            fileSaver = fileSaver,
            onOpenConfiguration = null,
            onWriteToDevice = null,
        ),
    )
}

/**
 * Редактор конфигурации (K5): «Мои», файл, облако, заводские. Тот же редактор, что для блока, — меняются шапка
 * (источник, модель, «Только просмотр»), меню «⋯» и действие панели («Сохранить» / «Сохранить как…»).
 *
 * @param onOpenConfiguration «Открыть» в тосте после «Сохранить в мои».
 * @param onWriteToDevice «Записать в блок» (пункт меню — только при подключённом блоке); `null` — не поддерживается.
 */
@Composable
fun ConfigurationEditorEntry(
    source: SettingsSource,
    onExit: () -> Unit,
    vehiclePicker: @Composable (deviceName: String?, onPick: (CarSummary) -> Unit, onBack: () -> Unit) -> Unit,
    fileSaver: SettingsFileSaver,
    onOpenConfiguration: (SettingsSource.LocalConfiguration) -> Unit,
    onWriteToDevice: ((SettingsSource) -> Unit)? = null,
) {
    SettingsEditorHost(
        source = source,
        options = SettingsEditorOptions(canWriteToDevice = onWriteToDevice != null),
        slots = EditorSlots(
            onExit = onExit,
            onOpenFirmwareUpdate = {},
            vehiclePicker = vehiclePicker,
            configurationPicker = { _, _, _, _ -> },
            filePicker = NoSettingsFilePicker,
            fileSaver = fileSaver,
            onOpenConfiguration = onOpenConfiguration,
            onWriteToDevice = onWriteToDevice,
        ),
    )
}

/** Слоты и колбэки хоста редактора. */
private class EditorSlots(
    val onExit: () -> Unit,
    val onOpenFirmwareUpdate: () -> Unit,
    val vehiclePicker: @Composable (deviceName: String?, onPick: (CarSummary) -> Unit, onBack: () -> Unit) -> Unit,
    val configurationPicker: @Composable (
        request: ConfigurationPickRequest,
        onPicked: (SettingsSource) -> Unit,
        onOpenFirmwareUpdate: () -> Unit,
        onBack: () -> Unit,
    ) -> Unit,
    val filePicker: SettingsFilePicker,
    val fileSaver: SettingsFileSaver,
    val onOpenConfiguration: ((SettingsSource.LocalConfiguration) -> Unit)?,
    val onWriteToDevice: ((SettingsSource) -> Unit)?,
)

@Composable
private fun SettingsEditorHost(source: SettingsSource, options: SettingsEditorOptions, slots: EditorSlots) {
    val viewModel = koinViewModel<DeviceSettingsViewModel>(
        key = "settings:$source",
        parameters = { parametersOf(source, options) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(navConfig, SettingsSectionRoute(null))
    val snackbarHostState = remember { CmpSnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(slots.filePicker) { slots.filePicker.results.collect(viewModel::onFilePicked) }
    // Каждый эффект — в своей корутине: тост «Сохранено» с «Открыть» висит до 10 с, и выход по «назад» (тоже эффект)
    // не должен ждать, пока он закроется. Тосты всё равно идут по очереди — их упорядочивает snackbarHostState.
    viewModel.effect.collectSideEffect { effect ->
        scope.launch { handleEffect(effect, viewModel, slots, snackbarHostState) }
    }

    val onBack = { if (!backStack.popSafe()) viewModel.onExitRequest(ExitTarget.BACK) }
    val navigateTo = { path: List<Int> -> backStack.navigateTo(path) }
    val rowActions = remember(viewModel, backStack) {
        SettingRowActions(
            onOpenSection = { id ->
                viewModel.state.value.menu.content?.let { root ->
                    backStack.navigateTo(root.pathTo(id).orEmpty().map { it.id } + id)
                }
            },
            onEdit = viewModel::onSettingClick,
            onValueChange = viewModel::onValueChange,
            onDescription = viewModel::onDescriptionClick,
        )
    }
    val actions = remember(viewModel, backStack) {
        DeviceSettingsActions(
            onBack = onBack,
            onSearch = { backStack.navigateSafe(SettingsSearchRoute) },
            onNavigateTo = navigateTo,
            onRetry = viewModel::onRetry,
            onTreeWidthChange = viewModel::onTreeWidthChange,
            onWrite = viewModel::onWrite,
            onExitWrite = viewModel::onExitWrite,
            onExitDiscard = viewModel::onExitDiscard,
            onDialogDismiss = viewModel::onDialogDismiss,
            onResetRequest = viewModel::onResetRequest,
            onResetConfirm = viewModel::onResetConfirm,
            onSingleCanUpdate = viewModel::onSingleCanUpdate,
            editor = EditorActions(
                onDraftChange = viewModel::onDraftChange,
                onReset = viewModel::onEditReset,
                onApply = viewModel::onEditApply,
                onDismiss = viewModel::onEditDismiss,
                onCarSearch = viewModel::onCarSearch,
                onCarPick = viewModel::onCarPick,
                onPickFromCatalog = viewModel::onVehiclePickerOpen,
            ),
            transfer = transferActions(viewModel),
            onEditSetting = viewModel::onSettingClick,
        )
    }

    CmpBox(modifier = Modifier.fillMaxSize()) {
        SettingsWithNav(
            state,
            backStack,
            actions,
            snackbarHostState,
            onBack,
            rowActions,
            viewModel::onSearchQueryChange,
        )
        if (state.vehiclePicker) {
            CmpSurface(modifier = Modifier.fillMaxSize()) {
                slots.vehiclePicker(state.header?.deviceName, viewModel::onCarPicked, viewModel::onVehiclePickerClose)
            }
        }
        state.configurationPicker?.let { request ->
            CmpSurface(modifier = Modifier.fillMaxSize()) {
                slots.configurationPicker(
                    request,
                    viewModel::onConfigurationPicked,
                    {
                        viewModel.onConfigurationPickerClose()
                        viewModel.onSingleCanUpdate()
                    },
                    viewModel::onConfigurationPickerClose,
                )
            }
        }
    }
}

private fun transferActions(viewModel: DeviceSettingsViewModel) = TransferActions(
    onLoadRequest = viewModel::onLoadRequest,
    onLoadSourcePick = viewModel::onLoadSourcePick,
    onMismatchContinue = viewModel::onMismatchContinue,
    onDiffConfirm = viewModel::onDiffConfirm,
    onDiffDismiss = viewModel::onDiffDismiss,
    onDiffRetry = viewModel::onDiffRetry,
    onSaveToRequest = { viewModel.onSaveToRequest() },
    onSaveTargetPick = viewModel::onSaveTargetPick,
    onSaveToMine = viewModel::onSaveToMineRequest,
    onExport = viewModel::onExportRequest,
    onSaveFormChange = viewModel::onSaveFormChange,
    onSaveFormSubmit = viewModel::onSaveFormSubmit,
    onSaveFormDismiss = viewModel::onSaveFormDismiss,
    onSaveRetry = viewModel::onSaveRetry,
    onOverwriteConfirm = viewModel::onOverwriteConfirm,
    onBarAction = viewModel::onBarAction,
    onExitSave = viewModel::onExitSave,
    onCopy = viewModel::onCopyRequest,
    onWriteToDevice = viewModel::onWriteToDeviceRequest,
)

@Suppress("CyclomaticComplexMethod") // плоский when по эффектам экрана
private suspend fun handleEffect(
    effect: DeviceSettingsEffect,
    viewModel: DeviceSettingsViewModel,
    slots: EditorSlots,
    snackbarHostState: CmpSnackbarHostState,
) {
    when (effect) {
        is DeviceSettingsEffect.Written -> snackbarHostState.showSnackbar(
            getString(Strings.string.settings_editor_msg_written, effect.count),
        )

        DeviceSettingsEffect.SetValueFailed -> snackbarHostState.showSnackbar(
            getString(Strings.string.settings_editor_error_set_value),
        )

        DeviceSettingsEffect.ResetDone -> snackbarHostState.showSnackbar(
            getString(Strings.string.settings_reset_msg_done),
        )

        is DeviceSettingsEffect.Exit -> when (effect.target) {
            ExitTarget.BACK -> slots.onExit()
            ExitTarget.FIRMWARE_UPDATE -> slots.onOpenFirmwareUpdate()
        }

        is DeviceSettingsEffect.Loaded -> snackbarHostState.showSnackbar(
            if (effect.count > 0) {
                getString(Strings.string.settings_loaded_msg, effect.count)
            } else {
                getString(Strings.string.settings_common_msg_loaded_same)
            },
            duration = CmpSnackbarDuration.Long,
        )

        DeviceSettingsEffect.PickFile -> slots.filePicker.launch()

        is DeviceSettingsEffect.ExportFile ->
            viewModel.onFileExported(slots.fileSaver.save(effect.path, effect.fileName))

        is DeviceSettingsEffect.FileSaved -> snackbarHostState.showSnackbar(
            getString(Strings.string.settings_common_msg_saved_file, effect.name),
        )

        is DeviceSettingsEffect.SavedToMine -> {
            val open = slots.onOpenConfiguration
            val result = snackbarHostState.showSnackbar(
                getString(Strings.string.settings_common_msg_saved_mine),
                actionLabel = open?.let { getString(Strings.string.settings_saved_btn_open) },
                duration = CmpSnackbarDuration.Long,
            )
            if (result == CmpSnackbarResult.ActionPerformed) open?.invoke(effect.configuration)
        }

        DeviceSettingsEffect.Saved -> snackbarHostState.showSnackbar(
            getString(Strings.string.settings_common_msg_saved),
        )

        is DeviceSettingsEffect.WriteToDevice -> slots.onWriteToDevice?.invoke(effect.source)
    }
}

@Suppress("LongParameterList") // разметка записи графа: всё приходит из DeviceSettingsEntry
@Composable
private fun SettingsWithNav(
    state: DeviceSettingsState,
    backStack: NavBackStack<NavKey>,
    actions: DeviceSettingsActions,
    snackbarHostState: CmpSnackbarHostState,
    onBack: () -> Unit,
    rowActions: SettingRowActions,
    onSearchQueryChange: (String) -> Unit,
) {
    // Строкам — только их пометки: ввод, диалоги и прочие поля state строки не перерисовывают.
    val marks = remember(state.changedIds, state.sectionChanges, state.cars) {
        RowMarks(state.changedIds, state.sectionChanges, state.cars)
    }
    val root = state.menu.content
    DeviceSettingsContent(
        state = state,
        location = backStack.location(),
        actions = actions,
        snackbarHostState = snackbarHostState,
    ) { layout ->
        NavDisplay(
            backStack = backStack,
            onBack = onBack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider<NavKey> {
                entry<SettingsSectionRoute> { key ->
                    EntryBackground { SectionEntry(root, marks, key.sectionId, layout, rowActions) }
                }
                entry<SettingsSearchRoute> { _ ->
                    EntryBackground {
                        SearchPane(
                            query = state.searchQuery,
                            hits = state.searchHits,
                            marks = marks,
                            wide = layout.isWide,
                            onQueryChange = onSearchQueryChange,
                            actions = rowActions,
                        )
                    }
                }
            },
        )
    }
}

/**
 * Непрозрачный фон записи: на переходе NavDisplay новая запись едет поверх старой, и без фона строки старой
 * просвечивают сквозь новую.
 */
@Composable
private fun EntryBackground(content: @Composable () -> Unit) {
    CmpBox(modifier = Modifier.fillMaxSize().background(cmpThemeColors().background)) { content() }
}

@Composable
internal fun SectionEntry(
    root: SettingsSection?,
    marks: RowMarks,
    sectionId: Int?,
    layout: SettingsLayout,
    actions: SettingRowActions,
) {
    root ?: return
    // На широкой раскладке корень не показывается — открыт первый раздел (он же выделен в дереве).
    val id = sectionId ?: firstSectionId(root).takeIf { layout.isWide }
    val section = id?.let { root.findNode(it) as? SettingsSection } ?: root
    SectionPane(section = section, marks = marks, wide = layout.isWide, actions = actions)
}

private fun NavBackStack<NavKey>.location(): SettingsLocation = SettingsLocation(
    sectionPath = mapNotNull { (it as? SettingsSectionRoute)?.sectionId },
    isSearch = lastOrNull() == SettingsSearchRoute,
)

/** Стек = корень + путь разделов (поиск закрывается). */
private fun NavBackStack<NavKey>.navigateTo(path: List<Int>) {
    clear()
    add(SettingsSectionRoute(null))
    path.forEach { add(SettingsSectionRoute(it)) }
}
