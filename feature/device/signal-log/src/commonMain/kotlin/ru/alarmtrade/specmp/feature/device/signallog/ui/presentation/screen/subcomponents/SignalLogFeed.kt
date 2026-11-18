package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.subcomponents

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.signallog_feed_btn_latest
import ru.alarmtrade.specmp.core.localization.resources.signallog_feed_btn_new
import ru.alarmtrade.specmp.core.localization.resources.signallog_search_msg_no_lines
import ru.alarmtrade.specmp.core.ui.format.rememberCmpDateLabeler
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.time.currentTimeZone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogActions
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogLayout
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogSearch
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogState

internal const val SIGNAL_LOG_FEED_TAG = "signallog_feed"
internal const val SIGNAL_LOG_JUMP_TAG = "signallog_jump"

private const val HEADER_KEY = "signallog_header"
private const val NO_ID = -1L
private const val LOAD_AHEAD = 10
private const val SOFT_SCROLL_DISTANCE = 30
private const val MATCH_CONTEXT = 3

@Composable
internal fun SignalLogFeed(
    state: SignalLogState,
    actions: SignalLogActions,
    layout: SignalLogLayout,
    modifier: Modifier = Modifier,
) {
    val filter = state.search?.takeIf { it.onlyMatches && it.query.isNotBlank() }
    when (val load = state.load) {
        LCEState.None, LCEState.Loading -> FeedSkeleton(layout = layout, modifier = modifier)

        is LCEState.Error -> FeedError(error = load.error, onRetry = actions.onRetry, modifier = modifier)

        is LCEState.Content -> if (state.summary.count == 0 && state.lines.isEmpty() && filter == null) {
            FeedEmpty(connected = state.connected, compact = layout.compact, modifier = modifier)
        } else {
            FeedLines(state = state, filter = filter, actions = actions, layout = layout, modifier = modifier)
        }
    }
}

/**
 * Лента снизу вверх (`reverseLayout`): вставка новых строк в индекс 0 не сдвигает то, что читают выше, а догрузка
 * старых в конец не трогает позицию. Пока лента у низа ([FollowState.follow]) — каждая новая строка сразу в кадре.
 */
