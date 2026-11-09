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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
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
import ru.alarmtrade.specmp.core.localization.resources.devicestate_details_item_engine
import ru.alarmtrade.specmp.core.localization.resources.devicestate_details_item_extra
import ru.alarmtrade.specmp.core.localization.resources.devicestate_details_item_link
import ru.alarmtrade.specmp.core.localization.resources.devicestate_details_item_zones
import ru.alarmtrade.specmp.core.localization.resources.devicestate_details_msg_no_extra
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_btn_keep
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_btn_keep_both
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_btn_turn_off
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_msg_both
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_msg_program
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_msg_service
import ru.alarmtrade.specmp.core.localization.resources.devicestate_exit_title_dialog
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
import ru.alarmtrade.specmp.core.localization.resources.devicestate_tabs_format_checks
import ru.alarmtrade.specmp.core.localization.resources.devicestate_tabs_item_details
import ru.alarmtrade.specmp.core.localization.resources.devicestate_tabs_item_pult
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarResult
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTabItem
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTabRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.deviceTitle
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialog
import ru.alarmtrade.specmp.feature.device.state.domain.model.HandsFreeMode
import ru.alarmtrade.specmp.feature.device.state.domain.model.SirenMode
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.AllCommandsSheet
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.ChecklistPanel
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.CommandsGrid
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.DangerSheet
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.EngineCard
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.ExtraGroups
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.LinkCard
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.PultCard
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.PultWideCard
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.SectionCard
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.StateBanner
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.StatusHero
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.ZonesList
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.ZonesSkeleton
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.label
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.components.snackbarFormat
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.model.CheckStateFastMount

internal const val CHECK_STATE_NEXT_TAG = "devicestate_next"
internal const val CHECK_STATE_TABS_TAG = "devicestate_tabs"

// Две колонки — только когда контенту хватает ширины (макет: ≥ 860 dp контента, в т.ч. справа от колонки
// шагов мастера).
private val WideFrom = 860.dp
private val WideSplitFrom = 1100.dp
private const val ALARM_BORDER_MIN_ALPHA = 0.25f
private const val ALARM_BORDER_MILLIS = 500

/** Действия экрана; один экземпляр на экран. */
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

/**
 * Экран «Проверка состояния»: раздел расширенного монтажа или — с [fastMount] — шаг быстрого монтажа (чек-лист,
 * «Далее»). Только BLE: плитку при USB не показывает расширенный монтаж.
 *
 * @param onBack выход с экрана (расширенный монтаж; в быстром монтаже — [CheckStateFastMount.onBack]).
 */
@Composable
fun CheckStateEntry(onBack: () -> Unit, fastMount: CheckStateFastMount? = null) {
    val viewModel = koinViewModel<CheckStateViewModel> { parametersOf(fastMount != null) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val leave = fastMount?.onBack ?: onBack
    val checklist = fastMount?.checklist
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            is CheckStateEffect.CommandFinished -> showResult(snackbar, effect, viewModel::onRetry)
            CheckStateEffect.Close -> onBack()
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

/**
 * Stateless-контент. Узко (< 860 dp): сверху статус и плашки, ниже вкладки «Пульт / Подробно» (в быстром монтаже
 * первая — «Проверки N/M»). Широко: слева статус, горизонтальный пульт и «Подробно» вкладками, справа — чек-лист
 * (быстрый монтаж) или все команды. [CheckStateFastMount.chrome] — шапка и панель мастера быстрого монтажа.
 */
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
    val chrome = fastMount?.chrome
    CmpScaffold(
        topBar = {
            if (chrome != null) {
                chrome.topBar(actions.onBack)
            } else {
                CmpTopAppBar(
                    title = { CmpText(text = stringResource(Strings.string.devicestate_check_title_screen)) },
                    subtitle = subtitle?.let { { CmpText(text = it, style = CmpTextStyle.Caption) } },
                    navigationIcon = {
                        CmpIconButton(
                            onClick = actions.onBack,
                            icon = CmpIcons.ArrowBack,
                            contentDescription = stringResource(Strings.string.devicestate_check_a11y_back),
                        )
                    },
                )
            }
        },
        snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val wide = maxWidth >= WideFrom
            val columns = if (maxWidth >= WideSplitFrom) 3 else 2
            val parts = Parts(state, actions, fastMount, onFire, columns, onAllCommands = { allCommands = true })
            if (wide) {
                WideLayout(
                    parts,
                    rightWidth = wideRightWidth(maxWidth),
                    carHeight = if (maxWidth >=
                        WideSplitFrom
                    ) {
                        180.dp
                    } else {
                        150.dp
                    },
                )
            } else {
                CompactLayout(parts, initialTab)
            }
            if (state.alarm) AlarmBorder()
        }
    }
    if (allCommands) {
        AllCommandsSheet(
            commands = state.commands,
            status = { state.status(it.command) },
            enabled = !state.loading && state.pending == null,
            columns = 2,
            onFire = onFire,
            onDismiss = { allCommands = false },
        )
    }
    CheckStateDialogs(state, actions)
}

