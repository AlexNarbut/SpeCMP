package ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_banner_btn_turn_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_banner_format_stale
import ru.alarmtrade.specmp.core.localization.resources.devicestate_banner_msg_not_guarding
import ru.alarmtrade.specmp.core.localization.resources.devicestate_banner_msg_program
import ru.alarmtrade.specmp.core.localization.resources.devicestate_banner_msg_reboot
import ru.alarmtrade.specmp.core.localization.resources.devicestate_banner_msg_stale
import ru.alarmtrade.specmp.core.localization.resources.devicestate_banner_title_program
import ru.alarmtrade.specmp.core.localization.resources.devicestate_banner_title_program_exit
import ru.alarmtrade.specmp.core.localization.resources.devicestate_banner_title_service
import ru.alarmtrade.specmp.core.localization.resources.devicestate_check_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.devicestate_check_title_details
import ru.alarmtrade.specmp.core.localization.resources.devicestate_check_title_screen
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.devicestate_cmd_msg_hold_hint
import ru.alarmtrade.specmp.core.localization.resources.devicestate_commands_title_section
import ru.alarmtrade.specmp.core.localization.resources.devicestate_details_btn_open
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_btn_keep
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_btn_keep_both
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_btn_turn_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_msg_both
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_msg_program
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_msg_service
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_title_dialog
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_btn_all_checks
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_btn_back
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_btn_continue
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_btn_finish
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_btn_next
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_format_finish
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_format_next
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_msg_back
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_title_back
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_title_checks
import ru.alarmtrade.specmp.core.localization.resources.devicestate_fast_title_finish
import ru.alarmtrade.specmp.core.localization.resources.devicestate_pin_msg_program
import ru.alarmtrade.specmp.core.localization.resources.devicestate_pult_btn_all_commands
import ru.alarmtrade.specmp.core.localization.resources.devicestate_pult_btn_device_commands
import ru.alarmtrade.specmp.core.localization.resources.devicestate_tabs_format_checks
import ru.alarmtrade.specmp.core.localization.resources.devicestate_tabs_item_commands
import ru.alarmtrade.specmp.core.localization.resources.devicestate_tabs_item_details
import ru.alarmtrade.specmp.core.localization.resources.devicestate_tabs_item_pult
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpActionBar
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarResult
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.divider.CmpVerticalDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWidthClass
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWindowSize
import ru.alarmtrade.specmp.core.ui.view.layout.cmpWindowSize
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTabItem
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTabRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.deviceTitle
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialog
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.ActiveZones
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.AllCommandsSheet
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.CheckStateBanner
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.ChecklistPanel
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.CommandsGrid
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.DangerSheet
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.EngineCard
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.ExtraGroups
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.HorizontalVehicle
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.LinkCard
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.LiveCard
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.MetricStrip
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.PanelSheet
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.PultCard
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.SectionCard
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.StatusHero
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.StatusPill
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.ZonesList
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.ZonesSkeleton
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.label
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.snackbarFormat
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.CheckStateFastMount

internal const val CHECK_STATE_NEXT_TAG = "devicestate_next"
internal const val CHECK_STATE_TABS_TAG = "devicestate_tabs"

/**
 * Раскладка — по ширине и высоте окна контента (не по типу устройства): < 600 — вкладки; высота < 520 — низкое
 * окно (телефон-ландшафт); 600–1099 — две равные панели (раскладной, планшет-портрет); ≥ 1100 — три колонки.
 */
private enum class CheckStateLayout { Compact, Low, Split, Desk }

private val WideDeskFrom = 1240.dp
private const val ALARM_BORDER_MIN_ALPHA = 0.25f
private const val ALARM_BORDER_MILLIS = 500
private val AlarmBorderWidth = 6.dp

@Immutable
internal data class CheckStateActions(
    val onBack: () -> Unit = {},
    val onCommand: (StateCommand) -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onPinSubmit: (String) -> Unit = {},
    val onExitTurnOff: () -> Unit = {},
    val onExitKeep: () -> Unit = {},
    val onFastBackConfirm: () -> Unit = {},
    val onNext: () -> Unit = {},
    val onFinishConfirm: () -> Unit = {},
)

