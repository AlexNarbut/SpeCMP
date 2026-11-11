package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.configurator_card_label_updated
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_a11y_clear
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_btn_create
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_btn_delete
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_btn_open
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_btn_reset_filter
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_error_title
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_format_copy_name
import ru.alarmtrade.specmp.core.localization.resources.configurator_common_title
import ru.alarmtrade.specmp.core.localization.resources.configurator_create_item_cloud
import ru.alarmtrade.specmp.core.localization.resources.configurator_create_item_file
import ru.alarmtrade.specmp.core.localization.resources.configurator_create_item_scratch
import ru.alarmtrade.specmp.core.localization.resources.configurator_create_subtitle_cloud
import ru.alarmtrade.specmp.core.localization.resources.configurator_create_subtitle_file
import ru.alarmtrade.specmp.core.localization.resources.configurator_create_subtitle_scratch
import ru.alarmtrade.specmp.core.localization.resources.configurator_create_title
import ru.alarmtrade.specmp.core.localization.resources.configurator_delete_msg
import ru.alarmtrade.specmp.core.localization.resources.configurator_delete_title
import ru.alarmtrade.specmp.core.localization.resources.configurator_detail_label_created
import ru.alarmtrade.specmp.core.localization.resources.configurator_detail_msg_empty
import ru.alarmtrade.specmp.core.localization.resources.configurator_detail_msg_no_description
import ru.alarmtrade.specmp.core.localization.resources.configurator_empty_msg
import ru.alarmtrade.specmp.core.localization.resources.configurator_empty_title
import ru.alarmtrade.specmp.core.localization.resources.configurator_menu_item_copy
import ru.alarmtrade.specmp.core.localization.resources.configurator_menu_item_delete
import ru.alarmtrade.specmp.core.localization.resources.configurator_menu_item_rename
import ru.alarmtrade.specmp.core.localization.resources.configurator_menu_item_save_file
import ru.alarmtrade.specmp.core.localization.resources.configurator_menu_item_write
import ru.alarmtrade.specmp.core.localization.resources.configurator_mine_msg_empty_pick
import ru.alarmtrade.specmp.core.localization.resources.configurator_mine_placeholder_search
import ru.alarmtrade.specmp.core.localization.resources.configurator_mine_title
import ru.alarmtrade.specmp.core.localization.resources.configurator_mine_title_empty_pick
import ru.alarmtrade.specmp.core.localization.resources.configurator_mine_value_all_models
import ru.alarmtrade.specmp.core.localization.resources.configurator_nothing_msg
import ru.alarmtrade.specmp.core.localization.resources.configurator_nothing_title
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpFloatingActionButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.chip.CmpChip
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSearchField
import ru.alarmtrade.specmp.core.ui.view.input.hideKeyboardOnScroll
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenu
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenuItem
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.ConfiguratorTestTags
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.BusyOverlay
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.ListDetailPane
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.ListDetailWideFrom
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.MineCard
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.components.rememberDateTimeText
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.device.presentation.core.components.ConfigurationInfoDialog
import ru.alarmtrade.specmp.feature.device.presentation.core.components.ConfigurationOptionRow
import ru.alarmtrade.specmp.feature.device.presentation.core.components.ConfigurationOptionsPanel
import ru.alarmtrade.specmp.feature.device.presentation.core.components.ModelBadge

@Composable
internal fun MineScreen(state: MineState, actions: MineActions, snackbarHostState: CmpSnackbarHostState) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= ListDetailWideFrom
        CmpScaffold(
            topBar = {
                CmpTopAppBar(
                    title = {
                        val title = if (state.pickMode) {
                            Strings.string.configurator_mine_title
                        } else {
                            Strings.string.configurator_common_title
                        }
                        CmpText(text = stringResource(title))
                    },
                    navigationIcon = {
                        CmpIconButton(
                            onClick = actions.onBack,
                            icon = CmpIcons.ArrowBack,
                            contentDescription = stringResource(Strings.string.configurator_common_a11y_back),
                        )
                    },
                )
            },
            floatingActionButton = {
                if (!state.pickMode) {
                    CmpFloatingActionButton(
                        onClick = actions.onCreateRequest,
                        modifier = Modifier.testTag(ConfiguratorTestTags.CREATE),
                    ) {
                        CmpRow(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CmpIcon(icon = CmpIcons.Add, contentDescription = null)
                            CmpText(text = stringResource(Strings.string.configurator_common_btn_create))
                        }
                    }
                }
            },
            snackbarHost = { CmpSnackbarHost(hostState = snackbarHostState) },
        ) { padding ->
            ListDetailPane(wide, detail = { MineDetail(state, actions) }, modifier = Modifier.padding(padding)) {
                MineToolbar(state, actions)
                MineContent(state, actions, wide)
            }
        }
        MineDialogs(state, actions, wide)
        if (state.busy) BusyOverlay()
    }
}

