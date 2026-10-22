package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareLayout
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.InfoBanner
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.deviceSubtitle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.firmwareLayoutOf
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents.ARCHIVE_REFRESH_TAG
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents.ArchiveDetail
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents.ArchiveEmpty
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents.ArchiveError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents.ArchiveFooter
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents.ArchiveLoading
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents.ArchiveRow
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents.RollbackDialog
import specmp.feature.device.firmware.generated.resources.Res
import specmp.feature.device.firmware.generated.resources.firmware_archive_a11y_refresh
import specmp.feature.device.firmware.generated.resources.firmware_archive_title_screen
import specmp.feature.device.firmware.generated.resources.firmware_common_a11y_back
import specmp.feature.device.firmware.generated.resources.firmware_common_msg_test_channel

private val ListMaxWidth = 760.dp
private val MasterWidth = 440.dp
private val DetailMaxWidth = 1180.dp

// Сколько строк до конца списка остаётся, когда пора подгружать следующую страницу.
private const val END_THRESHOLD = 3

/**
 * Точка входа «Архив прошивок»: версии модуля выбранного канала из [request]. Выбранная для установки версия
 * возвращается в [onPick] (закрыть архив — дело вызывающего), «назад» — [onBack]. Используется графом загрузчика;
 * другие места подключают её через слот навигации в `composeApp`.
 */
@Composable
fun FirmwareArchiveEntry(request: FirmwareArchiveRequest, onPick: (FirmwareVersion) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<FirmwareArchiveViewModel>(parameters = { parametersOf(request) })
    val state by viewModel.state.collectAsState()

    viewModel.effect.collectSideEffect { effect ->
        when (effect) {
            is FirmwareArchiveEffect.Picked -> onPick(effect.version)
        }
    }

    FirmwareArchiveContent(
        state = state,
        actions = FirmwareArchiveActions(
            onBack = onBack,
            onRefresh = viewModel::onRefresh,
            onEndReached = viewModel::onEndReached,
            onRetryMore = viewModel::onRetryMore,
            onSelectRow = viewModel::onSelectRow,
            onPick = viewModel::onPick,
            onRollbackConfirm = viewModel::onRollbackConfirm,
            onRollbackDismiss = viewModel::onRollbackDismiss
        )
    )
}

/** Stateless-контент архива: раскладка по ширине (на широком окне — список и панель версии) и диалог отката. */
@Composable
internal fun FirmwareArchiveContent(state: FirmwareArchiveState, actions: FirmwareArchiveActions) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Res.string.firmware_archive_title_screen)) },
                subtitle = state.deviceTitle.takeIf { it.isNotBlank() }?.let { title ->
                    { CmpText(text = deviceSubtitle(state.profile, title), style = CmpTextStyle.Caption) }
                },
                navigationIcon = {
                    CmpIconButton(
                        onClick = actions.onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.firmware_common_a11y_back)
                    )
                },
                actions = {
                    CmpIconButton(
                        onClick = actions.onRefresh,
                        icon = CmpIcons.Refresh,
                        contentDescription = stringResource(Res.string.firmware_archive_a11y_refresh),
                        tint = cmpThemeColors().primary,
                        modifier = Modifier.testTag(ARCHIVE_REFRESH_TAG)
                    )
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val layout = firmwareLayoutOf(maxWidth)
            when (val content = state.content) {
                ArchiveContent.Loading -> ArchiveLoading()
                ArchiveContent.Error -> ArchiveError(onRetry = actions.onRefresh)
                ArchiveContent.Empty -> ArchiveEmpty(onRefresh = actions.onRefresh)
                is ArchiveContent.Ready -> ReadyBody(state, content, layout, actions)
            }
        }
    }
    state.rollback?.let {
        RollbackDialog(it, onConfirm = actions.onRollbackConfirm, onDismiss = actions.onRollbackDismiss)
    }
}

@Composable
private fun ReadyBody(
    state: FirmwareArchiveState,
    content: ArchiveContent.Ready,
    layout: FirmwareLayout,
    actions: FirmwareArchiveActions
) {
    if (layout == FirmwareLayout.Expanded) {
        val selected = content.rows.firstOrNull { it.version == state.selected } ?: content.rows.first()
        CmpRow(
            modifier = Modifier.fillMaxSize().widthIn(max = DetailMaxWidth).padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            VersionList(
                state = state,
                content = content,
                master = true,
                actions = actions,
                modifier = Modifier.widthIn(max = MasterWidth).fillMaxHeight()
            )
            CmpBox(modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                ArchiveDetail(
                    row = selected,
                    installedVersion = state.installedVersion,
                    onInstall = { actions.onPick(selected.version) }
                )
            }
        }
    } else {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            VersionList(
                state = state,
                content = content,
                master = false,
                actions = actions,
                modifier = Modifier.widthIn(max = ListMaxWidth).fillMaxHeight(),
                padding = if (layout == FirmwareLayout.Compact) 14.dp else 24.dp
            )
        }
    }
}

@Composable
private fun VersionList(
    state: FirmwareArchiveState,
    content: ArchiveContent.Ready,
    master: Boolean,
    actions: FirmwareArchiveActions,
    modifier: Modifier = Modifier,
    padding: Dp = 2.dp
) {
    val listState = rememberLazyListState()
    // Докрутили почти до конца — просим следующую страницу (это сигнал UI → VM, не бизнес-логика).
    val nearEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= info.totalItemsCount - 1 - END_THRESHOLD
        }
    }
    LaunchedEffect(nearEnd, content.rows.size) { if (nearEnd) actions.onEndReached() }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.channel.isTest) {
            item(key = "test-channel") {
                InfoBanner(text = stringResource(Res.string.firmware_common_msg_test_channel))
            }
        }
        items(content.rows, key = { it.version }) { row ->
            ArchiveRow(
                row = row,
                master = master,
                selected = master && state.selected == row.version,
                onSelect = { actions.onSelectRow(row.version) },
                onPick = { actions.onPick(row.version) }
            )
        }
        item(key = "footer") {
            ArchiveFooter(content.loadingMore, content.moreError, actions.onRetryMore)
        }
    }
}
