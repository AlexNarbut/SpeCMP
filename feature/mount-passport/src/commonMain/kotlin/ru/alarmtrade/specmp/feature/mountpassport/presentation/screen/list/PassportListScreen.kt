package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_format_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_msg_delete
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_msg_delete_car
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_subtitle_connected
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_subtitle_offline
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_title_archive
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_title_delete
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_title_unit
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.divider.CmpVerticalDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.hideKeyboardOnScroll
import ru.alarmtrade.specmp.core.ui.view.layout.CmpAdaptiveBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWidthClass
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.component.DeleteConfirmDialog
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.ArchiveHeader
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.BackupReminder
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.BlankDetail
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.EmptyArchive
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.EmptyUnit
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.FoundLine
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.ListError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.ListSkeleton
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.NewPassportButton
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.NothingFound
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.PassportRow
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.PassportRowPick
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.PickBottomBar
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.PickTopBar
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.SelectHint
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.StorageLine
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.TransferActions

private val ListPaneWidth = 400.dp
private val MediumMaxWidth = 720.dp

/**
 * Список паспортов. Compact/Medium — одна колонка (Medium — по центру, 720); Expanded — список 400 dp и справа
 * выбранный паспорт: заготовка — карточкой «Дооформить», остальное — [detail] (предотчёт).
 */
@Composable
internal fun PassportListScreen(
    state: PassportListState,
    actions: PassportListActions,
    snackbar: CmpSnackbarHostState,
    detail: @Composable (passportId: String) -> Unit,
    modifier: Modifier = Modifier,
    overlay: @Composable (wide: Boolean) -> Unit = {},
) {
    CmpAdaptiveBox(modifier = modifier.fillMaxSize()) { screen ->
        val wide = screen.atLeast(CmpWidthClass.Medium)
        ListScaffold(state, actions, snackbar, detail, wide)
        overlay(wide)
    }
}