@Composable
private fun MineToolbar(state: MineState, actions: MineActions) {
    CmpColumn(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CmpSearchField(
            value = state.query,
            onValueChange = actions.onQueryChange,
            placeholder = stringResource(Strings.string.configurator_mine_placeholder_search),
            clearContentDescription = stringResource(Strings.string.configurator_common_a11y_clear),
            modifier = Modifier.fillMaxWidth().testTag(ConfiguratorTestTags.SEARCH),
        )
        ModelFilterChip(
            models = (state.models + listOfNotNull(state.modelFilter)).distinct(),
            selected = state.modelFilter,
            onSelect = actions.onModelFilter,
        )
    }
}

@Composable
internal fun ModelFilterChip(models: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val clear = stringResource(Strings.string.configurator_common_a11y_clear)
    CmpBox {
        CmpChip(
            text = selected ?: stringResource(Strings.string.configurator_mine_value_all_models),
            onClick = { expanded = true },
            icon = CmpIcons.FilterAlt,
            selected = selected != null,
            onClear = selected?.let { { onSelect(null) } },
            clearContentDescription = clear,
            modifier = Modifier.testTag(ConfiguratorTestTags.MODEL_FILTER),
        )
        CmpDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CmpDropdownMenuItem(
                text = stringResource(Strings.string.configurator_mine_value_all_models),
                selected = selected == null,
                onClick = {
                    expanded = false
                    onSelect(null)
                },
            )
            models.forEach { model ->
                CmpDropdownMenuItem(
                    text = model,
                    selected = model == selected,
                    onClick = {
                        expanded = false
                        onSelect(model)
                    },
                )
            }
        }
    }
}

@Composable
private fun MineContent(state: MineState, actions: MineActions, wide: Boolean) {
    when (state.list) {
        LCEState.None, LCEState.Loading -> CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(SKELETON_ROWS) { CmpSkeleton(modifier = Modifier.fillMaxWidth().height(96.dp)) }
        }

        is LCEState.Error -> CmpStateBox(
            icon = CmpIcons.ErrorOutline,
            title = state.list.error.text(Strings.string.configurator_common_error_title),
            tone = CmpStateTone.Error,
            modifier = Modifier.fillMaxSize(),
        ) {
            CmpButton(
                text = stringResource(Strings.string.configurator_common_btn_retry),
                onClick = actions.onRetry,
                modifier = Modifier.testTag(ConfiguratorTestTags.RETRY),
            )
        }

        is LCEState.Content -> {
            val items = state.filtered
            when {
                state.all.isEmpty() || (state.pickMode && items.isEmpty() && state.query.isBlank()) -> EmptyState(
                    state,
                    actions,
                )

                items.isEmpty() -> CmpStateBox(
                    icon = CmpIcons.SearchOff,
                    title = stringResource(Strings.string.configurator_nothing_title),
                    text = stringResource(Strings.string.configurator_nothing_msg),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CmpOutlinedButton(
                        text = stringResource(Strings.string.configurator_common_btn_reset_filter),
                        onClick = actions.onResetFilter,
                    )
                }

                else -> MineList(state, items, actions, wide)
            }
        }
    }
}

@Composable
private fun EmptyState(state: MineState, actions: MineActions) {
    if (state.pickMode) {
        CmpStateBox(
            icon = CmpIcons.Inventory2,
            title = stringResource(Strings.string.configurator_mine_title_empty_pick, state.modelFilter.orEmpty()),
            text = stringResource(Strings.string.configurator_mine_msg_empty_pick),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (state.modelFilter != null) {
                CmpOutlinedButton(
                    text = stringResource(Strings.string.configurator_common_btn_reset_filter),
                    onClick = actions.onResetFilter,
                )
            }
        }
    } else {
        CmpStateBox(
            icon = CmpIcons.Inventory2,
            title = stringResource(Strings.string.configurator_empty_title),
            text = stringResource(Strings.string.configurator_empty_msg),
            modifier = Modifier.fillMaxSize(),
        ) {
            CmpButton(
                text = stringResource(Strings.string.configurator_create_item_scratch),
                onClick = actions.onCreateScratch,
                modifier = CreateButtonModifier,
            )
            CmpOutlinedButton(
                text = stringResource(Strings.string.configurator_create_item_cloud),
                onClick = actions.onCreateCloud,
                modifier = CreateButtonModifier,
            )
            CmpOutlinedButton(
                text = stringResource(Strings.string.configurator_create_item_file),
                onClick = actions.onCreateFile,
                modifier = CreateButtonModifier,
            )
        }
    }
}

