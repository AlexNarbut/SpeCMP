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
import androidx.compose.runtime.State
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
import ru.alarmtrade.specmp.core.ui.format.rememberCmpDateLabeler
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
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatActions
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatEffect
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiMessage
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.toUiMessage
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
import specmp.feature.support_chat.generated.resources.supportchat_common_a11y_back
import specmp.feature.support_chat.generated.resources.supportchat_common_btn_retry
import specmp.feature.support_chat.generated.resources.supportchat_chat_snack_download_failed
import specmp.feature.support_chat.generated.resources.supportchat_chat_snack_download_success
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_load_history
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_send
import specmp.feature.support_chat.generated.resources.supportchat_chat_title_screen

@Composable
fun SupportChatScreen(
    state: State<SupportChatScreenState>,
    effect: SharedFlow<SupportChatEffect>,
    actions: SupportChatActions,
    modifier: Modifier = Modifier,
) {
    val colors = cmpThemeColors()
    val snackbarHostState = rememberCmpSnackbarHostState()
    val sendFailedText = stringResource(Res.string.supportchat_chat_error_send)
    val historyLoadFailedText = stringResource(Res.string.supportchat_chat_error_load_history)
    val downloadSuccessText = stringResource(Res.string.supportchat_chat_snack_download_success)
    val downloadFailedText = stringResource(Res.string.supportchat_chat_snack_download_failed)
    val retryLabel = stringResource(Res.string.supportchat_common_btn_retry)

    effect.collectSideEffect { event ->
        when (event) {
            is SupportChatEffect.MessageSendFailed -> {
                val result = snackbarHostState.showSnackbar(sendFailedText, actionLabel = retryLabel)
                if (result == CmpSnackbarResult.ActionPerformed) actions.onRetry(event.messageId)
            }
            SupportChatEffect.HistoryLoadFailed -> snackbarHostState.showSnackbar(historyLoadFailedText)
            is SupportChatEffect.AttachmentDownloaded -> snackbarHostState.showSnackbar(downloadSuccessText)
            SupportChatEffect.AttachmentDownloadFailed -> snackbarHostState.showSnackbar(downloadFailedText)
        }
    }
    val dateLabeler = rememberCmpDateLabeler()
    var viewerImageSource by remember { mutableStateOf<String?>(null) }
    // Черновик — локальное UI-состояние (U-11): каждый символ не рекомпозирует
    // весь экран через state ViewModel. В VM текст попадает только в момент отправки.
    var draftText by rememberSaveable { mutableStateOf("") }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val layoutMode = when {
            maxWidth >= 840.dp -> SupportChatLayoutMode.Expanded
            maxWidth >= 600.dp -> SupportChatLayoutMode.Medium
            else -> SupportChatLayoutMode.Compact
        }

        SupportChatScaffold(
            state = state,
            colors = colors,
            layoutMode = layoutMode,
            snackbarHostState = snackbarHostState,
            draftText = draftText,
            onDraftChange = { draftText = it },
            onSend = { actions.onSend(draftText); draftText = "" },
            actions = actions,
            dateLabeler = dateLabeler,
            onImageClick = { viewerImageSource = it },
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
    state: State<SupportChatScreenState>,
    colors: CmpThemeColors,
    layoutMode: SupportChatLayoutMode,
    snackbarHostState: CmpSnackbarHostState,
    draftText: String,
    onDraftChange: (String) -> Unit,
    onSend: (String) -> Unit,
    actions: SupportChatActions,
    dateLabeler: (LocalDate) -> String,
    onImageClick: (String) -> Unit,
) {
    val listState = rememberLazyListState()

    // Пагинация: reverseLayout — «верх истории» это конец списка, подгрузка
    // старой страницы срабатывает, когда дальше скроллить некуда.
    LaunchedEffect(listState) {
        snapshotFlow { listState.canScrollForward }
            .distinctUntilChanged()
            .collect { canScrollForward ->
                val current = state.value
                if (!canScrollForward &&
                    current.hasMoreHistory &&
                    !current.isLoadingMore &&
                    current.messages.isNotEmpty()
                ) {
                    actions.onLoadMore()
                }
            }
    }

    // Видимые сообщения — пачкой в VM (U-8): один вызов на изменение набора,
    // батч + debounce сетевых read-отметок — в ViewModel (P-11).
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.mapNotNull { info ->
                (info.key as? String)?.removePrefix(MESSAGE_KEY_PREFIX)
            }
        }
            .distinctUntilChanged()
            .collect { messageIds ->
                if (messageIds.isNotEmpty()) actions.onMessagesVisible(messageIds)
            }
    }

    CmpScaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { CmpSnackbarHost(snackbarHostState) },
        topBar = {
            CmpTopAppBar(
                title = {
                    CmpText(text = stringResource(Res.string.supportchat_chat_title_screen))
                },
                navigationIcon = {
                    CmpIconButton(
                        onClick = actions.onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.supportchat_common_a11y_back),
                    )
                },
                // Выравнивание заголовка — свойство раскладки, а не платформы
                // (U-15): на Android-планшете экран тоже широкий.
                style = if (layoutMode == SupportChatLayoutMode.Compact) {
                    CmpTopAppBarStyle.Small
                } else {
                    CmpTopAppBarStyle.CenterAligned
                },
            )
        },
    ) { paddingValues ->
        SupportChatBody(
            state = state,
            colors = colors,
            layoutMode = layoutMode,
            listState = listState,
            paddingValues = paddingValues,
            draftText = draftText,
            onDraftChange = onDraftChange,
            onSend = onSend,
            actions = actions,
            dateLabeler = dateLabeler,
            onImageClick = onImageClick,
        )
    }
}