/** Только BLE: при USB плитку не показывает расширенный монтаж. */
@Composable
fun CheckStateEntry(onBack: () -> Unit, fastMount: CheckStateFastMount? = null) {
    val viewModel = koinViewModel<CheckStateViewModel> { parametersOf(fastMount != null) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()
    val leave = fastMount?.onBack ?: onBack
    val checklist = fastMount?.checklist
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            is CheckStateEffect.CommandFinished -> scope.launch { showResult(snackbar, effect, viewModel::onRetry) }
            CheckStateEffect.Close -> leave()
            CheckStateEffect.Next -> fastMount?.onNext?.invoke()
        }
    }
    val actions = remember(viewModel, leave, checklist) {
        CheckStateActions(
            onBack = viewModel::onBack,
            onCommand = viewModel::onCommand,
            onConfirm = viewModel::onConfirm,
            onDialogDismiss = viewModel::onDialogDismiss,
            onPinSubmit = viewModel::onPinSubmit,
            onExitTurnOff = viewModel::onExitTurnOff,
            onExitKeep = {
                viewModel.onDialogDismiss()
                leave()
            },
            onFastBackConfirm = {
                viewModel.onDialogDismiss()
                leave()
            },
            onNext = {
                val tasks = checklist?.tasks.orEmpty()
                viewModel.onNext(notDone = tasks.size - (checklist?.closed ?: 0), total = tasks.size)
            },
            onFinishConfirm = viewModel::onFinishConfirm,
        )
    }
    if (fastMount != null) {
        // Шаг мастера: системный «Назад» — как стрелка в шапке (диалог «Вернуться на предыдущий шаг?»).
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = actions.onBack,
        )
    }
    CheckStateContent(state = state, actions = actions, fastMount = fastMount, snackbar = snackbar)
}

private suspend fun showResult(
    snackbar: CmpSnackbarHostState,
    effect: CheckStateEffect.CommandFinished,
    onRetry: (StateCommand) -> Unit,
) {
    val message = getString(effect.outcome.snackbarFormat, getString(effect.command.label))
    val failed = effect.outcome != CommandOutcome.DONE
    val result = snackbar.showSnackbar(
        message = message,
        actionLabel = if (failed) getString(Strings.string.devicestate_cmd_btn_retry) else null,
    )
    if (failed && result == CmpSnackbarResult.ActionPerformed) onRetry(effect.command)
}

private enum class Tab { CHECKS, PULT, DETAILS }

/** Листы низкого окна: «Подробно» и весь чек-лист. */
private enum class PanelKind { DETAILS, CHECKS }

/** Вкладки правой панели раскладки «две панели». */
private enum class PaneTab { CHECKS, COMMANDS, DETAILS }

@Composable
internal fun CheckStateContent(
    state: CheckStateState,
    actions: CheckStateActions,
    fastMount: CheckStateFastMount? = null,
    snackbar: CmpSnackbarHostState = rememberCmpSnackbarHostState(),
    initialTab: Int? = null,
) {
    val subtitle = state.deviceId?.let { id -> deviceTitle(state.deviceName.orEmpty(), id) }
    var allCommands by rememberSaveable { mutableStateOf(false) }
    val onFire: (CommandUi) -> Unit =
        remember(actions) { { actions.onCommand(it.command) } }
    var sheet by rememberSaveable { mutableStateOf<PanelKind?>(null) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val layout = checkStateLayout(cmpWindowSize)
        CmpScaffold(
            topBar = { CheckStateTopBar(state, actions, fastMount, subtitle, layout) },
            snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
        ) { padding ->
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                val parts = Parts(
                    state = state,
                    actions = actions,
                    fastMount = fastMount,
                    onFire = onFire,
                    columns = 2,
                    onAllCommands = { allCommands = true },
                    onOpenSheet = { sheet = it },
                )
                when (layout) {
                    CheckStateLayout.Compact -> CompactLayout(parts, initialTab)
                    CheckStateLayout.Low -> LowLayout(parts, width = maxWidth, height = maxHeight)
                    CheckStateLayout.Split -> SplitLayout(parts, half = maxWidth / 2)
                    CheckStateLayout.Desk -> DeskLayout(parts, width = maxWidth)
                }
                if (state.alarm) AlarmBorder()
            }
        }
    }
    sheet?.let { kind -> PanelSheets(kind, state, fastMount, onDismiss = { sheet = null }) }
    if (allCommands) {
        AllCommandsSheet(
            commands = state.commands,
            status = { state.status(it.command) },
            enabled = state.commandsIdle,
            columns = 2,
            onFire = onFire,
            onDismiss = { allCommands = false },
        )
    }
    CheckStateDialogs(state, actions)
}

