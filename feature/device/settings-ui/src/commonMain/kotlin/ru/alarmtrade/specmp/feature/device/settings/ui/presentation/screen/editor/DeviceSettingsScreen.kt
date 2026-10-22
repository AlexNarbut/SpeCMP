package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.badge.CmpDot
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenu
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenuItem
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpBreadcrumb
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpBreadcrumbs
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.findNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.pathTo
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.SettingsLayout
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.settingsLayoutOf
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.DescriptionDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.EditorActions
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.EditorPanel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.ExitDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.MenuError
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.MenuLoading
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.ResetConfirmDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.ResetErrorDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.ResetProgressDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SectionTree
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.WriteErrorDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.WriteProgressDialog
import specmp.feature.device.settings_ui.generated.resources.Res
import specmp.feature.device.settings_ui.generated.resources.settings_common_a11y_back
import specmp.feature.device.settings_ui.generated.resources.settings_common_a11y_more
import specmp.feature.device.settings_ui.generated.resources.settings_common_a11y_search
import specmp.feature.device.settings_ui.generated.resources.settings_editor_btn_write
import specmp.feature.device.settings_ui.generated.resources.settings_editor_crumb_all
import specmp.feature.device.settings_ui.generated.resources.settings_editor_label_changed
import specmp.feature.device.settings_ui.generated.resources.settings_editor_label_device
import specmp.feature.device.settings_ui.generated.resources.settings_editor_label_single_can
import specmp.feature.device.settings_ui.generated.resources.settings_editor_title_screen
import specmp.feature.device.settings_ui.generated.resources.settings_menu_defaults
import specmp.feature.device.settings_ui.generated.resources.settings_menu_open_from
import specmp.feature.device.settings_ui.generated.resources.settings_menu_reset
import specmp.feature.device.settings_ui.generated.resources.settings_menu_save_to

/** Где пользователь в меню: путь разделов от корня (пусто — корень) или поиск. */
@Immutable
internal data class SettingsLocation(val sectionPath: List<Int>, val isSearch: Boolean) {
    val sectionId: Int? get() = sectionPath.lastOrNull()
}

/** Действия экрана, кроме строк меню (они — в панелях). */
@Immutable
internal class DeviceSettingsActions(
    val onBack: () -> Unit,
    val onSearch: () -> Unit,
    /** Перейти к разделу по пути от корня (крошки, дерево). */
    val onNavigateTo: (sectionPath: List<Int>) -> Unit,
    val onRetry: () -> Unit,
    /** Ширина колонки дерева изменена перетаскиванием (dp, по окончании). */
    val onTreeWidthChange: (Int) -> Unit,
    val onWrite: () -> Unit,
    val onExitWrite: () -> Unit,
    val onExitDiscard: () -> Unit,
    val onDialogDismiss: () -> Unit,
    val onResetRequest: () -> Unit,
    val onResetConfirm: () -> Unit,
    val editor: EditorActions
)

/**
 * Каркас редактора: шапка (устройство, бейдж единой CAN), крошки (compact), дерево разделов (medium/expanded),
 * панель содержимого [pane] (раздел или поиск — её даёт граф), нижняя панель «Изменено: N · Записать», окно
 * редактирования и диалоги.
 */
@Composable
internal fun DeviceSettingsContent(
    state: DeviceSettingsState,
    location: SettingsLocation,
    actions: DeviceSettingsActions,
    snackbarHostState: CmpSnackbarHostState,
    pane: @Composable (layout: SettingsLayout) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val layout = settingsLayoutOf(maxWidth)
        val windowWidth = maxWidth
        CmpScaffold(
            topBar = { TopBar(state, location, actions) },
            bottomBar = { WriteBar(state, actions) },
            snackbarHost = { CmpSnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            CmpColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (val menu = state.menu) {
                    SettingsMenu.Loading -> MenuLoading()
                    SettingsMenu.Error -> MenuError(onRetry = actions.onRetry)
                    is SettingsMenu.Ready -> ReadyBody(menu.root, state, location, layout, windowWidth, actions, pane)
                }
            }
        }
        state.editing?.let {
            EditorPanel(editing = it, cars = state.cars, wide = layout.isWide, actions = actions.editor)
        }
        Dialogs(state, actions)
    }
}