private val CreateButtonModifier = Modifier.fillMaxWidth().height(52.dp)

@Composable
private fun MineList(state: MineState, items: List<LocalConfiguration>, actions: MineActions, wide: Boolean) {
    val listState = rememberLazyListState()
    val dateText = rememberDateTimeText()
    val shown = items.take(state.shown)
    val hasMore = shown.size < items.size
    val loadMore by rememberUpdatedState(actions.onLoadMore)
    LaunchedEffect(listState, hasMore) {
        if (!hasMore) return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { last -> if (last >= listState.layoutInfo.totalItemsCount - LOAD_MORE_AHEAD) loadMore() }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().hideKeyboardOnScroll(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(shown, key = { it.id }) { item ->
            MineCard(
                item = item,
                selected = wide && item.id == state.selectedId,
                dateText = dateText,
                onClick = { if (wide && !state.pickMode) actions.onSelect(item.id) else actions.onOpen(item) },
                onMenu = if (state.pickMode) null else ({ actions.onMenu(item) }),
            )
        }
        if (hasMore) {
            item(key = "more") {
                CmpBox(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CmpCircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun MineDetail(state: MineState, actions: MineActions) {
    val colors = cmpThemeColors()
    val item = state.selected
    if (item == null) {
        CmpStateBox(
            icon = CmpIcons.Inventory2,
            title = stringResource(Strings.string.configurator_detail_msg_empty),
            compact = true,
            modifier = Modifier.fillMaxSize(),
        )
        return
    }
    val dateText = rememberDateTimeText()
    val copyName = stringResource(Strings.string.configurator_common_format_copy_name, item.name)
    CmpColumn(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpText(text = item.name, style = CmpTextStyle.Title)
        CmpFlowRow(
            itemVerticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ModelBadge(item.deviceName)
            CmpText(
                text = stringResource(Strings.string.configurator_card_label_updated, dateText(item.updated)),
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant,
            )
            CmpText(
                text = stringResource(Strings.string.configurator_detail_label_created, dateText(item.created)),
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant,
            )
        }
        CmpText(
            text = item.description.ifBlank { stringResource(Strings.string.configurator_detail_msg_no_description) },
            style = CmpTextStyle.BodyLarge,
            color = if (item.description.isBlank()) colors.onSurfaceVariant else colors.onSurface,
        )
        CmpFlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CmpButton(
                text = stringResource(Strings.string.configurator_common_btn_open),
                onClick = { actions.onOpen(item) },
                modifier = Modifier.testTag(ConfiguratorTestTags.OPEN),
            )
            if (state.connectedDeviceName != null) {
                CmpOutlinedButton(
                    text = stringResource(Strings.string.configurator_menu_item_write),
                    onClick = { actions.onWrite(item) },
                )
            }
            CmpOutlinedButton(
                text = stringResource(Strings.string.configurator_menu_item_save_file),
                onClick = { actions.onExport(item) },
            )
        }
        CmpFlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CmpTextButton(
                text = stringResource(Strings.string.configurator_menu_item_rename),
                onClick = { actions.onRenameRequest(item) },
            )
            CmpTextButton(text = stringResource(Strings.string.configurator_menu_item_copy), onClick = {
                actions.onCopy(item, copyName)
            })
            CmpTextButton(
                text = stringResource(Strings.string.configurator_menu_item_delete),
                onClick = { actions.onDeleteRequest(item) },
            )
        }
    }
}

@Composable
private fun MineDialogs(state: MineState, actions: MineActions, wide: Boolean) {
    when (val dialog = state.dialog) {
        MineDialog.Create -> ConfigurationOptionsPanel(
            title = stringResource(Strings.string.configurator_create_title),
            wide = wide,
            onDismiss = actions.onDialogDismiss,
        ) {
            ConfigurationOptionRow(
                icon = CmpIcons.Factory,
                title = stringResource(Strings.string.configurator_create_item_scratch),
                subtitle = stringResource(Strings.string.configurator_create_subtitle_scratch),
                onClick = actions.onCreateScratch,
                modifier = Modifier.testTag(ConfiguratorTestTags.CREATE_SCRATCH),
            )
            ConfigurationOptionRow(
                icon = CmpIcons.Cloud,
                title = stringResource(Strings.string.configurator_create_item_cloud),
                subtitle = stringResource(Strings.string.configurator_create_subtitle_cloud),
                onClick = actions.onCreateCloud,
                modifier = Modifier.testTag(ConfiguratorTestTags.CREATE_CLOUD),
            )
            ConfigurationOptionRow(
                icon = CmpIcons.Description,
                title = stringResource(Strings.string.configurator_create_item_file),
                subtitle = stringResource(Strings.string.configurator_create_subtitle_file),
                onClick = actions.onCreateFile,
                modifier = Modifier.testTag(ConfiguratorTestTags.CREATE_FILE),
            )
        }

        is MineDialog.Menu -> ItemMenu(dialog.item, state.connectedDeviceName != null, wide, actions)

        is MineDialog.Rename -> ConfigurationInfoDialog(
            title = stringResource(Strings.string.configurator_menu_item_rename),
            name = dialog.name,
            description = dialog.description,
            deviceName = null,
            onChange = actions.onRenameChange,
            onConfirm = actions.onRenameSubmit,
            onDismiss = actions.onDialogDismiss,
        )

        is MineDialog.Delete -> CmpDialog(
            onDismissRequest = actions.onDialogDismiss,
            title = { CmpText(text = stringResource(Strings.string.configurator_delete_title, dialog.item.name)) },
            text = { CmpText(text = stringResource(Strings.string.configurator_delete_msg)) },
            confirmButton = {
                CmpButton(
                    text = stringResource(Strings.string.configurator_common_btn_delete),
                    onClick = actions.onDeleteConfirm,
                    modifier = Modifier.testTag(ConfiguratorTestTags.DELETE_CONFIRM),
                )
            },
            dismissButton = {
                CmpTextButton(
                    text = stringResource(Strings.string.configurator_common_btn_cancel),
                    onClick = actions.onDialogDismiss,
                )
            },
        )

        null -> Unit
    }
}

@Composable
private fun ItemMenu(item: LocalConfiguration, canWrite: Boolean, wide: Boolean, actions: MineActions) {
    val copyName = stringResource(Strings.string.configurator_common_format_copy_name, item.name)
    ConfigurationOptionsPanel(title = item.name, wide = wide, onDismiss = actions.onDialogDismiss) {
        ConfigurationOptionRow(
            icon = CmpIcons.Edit,
            title = stringResource(Strings.string.configurator_menu_item_rename),
            subtitle = null,
            onClick = { actions.onRenameRequest(item) },
            modifier = Modifier.testTag(ConfiguratorTestTags.MENU_RENAME),
        )
        ConfigurationOptionRow(
            icon = CmpIcons.ContentCopy,
            title = stringResource(Strings.string.configurator_menu_item_copy),
            subtitle = null,
            onClick = { actions.onCopy(item, copyName) },
            modifier = Modifier.testTag(ConfiguratorTestTags.MENU_COPY),
        )
        ConfigurationOptionRow(
            icon = CmpIcons.Description,
            title = stringResource(Strings.string.configurator_menu_item_save_file),
            subtitle = null,
            onClick = { actions.onExport(item) },
            modifier = Modifier.testTag(ConfiguratorTestTags.MENU_SAVE_FILE),
        )
        if (canWrite) {
            ConfigurationOptionRow(
                icon = CmpIcons.Upload,
                title = stringResource(Strings.string.configurator_menu_item_write),
                subtitle = null,
                onClick = { actions.onWrite(item) },
                modifier = Modifier.testTag(ConfiguratorTestTags.MENU_WRITE),
            )
        }
        ConfigurationOptionRow(
            icon = CmpIcons.Delete,
            title = stringResource(Strings.string.configurator_menu_item_delete),
            subtitle = null,
            onClick = { actions.onDeleteRequest(item) },
            modifier = Modifier.testTag(ConfiguratorTestTags.MENU_DELETE),
        )
    }
}

private const val SKELETON_ROWS = 4
private const val LOAD_MORE_AHEAD = 3