@Composable
private fun ListScaffold(
    state: PassportListState,
    actions: PassportListActions,
    snackbar: CmpSnackbarHostState,
    detail: @Composable (passportId: String) -> Unit,
    wideBar: Boolean,
) {
    CmpScaffold(
        topBar = {
            if (state.picking) {
                PickTopBar(
                    picked = state.picked.size,
                    allPicked = state.exportable.isNotEmpty() && state.picked.containsAll(state.exportable),
                    onCancel = actions.onPickCancel,
                    onPickAll = actions.onPickAll,
                )
            } else {
                ListTopBar(state, actions, wideBar)
            }
        },
        bottomBar = { if (state.picking) PickBottomBar(state.picked.size, actions.onExportPicked) },
        snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
    ) { padding ->
        CmpAdaptiveBox(modifier = Modifier.fillMaxSize().padding(padding)) { window ->
            if (window.atLeast(CmpWidthClass.Expanded)) {
                Row(modifier = Modifier.fillMaxSize()) {
                    ListPane(state, actions, wide = true, gutter = CmpSpacing.space16, Modifier.width(ListPaneWidth))
                    CmpVerticalDivider()
                    WideDetail(state, actions, detail, Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                val medium = window.atLeast(CmpWidthClass.Medium)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    ListPane(
                        state = state,
                        actions = actions,
                        wide = false,
                        gutter = if (medium) CmpSpacing.screenPaddingHMedium else CmpSpacing.screenPaddingH,
                        modifier = Modifier.widthIn(max = if (medium) MediumMaxWidth else Dp.Infinity),
                    )
                }
            }
        }
    }
    state.deleteCandidate?.let { item ->
        val car = listOfNotNull(item.carTitle?.takeIf { it.isNotBlank() }, item.plate?.takeIf { it.isNotBlank() })
            .joinToString(", ")
        DeleteConfirmDialog(
            title = stringResource(Strings.string.mountpassport_list_title_delete),
            text = if (car.isEmpty()) {
                stringResource(Strings.string.mountpassport_list_msg_delete)
            } else {
                stringResource(Strings.string.mountpassport_list_msg_delete_car, car)
            },
            onConfirm = actions.onDeleteConfirm,
            onDismiss = actions.onDeleteDismiss,
            confirmTag = MountPassportTags.DELETE_CONFIRM,
        )
    }
}

@Composable
private fun ListTopBar(state: PassportListState, actions: PassportListActions, wide: Boolean) {
    val connected = state.connected
    val subtitle = when {
        !state.isArchive && connected != null ->
            stringResource(Strings.string.mountpassport_common_format_unit, connected.name, connected.systemId)

        connected != null ->
            stringResource(Strings.string.mountpassport_list_subtitle_connected, connected.name, connected.systemId)

        state.isArchive -> stringResource(Strings.string.mountpassport_list_subtitle_offline)

        else -> null
    }
    CmpTopAppBar(
        title = stringResource(
            if (state.isArchive) {
                Strings.string.mountpassport_list_title_archive
            } else {
                Strings.string.mountpassport_list_title_unit
            },
        ),
        subtitle = subtitle,
        navigationIcon = {
            CmpIconButton(
                onClick = actions.onBack,
                icon = CmpIcons.ArrowBack,
                contentDescription = stringResource(Strings.string.mountpassport_common_a11y_back),
            )
        },
        actions = {
            if (state.isArchive) {
                TransferActions(
                    wide = wide,
                    canExport = state.exportable.isNotEmpty(),
                    onImport = actions.onImport,
                    onPickStart = actions.onPickStart,
                    onExportAll = actions.onExportAll,
                )
            }
        },
    )
}

@Composable
private fun ListPane(
    state: PassportListState,
    actions: PassportListActions,
    wide: Boolean,
    gutter: Dp,
    modifier: Modifier = Modifier,
) {
    val content = (state.list as? LCEState.Content)?.content
    Column(modifier = modifier.fillMaxHeight()) {
        val header = Modifier.fillMaxWidth().padding(horizontal = gutter, vertical = CmpSpacing.space8)
        when {
            state.picking -> Unit

            state.isArchive -> Column(
                modifier = header,
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                ArchiveHeader(state.query, actions.onQueryChange, actions.onNewWithoutUnit)
                if (state.backupReminder && state.query.isEmpty() && !content?.items.isNullOrEmpty()) {
                    BackupReminder(onExportAll = actions.onExportAll, onLater = actions.onBackupLater)
                }
            }

            !content?.items.isNullOrEmpty() -> NewPassportButton(onNew = actions.onNew, modifier = header)
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().hideKeyboardOnScroll().testTag(MountPassportTags.LIST),
            contentPadding = PaddingValues(
                start = gutter,
                end = gutter,
                top = CmpSpacing.space8,
                bottom = CmpSpacing.screenPaddingBottom,
            ),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            when (val list = state.list) {
                LCEState.None, LCEState.Loading -> item(key = "skeleton") { ListSkeleton() }
                is LCEState.Error -> item(key = "error") { ListError(list.error, actions.onRetry) }
                is LCEState.Content -> passportItems(state, list.content, actions, wide)
            }
        }
    }
}

private fun LazyListScope.passportItems(
    state: PassportListState,
    content: PassportListContent,
    actions: PassportListActions,
    wide: Boolean,
) {
    val passports = content.items
    when {
        passports.isEmpty() && content.query.isNotEmpty() ->
            item(key = "not-found") { NothingFound(content.query, onReset = { actions.onQueryChange("") }) }

        passports.isEmpty() && state.isArchive -> item(key = "empty") { EmptyArchive() }

        passports.isEmpty() -> item(key = "empty") { EmptyUnit(onNew = actions.onNew) }

        else -> {
            if (content.query.isNotEmpty()) {
                item(key = "found") { FoundLine(passports.size) }
            } else if (state.isArchive && !state.picking) {
                state.storage?.let { usage -> item(key = "storage") { StorageLine(usage) } }
            }
            val selected = state.selectedId ?: passports.first().id
            items(passports, key = { it.id }) { item ->
                ListRow(state, item, content.query, actions, selected = wide && item.id == selected, wide = wide)
            }
        }
    }
}

@Composable
private fun ListRow(
    state: PassportListState,
    item: MountPassportSummary,
    query: String,
    actions: PassportListActions,
    selected: Boolean,
    wide: Boolean,
) {
    PassportRow(
        item = item,
        query = query,
        canFinish = state.canFinish(item),
        selected = selected && !state.picking,
        onOpen = { if (wide) actions.onSelect(item) else actions.onOpen(item) },
        onFinish = { actions.onFinish(item) },
        onDelete = { actions.onDeleteRequest(item) },
        onExport = { actions.onExport(item) },
        pick = if (state.picking) PassportRowPick(item.id in state.picked) { actions.onPickToggle(item) } else null,
    )
}

@Composable
private fun WideDetail(
    state: PassportListState,
    actions: PassportListActions,
    detail: @Composable (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val passports = (state.list as? LCEState.Content)?.content?.items.orEmpty()
    val selected: MountPassportSummary? = passports.firstOrNull { it.id == state.selectedId } ?: passports.firstOrNull()
    Box(modifier = modifier) {
        when {
            selected == null -> SelectHint()

            selected.status == PassportStatus.BLANK ->
                BlankDetail(selected, state.canFinish(selected), onFinish = { actions.onFinish(selected) })

            else -> detail(selected.id)
        }
    }
}