/** Части экрана, общие для узкой и широкой раскладки. */
private class Parts(
    val state: CheckStateState,
    val actions: CheckStateActions,
    val fastMount: CheckStateFastMount?,
    val onFire: (CommandUi) -> Unit,
    val columns: Int,
    val onAllCommands: () -> Unit,
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
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            StatusArea(state, parts.actions)
            Banners(state, parts.actions)
        }
        CmpTabRow(
            selectedTabIndex = index,
            tabs = tabs.map { CmpTabItem(title = tabTitle(it, parts)) },
            onTabSelect = { selected = it },
            modifier = Modifier.testTag(CHECK_STATE_TABS_TAG),
        )
        CmpColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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

/** Ширина правой колонки (чек-лист / все команды) в широком окне. */
private fun wideRightWidth(width: Dp): Dp = when {
    width >= 1200.dp -> 440.dp
    width >= 960.dp -> 380.dp
    else -> 340.dp
}

/**
 * Широкое окно (макет): слева — статус, пульт с машиной горизонтально и «Подробно» одной карточкой с вкладками;
 * справа — чек-лист (быстрый монтаж) или все команды (расширенный). «Далее» — в нижней панели справа.
 */
@Composable
private fun WideLayout(parts: Parts, rightWidth: Dp, carHeight: Dp) {
    val state = parts.state
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        CmpRow(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CmpColumn(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatusArea(state, parts.actions)
                Banners(state, parts.actions)
                PultWideCard(state, carHeight = carHeight, onFire = parts.onFire, onAllCommands = parts.onAllCommands)
                DetailsTabs(parts)
            }
            CmpColumn(
                modifier = Modifier.width(rightWidth).verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (parts.hasChecks) {
                    SectionCard(
                        title = stringResource(Strings.string.devicestate_fast_title_checks),
                        contentPadding = 8.dp,
                    ) {
                        Checks(parts)
                    }
                } else {
                    CommandsSection(parts, columns = if (rightWidth >= 400.dp) 2 else 1)
                }
            }
        }
        FastDock(parts, wide = true)
    }
}

private enum class DetailsTab { ZONES, ENGINE, LINK, EXTRA }

/** «Подробно» в широком окне — одна карточка с вкладками вместо длинной ленты карточек. */
@Composable
private fun DetailsTabs(parts: Parts) {
    val vehicle = parts.state.vehicle
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val tab = DetailsTab.entries[selected.coerceIn(0, DetailsTab.entries.lastIndex)]
    SectionCard(title = stringResource(Strings.string.devicestate_check_title_details), contentPadding = 0.dp) {
        CmpTabRow(
            selectedTabIndex = tab.ordinal,
            tabs = DetailsTab.entries.map { CmpTabItem(title = stringResource(it.title())) },
            onTabSelect = { selected = it },
        )
        CmpColumn(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                vehicle == null -> ZonesSkeleton()

                tab == DetailsTab.ZONES -> ZonesList(
                    vehicle = vehicle,
                    changed = parts.state.changedZones,
                    tick = parts.state.tick,
                )

                tab == DetailsTab.ENGINE -> EngineCard(vehicle)

                tab == DetailsTab.LINK -> LinkCard(vehicle)

                vehicle.hasExtra() -> ExtraGroups(vehicle)

                else -> CmpText(
                    text = stringResource(Strings.string.devicestate_details_msg_no_extra),
                    color = cmpThemeColors().onSurfaceVariant,
                )
            }
        }
    }
}

private fun DetailsTab.title() = when (this) {
    DetailsTab.ZONES -> Strings.string.devicestate_details_item_zones
    DetailsTab.ENGINE -> Strings.string.devicestate_details_item_engine
    DetailsTab.LINK -> Strings.string.devicestate_details_item_link
    DetailsTab.EXTRA -> Strings.string.devicestate_details_item_extra
}

private fun VehicleState.hasExtra(): Boolean =
    listOf(can, ev, heater, camera).any { it != null } || siren != SirenMode.OFF || handsFree != HandsFreeMode.OFF