@Composable
private fun ReadyBody(
    root: SettingsSection,
    state: DeviceSettingsState,
    location: SettingsLocation,
    layout: SettingsLayout,
    windowWidth: Dp,
    actions: DeviceSettingsActions,
    pane: @Composable (layout: SettingsLayout) -> Unit
) {
    CmpRow(modifier = Modifier.fillMaxSize()) {
        if (layout.isWide) {
            val maxTree = minOf(TreeMaxWidth, windowWidth * TREE_MAX_FRACTION)
            val saved = state.treeWidthDp?.dp ?: layout.treeWidth
            var treeWidth by remember(saved, maxTree) { mutableStateOf(saved.coerceIn(TreeMinWidth, maxTree)) }
            SectionTree(
                root = root,
                selectedId = (location.sectionId ?: firstSectionId(root)).takeUnless { location.isSearch },
                selectedPath = location.sectionPath,
                sectionChanges = state.sectionChanges,
                width = treeWidth,
                onSelect = { id -> actions.onNavigateTo(root.pathTo(id).orEmpty().map { it.id } + id) }
            )
            TreeResizeHandle(
                onDrag = { delta -> treeWidth = (treeWidth + delta).coerceIn(TreeMinWidth, maxTree) },
                onDragStop = { actions.onTreeWidthChange(treeWidth.value.roundToInt()) }
            )
        }
        CmpColumn(modifier = Modifier.weight(1f).fillMaxSize().background(cmpThemeColors().background)) {
            if (!layout.isWide && !location.isSearch) Crumbs(root, state, location, actions)
            CmpBox(modifier = Modifier.weight(1f).fillMaxWidth()) { pane(layout) }
        }
    }
}

/** Разделитель между деревом и разделом: тянется по горизонтали, меняет ширину дерева. */
@Composable
private fun TreeResizeHandle(onDrag: (Dp) -> Unit, onDragStop: () -> Unit) {
    val colors = cmpThemeColors()
    val density = LocalDensity.current
    val currentOnDrag by rememberUpdatedState(onDrag)
    CmpBox(
        modifier = Modifier
            .width(ResizeHandleWidth)
            .fillMaxHeight()
            .testTag(SettingsTestTags.TREE_RESIZE)
            .background(colors.surfaceContainerLow)
            .pointerHoverIcon(PointerIcon.Hand)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { deltaPx -> currentOnDrag(with(density) { deltaPx.toDp() }) },
                onDragStopped = { onDragStop() }
            ),
        contentAlignment = Alignment.Center
    ) {
        CmpBox(modifier = Modifier.width(1.dp).fillMaxHeight().background(colors.outlineVariant))
        CmpBox(
            modifier = Modifier
                .size(width = 4.dp, height = 32.dp)
                .background(colors.outline, CircleShape)
        )
    }
}

@Composable
private fun TopBar(state: DeviceSettingsState, location: SettingsLocation, actions: DeviceSettingsActions) {
    val header = state.header
    CmpTopAppBar(
        title = { CmpText(text = stringResource(Res.string.settings_editor_title_screen)) },
        subtitle = header?.let { device ->
            {
                // С переносом: на узкой шапке бейдж единой CAN уходит на вторую строку, а не обрезается.
                CmpFlowRow(
                    itemVerticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    CmpText(
                        text = stringResource(
                            Res.string.settings_editor_label_device,
                            device.deviceName,
                            device.deviceId.toString()
                        ),
                        style = CmpTextStyle.Caption
                    )
                    device.singleCanCode?.let { code -> CanBadge(code) }
                }
            }
        },
        navigationIcon = {
            CmpIconButton(
                onClick = actions.onBack,
                icon = CmpIcons.ArrowBack,
                contentDescription = stringResource(Res.string.settings_common_a11y_back)
            )
        },
        actions = {
            if (!location.isSearch && state.menu is SettingsMenu.Ready) {
                CmpIconButton(
                    onClick = actions.onSearch,
                    modifier = Modifier.testTag(SettingsTestTags.SEARCH_OPEN),
                    icon = CmpIcons.Search,
                    contentDescription = stringResource(Res.string.settings_common_a11y_search)
                )
                MoreMenu(state, actions)
            }
        }
    )
}

/** Меню «⋯»: сброс к заводским (если источник умеет), конфигурации — пока неактивны (второй этап). */
@Composable
private fun MoreMenu(state: DeviceSettingsState, actions: DeviceSettingsActions) {
    var expanded by remember { mutableStateOf(false) }
    CmpBox {
        CmpIconButton(
            onClick = { expanded = true },
            icon = CmpIcons.MoreVert,
            contentDescription = stringResource(Res.string.settings_common_a11y_more),
            modifier = Modifier.testTag(SettingsTestTags.MENU)
        )
        CmpDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (state.canResetToFactory) {
                CmpDropdownMenuItem(
                    text = stringResource(Res.string.settings_menu_reset),
                    icon = CmpIcons.Refresh,
                    onClick = {
                        expanded = false
                        actions.onResetRequest()
                    },
                    modifier = Modifier.testTag(SettingsTestTags.MENU_RESET)
                )
            }
            CmpDropdownMenuItem(
                text = stringResource(Res.string.settings_menu_open_from),
                icon = CmpIcons.FolderOpen,
                onClick = {},
                enabled = false
            )
            CmpDropdownMenuItem(
                text = stringResource(Res.string.settings_menu_save_to),
                icon = CmpIcons.Download,
                onClick = {},
                enabled = false
            )
            CmpDropdownMenuItem(
                text = stringResource(Res.string.settings_menu_defaults),
                icon = CmpIcons.Inventory2,
                onClick = {},
                enabled = false
            )
        }
    }
}

