package ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_new
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_label_active
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_label_finished
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_title_screen
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpFloatingActionButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWidthClass
import ru.alarmtrade.specmp.core.ui.view.layout.cmpWindowSize
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.techrequests.domain.list.TechRequestList
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequest
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsActions
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsTags
import ru.alarmtrade.specmp.feature.techrequests.presentation.platform.showsKeyboardHints
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents.FormKind
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents.ListFooter
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents.NoDeviceBanner
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents.SectionHeader
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents.TechRequestCard
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents.TechRequestCardSkeleton
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents.TechRequestFormPane
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents.TechRequestsDialogHost
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents.TechRequestsEmpty
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents.TechRequestsLoadError

/** Compact — список + FAB, Medium — форма диалогом, от Expanded (desktop — от 780) — список + панель формы. */
private enum class TechRequestsLayout { Compact, Medium, Split }

private const val SKELETON_COUNT = 4
private const val LOAD_MORE_THRESHOLD = 3
private val DesktopSplitFrom = 780.dp

@Composable
internal fun TechRequestsScreen(
    state: State<TechRequestsState>,
    actions: TechRequestsActions,
    snackbar: CmpSnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val keyboardHints = remember { showsKeyboardHints() }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val window = cmpWindowSize
        val desktopSplit = keyboardHints && maxWidth >= DesktopSplitFrom
        val layout = when {
            window.atLeast(CmpWidthClass.Expanded) || desktopSplit -> TechRequestsLayout.Split
            window.atLeast(CmpWidthClass.Medium) -> TechRequestsLayout.Medium
            else -> TechRequestsLayout.Compact
        }
        val panelWidth = when {
            maxWidth >= 1200.dp -> 480.dp
            maxWidth >= 960.dp -> 440.dp
            else -> 380.dp
        }
        CmpScaffold(
            topBar = { TopBar(state, layout, actions) },
            snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
            floatingActionButton = { CreateFab(state, layout, actions.onCreate) },
        ) { padding ->
            Row(modifier = Modifier.fillMaxSize().padding(padding)) {
                RequestsList(state, layout, actions, Modifier.weight(1f).fillMaxHeight())
                if (layout == TechRequestsLayout.Split) {
                    SidePanel(state, keyboardHints, actions, Modifier.width(panelWidth).fillMaxHeight())
                }
            }
        }
        FormOverlay(state, layout, keyboardHints, actions)
        val dialog by remember(state) { derivedStateOf { state.value.dialog } }
        TechRequestsDialogHost(
            dialog = dialog,
            onConfirmAction = actions.onConfirmAction,
            onDiscardDraft = actions.form.onDiscardDraft,
            onConnectDevice = actions.onConnectDevice,
            onDismiss = actions.onDismissDialog,
        )
    }
}

@Composable
private fun TopBar(state: State<TechRequestsState>, layout: TechRequestsLayout, actions: TechRequestsActions) {
    val device by remember(state) { derivedStateOf { state.value.device } }
    val showCreate by remember(state, layout) {
        derivedStateOf { layout == TechRequestsLayout.Medium && state.value.hasRequests() }
    }
    CmpTopAppBar(
        title = { CmpText(text = stringResource(Strings.string.techrequests_list_title_screen)) },
        subtitle = device?.let { attached -> { CmpText(text = attached.model) } },
        navigationIcon = {
            CmpIconButton(
                onClick = actions.onBack,
                icon = CmpIcons.ArrowBack,
                contentDescription = stringResource(Strings.string.techrequests_common_a11y_back),
            )
        },
        actions = {
            if (showCreate) {
                CmpButton(
                    onClick = actions.onCreate,
                    modifier = Modifier.padding(end = 8.dp).heightIn(min = 48.dp).testTag(TechRequestsTags.CREATE),
                ) {
                    CmpIcon(icon = CmpIcons.Add, contentDescription = null)
                    CmpText(
                        text = stringResource(Strings.string.techrequests_list_btn_new),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        },
    )
}

@Composable
private fun CreateFab(state: State<TechRequestsState>, layout: TechRequestsLayout, onCreate: () -> Unit) {
    val visible by remember(state, layout) {
        derivedStateOf {
            layout == TechRequestsLayout.Compact && state.value.hasRequests() && !state.value.formOpen
        }
    }
    if (!visible) return
    CmpFloatingActionButton(onClick = onCreate, modifier = Modifier.testTag(TechRequestsTags.CREATE)) {
        Row(
            modifier = Modifier.padding(start = 18.dp, end = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CmpIcon(icon = CmpIcons.Add, contentDescription = null)
            CmpText(text = stringResource(Strings.string.techrequests_list_btn_new))
        }
    }
}

@Composable
private fun RequestsList(
    state: State<TechRequestsState>,
    layout: TechRequestsLayout,
    actions: TechRequestsActions,
    modifier: Modifier = Modifier,
) {
    val current = state.value
    val maxWidth = when (layout) {
        TechRequestsLayout.Compact -> Dp.Unspecified
        TechRequestsLayout.Medium -> 720.dp
        TechRequestsLayout.Split -> 680.dp
    }
    val horizontal = if (layout == TechRequestsLayout.Compact) 16.dp else 24.dp
    val columnModifier = Modifier.fillMaxSize().widthIn(max = maxWidth).padding(horizontal = horizontal)
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        when (val requests = current.requests) {
            LCEState.None, LCEState.Loading -> SkeletonList(columnModifier)

            is LCEState.Error -> TechRequestsLoadError(requests.error, actions.onRetry, columnModifier)

            is LCEState.Content -> if (requests.content.items.isEmpty()) {
                TechRequestsEmpty(
                    canCreate = current.canCreate,
                    onCreate = actions.onCreate,
                    onConnectDevice = actions.onConnectDevice,
                    onOpenChat = actions.onOpenChat,
                    modifier = columnModifier.fillMaxHeight(),
                )
            } else {
                LoadedList(current, requests.content, layout, actions, columnModifier)
            }
        }
    }
}

@Composable
private fun SkeletonList(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(SKELETON_COUNT) { TechRequestCardSkeleton() }
    }
}

@Composable
private fun LoadedList(
    state: TechRequestsState,
    requests: TechRequestList,
    layout: TechRequestsLayout,
    actions: TechRequestsActions,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LoadMoreTrigger(listState, state.canLoadMore, actions.onLoadMore)
    val bottom = if (layout == TechRequestsLayout.Compact) 104.dp else 32.dp
    val showBanner = !state.canCreate && layout != TechRequestsLayout.Split
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(top = 8.dp, bottom = bottom),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showBanner) {
            item(key = BANNER_KEY) { NoDeviceBanner(actions.onConnectDevice, Modifier.animateItem()) }
        }
        section(SECTION_ACTIVE, requests.active, state, actions)
        section(SECTION_FINISHED, requests.finished, state, actions)
        item(key = FOOTER_KEY) {
            ListFooter(state.loadingMore, state.moreError, actions.onRetryMore, Modifier.animateItem())
        }
    }
}