private fun checkStateLayout(window: CmpWindowSize): CheckStateLayout = when {
    window.isLowHeight -> CheckStateLayout.Low
    window.atLeast(CmpWidthClass.Large) -> CheckStateLayout.Desk
    window.atLeast(CmpWidthClass.Medium) -> CheckStateLayout.Split
    else -> CheckStateLayout.Compact
}

@Composable
private fun CheckStateTopBar(
    state: CheckStateState,
    actions: CheckStateActions,
    fastMount: CheckStateFastMount?,
    subtitle: String?,
    layout: CheckStateLayout,
) {
    val chrome = fastMount?.chrome
    if (chrome != null) {
        chrome.topBar(actions.onBack)
        return
    }
    CmpTopAppBar(
        title = stringResource(Strings.string.devicestate_check_title_screen),
        subtitle = subtitle,
        navigationIcon = {
            CmpIconButton(
                onClick = actions.onBack,
                icon = CmpIcons.ArrowBack,
                contentDescription = stringResource(Strings.string.devicestate_check_a11y_back),
            )
        },
        actions = {
            // Низкое окно: карточки статуса нет — статус в шапке.
            if (layout == CheckStateLayout.Low) StatusPill(state, Modifier.padding(end = CmpSpacing.space12))
        },
    )
}

@Composable
private fun PanelSheets(
    kind: PanelKind,
    state: CheckStateState,
    fastMount: CheckStateFastMount?,
    onDismiss: () -> Unit,
) {
    when (kind) {
        PanelKind.DETAILS -> PanelSheet(
            title = stringResource(Strings.string.devicestate_check_title_details),
            onDismiss = onDismiss,
        ) { DetailBlocks(state) }

        PanelKind.CHECKS -> PanelSheet(
            title = stringResource(Strings.string.devicestate_fast_title_checks),
            onDismiss = onDismiss,
        ) {
            fastMount?.let { mount ->
                ChecklistPanel(mount.checklist, state.vehicle, onSkip = mount.onSkip, onAnswer = mount.onAnswer)
            }
        }
    }
}

private class Parts(
    val state: CheckStateState,
    val actions: CheckStateActions,
    val fastMount: CheckStateFastMount?,
    val onFire: (CommandUi) -> Unit,
    val columns: Int,
    val onAllCommands: () -> Unit,
    val onOpenSheet: (PanelKind) -> Unit,
) {
    val hasChecks: Boolean get() = fastMount != null && fastMount.checklist.tasks.isNotEmpty()
}

@Composable
private fun CompactLayout(parts: Parts, initialTab: Int?) {
    val state = parts.state
    val tabs = buildList {
        if (parts.hasChecks) add(Tab.CHECKS)
        add(Tab.PULT)
        add(Tab.DETAILS)
    }
    var selected by rememberSaveable { mutableIntStateOf(initialTab ?: 0) }
    val index = selected.coerceIn(0, tabs.lastIndex)
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(
                start = CmpSpacing.screenPaddingH,
                end = CmpSpacing.screenPaddingH,
                top = CmpSpacing.space4,
            ),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            StatusArea(state, parts.actions)
            Banners(state, parts.actions)
        }
        TabbedPane(
            titles = tabs.map { tabTitle(it, parts) },
            selected = index,
            onSelect = { selected = it },
        ) {
            when (tabs[index]) {
                Tab.CHECKS -> Checks(parts)

                Tab.PULT -> PultCard(
                    state,
                    carWidth = 196.dp,
                    onFire = parts.onFire,
                    onAllCommands = parts.onAllCommands,
                )

                Tab.DETAILS -> Details(parts)
            }
        }
        FastDock(parts)
    }
}