@Composable
private fun CanBadge(code: String) {
    val colors = cmpThemeColors()
    CmpText(
        text = stringResource(Res.string.settings_editor_label_single_can, code),
        style = CmpTextStyle.Caption,
        fontWeight = FontWeight.SemiBold,
        color = colors.onSecondaryContainer,
        maxLines = 1,
        modifier = Modifier.background(
            colors.secondaryContainer,
            CircleShape
        ).padding(horizontal = 10.dp, vertical = 2.dp)
    )
}

@Composable
private fun Crumbs(
    root: SettingsSection,
    state: DeviceSettingsState,
    location: SettingsLocation,
    actions: DeviceSettingsActions
) {
    val sections = location.sectionPath.mapNotNull { id -> root.findNode(id) as? SettingsSection }
    val items = listOf(CmpBreadcrumb(stringResource(Res.string.settings_editor_crumb_all), state.changedCount > 0)) +
        sections.map { section ->
            CmpBreadcrumb(
                label = if (section.number.isEmpty()) section.name else "${section.number} ${section.name}",
                marked = (state.sectionChanges[section.id] ?: 0) > 0
            )
        }
    CmpBreadcrumbs(
        items = items,
        onItemClick = { index -> actions.onNavigateTo(location.sectionPath.take(index)) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    )
    CmpDivider(color = cmpThemeColors().outlineVariant)
}

@Composable
private fun WriteBar(state: DeviceSettingsState, actions: DeviceSettingsActions) {
    if (state.changedCount == 0 || state.menu !is SettingsMenu.Ready) return
    val colors = cmpThemeColors()
    CmpColumn(modifier = Modifier.fillMaxWidth().background(colors.surfaceContainer)) {
        CmpDivider(color = colors.outlineVariant)
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CmpDot()
            CmpText(
                text = stringResource(Res.string.settings_editor_label_changed, state.changedCount),
                style = CmpTextStyle.Title,
                modifier = Modifier.weight(1f)
            )
            CmpButton(
                text = stringResource(Res.string.settings_editor_btn_write),
                onClick = actions.onWrite,
                modifier = Modifier.widthIn(min = 160.dp).heightIn(min = 52.dp).testTag(SettingsTestTags.WRITE)
            )
        }
    }
}

@Composable
private fun Dialogs(state: DeviceSettingsState, actions: DeviceSettingsActions) {
    when (val dialog = state.dialog) {
        is SettingsDialog.Description -> DescriptionDialog(node = dialog.node, onDismiss = actions.onDialogDismiss)

        SettingsDialog.ExitConfirm -> ExitDialog(
            changed = state.changedCount,
            onWrite = actions.onExitWrite,
            onDiscard = actions.onExitDiscard,
            onDismiss = actions.onDialogDismiss
        )

        SettingsDialog.ResetConfirm -> ResetConfirmDialog(
            changed = state.changedCount,
            onConfirm = actions.onResetConfirm,
            onDismiss = actions.onDialogDismiss
        )

        SettingsDialog.ResetError -> ResetErrorDialog(
            onRetry = actions.onResetConfirm,
            onDismiss = actions.onDialogDismiss
        )

        SettingsDialog.WriteError -> WriteErrorDialog(
            changed = state.changedCount,
            onRetry = actions.onWrite,
            onDismiss = actions.onDialogDismiss
        )

        null -> Unit
    }
    if (state.isWriting) WriteProgressDialog()
    if (state.isResetting) ResetProgressDialog()
}

/** Ширина дерева: от 180 dp до 480 dp, но не больше половины окна. */
private val TreeMinWidth = 180.dp
private val TreeMaxWidth = 480.dp
private const val TREE_MAX_FRACTION = 0.5f
private val ResizeHandleWidth = 12.dp

/** На широкой раскладке корень не показывается: открыт первый раздел. */
internal fun firstSectionId(root: SettingsSection): Int? = root.children.firstOrNull { it is SettingsSection }?.id
