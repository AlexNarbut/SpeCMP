package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.settings_common_a11y_more
import ru.alarmtrade.specmp.core.localization.resources.settings_common_a11y_search
import ru.alarmtrade.specmp.core.localization.resources.settings_common_format_copy_name
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_source_cloud
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_source_defaults
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_source_file
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_source_mine
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_btn_save
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_btn_save_as
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_btn_write
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_label_changed
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_label_crumb_all
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_label_device
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_label_single_can
import ru.alarmtrade.specmp.core.localization.resources.settings_editor_title_screen
import ru.alarmtrade.specmp.core.localization.resources.settings_menu_item_copy
import ru.alarmtrade.specmp.core.localization.resources.settings_menu_item_load_from
import ru.alarmtrade.specmp.core.localization.resources.settings_menu_item_reset
import ru.alarmtrade.specmp.core.localization.resources.settings_menu_item_save_file
import ru.alarmtrade.specmp.core.localization.resources.settings_menu_item_save_file_as
import ru.alarmtrade.specmp.core.localization.resources.settings_menu_item_save_mine
import ru.alarmtrade.specmp.core.localization.resources.settings_menu_item_save_to
import ru.alarmtrade.specmp.core.localization.resources.settings_menu_item_write_device
import ru.alarmtrade.specmp.core.localization.resources.settings_source_title_defaults
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
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
import ru.alarmtrade.specmp.core.ui.view.layout.CmpResizeHandle
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.layout.rememberPaneWidthState
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenu
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenuItem
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpBreadcrumb
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpBreadcrumbs
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.presentation.core.components.ReadOnlyBadge
import ru.alarmtrade.specmp.feature.device.presentation.core.components.SourceTag
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.findNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.pathTo
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.SettingsLayout
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components.settingsLayoutOf
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.DescriptionDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.DiffPanel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.EditorPanel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.ExitDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.ExitSaveDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.LoadErrorDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.LoadSourcesPanel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.MenuError
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.MenuLoading
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.ModelMismatchDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.OverwriteDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.ResetConfirmDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.ResetErrorDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.ResetProgressDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SaveErrorDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SaveFormDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SaveTargetsPanel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SectionTree
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SingleCanFirmwareDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.TransferProgressDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.WriteErrorDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.WriteProgressDialog

@Composable
internal fun DeviceSettingsContent(
    state: DeviceSettingsState,
    location: SettingsLocation,
    actions: DeviceSettingsActions,
    snackbarHostState: CmpSnackbarHostState,
    pane: @Composable (layout: SettingsLayout) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val layout = settingsLayoutOf(maxWidth)
        val windowWidth = maxWidth
        val ready = state.menu is LCEState.Content
        val topBarInPage = ready && !layout.isWide
        CmpScaffold(
            topBar = { if (!topBarInPage) TopBar(state, showActions = ready, actions) },
            bottomBar = { WriteBar(state, actions) },
            snackbarHost = { CmpSnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = if (topBarInPage) {
                WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            } else {
                WindowInsets.systemBars
            },
        ) { padding ->
            CmpColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (val menu = state.menu) {
                    LCEState.None, LCEState.Loading -> MenuLoading()
                    is LCEState.Error -> MenuError(error = menu.error, onRetry = actions.onRetry)
                    is LCEState.Content -> ReadyBody(menu.content, state, location, layout, windowWidth, actions, pane)
                }
            }
        }
        state.editing?.takeUnless { state.vehiclePicker }?.let {
            EditorPanel(editing = it, cars = state.cars, wide = layout.isWide, actions = actions.editor)
        }
        state.diff?.let { DiffPanel(state = it, wide = layout.isWide, actions = actions.transfer) }
        state.saveForm?.let { SaveFormDialog(form = it, actions = actions.transfer) }
        Dialogs(state, layout.isWide, actions)
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
    pane: @Composable (layout: SettingsLayout) -> Unit,
) {
    CmpRow(modifier = Modifier.fillMaxSize()) {
        if (layout.isWide) {
            val maxTree = minOf(TreeMaxWidth, windowWidth * TREE_MAX_FRACTION)
            val tree = rememberPaneWidthState(state.treeWidthDp?.dp, layout.treeWidth, TreeMinWidth, maxTree)
            SectionTree(
                root = root,
                selectedId = (location.sectionId ?: firstSectionId(root)).takeUnless { location.isSearch },
                selectedPath = location.sectionPath,
                sectionChanges = state.sectionChanges,
                changedIds = state.changedIds,
                width = { tree.width },
                onEditSetting = actions.onEditSetting,
                onSelect = { id -> actions.onNavigateTo(root.pathTo(id).orEmpty().map { it.id } + id) },
            )
            CmpResizeHandle(
                state = tree,
                onDragStop = { width -> actions.onTreeWidthChange(width.value.roundToInt()) },
                modifier = Modifier.testTag(SettingsTestTags.TREE_RESIZE),
            )
        }
        // Переход между разделами (запись едет из-за края) не рисуется поверх дерева.
        CmpBox(modifier = Modifier.weight(1f).fillMaxHeight().clipToBounds()) { pane(layout) }
    }
}