/** Две равные панели (граница — посередине, по сгибу раскладного): слева «живое», справа вкладки. */
@Composable
private fun SplitLayout(parts: Parts, half: Dp) {
    val tabs = buildList {
        if (parts.hasChecks) add(PaneTab.CHECKS)
        add(PaneTab.COMMANDS)
        add(PaneTab.DETAILS)
    }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val tab = tabs[selected.coerceIn(0, tabs.lastIndex)]
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        CmpRow(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CmpColumn(
                modifier = Modifier
                    .width(half)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = CmpSpacing.screenPaddingH,
                        end = CmpSpacing.screenPaddingH,
                        top = CmpSpacing.space12,
                        bottom = CmpSpacing.screenPaddingBottom,
                    ),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                StatusArea(parts.state, parts.actions)
                Banners(parts.state, parts.actions)
                LiveCard(parts.state, carWidth = minOf(SplitCarMax, half * SPLIT_CAR_FRACTION))
            }
            CmpVerticalDivider(modifier = Modifier.fillMaxHeight())
            CmpColumn(modifier = Modifier.weight(1f).fillMaxHeight()) {
                TabbedPane(
                    titles = tabs.map { paneTabTitle(it, parts) },
                    selected = tabs.indexOf(tab),
                    onSelect = { selected = it },
                ) {
                    when (tab) {
                        PaneTab.CHECKS -> Checks(parts)
                        PaneTab.COMMANDS -> CommandsBlock(parts, columns = if (half >= TwoColumnCommandsFrom) 2 else 1)
                        PaneTab.DETAILS -> DetailBlocks(parts.state)
                    }
                }
            }
        }
        FastDock(parts, buttonWidth = half)
    }
}

/** Три колонки (планшет-ландшафт, desktop): машина и показатели | все зоны и детали | команды или чек-лист. */
@Composable
private fun DeskLayout(parts: Parts, width: Dp) {
    val state = parts.state
    val leftWidth = if (width >= WideDeskFrom) 340.dp else 300.dp
    val rightWidth = if (width >= WideDeskFrom) 400.dp else 340.dp
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        CmpRow(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(
                start = CmpSpacing.screenPaddingHMedium,
                end = CmpSpacing.screenPaddingHMedium,
                top = CmpSpacing.space12,
            ),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space24),
        ) {
            ScrollColumn(Modifier.width(leftWidth)) {
                StatusArea(state, parts.actions)
                Banners(state, parts.actions)
                LiveCard(state, carWidth = DeskCarWidth, chips = false)
                if (parts.hasChecks) {
                    // Команды не мешают чек-листу: все команды — листом.
                    CmpButton(
                        text = stringResource(Strings.string.devicestate_pult_btn_device_commands),
                        onClick = parts.onAllCommands,
                        type = CmpButtonType.Secondary,
                        fullWidth = true,
                    )
                }
            }
            ScrollColumn(Modifier.weight(1f)) {
                state.vehicle?.let { vehicle ->
                    SectionCard {
                        ActiveZones(vehicle = vehicle, changed = state.changedZones, tick = state.tick)
                    }
                }
                DetailBlocks(state)
            }
            ScrollColumn(Modifier.width(rightWidth)) {
                if (parts.hasChecks) {
                    SectionCard(
                        title = stringResource(Strings.string.devicestate_fast_title_checks),
                        contentPadding = CmpSpacing.space8,
                    ) {
                        Checks(parts)
                    }
                } else {
                    CommandsSection(parts, columns = 1)
                }
            }
        }
        FastDock(parts, buttonWidth = rightWidth)
    }
}

/** Низкое окно (телефон-ландшафт): без карточки статуса и вкладок; главное — без прокрутки. */
@Composable
private fun LowLayout(parts: Parts, width: Dp, height: Dp) {
    val state = parts.state
    val vehicle = state.vehicle
    val rightWidth = minOf(LowRightMax, width * LOW_RIGHT_FRACTION)
    val carHeight = (height - LOW_CAR_RESERVE).coerceIn(LowCarMin, LowCarMax)
    CmpRow(
        modifier = Modifier.fillMaxSize().padding(
            start = CmpSpacing.screenPaddingH,
            end = CmpSpacing.screenPaddingH,
            top = CmpSpacing.space8,
            bottom = CmpSpacing.space12,
        ),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
    ) {
        ScrollColumn(Modifier.weight(1f)) {
            if (vehicle?.programMode == true) StatusArea(state, parts.actions)
            Banners(state, parts.actions)
            CmpBox(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                HorizontalVehicle(vehicle = vehicle, height = carHeight)
            }
            if (vehicle != null) {
                ActiveZones(vehicle = vehicle, changed = state.changedZones, tick = state.tick)
                MetricStrip(vehicle, wrap = true)
            }
        }
        CmpColumn(
            modifier = Modifier.width(rightWidth).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            val fastMount = parts.fastMount
            if (parts.hasChecks && fastMount != null) {
                ScrollColumn(Modifier.weight(1f)) {
                    ChecklistPanel(
                        checklist = fastMount.checklist,
                        vehicle = vehicle,
                        onSkip = fastMount.onSkip,
                        onAnswer = fastMount.onAnswer,
                        onlyCurrent = true,
                    )
                }
                CmpButton(
                    text = stringResource(Strings.string.devicestate_fast_btn_all_checks),
                    onClick = { parts.onOpenSheet(PanelKind.CHECKS) },
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                    fullWidth = true,
                )
            } else {
                ScrollColumn(Modifier.weight(1f)) {
                    CommandsGrid(
                        commands = state.commands.filter { it.primary },
                        status = { state.status(it.command) },
                        enabled = state.commandsIdle,
                        columns = LOW_COMMAND_COLUMNS,
                        onFire = parts.onFire,
                        compact = true,
                    )
                    CmpButton(
                        text = stringResource(Strings.string.devicestate_pult_btn_all_commands),
                        onClick = parts.onAllCommands,
                        type = CmpButtonType.Tertiary,
                        size = CmpButtonSize.S,
                        fullWidth = true,
                    )
                }
                CmpButton(
                    text = stringResource(Strings.string.devicestate_details_btn_open),
                    onClick = { parts.onOpenSheet(PanelKind.DETAILS) },
                    type = CmpButtonType.Secondary,
                    size = CmpButtonSize.S,
                    fullWidth = true,
                )
            }
            FastDock(parts)
        }
    }
}

