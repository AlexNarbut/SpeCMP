package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarResult
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBarStyle
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatEffect
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.isSupportChatDesktopPlatform
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatComposer
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatDateDivider
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatEmptyState
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatErrorState
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatImageViewer
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatLoadingSkeleton
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatMessageBubble
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatOfflineBanner
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatTypingIndicator
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatWallpaper
import specmp.feature.support_chat.generated.resources.Res
import specmp.feature.support_chat.generated.resources.support_chat_a11y_back
import specmp.feature.support_chat.generated.resources.support_chat_btn_retry
import specmp.feature.support_chat.generated.resources.support_chat_download_failed
import specmp.feature.support_chat.generated.resources.support_chat_download_success
import specmp.feature.support_chat.generated.resources.support_chat_error_load_history
import specmp.feature.support_chat.generated.resources.support_chat_error_send
import specmp.feature.support_chat.generated.resources.support_chat_month_april
import specmp.feature.support_chat.generated.resources.support_chat_month_august
import specmp.feature.support_chat.generated.resources.support_chat_month_december
import specmp.feature.support_chat.generated.resources.support_chat_month_february
import specmp.feature.support_chat.generated.resources.support_chat_month_january
import specmp.feature.support_chat.generated.resources.support_chat_month_july
import specmp.feature.support_chat.generated.resources.support_chat_month_june
import specmp.feature.support_chat.generated.resources.support_chat_month_march
import specmp.feature.support_chat.generated.resources.support_chat_month_may
import specmp.feature.support_chat.generated.resources.support_chat_month_november
import specmp.feature.support_chat.generated.resources.support_chat_month_october
import specmp.feature.support_chat.generated.resources.support_chat_month_september
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_history_title
import specmp.feature.support_chat.generated.resources.support_chat_today
import specmp.feature.support_chat.generated.resources.support_chat_title_screen
import specmp.feature.support_chat.generated.resources.support_chat_yesterday