@Composable
private fun FeedLines(
    state: SignalLogState,
    filter: SignalLogSearch?,
    actions: SignalLogActions,
    layout: SignalLogLayout,
    modifier: Modifier,
) {
    val filtered = filter?.matches
    val shown: ImmutableList<SignalLogLine> = remember(state.lines, filtered) {
        filtered?.asReversed()?.toImmutableList() ?: state.lines
    }
    val listState = rememberLazyListState()
    val follow = rememberSaveable(saver = FollowState.Saver) { FollowState() }
    val newestId = state.lines.firstOrNull()?.id
    val currentNewest by rememberUpdatedState(newestId)
    FollowEffects(
        listState = listState,
        follow = follow,
        newestId = newestId,
        followNewest = filter == null,
        searching = state.search != null,
    )
    MatchScroll(shown = shown, currentId = state.search?.currentLine?.id, listState = listState) {
        follow.leave(currentNewest)
    }
    OlderPagination(
        listState = listState,
        canLoad = state.hasOlder && !state.loadingOlder && filter == null,
        onLoadOlder = actions.onLoadOlder,
    )
    CmpBox(modifier = modifier.fillMaxWidth()) {
        LinesList(
            shown = shown,
            state = state,
            atStart = !state.hasOlder && filter == null && shown.isNotEmpty(),
            listState = listState,
            layout = layout,
            onCopy = actions.onCopy,
        )
        if (filter != null && shown.isEmpty()) {
            CmpText(
                text = stringResource(Strings.string.signallog_search_msg_no_lines, filter.query.trim()),
                style = CmpTextStyle.Paragraph2,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.TopCenter).padding(layout.paddingH, CmpSpacing.space32),
            )
        }
        val jumpVisible = !follow.follow && !state.paused && filter == null
        if (jumpVisible && shown.isNotEmpty()) {
            JumpToLatest(
                unseen = follow.unseen(state.lines),
                listState = listState,
                onJump = follow::resume,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun LinesList(
    shown: ImmutableList<SignalLogLine>,
    state: SignalLogState,
    atStart: Boolean,
    listState: LazyListState,
    layout: SignalLogLayout,
    onCopy: (String) -> Unit,
) {
    var selectedId by remember { mutableStateOf<Long?>(null) }
    val timeZone = currentTimeZone()
    val dateLabeler = rememberCmpDateLabeler()
    val multiDay = remember(shown.firstOrNull()?.timeMs, shown.lastOrNull()?.timeMs, timeZone) {
        shown.isNotEmpty() && shown.first().date(timeZone) != shown.last().date(timeZone)
    }
    val query = state.search?.query?.trim().orEmpty()
    val currentId = state.search?.currentLine?.id
    LazyColumn(
        state = listState,
        reverseLayout = true,
        modifier = Modifier.fillMaxSize().testTag(SIGNAL_LOG_FEED_TAG),
        contentPadding = PaddingValues(bottom = CmpSpacing.space8),
    ) {
        items(count = shown.size, key = { shown[it].id }) { index ->
            val line = shown[index]
            val date = line.date(timeZone)
            val showDay = shown.getOrNull(index + 1)?.let { it.date(timeZone) != date } ?: multiDay
            SignalLogLineRow(
                line = line,
                dayLabel = if (showDay) dateLabeler(date) else null,
                query = query,
                current = line.id == currentId,
                selected = line.id == selectedId,
                layout = layout,
                onSelect = { selectedId = line.id },
                onUnselect = { selectedId = null },
                onCopy = onCopy,
            )
        }
        item(key = HEADER_KEY) {
            FeedTop(loadingOlder = state.loadingOlder, atStart = atStart, layout = layout)
        }
    }
}

@Composable
private fun FollowEffects(
    listState: LazyListState,
    follow: FollowState,
    newestId: Long?,
    followNewest: Boolean,
    searching: Boolean,
) {
    val currentNewest by rememberUpdatedState(newestId)
    LaunchedEffect(listState, follow) {
        // Решает только прокрутка пользователя: вставка строки сдвигает низ, но не выключает слежение.
        snapshotFlow { !listState.canScrollBackward }
            .collect { atBottom ->
                if (!listState.isScrollInProgress || atBottom == follow.follow) return@collect
                if (atBottom) follow.resume() else follow.leave(currentNewest)
            }
    }
    LaunchedEffect(newestId, follow.follow, followNewest) {
        val following = follow.follow && followNewest
        if (following && newestId != null && listState.canScrollBackward) listState.scrollToItem(0)
    }
    LaunchedEffect(searching) {
        if (!searching) follow.resume()
    }
}

@Composable
private fun JumpToLatest(unseen: Int, listState: LazyListState, onJump: () -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    CmpButton(
        text = if (unseen > 0) {
            pluralStringResource(Strings.plurals.signallog_feed_btn_new, unseen, unseen)
        } else {
            stringResource(Strings.string.signallog_feed_btn_latest)
        },
        onClick = {
            onJump()
            scope.launch { listState.softScrollToNewest() }
        },
        size = CmpButtonSize.S,
        icon = CmpIcons.KeyboardArrowDown,
        uppercase = false,
        modifier = modifier.padding(CmpSpacing.space16).testTag(SIGNAL_LOG_JUMP_TAG),
    )
}

/** Следит ли лента за последней строкой и какая строка была последней, когда пользователь ушёл вверх. */
@Stable
private class FollowState(follow: Boolean = true, leftAtId: Long = NO_ID) {
    var follow by mutableStateOf(follow)
        private set
    private var leftAtId by mutableLongStateOf(leftAtId)

    fun leave(newestId: Long?) {
        follow = false
        leftAtId = newestId ?: NO_ID
    }

    fun resume() {
        follow = true
        leftAtId = NO_ID
    }

    fun unseen(lines: List<SignalLogLine>): Int =
        if (follow || leftAtId == NO_ID) 0 else lines.takeWhile { it.id > leftAtId }.size

    companion object {
        val Saver: Saver<FollowState, Any> = listSaver(
            save = { listOf(it.follow, it.leftAtId) },
            restore = { FollowState(it[0] as Boolean, it[1] as Long) },
        )
    }
}

/** Переход к текущему совпадению поиска; ленту при этом перестаём вести к низу. */
@Composable
private fun MatchScroll(
    shown: ImmutableList<SignalLogLine>,
    currentId: Long?,
    listState: LazyListState,
    onLeaveBottom: () -> Unit,
) {
    val index = remember(shown, currentId) { if (currentId == null) -1 else shown.indexOfFirst { it.id == currentId } }
    val leave by rememberUpdatedState(onLeaveBottom)
    LaunchedEffect(currentId, index >= 0) {
        if (index >= 0) {
            leave()
            listState.scrollToItem((index - MATCH_CONTEXT).coerceAtLeast(0))
        }
    }
}

@Composable
private fun OlderPagination(listState: LazyListState, canLoad: Boolean, onLoadOlder: () -> Unit) {
    val loadAllowed by rememberUpdatedState(canLoad)
    val load by rememberUpdatedState(onLoadOlder)
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            loadAllowed && info.totalItemsCount > 1 && last >= info.totalItemsCount - LOAD_AHEAD
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { load() }
    }
}

private suspend fun LazyListState.softScrollToNewest() {
    if (firstVisibleItemIndex > SOFT_SCROLL_DISTANCE) scrollToItem(SOFT_SCROLL_DISTANCE)
    animateScrollToItem(0)
}

private fun SignalLogLine.date(timeZone: TimeZone): LocalDate =
    Instant.fromEpochMilliseconds(timeMs).toLocalDateTime(timeZone).date