/** Вкладки-сегменты и прокручиваемое содержимое выбранной вкладки; три вкладки не влезают в ширину — прокрутка. */
@Composable
private fun ColumnScope.TabbedPane(
    titles: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    CmpTabRow(
        selectedTabIndex = selected,
        tabs = titles.map { CmpTabItem(title = it) },
        onTabSelect = onSelect,
        inset = CmpSpacing.screenPaddingH,
        scrollable = titles.size > SEGMENT_TABS_MAX,
        modifier = Modifier.testTag(CHECK_STATE_TABS_TAG),
    )
    CmpColumn(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                start = CmpSpacing.screenPaddingH,
                end = CmpSpacing.screenPaddingH,
                top = CmpSpacing.space4,
                bottom = CmpSpacing.screenPaddingBottom,
            ),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        content = content,
    )
}

@Composable
private fun ScrollColumn(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    CmpColumn(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(bottom = CmpSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        content = content,
    )
}

@Composable
private fun paneTabTitle(tab: PaneTab, parts: Parts): String = when (tab) {
    PaneTab.CHECKS -> tabTitle(Tab.CHECKS, parts)
    PaneTab.COMMANDS -> stringResource(Strings.string.devicestate_tabs_item_commands)
    PaneTab.DETAILS -> tabTitle(Tab.DETAILS, parts)
}

private val SplitCarMax = 200.dp
private const val SPLIT_CAR_FRACTION = 0.46f
private val TwoColumnCommandsFrom = 400.dp
private val DeskCarWidth = 170.dp
private val LowRightMax = 420.dp
private const val LOW_RIGHT_FRACTION = 0.44f
private val LOW_CAR_RESERVE = 250.dp
private val LowCarMin = 110.dp
private val LowCarMax = 150.dp
private const val LOW_COMMAND_COLUMNS = 3
private const val SEGMENT_TABS_MAX = 2
private val DialogButtonsMinWidth = 240.dp

@Composable
private fun CommandsSection(parts: Parts, columns: Int) {
    SectionCard(title = stringResource(Strings.string.devicestate_commands_title_section)) {
        CommandsBlock(parts, columns)
    }
}

/** Подсказка «удерживайте» и все команды сеткой — каждая команда показывается один раз. */
@Composable
private fun CommandsBlock(parts: Parts, columns: Int) {
    val state = parts.state
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpBanner(
            text = stringResource(Strings.string.devicestate_cmd_msg_hold_hint),
            tone = CmpBannerTone.Info,
            icon = CmpIcons.TouchApp,
        )
        CommandsGrid(
            commands = state.commands,
            status = { state.status(it.command) },
            enabled = state.commandsIdle,
            columns = columns,
            onFire = parts.onFire,
        )
    }
}

