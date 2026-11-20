package ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWidthClass
import ru.alarmtrade.specmp.core.ui.view.layout.cmpWindowSize
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.matchCatalog
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.AssistantFeed
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.CollapsedDock
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.DOCK_TAG
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.DeviceHeader
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.DockLevelHead
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.DockTiles
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.FeedCallbacks
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.SearchResultsPanel
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.SearchRow
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.carouselTileHeight
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.DockTile
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.DockUi
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.dockOf
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.quickActions
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.rememberCatalogTexts

internal enum class AssistantLayout { Compact, Medium, Expanded, Low }

private val SideWidth = 400.dp
private val FeedMaxWide = 760.dp
private val ResultsMaxWidth = 712.dp
private const val RESULTS_HEIGHT_PART = 0.78f
private val FeedMaxMedium = 840.dp
private val DockMaxMedium = 888.dp
private val CollapseDistance = 60.dp
private val LowSideMax = 340.dp
private const val LOW_SIDE_PART = 0.44f
private val LowFeedMax = 720.dp

/** Действия экрана для раскладок: VM не протаскиваем в компоненты. */
internal class AssistantScreenActions(
    val onTile: (DockTile) -> Unit,
    val onTopicBack: () -> Unit,
    val onQueryChange: (String) -> Unit,
    val onSubmit: (AssistantTopic?) -> Unit,
    val onDockToggle: () -> Unit,
    val onFeedScrolledUp: () -> Unit,
    val onHeaderToggle: (collapsed: Boolean) -> Unit,
    val onCanUpdate: () -> Unit,
    val feed: FeedCallbacks,
)

@Composable
internal fun AssistantBody(
    state: MountAssistantState,
    facts: DeviceFacts,
    actions: AssistantScreenActions,
    focusRequester: FocusRequester,
) {
    val texts = rememberCatalogTexts()
    val match =
        remember(state.query, texts) {
            matchCatalog(state.query, texts.actions, texts.topics, texts.keywords, texts.actionKeywords)
        }
    val progress = state.journal.progress
    val dock = remember(facts, progress, state.level, state.query, match, state.liveSettings, state.pulse) {
        dockOf(facts, progress, state.level, state.query, match, state.liveSettings, state.pulse)
    }
    val submit = { actions.onSubmit(match.topics.firstOrNull()) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val window = cmpWindowSize
        val layout = when {
            window.isLowHeight -> AssistantLayout.Low
            window.isCompact -> AssistantLayout.Compact
            !window.atLeast(CmpWidthClass.Expanded) -> AssistantLayout.Medium
            else -> AssistantLayout.Expanded
        }
        if (layout == AssistantLayout.Expanded || layout == AssistantLayout.Low) {
            val console = remember(facts, progress, state.level, state.pulse) {
                dockOf(facts, progress, state.level, query = "", match, emptyList(), state.pulse)
            }
            val results = dock.takeIf { state.query.isNotBlank() }
            if (layout == AssistantLayout.Low) {
                val side = minOf(LowSideMax, maxWidth * LOW_SIDE_PART)
                LowBody(state, facts, console, results, side, actions, focusRequester, submit)
            } else {
                WideBody(state, facts, console, results, actions, focusRequester, submit)
            }
        } else {
            NarrowBody(state, facts, dock, layout, carouselTileHeight(maxHeight), actions, focusRequester, submit)
        }
    }
}

@Composable
private fun WideBody(
    state: MountAssistantState,
    facts: DeviceFacts,
    dock: DockUi,
    results: DockUi?,
    actions: AssistantScreenActions,
    focusRequester: FocusRequester,
    onSubmit: () -> Unit,
) {
    val colors = CmpTheme.colors
    val gutter = CmpSpacing.screenPaddingHMedium
    CmpRow(modifier = Modifier.fillMaxSize()) {
        CmpColumn(
            modifier = Modifier.width(SideWidth).fillMaxHeight()
                .background(colors.backgroundSurfaceUltraHighlight)
                .verticalScroll(rememberScrollState())
                .padding(CmpSpacing.space24),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space20),
        ) {
            DeviceHeader(facts, collapsed = false, wide = true, onToggle = null, onCanUpdate = actions.onCanUpdate)
            CmpColumn(
                modifier = Modifier.testTag(DOCK_TAG),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                DockLevelHead(dock.head, onBack = actions.onTopicBack, onCollapse = null)
                DockTiles(dock, facts.firmwareVersion, onTile = actions.onTile)
            }
        }
        CmpDivider(modifier = Modifier.fillMaxHeight().width(CmpBorderWidth.listItem))
        CmpColumn(modifier = Modifier.weight(1f).fillMaxHeight()) {
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                AssistantFeed(
                    entries = state.journal.entries,
                    facts = facts,
                    callbacks = actions.feed,
                    listState = rememberLazyListState(),
                    contentPadding = PaddingValues(
                        start = gutter,
                        end = gutter,
                        top = CmpSpacing.space24,
                        bottom = CmpSpacing.space32,
                    ),
                    modifier = Modifier.widthIn(max = FeedMaxWide).fillMaxSize(),
                )
                if (results != null) {
                    SearchResultsPanel(
                        results = results,
                        query = state.query.trim(),
                        firmwareVersion = facts.firmwareVersion,
                        onTile = actions.onTile,
                        onClose = { actions.onQueryChange("") },
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(horizontal = gutter)
                            .widthIn(max = ResultsMaxWidth)
                            .fillMaxWidth()
                            .heightIn(max = maxHeight * RESULTS_HEIGHT_PART),
                    )
                }
            }
            CmpDivider()
            CmpBox(
                modifier = Modifier.fillMaxWidth()
                    .background(colors.backgroundSurfaceUltraHighlight)
                    .padding(vertical = CmpSpacing.space12),
                contentAlignment = Alignment.TopCenter,
            ) {
                CmpBox(modifier = Modifier.widthIn(max = FeedMaxWide).fillMaxWidth()) {
                    SearchRow(state.query, actions.onQueryChange, onSubmit, focusRequester, horizontalPadding = gutter)
                }
            }
        }
    }
}