@Composable
private fun CommandsSection(parts: Parts, columns: Int) {
    val state = parts.state
    SectionCard(title = stringResource(Strings.string.devicestate_commands_title_section)) {
        CmpText(
            text = stringResource(Strings.string.devicestate_cmd_msg_hold_hint),
            fontSize = 14.sp,
            color = cmpThemeColors().onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        CommandsGrid(
            commands = state.commands,
            status = { state.status(it.command) },
            enabled = !state.loading && state.pending == null,
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

/**
 * Место главного статуса. В режиме программирования блок не отдаёт охрану и не выполняет часть команд — вместо
 * карточки «Снято с охраны» плашка режима (с «Выключить»; в быстром монтаже — выход идёт сам).
 */
@Composable
private fun StatusArea(state: CheckStateState, actions: CheckStateActions) {
    val vehicle = state.vehicle
    when {
        vehicle == null || !vehicle.programMode -> StatusHero(state)

        state.programExiting -> StateBanner(
            title = stringResource(Strings.string.devicestate_banner_title_program_exit),
            text = stringResource(Strings.string.devicestate_banner_msg_reboot),
            busy = true,
            modifier = Modifier.heightIn(min = 72.dp),
        )

        else -> StateBanner(
            title = stringResource(Strings.string.devicestate_banner_title_program),
            text = stringResource(Strings.string.devicestate_banner_msg_program),
            icon = CmpIcons.DeveloperBoard,
            action = stringResource(Strings.string.devicestate_banner_btn_turn_off).takeIf {
                state.pending == null && !state.fastMount
            },
            onAction = { actions.onCommand(StateCommand.PROGRAM_MODE_OFF) },
            modifier = Modifier.heightIn(min = 72.dp),
        )
    }
}

@Composable
private fun ColumnScope.Banners(state: CheckStateState, actions: CheckStateActions) {
    val vehicle = state.vehicle ?: return
    val stale = state.staleSeconds
    if (stale != null) {
        StateBanner(
            title = stringResource(Strings.string.devicestate_banner_format_stale, stale),
            text = stringResource(Strings.string.devicestate_banner_msg_stale),
            icon = CmpIcons.SyncProblem,
            neutral = true,
        )
    }
    val enabled = state.pending == null
    if (vehicle.serviceMode) {
        StateBanner(
            title = stringResource(Strings.string.devicestate_banner_title_service),
            text = stringResource(Strings.string.devicestate_banner_msg_not_guarding),
            icon = CmpIcons.Build,
            action = stringResource(Strings.string.devicestate_banner_btn_turn_off).takeIf { enabled },
            onAction = { actions.onCommand(StateCommand.SERVICE_MODE_OFF) },
        )
    }
}

@Composable
private fun Details(parts: Parts) {
    val state = parts.state
    val vehicle = state.vehicle
    if (vehicle == null) {
        ZonesSkeleton()
    } else {
        ZonesList(vehicle = vehicle, changed = state.changedZones, tick = state.tick)
        EngineCard(vehicle)
        LinkCard(vehicle)
        ExtraGroups(vehicle)
    }
    SectionCard(title = stringResource(Strings.string.devicestate_commands_title_section)) {
        CmpText(
            text = stringResource(Strings.string.devicestate_cmd_msg_hold_hint),
            fontSize = 14.sp,
            color = cmpThemeColors().onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        CommandsGrid(
            commands = state.commands,
            status = { state.status(it.command) },
            enabled = !state.loading && state.pending == null,
            columns = parts.columns,
            onFire = parts.onFire,
        )
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
private fun FastDock(parts: Parts, wide: Boolean = false) {
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
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(CHECK_STATE_NEXT_TAG),
        )
    }
    val chrome = fastMount.chrome
    if (chrome != null) {
        chrome.dock(button)
        return
    }
    val colors = cmpThemeColors()
    CmpSurface(color = colors.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        CmpBox(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            CmpColumn(modifier = if (wide) Modifier.widthIn(max = 400.dp).fillMaxWidth() else Modifier.fillMaxWidth()) {
                button()
            }
        }
    }
}

/** Красная пульсирующая рамка по краю экрана при тревоге. */
@Composable
private fun AlarmBorder() {
    val colors = cmpThemeColors()
    val transition = rememberInfiniteTransition(label = "alarmBorder")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = ALARM_BORDER_MIN_ALPHA,
        animationSpec = infiniteRepeatable(tween(ALARM_BORDER_MILLIS), RepeatMode.Reverse),
        label = "alarmBorderAlpha",
    )
    CmpBox(modifier = Modifier.fillMaxSize().alpha(alpha).border(6.dp, colors.error))
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

/** Диалог с вертикальными кнопками во всю ширину: основная, вторичная, «Отмена». */
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
        title = { CmpText(text = title) },
        text = { CmpText(text = text) },
        confirmButton = {
            CmpColumn(modifier = Modifier.widthIn(min = 240.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CmpButton(
                    text = primary.first,
                    onClick = primary.second,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                )
                if (secondary != null) {
                    CmpOutlinedButton(
                        text = secondary.first,
                        onClick = secondary.second,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    )
                }
                if (cancel != null) {
                    CmpTextButton(
                        text = cancel,
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    )
                }
            }
        },
    )
}