/**
 * Непрозрачная страница записи графа. На узкой раскладке панель и крошки — свои у каждой записи: iOS push/pop
 * двигает и затемняет их вместе со страницей. На широкой панель общая (над деревом) и от записи не зависит.
 */
@Composable
internal fun SettingsPage(
    state: DeviceSettingsState,
    location: SettingsLocation,
    layout: SettingsLayout,
    actions: DeviceSettingsActions,
    content: @Composable () -> Unit,
) {
    CmpColumn(modifier = Modifier.fillMaxSize().background(CmpTheme.colors.backgroundPagePrimary)) {
        val root = state.menu.content
        if (!layout.isWide) {
            CmpBox(modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))) {
                TopBar(state, showActions = !location.isSearch && root != null, actions)
            }
            if (!location.isSearch && root != null) Crumbs(root, state, location, actions)
        }
        CmpBox(modifier = Modifier.weight(1f).fillMaxWidth()) { content() }
    }
}

@Composable
private fun TopBar(state: DeviceSettingsState, showActions: Boolean, actions: DeviceSettingsActions) {
    val header = state.header
    if (state.source.kind != SourceKind.DEVICE) {
        ConfigurationTopBar(state, showActions, actions)
        return
    }
    SettingsTopBar(
        state = state,
        showActions = showActions,
        actions = actions,
        title = {
            CmpText(text = stringResource(Strings.string.settings_editor_title_screen), style = CmpTextStyle.Headline2)
        },
        subtitle = header?.let { device ->
            {
                // С переносом: на узкой шапке бейдж единой CAN уходит на вторую строку, а не обрезается.
                CmpFlowRow(
                    itemVerticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
                ) {
                    CmpText(
                        text = stringResource(
                            Strings.string.settings_editor_label_device,
                            device.deviceName,
                            device.deviceId.toString(),
                        ),
                        style = CmpTextStyle.Paragraph3,
                    )
                    device.singleCanCode?.let { code -> CanBadge(code) }
                }
            }
        },
    )
}