@Composable
private fun tabTitle(tab: Tab, parts: Parts): String = when (tab) {
    Tab.CHECKS -> {
        val checklist = parts.fastMount?.checklist
        stringResource(
            Strings.string.devicestate_tabs_format_checks,
            checklist?.closed ?: 0,
            checklist?.tasks?.size ?: 0,
        )
    }

    Tab.PULT -> stringResource(Strings.string.devicestate_tabs_item_pult)

    Tab.DETAILS -> stringResource(Strings.string.devicestate_tabs_item_details)
}

/** В режиме программирования блок не отдаёт охрану и не выполняет часть команд — вместо статуса плашка режима. */
@Composable
private fun StatusArea(state: CheckStateState, actions: CheckStateActions) {
    val vehicle = state.vehicle
    when {
        vehicle == null || !vehicle.programMode -> StatusHero(state)

        state.programExiting -> CheckStateBanner(
            title = stringResource(Strings.string.devicestate_banner_title_program_exit),
            text = stringResource(Strings.string.devicestate_banner_msg_reboot),
            busy = true,
        )

        else -> CheckStateBanner(
            title = stringResource(Strings.string.devicestate_banner_title_program),
            text = stringResource(Strings.string.devicestate_banner_msg_program),
            icon = CmpIcons.DeveloperBoard,
            action = stringResource(Strings.string.devicestate_banner_btn_turn_off).takeIf { !state.fastMount },
            actionEnabled = state.commandsIdle,
            onAction = { actions.onCommand(StateCommand.PROGRAM_MODE_OFF) },
        )
    }
}

@Composable
private fun ColumnScope.Banners(state: CheckStateState, actions: CheckStateActions) {
    val vehicle = state.vehicle ?: return
    val stale = state.staleSeconds
    if (stale != null) {
        CheckStateBanner(
            title = stringResource(Strings.string.devicestate_banner_format_stale, stale),
            text = stringResource(Strings.string.devicestate_banner_msg_stale),
            icon = CmpIcons.SyncProblem,
            neutral = true,
        )
    }
    if (vehicle.serviceMode) {
        CheckStateBanner(
            title = stringResource(Strings.string.devicestate_banner_title_service),
            text = stringResource(Strings.string.devicestate_banner_msg_not_guarding),
            icon = CmpIcons.Build,
            action = stringResource(Strings.string.devicestate_banner_btn_turn_off),
            actionEnabled = state.commandsIdle,
            onAction = { actions.onCommand(StateCommand.SERVICE_MODE_OFF) },
        )
    }
}

@Composable
private fun Details(parts: Parts) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        DetailBlocks(parts.state)
        CommandsSection(parts, columns = parts.columns)
    }
}

/** Зоны, двигатель, связь и дополнительно — без команд. */
@Composable
private fun DetailBlocks(state: CheckStateState) {
    val vehicle = state.vehicle
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        if (vehicle == null) {
            ZonesSkeleton()
        } else {
            ZonesList(vehicle = vehicle, changed = state.changedZones, tick = state.tick)
            EngineCard(vehicle)
            LinkCard(vehicle)
            ExtraGroups(vehicle)
        }
    }
}

@Composable
private fun Checks(parts: Parts) {
    val fastMount = parts.fastMount ?: return
    ChecklistPanel(
        checklist = fastMount.checklist,
        vehicle = parts.state.vehicle,
        onSkip = fastMount.onSkip,
        onAnswer = fastMount.onAnswer,
    )
}

@Composable
private fun FastDock(parts: Parts, buttonWidth: Dp? = null) {
    val fastMount = parts.fastMount ?: return
    val checklist = fastMount.checklist
    val button: @Composable ColumnScope.() -> Unit = {
        CmpButton(
            text = if (checklist.tasks.isNotEmpty() && !checklist.allClosed) {
                stringResource(Strings.string.devicestate_fast_format_next, checklist.closed, checklist.tasks.size)
            } else {
                stringResource(Strings.string.devicestate_fast_btn_next)
            },
            onClick = parts.actions.onNext,
            enabled = !fastMount.nextBusy,
            size = CmpButtonSize.M,
            fullWidth = true,
            modifier = Modifier.testTag(CHECK_STATE_NEXT_TAG),
        )
    }
    val chrome = fastMount.chrome
    if (chrome != null) {
        chrome.dock(button)
        return
    }
    CmpActionBar(navigationBarPadding = false) {
        CmpColumn(modifier = if (buttonWidth != null) Modifier.width(buttonWidth) else Modifier.weight(1f)) {
            button()
        }
    }
}