@Composable
private fun SupportChatBody(
    state: State<SupportChatScreenState>,
    colors: CmpThemeColors,
    layoutMode: SupportChatLayoutMode,
    listState: LazyListState,
    paddingValues: PaddingValues,
    draftText: String,
    onDraftChange: (String) -> Unit,
    onSend: (String) -> Unit,
    actions: SupportChatActions,
    dateLabeler: (LocalDate) -> String,
    onImageClick: (String) -> Unit,
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
    val current = state.value
    // Список мемоизируется только по сообщениям и меткам: индикатор набора —
    // отдельный item списка (index 0 при reverseLayout), не инвалидирует ленту (U-7).
    val listItems = remember(current.messages, dateLabeler) {
        buildListItems(
            messages = current.messages,
            dateLabeler = dateLabeler,
        )
    }

    CmpColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (current.isLoadingMore) {
            CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (current.isOffline) {
            SupportChatOfflineBanner(
                onRetry = actions.onConnectionRetry,
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

            when (val history = current.history) {
                is LCEState.None, is LCEState.Loading -> {
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

                is LCEState.Error -> {
                    SupportChatErrorState(
                        onRetry = actions.onHistoryRetry,
                        colors = colors,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = columnMaxWidth),
                    )
                }

                is LCEState.Content -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        reverseLayout = true,
                        contentPadding = PaddingValues(
                            start = contentPadding,
                            end = contentPadding,
                            top = 8.dp,
                            bottom = 16.dp,
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (current.messages.isEmpty()) {
                            item {
                                SupportChatEmptyState(
                                    colors = colors,
                                    modifier = Modifier
                                        .fillParentMaxSize()
                                        .widthIn(max = columnMaxWidth),
                                )
                            }
                        } else {
                            current.typingAuthor?.let { author ->
                                item(key = "$TYPING_KEY_PREFIX$author") {
                                    SupportChatTypingIndicator(
                                        author = author,
                                        colors = colors,
                                        modifier = Modifier.widthIn(max = columnMaxWidth),
                                    )
                                }
                            }
                            items(
                                count = listItems.size,
                                key = { index ->
                                    when (val item = listItems[index]) {
                                        is SupportChatListItem.DateDivider -> "${DATE_KEY_PREFIX}${item.date}"
                                        is SupportChatListItem.Message -> "${MESSAGE_KEY_PREFIX}${item.message.id}"
                                    }
                                },
                            ) { index ->
                                when (val item = listItems[index]) {
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
                                            isDownloading = current.downloadingAttachmentId != null &&
                                                current.downloadingAttachmentId == item.message.attachment?.id,
                                            onRetry = actions.onRetry,
                                            onImageClick = onImageClick,
                                            onDownloadAttachment = actions.onDownloadAttachment,
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
            onSend = { onSend(draftText) },
            onAttachFile = actions.onAttachFile,
            onAttachPhoto = actions.onAttachPhoto,
            pendingAttachments = current.pendingAttachments,
            onRemoveAttachment = actions.onRemoveAttachment,
            colors = colors,
            // Композер ровно по ширине чат-колонки: те же боковые отступы,
            // что у LazyColumn (contentPadding), и тот же потолок ширины.
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = contentPadding, vertical = 8.dp)
                .widthIn(max = columnMaxWidth),
            enabled = !current.isOffline,
        )
    }
}

internal sealed interface SupportChatListItem {
    data class DateDivider(
        val date: LocalDate,
        val label: String,
    ) : SupportChatListItem

    data class Message(val message: SupportChatUiMessage) : SupportChatListItem
}

/**
 * Элементы списка в отображаемом порядке (newest-first; экран использует
 * `reverseLayout = true`, поэтому index 0 — внизу, индикатор набора — первым).
 *
 * Порядок `messages` поддерживает ViewModel (инвариант state, P-16):
 * здесь только разворот, без сортировки на каждом построении (U-7).
 */
internal fun buildListItems(
    messages: List<SupportChatMessage>,
    dateLabeler: (LocalDate) -> String,
): ImmutableList<SupportChatListItem> {
    val result = mutableListOf<SupportChatListItem>()

    var groupDate: LocalDate? = null
    messages.asReversed().forEach { message ->
        val date = message.time.date
        val closedGroupDate = groupDate
        if (closedGroupDate != null && date != closedGroupDate) {
            // Граница групп: разделитель закрывает более новую группу сверху.
            result.add(
                SupportChatListItem.DateDivider(
                    date = closedGroupDate,
                    label = dateLabeler(closedGroupDate),
                )
            )
        }
        result.add(SupportChatListItem.Message(message.toUiMessage()))
        groupDate = date
    }
    groupDate?.let {
        result.add(
            SupportChatListItem.DateDivider(
                date = it,
                label = dateLabeler(it),
            )
        )
    }

    return result.toPersistentList()
}

private enum class SupportChatLayoutMode {
    Compact,
    Medium,
    Expanded,
}

private const val DATE_KEY_PREFIX = "date_"
private const val MESSAGE_KEY_PREFIX = "msg_"
private const val TYPING_KEY_PREFIX = "typing_"