@Composable
private fun ConfigurationTopBar(state: DeviceSettingsState, showActions: Boolean, actions: DeviceSettingsActions) {
    val source = state.source
    SettingsTopBar(
        state = state,
        showActions = showActions,
        actions = actions,
        title = {
            CmpText(
                text = source.title.ifEmpty { stringResource(Strings.string.settings_source_title_defaults) },
                style = CmpTextStyle.Headline2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        subtitle = {
            CmpFlowRow(
                itemVerticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
            ) {
                val (icon, label) = when (source.kind) {
                    SourceKind.MINE -> CmpIcons.Inventory2 to Strings.string.settings_common_value_source_mine

                    SourceKind.FILE -> CmpIcons.Description to Strings.string.settings_common_value_source_file

                    SourceKind.CLOUD -> CmpIcons.Cloud to Strings.string.settings_common_value_source_cloud

                    SourceKind.DEFAULTS, SourceKind.DEVICE ->
                        CmpIcons.Factory to
                            Strings.string.settings_common_value_source_defaults
                }
                SourceTag(icon = icon, text = stringResource(label))
                CmpText(text = source.deviceName, style = CmpTextStyle.Paragraph3, maxLines = 1)
                if (source.isReadOnly) ReadOnlyBadge()
            }
        },
    )
}

@Composable
private fun SettingsTopBar(
    state: DeviceSettingsState,
    showActions: Boolean,
    actions: DeviceSettingsActions,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)?,
) {
    CmpTopAppBar(
        title = title,
        subtitle = subtitle,
        navigationIcon = {
            CmpIconButton(
                onClick = actions.onBack,
                icon = CmpIcons.ArrowBack,
                contentDescription = stringResource(Strings.string.settings_common_a11y_back),
            )
        },
        actions = {
            if (showActions) {
                CmpIconButton(
                    onClick = actions.onSearch,
                    modifier = Modifier.testTag(SettingsTestTags.SEARCH_OPEN),
                    icon = CmpIcons.Search,
                    contentDescription = stringResource(Strings.string.settings_common_a11y_search),
                )
                MoreMenu(state, actions)
            }
        },
    )
}

@Composable
private fun MoreMenu(state: DeviceSettingsState, actions: DeviceSettingsActions) {
    var expanded by remember { mutableStateOf(false) }
    val transfer = actions.transfer
    val copyName = stringResource(Strings.string.settings_common_format_copy_name, state.source.title)
    val item: @Composable (StringResource, CmpIcons, String, () -> Unit) -> Unit = { text, icon, tag, onClick ->
        CmpDropdownMenuItem(
            text = stringResource(text),
            icon = icon,
            onClick = {
                expanded = false
                onClick()
            },
            modifier = Modifier.testTag(tag),
        )
    }
    CmpBox {
        CmpIconButton(
            onClick = { expanded = true },
            icon = CmpIcons.MoreVert,
            contentDescription = stringResource(Strings.string.settings_common_a11y_more),
            modifier = Modifier.testTag(SettingsTestTags.MENU),
        )
        CmpDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            when (state.source.kind) {
                SourceKind.DEVICE -> {
                    item(Strings.string.settings_menu_item_load_from, CmpIcons.Download, SettingsTestTags.MENU_LOAD) {
                        transfer.onLoadRequest()
                    }
                    item(Strings.string.settings_menu_item_save_to, CmpIcons.Save, SettingsTestTags.MENU_SAVE_TO) {
                        transfer.onSaveToRequest()
                    }
                    if (state.canResetToFactory) {
                        CmpDivider()
                        item(Strings.string.settings_menu_item_reset, CmpIcons.Refresh, SettingsTestTags.MENU_RESET) {
                            actions.onResetRequest()
                        }
                    }
                }

                SourceKind.MINE -> {
                    item(
                        Strings.string.settings_menu_item_save_file,
                        CmpIcons.Description,
                        SettingsTestTags.MENU_SAVE_FILE,
                    ) {
                        transfer.onExport()
                    }
                    item(Strings.string.settings_menu_item_copy, CmpIcons.ContentCopy, SettingsTestTags.MENU_COPY) {
                        transfer.onCopy(copyName)
                    }
                }

                SourceKind.FILE -> {
                    item(
                        Strings.string.settings_menu_item_save_mine,
                        CmpIcons.Inventory2,
                        SettingsTestTags.MENU_SAVE_MINE,
                    ) {
                        transfer.onSaveToMine()
                    }
                    item(
                        Strings.string.settings_menu_item_save_file_as,
                        CmpIcons.SaveAs,
                        SettingsTestTags.MENU_SAVE_FILE,
                    ) {
                        transfer.onExport()
                    }
                }

                SourceKind.CLOUD, SourceKind.DEFAULTS -> {
                    item(
                        Strings.string.settings_menu_item_save_mine,
                        CmpIcons.Inventory2,
                        SettingsTestTags.MENU_SAVE_MINE,
                    ) {
                        transfer.onSaveToMine()
                    }
                    item(
                        Strings.string.settings_menu_item_save_file,
                        CmpIcons.Description,
                        SettingsTestTags.MENU_SAVE_FILE,
                    ) {
                        transfer.onExport()
                    }
                }
            }
            if (state.showWriteToDevice) {
                item(
                    Strings.string.settings_menu_item_write_device,
                    CmpIcons.Upload,
                    SettingsTestTags.MENU_WRITE_DEVICE,
                ) {
                    transfer.onWriteToDevice()
                }
            }
        }
    }
}

@Composable
private fun CanBadge(code: String) {
    CmpTag(text = stringResource(Strings.string.settings_editor_label_single_can, code), tone = CmpTagTone.Neutral)
}

@Composable
private fun Crumbs(
    root: SettingsSection,
    state: DeviceSettingsState,
    location: SettingsLocation,
    actions: DeviceSettingsActions,
) {
    val sections = location.sectionPath.mapNotNull { id -> root.findNode(id) as? SettingsSection }
    val items =
        listOf(CmpBreadcrumb(stringResource(Strings.string.settings_editor_label_crumb_all), state.changedCount > 0)) +
            sections.map { section ->
                CmpBreadcrumb(
                    label = if (section.number.isEmpty()) section.name else "${section.number} ${section.name}",
                    marked = (state.sectionChanges[section.id] ?: 0) > 0,
                )
            }
    CmpBreadcrumbs(
        items = items,
        onItemClick = { index -> actions.onNavigateTo(location.sectionPath.take(index)) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = CmpSpacing.space8),
    )
    CmpDivider()
}