/** Низкое окно (телефон-ландшафт): колонка пульта слева, лента на всю высоту справа, поиск под лентой. */
@Composable
private fun LowBody(
    state: MountAssistantState,
    facts: DeviceFacts,
    dock: DockUi,
    results: DockUi?,
    sideWidth: Dp,
    actions: AssistantScreenActions,
    focusRequester: FocusRequester,
    onSubmit: () -> Unit,
) {
    val colors = CmpTheme.colors
    var headerOpen by rememberSaveable { mutableStateOf(false) }
    CmpRow(modifier = Modifier.fillMaxSize()) {
        CmpColumn(
            modifier = Modifier.width(sideWidth).fillMaxHeight().background(colors.backgroundSurfaceUltraHighlight),
        ) {
            DeviceHeader(
                facts = facts,
                collapsed = !headerOpen,
                wide = false,
                onToggle = { headerOpen = !headerOpen },
                onCanUpdate = actions.onCanUpdate,
            )
            CmpColumn(modifier = Modifier.weight(1f).testTag(DOCK_TAG)) {
                DockLevelHead(
                    dock.head,
                    onBack = actions.onTopicBack,
                    onCollapse = null,
                    modifier = Modifier.padding(start = CmpSpacing.screenPaddingH, end = CmpSpacing.space8),
                )
                CmpColumn(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(
                        start = CmpSpacing.space12,
                        end = CmpSpacing.space12,
                        top = CmpSpacing.space4,
                        bottom = CmpSpacing.space16,
                    ),
                ) {
                    DockTiles(dock, facts.firmwareVersion, onTile = actions.onTile, rows = true)
                }
            }
        }
        CmpDivider(modifier = Modifier.fillMaxHeight().width(CmpBorderWidth.listItem))
        CmpColumn(modifier = Modifier.weight(1f).fillMaxHeight()) {
            CmpBox(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                AssistantFeed(
                    entries = state.journal.entries,
                    facts = facts,
                    callbacks = actions.feed,
                    listState = rememberLazyListState(),
                    contentPadding = PaddingValues(
                        start = CmpSpacing.screenPaddingH,
                        end = CmpSpacing.screenPaddingH,
                        top = CmpSpacing.space16,
                        bottom = CmpSpacing.space24,
                    ),
                    modifier = Modifier.widthIn(max = LowFeedMax).fillMaxSize(),
                )
                if (results != null) {
                    SearchResultsPanel(
                        results = results,
                        query = state.query.trim(),
                        firmwareVersion = facts.firmwareVersion,
                        onTile = actions.onTile,
                        onClose = { actions.onQueryChange("") },
                        modifier = Modifier.fillMaxSize()
                            .padding(start = CmpSpacing.space8, end = CmpSpacing.space8, top = CmpSpacing.space8),
                    )
                }
            }
            CmpDivider()
            CmpBox(
                modifier = Modifier.fillMaxWidth()
                    .background(colors.backgroundSurfaceUltraHighlight)
                    .padding(vertical = CmpSpacing.space8),
                contentAlignment = Alignment.TopCenter,
            ) {
                CmpBox(modifier = Modifier.widthIn(max = LowFeedMax).fillMaxWidth()) {
                    SearchRow(
                        state.query,
                        actions.onQueryChange,
                        onSubmit,
                        focusRequester,
                        horizontalPadding = CmpSpacing.screenPaddingH,
                    )
                }
            }
        }
    }
}