@Composable
private fun AlarmBorder() {
    val transition = rememberInfiniteTransition(label = "alarmBorder")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = ALARM_BORDER_MIN_ALPHA,
        animationSpec = infiniteRepeatable(tween(ALARM_BORDER_MILLIS), RepeatMode.Reverse),
        label = "alarmBorderAlpha",
    )
    CmpBox(modifier = Modifier.fillMaxSize().alpha(alpha).border(AlarmBorderWidth, CmpTheme.colors.errorDefault))
}

@Composable
private fun CheckStateDialogs(state: CheckStateState, actions: CheckStateActions) {
    when (val dialog = state.dialog) {
        null -> Unit

        is CheckStateDialog.Confirm -> DangerSheet(
            command = dialog.command,
            vehicle = state.vehicle,
            onConfirm = actions.onConfirm,
            onDismiss = actions.onDialogDismiss,
        )

        is CheckStateDialog.Pin -> ServicePinDialog(
            message = stringResource(Strings.string.devicestate_pin_msg_program),
            state = dialog.pin,
            onDismiss = actions.onDialogDismiss,
            onSubmit = actions.onPinSubmit,
            testTagPrefix = "devicestate_pin",
        )

        is CheckStateDialog.Exit -> ChoiceDialog(
            icon = CmpIcons.Warning,
            title = stringResource(Strings.string.devicestate_exit_title_dialog),
            text = stringResource(
                when {
                    dialog.service && dialog.program -> Strings.string.devicestate_exit_msg_both
                    dialog.service -> Strings.string.devicestate_exit_msg_service
                    else -> Strings.string.devicestate_exit_msg_program
                },
            ),
            primary = stringResource(Strings.string.devicestate_exit_btn_turn_off) to actions.onExitTurnOff,
            secondary = stringResource(
                if (dialog.service &&
                    dialog.program
                ) {
                    Strings.string.devicestate_exit_btn_keep_both
                } else {
                    Strings.string.devicestate_exit_btn_keep
                },
            ) to actions.onExitKeep,
            cancel = stringResource(Strings.string.devicestate_exit_btn_cancel),
            onDismiss = actions.onDialogDismiss,
        )

        CheckStateDialog.FastBack -> ChoiceDialog(
            icon = CmpIcons.ArrowBack,
            title = stringResource(Strings.string.devicestate_fast_title_back),
            text = stringResource(Strings.string.devicestate_fast_msg_back),
            primary = stringResource(Strings.string.devicestate_fast_btn_back) to actions.onFastBackConfirm,
            secondary = null,
            cancel = stringResource(Strings.string.devicestate_fast_btn_cancel),
            onDismiss = actions.onDialogDismiss,
        )

        is CheckStateDialog.FastFinish -> ChoiceDialog(
            icon = CmpIcons.FactCheck,
            title = stringResource(Strings.string.devicestate_fast_title_finish),
            text = stringResource(Strings.string.devicestate_fast_format_finish, dialog.notDone, dialog.total),
            primary = stringResource(Strings.string.devicestate_fast_btn_continue) to actions.onDialogDismiss,
            secondary = stringResource(Strings.string.devicestate_fast_btn_finish) to actions.onFinishConfirm,
            cancel = null,
            onDismiss = actions.onDialogDismiss,
        )
    }
}

@Composable
private fun ChoiceDialog(
    icon: CmpIcons,
    title: String,
    text: String,
    primary: Pair<String, () -> Unit>,
    secondary: Pair<String, () -> Unit>?,
    cancel: String?,
    onDismiss: () -> Unit,
) {
    CmpDialog(
        onDismissRequest = onDismiss,
        icon = icon.imageVector(),
        title = { CmpText(text = title, style = CmpTextStyle.Headline2) },
        text = { CmpText(text = text, style = CmpTextStyle.Paragraph2) },
        confirmButton = {
            CmpColumn(
                modifier = Modifier.widthIn(min = DialogButtonsMinWidth),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CmpButton(text = primary.first, onClick = primary.second, fullWidth = true)
                if (secondary != null) {
                    CmpButton(
                        text = secondary.first,
                        onClick = secondary.second,
                        type = CmpButtonType.Secondary,
                        fullWidth = true,
                    )
                }
                if (cancel != null) {
                    CmpButton(text = cancel, onClick = onDismiss, type = CmpButtonType.Tertiary, fullWidth = true)
                }
            }
        },
    )
}

private val CheckStateState.commandsIdle: Boolean get() = !loading && pending == null