@Composable
private fun WriteBar(state: DeviceSettingsState, actions: DeviceSettingsActions) {
    if (state.changedCount == 0 || state.menu !is LCEState.Content) return
    val colors = CmpTheme.colors
    CmpColumn(modifier = Modifier.fillMaxWidth().background(colors.backgroundSurfaceCard)) {
        CmpDivider()
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpDot()
            CmpText(
                text = stringResource(Strings.string.settings_editor_label_changed, state.changedCount),
                style = CmpTextStyle.Headline2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            CmpButton(
                text = stringResource(
                    when (state.barAction) {
                        BarAction.WRITE -> Strings.string.settings_editor_btn_write
                        BarAction.SAVE -> Strings.string.settings_editor_btn_save
                        BarAction.SAVE_AS -> Strings.string.settings_editor_btn_save_as
                    },
                ),
                onClick = actions.transfer.onBarAction,
                modifier = Modifier.widthIn(min = WriteButtonMinWidth).testTag(SettingsTestTags.WRITE),
            )
        }
    }
}

@Suppress("CyclomaticComplexMethod") // плоский when по диалогам экрана
@Composable
private fun Dialogs(state: DeviceSettingsState, wide: Boolean, actions: DeviceSettingsActions) {
    val transfer = actions.transfer
    val deviceName = state.source.deviceName.ifEmpty { state.header?.deviceName.orEmpty() }
    when (val dialog = state.dialog) {
        is SettingsDialog.Description -> DescriptionDialog(node = dialog.node, onDismiss = actions.onDialogDismiss)

        SettingsDialog.SingleCanFirmware -> SingleCanFirmwareDialog(
            canCode = state.header?.singleCanCode.orEmpty(),
            onUpdate = actions.onSingleCanUpdate,
            onDismiss = actions.onDialogDismiss,
        )

        is SettingsDialog.ExitConfirm -> ExitDialog(
            changed = state.changedCount,
            onWrite = actions.onExitWrite,
            onDiscard = actions.onExitDiscard,
            onDismiss = actions.onDialogDismiss,
        )

        SettingsDialog.ResetConfirm -> ResetConfirmDialog(
            changed = state.changedCount,
            onConfirm = actions.onResetConfirm,
            onDismiss = actions.onDialogDismiss,
        )

        SettingsDialog.ResetError -> ResetErrorDialog(
            onRetry = actions.onResetConfirm,
            onDismiss = actions.onDialogDismiss,
        )

        SettingsDialog.WriteError -> WriteErrorDialog(
            changed = state.changedCount,
            onRetry = actions.onWrite,
            onDismiss = actions.onDialogDismiss,
        )

        SettingsDialog.LoadSources -> LoadSourcesPanel(
            deviceName = deviceName,
            wide = wide,
            onPick = transfer.onLoadSourcePick,
            onDismiss = actions.onDialogDismiss,
        )

        is SettingsDialog.SaveTargets -> SaveTargetsPanel(
            deviceName = deviceName,
            wide = wide,
            onPick = transfer.onSaveTargetPick,
            onDismiss = actions.onDialogDismiss,
        )

        is SettingsDialog.ModelMismatch -> ModelMismatchDialog(
            sourceDeviceName = dialog.sourceDeviceName,
            deviceName = dialog.deviceName,
            onContinue = transfer.onMismatchContinue,
            onDismiss = actions.onDialogDismiss,
        )

        is SettingsDialog.LoadError -> LoadErrorDialog(
            kind = dialog.kind,
            onOtherSource = transfer.onLoadRequest,
            onDismiss = actions.onDialogDismiss,
        )

        is SettingsDialog.SaveError -> SaveErrorDialog(
            onRetry = transfer.onSaveRetry,
            onDismiss = actions.onDialogDismiss,
        )

        is SettingsDialog.OverwriteConfirm -> OverwriteDialog(
            fileName = state.source.title,
            onConfirm = transfer.onOverwriteConfirm,
            onDismiss = actions.onDialogDismiss,
        )

        SettingsDialog.ExitSave -> ExitSaveDialog(
            changed = state.changedCount,
            saveAs = state.barAction != BarAction.SAVE,
            onSave = transfer.onExitSave,
            onDiscard = actions.onExitDiscard,
            onDismiss = actions.onDialogDismiss,
        )

        null -> Unit
    }
    if (state.isWriting) WriteProgressDialog()
    if (state.isResetting) ResetProgressDialog()
    if (state.isApplying || state.isSaving) TransferProgressDialog(applying = state.isApplying)
}

private val TreeMinWidth = 180.dp
private val WriteButtonMinWidth = 160.dp
private val TreeMaxWidth = 480.dp
private const val TREE_MAX_FRACTION = 0.5f

internal fun firstSectionId(root: SettingsSection): Int? = root.children.firstOrNull { it is SettingsSection }?.id