@Composable
fun SupportChatScreen(
    state: SupportChatScreenState,
    effect: SharedFlow<SupportChatEffect>,
    onBack: () -> Unit,
    onAttachFile: () -> Unit,
    onAttachPhoto: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onSend: (String) -> Unit,
    onRetry: (String) -> Unit,
    onConnectionRetry: () -> Unit,
    onHistoryRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onMessageVisible: (String) -> Unit,
    onDownloadAttachment: (SupportChatUiAttachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cmpThemeColors()
    val isDesktop = isSupportChatDesktopPlatform()
    val todayLabel = stringResource(Res.string.support_chat_today)
    val yesterdayLabel = stringResource(Res.string.support_chat_yesterday)
    val snackbarHostState = rememberCmpSnackbarHostState()
    val sendFailedText = stringResource(Res.string.support_chat_error_send)
    val historyLoadFailedText = stringResource(Res.string.support_chat_error_load_history)
    val downloadSuccessText = stringResource(Res.string.support_chat_download_success)
    val downloadFailedText = stringResource(Res.string.support_chat_download_failed)
    val retryLabel = stringResource(Res.string.support_chat_btn_retry)

    effect.collectSideEffect { event ->
        when (event) {
            is SupportChatEffect.MessageSendFailed -> {
                val result = snackbarHostState.showSnackbar(sendFailedText, actionLabel = retryLabel)
                if (result == CmpSnackbarResult.ActionPerformed) onRetry(event.messageId)
            }
            SupportChatEffect.HistoryLoadFailed -> snackbarHostState.showSnackbar(historyLoadFailedText)
            is SupportChatEffect.AttachmentDownloaded -> snackbarHostState.showSnackbar(downloadSuccessText)
            SupportChatEffect.AttachmentDownloadFailed -> snackbarHostState.showSnackbar(downloadFailedText)
        }
    }
    val monthLabels = listOf(
        stringResource(Res.string.support_chat_month_january),
        stringResource(Res.string.support_chat_month_february),
        stringResource(Res.string.support_chat_month_march),
        stringResource(Res.string.support_chat_month_april),
        stringResource(Res.string.support_chat_month_may),
        stringResource(Res.string.support_chat_month_june),
        stringResource(Res.string.support_chat_month_july),
        stringResource(Res.string.support_chat_month_august),
        stringResource(Res.string.support_chat_month_september),
        stringResource(Res.string.support_chat_month_october),
        stringResource(Res.string.support_chat_month_november),
        stringResource(Res.string.support_chat_month_december),
    )
    var viewerImageSource by remember { mutableStateOf<String?>(null) }
    // Черновик — локальное UI-состояние (U-11): каждый символ не должен
    // рекомпозировать весь экран через state ViewModel. В VM текст попадает
    // только в момент отправки.
    var draftText by rememberSaveable { mutableStateOf("") }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val layoutMode = when {
            maxWidth >= 840.dp -> SupportChatLayoutMode.Expanded
            maxWidth >= 600.dp -> SupportChatLayoutMode.Medium
            else -> SupportChatLayoutMode.Compact
        }

        val listItems = remember(state.messages, state.typingAuthor, todayLabel, yesterdayLabel, monthLabels) {
            buildListItems(
                messages = state.messages,
                todayLabel = todayLabel,
                yesterdayLabel = yesterdayLabel,
                monthLabels = monthLabels,
                typingAuthor = state.typingAuthor,
            )
        }

        SupportChatScaffold(
            state = state,
            colors = colors,
            layoutMode = layoutMode,
            isDesktop = isDesktop,
            listItems = listItems,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onDraftChange = { draftText = it },
            onAttachFile = onAttachFile,
            onAttachPhoto = onAttachPhoto,
            onRemoveAttachment = onRemoveAttachment,
            onSend = { onSend(draftText); draftText = "" },
            draftText = draftText,
            onRetry = onRetry,
            onConnectionRetry = onConnectionRetry,
            onHistoryRetry = onHistoryRetry,
            onLoadMore = onLoadMore,
            onMessageVisible = onMessageVisible,
            onImageClick = { viewerImageSource = it },
            onDownloadAttachment = onDownloadAttachment,
        )

        viewerImageSource?.let { source ->
            SupportChatImageViewer(
                imageSource = source,
                onDismiss = { viewerImageSource = null },
            )
        }
    }
}

@Composable
private fun SupportChatScaffold(
    state: SupportChatScreenState,
    colors: CmpThemeColors,
    layoutMode: SupportChatLayoutMode,
    isDesktop: Boolean,
    listItems: ImmutableList<SupportChatListItem>,
    snackbarHostState: CmpSnackbarHostState,
    onBack: () -> Unit,
    onDraftChange: (String) -> Unit,
    onAttachFile: () -> Unit,
    onAttachPhoto: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onSend: () -> Unit,
    draftText: String,
    onRetry: (String) -> Unit,
    onConnectionRetry: () -> Unit,
    onHistoryRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onMessageVisible: (String) -> Unit,
    onImageClick: (String) -> Unit,
    onDownloadAttachment: (SupportChatUiAttachment) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.lastOrNull()?.id, state.typingAuthor) {
        if (listItems.isNotEmpty()) {
            listState.animateScrollToItem(listItems.size - 1)
        }
    }

    LaunchedEffect(listState, state.isLoadingMore, state.hasMoreHistory) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.canScrollBackward }
            .distinctUntilChanged()
            .collect { (index, canScrollBackward) ->
                if (index == 0 &&
                    canScrollBackward &&
                    state.hasMoreHistory &&
                    !state.isLoadingMore &&
                    state.messages.isNotEmpty()
                ) {
                    onLoadMore()
                }
            }
    }

    CmpScaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { CmpSnackbarHost(snackbarHostState) },
        topBar = {
            CmpTopAppBar(
                title = {
                    CmpText(text = stringResource(Res.string.support_chat_title_screen))
                },
                navigationIcon = {
                    CmpIconButton(
                        onClick = onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.support_chat_a11y_back),
                    )
                },
                style = if (isDesktop) {
                    CmpTopAppBarStyle.CenterAligned
                } else {
                    CmpTopAppBarStyle.Small
                },
            )
        },
    ) { paddingValues ->
        SupportChatBody(
            state = state,
            colors = colors,
            layoutMode = layoutMode,
            listItems = listItems,
            listState = listState,
            paddingValues = paddingValues,
            onDraftChange = onDraftChange,
            onAttachFile = onAttachFile,
            onAttachPhoto = onAttachPhoto,
            onRemoveAttachment = onRemoveAttachment,
            onSend = onSend,
            draftText = draftText,
            onRetry = onRetry,
            onConnectionRetry = onConnectionRetry,
            onHistoryRetry = onHistoryRetry,
            onLoadMore = onLoadMore,
            onMessageVisible = onMessageVisible,
            onImageClick = onImageClick,
            onDownloadAttachment = onDownloadAttachment,
        )
    }
}

