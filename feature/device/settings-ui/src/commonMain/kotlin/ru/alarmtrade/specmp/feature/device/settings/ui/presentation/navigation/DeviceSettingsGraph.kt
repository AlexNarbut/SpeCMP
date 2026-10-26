package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.findNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.pathTo
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.SettingsLayout
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsActions
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsContent
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsEffect
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsState
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsViewModel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.ExitTarget
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SettingsLocation
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SettingsMenu
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.firstSectionId
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.EditorActions
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.RowMarks
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SearchPane
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SectionPane
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SettingRowActions
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import specmp.feature.device.settings_ui.generated.resources.Res
import specmp.feature.device.settings_ui.generated.resources.settings_editor_error_set_value
import specmp.feature.device.settings_ui.generated.resources.settings_editor_msg_written
import specmp.feature.device.settings_ui.generated.resources.settings_reset_msg_done

/** Раздел меню; `null` — корень. */
@Serializable
internal data class SettingsSectionRoute(val sectionId: Int?) : NavKey

@Serializable
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
 */
@Composable
fun DeviceSettingsEntry(
    onExit: () -> Unit,
    onOpenFirmwareUpdate: () -> Unit,
    vehiclePicker: @Composable (deviceName: String?, onPick: (CarSummary) -> Unit, onBack: () -> Unit) -> Unit
) {
    val viewModel = koinViewModel<DeviceSettingsViewModel>(parameters = { parametersOf(SettingsSource.Device) })
    val state by viewModel.state.collectAsState()
    val backStack = rememberNavBackStack(navConfig, SettingsSectionRoute(null))
    val snackbarHostState = remember { CmpSnackbarHostState() }

    viewModel.effect.collectSideEffect { effect ->
        when (effect) {
            is DeviceSettingsEffect.Written -> snackbarHostState.showSnackbar(
                getString(Res.string.settings_editor_msg_written, effect.count)
            )

            DeviceSettingsEffect.SetValueFailed -> snackbarHostState.showSnackbar(
                getString(Res.string.settings_editor_error_set_value)
            )

            DeviceSettingsEffect.ResetDone -> snackbarHostState.showSnackbar(
                getString(Res.string.settings_reset_msg_done)
            )

            is DeviceSettingsEffect.Exit -> when (effect.target) {
                ExitTarget.BACK -> onExit()
                ExitTarget.FIRMWARE_UPDATE -> onOpenFirmwareUpdate()
            }
        }
    }

    val onBack = { if (!backStack.popSafe()) viewModel.onExitRequest(ExitTarget.BACK) }
    val navigateTo = { path: List<Int> -> backStack.navigateTo(path) }
    val rowActions = remember(viewModel, backStack) {
        SettingRowActions(
            onOpenSection = { id ->
                (viewModel.state.value.menu as? SettingsMenu.Ready)?.root?.let { root ->
                    backStack.navigateTo(root.pathTo(id).orEmpty().map { it.id } + id)
                }
            },
            onEdit = viewModel::onSettingClick,
            onValueChange = viewModel::onValueChange,
            onDescription = viewModel::onDescriptionClick
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
                onPickFromCatalog = viewModel::onVehiclePickerOpen
            ),
            onEditSetting = viewModel::onSettingClick
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
            viewModel::onSearchQueryChange
        )
        if (state.vehiclePicker) {
            CmpSurface(modifier = Modifier.fillMaxSize()) {
                vehiclePicker(state.header?.deviceName, viewModel::onCarPicked, viewModel::onVehiclePickerClose)
            }
        }
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
    onSearchQueryChange: (String) -> Unit
) {
    // Строкам — только их пометки: ввод, диалоги и прочие поля state строки не перерисовывают.
    val marks = remember(state.changedIds, state.sectionChanges, state.cars) {
        RowMarks(state.changedIds, state.sectionChanges, state.cars)
    }
    val root = (state.menu as? SettingsMenu.Ready)?.root
    DeviceSettingsContent(
        state = state,
        location = backStack.location(),
        actions = actions,
        snackbarHostState = snackbarHostState
    ) { layout ->
        NavDisplay(
            backStack = backStack,
            onBack = onBack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider<NavKey> {
                entry<SettingsSectionRoute> { key ->
                    SectionEntry(root, marks, key.sectionId, layout, rowActions)
                }
                entry<SettingsSearchRoute> { _ ->
                    SearchPane(
                        query = state.searchQuery,
                        hits = state.searchHits,
                        marks = marks,
                        wide = layout.isWide,
                        onQueryChange = onSearchQueryChange,
                        actions = rowActions
                    )
                }
            }
        )
    }
}

@Composable
internal fun SectionEntry(
    root: SettingsSection?,
    marks: RowMarks,
    sectionId: Int?,
    layout: SettingsLayout,
    actions: SettingRowActions
) {
    root ?: return
    // На широкой раскладке корень не показывается — открыт первый раздел (он же выделен в дереве).
    val id = sectionId ?: firstSectionId(root).takeIf { layout.isWide }
    val section = id?.let { root.findNode(it) as? SettingsSection } ?: root
    SectionPane(section = section, marks = marks, wide = layout.isWide, actions = actions)
}

private fun NavBackStack<NavKey>.location(): SettingsLocation = SettingsLocation(
    sectionPath = mapNotNull { (it as? SettingsSectionRoute)?.sectionId },
    isSearch = lastOrNull() == SettingsSearchRoute
)

/** Стек = корень + путь разделов (поиск закрывается). */
private fun NavBackStack<NavKey>.navigateTo(path: List<Int>) {
    clear()
    add(SettingsSectionRoute(null))
    path.forEach { add(SettingsSectionRoute(it)) }
}