@Composable
private fun NarrowBody(
    state: MountAssistantState,
    facts: DeviceFacts,
    dock: DockUi,
    layout: AssistantLayout,
    tileHeight: Dp,
    actions: AssistantScreenActions,
    focusRequester: FocusRequester,
    onSubmit: () -> Unit,
) {
    val compact = layout == AssistantLayout.Compact
    val listState = rememberLazyListState()
    val scrolled by remember(listState) { derivedStateOf { listState.isScrolled() } }
    val density = LocalDensity.current
    val keyboard = WindowInsets.ime.getBottom(density) > 0
    val collapsedHeader = when (state.header) {
        HeaderMode.CLOSED -> true
        HeaderMode.OPEN -> false
        HeaderMode.AUTO -> state.journal.entries.size > 1 || keyboard || scrolled
    }
    val collapseOnScroll = rememberCollapseOnScroll(actions.onFeedScrolledUp)
    CmpColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        CmpBox(modifier = Modifier.fillMaxWidth().widthIn(max = if (compact) Dp.Unspecified else FeedMaxMedium)) {
            DeviceHeader(
                facts = facts,
                collapsed = collapsedHeader,
                wide = false,
                onToggle = { actions.onHeaderToggle(collapsedHeader) },
                onCanUpdate = actions.onCanUpdate,
            )
        }
        AssistantFeed(
            entries = state.journal.entries,
            facts = facts,
            callbacks = actions.feed,
            listState = listState,
            contentPadding = if (compact) {
                PaddingValues(
                    start = CmpSpacing.screenPaddingH,
                    end = CmpSpacing.screenPaddingH,
                    top = CmpSpacing.space16,
                    bottom = CmpSpacing.space24,
                )
            } else {
                PaddingValues(
                    start = CmpSpacing.screenPaddingHMedium,
                    end = CmpSpacing.screenPaddingHMedium,
                    top = CmpSpacing.space24,
                    bottom = CmpSpacing.space32,
                )
            },
            modifier = Modifier.weight(1f).fillMaxWidth().widthIn(max = if (compact) Dp.Unspecified else FeedMaxMedium)
                .nestedScroll(collapseOnScroll),
        )
        BottomDock(state, facts, dock, compact, tileHeight, actions, focusRequester, onSubmit)
    }
}

@Composable
private fun BottomDock(
    state: MountAssistantState,
    facts: DeviceFacts,
    dock: DockUi,
    compact: Boolean,
    tileHeight: Dp,
    actions: AssistantScreenActions,
    focusRequester: FocusRequester,
    onSubmit: () -> Unit,
) {
    val colors = CmpTheme.colors
    val collapsed = state.dockCollapsed && state.query.isEmpty()
    val gutter = if (compact) CmpSpacing.screenPaddingH else CmpSpacing.screenPaddingHMedium
    CmpSurface(
        modifier = Modifier.fillMaxWidth().widthIn(
            max = if (compact) Dp.Unspecified else DockMaxMedium,
        ).testTag(DOCK_TAG),
        shape = RoundedCornerShape(topStart = CmpRadius.sheet, topEnd = CmpRadius.sheet),
        color = colors.backgroundPagePrimary,
        border = BorderStroke(CmpBorderWidth.listItem, colors.borderHighlight),
    ) {
        CmpColumn(
            modifier = Modifier.background(colors.backgroundSurfaceUltraHighlight)
                .padding(top = CmpSpacing.space8, bottom = CmpSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            if (collapsed) {
                val progress = state.journal.progress
                val quick = remember(facts, progress) { quickActions(facts, progress) }
                CollapsedDock(
                    quick,
                    onTile = actions.onTile,
                    onExpand = actions.onDockToggle,
                    horizontalPadding = gutter,
                )
            } else {
                DockLevelHead(
                    dock.head,
                    onBack = actions.onTopicBack,
                    onCollapse = actions.onDockToggle,
                    modifier = Modifier.padding(start = gutter, end = CmpSpacing.space8),
                )
                DockTiles(
                    dock,
                    facts.firmwareVersion,
                    onTile = actions.onTile,
                    carouselHeight = tileHeight,
                    horizontalPadding = gutter,
                )
            }
            SearchRow(state.query, actions.onQueryChange, onSubmit, focusRequester, horizontalPadding = gutter)
        }
    }
}

private fun LazyListState.isScrolled(): Boolean = firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0

/** Прокрутка ленты пальцем вверх больше чем на [CollapseDistance] сворачивает пульт. */
@Composable
private fun rememberCollapseOnScroll(onScrolledUp: () -> Unit): NestedScrollConnection {
    val density = LocalDensity.current
    val threshold = with(density) { CollapseDistance.toPx() }
    return remember(threshold, onScrolledUp) {
        object : NestedScrollConnection {
            private var pulled = 0f

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                pulled = if (available.y > 0f) pulled + available.y else 0f
                if (pulled > threshold) {
                    pulled = 0f
                    onScrolledUp()
                }
                return Offset.Zero
            }
        }
    }
}