@Composable
private fun SupportChatBody(
    state: SupportChatScreenState,
    colors: CmpThemeColors,
    layoutMode: SupportChatLayoutMode,
    listItems: ImmutableList<SupportChatListItem>,
    listState: LazyListState,
    paddingValues: PaddingValues,
    onDraftChange: (String) -> Unit,
    draftText: String,
    onAttachFile: () -> Unit,
    onAttachPhoto: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onSend: () -> Unit,
    onRetry: (String) -> Unit,
    onConnectionRetry: () -> Unit,
    onHistoryRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onMessageVisible: (String) -> Unit,
    onImageClick: (String) -> Unit,
    onDownloadAttachment: (SupportChatUiAttachment) -> Unit,
) {
    val columnMaxWidth = when (layoutMode) {
        SupportChatLayoutMode.Compact -> Dp.Unspecified
        SupportChatLayoutMode.Medium -> 720.dp
        SupportChatLayoutMode.Expanded -> 840.dp
    }
    val contentPadding = when (layoutMode) {
        SupportChatLayoutMode.Compact -> 12.dp
        SupportChatLayoutMode.Medium -> 24.dp
        SupportChatLayoutMode.Expanded -> 32.dp
    }

    LaunchedEffect(listState, listItems) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.key } }
            .distinctUntilChanged()
            .collect { keys ->
                keys.forEach { key ->
                    val messageId = (key as? String)?.removePrefix(MESSAGE_KEY_PREFIX) ?: return@forEach
                    onMessageVisible(messageId)
                }
            }
    }

    CmpColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.isLoadingMore) {
            CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (state.isOffline) {
            SupportChatOfflineBanner(
                onRetry = onConnectionRetry,
                colors = colors,
            )
        }

        CmpBox(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            SupportChatWallpaper(
                colors = colors,
                modifier = Modifier.fillMaxSize(),
            )

            val historyState = state.history
            when {
                historyState is LCEState.None || historyState is LCEState.Loading -> {
                    CmpBox(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        SupportChatLoadingSkeleton(
                            colors = colors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = columnMaxWidth)
                                .padding(horizontal = contentPadding),
                        )
                    }
                }

                historyState is LCEState.Error -> {
                    SupportChatErrorState(
                        onRetry = onHistoryRetry,
                        colors = colors,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = columnMaxWidth),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(
                            start = contentPadding,
                            end = contentPadding,
                            top = 8.dp,
                            bottom = 16.dp,
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (state.messages.isEmpty()) {
                            item {
                                SupportChatEmptyState(
                                    colors = colors,
                                    modifier = Modifier
                                        .fillParentMaxSize()
                                        .widthIn(max = columnMaxWidth),
                                )
                            }
                        } else {
                            items(
                                count = listItems.size,
                                key = { index ->
                                    when (val item = listItems[index]) {
                                        is SupportChatListItem.DateDivider -> "${DATE_KEY_PREFIX}${item.date}"
                                        is SupportChatListItem.Message -> "${MESSAGE_KEY_PREFIX}${item.message.id}"
                                        is SupportChatListItem.Typing -> "typing_${item.author}"
                                    }
                                },
                            ) { index ->
                                val item = listItems[index]
                                when (item) {
                                    is SupportChatListItem.DateDivider -> {
                                        SupportChatDateDivider(
                                            label = item.label,
                                            colors = colors,
                                        )
                                    }

                                    is SupportChatListItem.Message -> {
                                        SupportChatMessageBubble(
                                            message = item.message,
                                            colors = colors,
                                            modifier = Modifier.widthIn(max = columnMaxWidth),
                                            isDownloading = state.downloadingAttachmentId != null &&
                                                state.downloadingAttachmentId == item.message.attachment?.id,
                                            onRetry = onRetry,
                                            onImageClick = onImageClick,
                                            onDownloadAttachment = onDownloadAttachment,
                                        )
                                    }

                                    is SupportChatListItem.Typing -> {
                                        SupportChatTypingIndicator(
                                            author = item.author,
                                            colors = colors,
                                            modifier = Modifier.widthIn(max = columnMaxWidth),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SupportChatComposer(
            draftText = draftText,
            onDraftChange = onDraftChange,
            onSend = onSend,
            onAttachFile = onAttachFile,
            onAttachPhoto = onAttachPhoto,
            pendingAttachments = state.pendingAttachments,
            onRemoveAttachment = onRemoveAttachment,
            colors = colors,
            // Композер ровно по ширине чат-колонки: те же боковые отступы,
            // что у LazyColumn (contentPadding), и тот же потолок ширины.
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = contentPadding, vertical = 8.dp)
                .widthIn(max = columnMaxWidth),
            enabled = !state.isOffline,
        )
    }
}

internal sealed interface SupportChatListItem {
    data class DateDivider(
        val date: LocalDate,
        val label: String,
    ) : SupportChatListItem

    data class Message(val message: SupportChatMessage) : SupportChatListItem
    data class Typing(val author: String) : SupportChatListItem
}

internal fun buildListItems(
    messages: List<SupportChatMessage>,
    todayLabel: String,
    yesterdayLabel: String,
    monthLabels: List<String>,
    typingAuthor: String?,
): ImmutableList<SupportChatListItem> {
    val result = mutableListOf<SupportChatListItem>()
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = now.minus(DatePeriod(days = 1))
    var lastDate: LocalDate? = null

    messages.sortedBy { it.time }.forEach { message ->
        val date = message.time.date
        if (date != lastDate) {
            val label = when (date) {
                now -> todayLabel
                yesterday -> yesterdayLabel
                else -> formatDate(date, monthLabels, now.year)
            }
            result.add(SupportChatListItem.DateDivider(date = date, label = label))
            lastDate = date
        }
        result.add(SupportChatListItem.Message(message))
    }

    typingAuthor?.let { author ->
        result.add(SupportChatListItem.Typing(author))
    }

    return result.toPersistentList()
}

private fun formatDate(
    date: LocalDate,
    monthLabels: List<String>,
    currentYear: Int,
): String {
    val month = monthLabels.getOrNull(date.monthNumber - 1).orEmpty()
    return buildString {
        append(date.dayOfMonth)
        if (month.isNotBlank()) {
            append(' ')
            append(month)
        }
        if (date.year != currentYear) {
            append(' ')
            append(date.year)
        }
    }
}

private enum class SupportChatLayoutMode {
    Compact,
    Medium,
    Expanded,
}

private const val DATE_KEY_PREFIX = "date_"
private const val MESSAGE_KEY_PREFIX = "msg_"