private fun LazyListScope.section(
    key: String,
    requests: List<TechRequest>,
    state: TechRequestsState,
    actions: TechRequestsActions,
) {
    if (requests.isEmpty()) return
    item(key = key) {
        SectionHeader(
            label = stringResource(
                if (key == SECTION_ACTIVE) {
                    Strings.string.techrequests_list_label_active
                } else {
                    Strings.string.techrequests_list_label_finished
                },
            ),
            count = requests.size,
            modifier = Modifier.animateItem(),
        )
    }
    items(requests, key = { it.id }) { request ->
        TechRequestCard(
            request = request,
            busyAction = state.pendingAction.takeIf { state.pendingActionId == request.id },
            highlighted = state.highlightedId == request.id,
            onAction = { action -> actions.onRequestAction(action, request.id) },
            modifier = Modifier.animateItem(),
        )
    }
}

@Composable
private fun LoadMoreTrigger(listState: LazyListState, canLoadMore: Boolean, onLoadMore: () -> Unit) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val nearEnd by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(listState, canLoadMore) {
        if (!canLoadMore) return@LaunchedEffect
        snapshotFlow { nearEnd }.filter { it }.collect { currentOnLoadMore() }
    }
}

@Composable
private fun SidePanel(
    state: State<TechRequestsState>,
    keyboardHints: Boolean,
    actions: TechRequestsActions,
    modifier: Modifier = Modifier,
) {
    val colors = cmpThemeColors()
    val current = state.value
    Box(modifier = modifier.background(colors.surface)) {
        Box(Modifier.fillMaxHeight().width(1.dp).background(colors.outlineVariant))
        if (current.canCreate || current.sending is TechRequestSending.Sent) {
            TechRequestFormPane(
                kind = FormKind.Panel,
                form = current.form,
                sending = current.sending,
                device = current.device,
                language = current.language,
                keyboardHints = keyboardHints,
                actions = actions.form,
                origin = current.origin,
                modifier = Modifier.padding(start = 1.dp),
            )
        } else {
            NoDeviceBanner(actions.onConnectDevice, Modifier.padding(24.dp))
        }
    }
}

@Composable
private fun FormOverlay(
    state: State<TechRequestsState>,
    layout: TechRequestsLayout,
    keyboardHints: Boolean,
    actions: TechRequestsActions,
) {
    val current = state.value
    if (!current.formOpen || layout == TechRequestsLayout.Split) return
    val colors = cmpThemeColors()
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = actions.form.onClose,
    )
    val pane: @Composable (FormKind, Modifier) -> Unit = { kind, paneModifier ->
        TechRequestFormPane(
            kind = kind,
            form = current.form,
            sending = current.sending,
            device = current.device,
            language = current.language,
            keyboardHints = keyboardHints,
            actions = actions.form,
            origin = current.origin,
            modifier = paneModifier,
        )
    }
    if (layout == TechRequestsLayout.Compact) {
        pane(FormKind.Page, Modifier.fillMaxSize())
        return
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.scrim.copy(alpha = SCRIM_ALPHA))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                actions.form.onClose()
            }
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CmpSurface(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .heightIn(max = 780.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            shape = RoundedCornerShape(28.dp),
            color = colors.surface,
            shadowElevation = 6.dp,
        ) {
            pane(FormKind.Dialog, Modifier)
        }
    }
}

private fun TechRequestsState.hasRequests(): Boolean =
    (requests as? LCEState.Content)?.content?.items?.isNotEmpty() == true

private const val SECTION_ACTIVE = "section_active"
private const val SECTION_FINISHED = "section_finished"
private const val BANNER_KEY = "no_device"
private const val FOOTER_KEY = "footer"
private const val SCRIM_ALPHA = 0.4f
