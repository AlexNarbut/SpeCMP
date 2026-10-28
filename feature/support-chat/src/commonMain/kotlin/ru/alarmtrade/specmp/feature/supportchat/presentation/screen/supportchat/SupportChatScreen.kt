package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
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
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiMessage
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.toUiMessage
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentLimits
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatComposer
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatConnectionStatus
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatDateDivider
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatDocumentActionsDialog
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatEmptyState
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatErrorState
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatImageViewer
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatLoadingSkeleton
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatMessageBubble
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatTypingIndicator
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents.SupportChatWallpaper
import specmp.feature.support_chat.generated.resources.Res
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_attach
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_attachment_too_large
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_download
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_load_history
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_open
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_open_no_app
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_save_permission
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_send
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_send_no_file
import specmp.feature.support_chat.generated.resources.supportchat_chat_msg_download_success
import specmp.feature.support_chat.generated.resources.supportchat_chat_title_screen
import specmp.feature.support_chat.generated.resources.supportchat_common_a11y_back
import specmp.feature.support_chat.generated.resources.supportchat_common_btn_retry

@Suppress("CyclomaticComplexMethod")
@Composable
fun SupportChatScreen(
    state: State<SupportChatScreenState>,
    effect: SharedFlow<SupportChatEffect>,
    actions: SupportChatActions,
    modifier: Modifier = Modifier
) {
    val colors = cmpThemeColors()
    val snackbarHostState = rememberCmpSnackbarHostState()
    val sendFailedText = stringResource(Res.string.supportchat_chat_error_send)
    val sendNoFileText = stringResource(Res.string.supportchat_chat_error_send_no_file)
    val historyLoadFailedText = stringResource(Res.string.supportchat_chat_error_load_history)
    val downloadSuccessText = stringResource(Res.string.supportchat_chat_msg_download_success)
    val downloadFailedText = stringResource(Res.string.supportchat_chat_error_download)
    val retryLabel = stringResource(Res.string.supportchat_common_btn_retry)
    val attachmentTooLargeText = stringResource(
        Res.string.supportchat_chat_error_attachment_too_large,
        SupportChatAttachmentLimits.MAX_MEGABYTES
    )
    val attachFailedText = stringResource(Res.string.supportchat_chat_error_attach)
    val openFailedText = stringResource(Res.string.supportchat_chat_error_open)
    val openNoAppText = stringResource(Res.string.supportchat_chat_error_open_no_app)
    val savePermissionText = stringResource(Res.string.supportchat_chat_error_save_permission)

    effect.collectSideEffect { event ->
        when (event) {
            is SupportChatEffect.MessageSendFailed -> if (event.canRetry) {
                val result = snackbarHostState.showSnackbar(sendFailedText, actionLabel = retryLabel)
                if (result == CmpSnackbarResult.ActionPerformed) actions.onRetry(event.messageId)
            } else {
                snackbarHostState.showSnackbar(sendNoFileText)
            }

            SupportChatEffect.HistoryLoadFailed -> snackbarHostState.showSnackbar(historyLoadFailedText)

            is SupportChatEffect.AttachmentDownloaded -> snackbarHostState.showSnackbar(downloadSuccessText)

            SupportChatEffect.AttachmentDownloadFailed -> snackbarHostState.showSnackbar(downloadFailedText)

            is SupportChatEffect.AttachmentRejected -> snackbarHostState.showSnackbar(
                if (event.tooLarge) attachmentTooLargeText else attachFailedText
            )

            is SupportChatEffect.AttachmentOpenFailed -> snackbarHostState.showSnackbar(
                if (event.noApp) openNoAppText else openFailedText
            )

            SupportChatEffect.AttachmentSavePermissionDenied -> snackbarHostState.showSnackbar(savePermissionText)
        }
    }
    var viewerImageSource by remember { mutableStateOf<String?>(null) }
    var documentActionsFor by remember { mutableStateOf<SupportChatUiAttachment?>(null) }
    // Запрос «к последним сообщениям» от своей отправки. Счётчик, а не ключ сообщения:
    // серверное эхо (через ~20 мс) меняет ключ и не должно отменять начатую прокрутку.
    val scrollToNewestRequests = remember { mutableIntStateOf(0) }

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
            actions = actions,
            onImageClick = { viewerImageSource = it },
            onDocumentClick = { documentActionsFor = it },
            scrollToNewestRequests = scrollToNewestRequests
        )

        viewerImageSource?.let { source ->
            SupportChatImageViewer(
                imageSource = source,
                onDismiss = { viewerImageSource = null }
            )
        }

        documentActionsFor?.let { attachment ->
            SupportChatDocumentActionsDialog(
                attachment = attachment,
                onOpen = {
                    documentActionsFor = null
                    actions.onOpenAttachment(attachment)
                },
                onDownload = {
                    documentActionsFor = null
                    actions.onDownloadAttachment(attachment)
                },
                onDismiss = { documentActionsFor = null }
            )
        }
    }
}

// scrollToNewestRequests — счётчик-сигнал: читается только в ленте (отложенное чтение), пишется по «Отправить»
@Suppress("ktlint:compose:mutable-state-param-check")
@Composable
private fun SupportChatScaffold(
    state: State<SupportChatScreenState>,
    colors: CmpThemeColors,
    layoutMode: SupportChatLayoutMode,
    snackbarHostState: CmpSnackbarHostState,
    actions: SupportChatActions,
    onImageClick: (String) -> Unit,
    onDocumentClick: (SupportChatUiAttachment) -> Unit,
    scrollToNewestRequests: MutableIntState
) {
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
                        contentDescription = stringResource(Res.string.supportchat_common_a11y_back)
                    )
                },
                // Выравнивание заголовка — свойство раскладки, а не платформы.
                style = if (layoutMode == SupportChatLayoutMode.Compact) {
                    CmpTopAppBarStyle.Small
                } else {
                    CmpTopAppBarStyle.CenterAligned
                }
            )
        }
    ) { paddingValues ->
        SupportChatBody(
            state = state,
            colors = colors,
            layoutMode = layoutMode,
            paddingValues = paddingValues,
            actions = actions,
            onImageClick = onImageClick,
            onDocumentClick = onDocumentClick,
            scrollToNewestRequests = scrollToNewestRequests
        )
    }
}

/**
 * Каркас тела экрана. Значение state здесь не читается: каждая секция (баннер,
 * лента, композер) читает только своё — индикатор набора или прогресс
 * скачивания не рекомпозируют композер, а символ ввода — ленту.
 */
@Suppress("ktlint:compose:mutable-state-param-check")
@Composable
private fun SupportChatBody(
    state: State<SupportChatScreenState>,
    colors: CmpThemeColors,
    layoutMode: SupportChatLayoutMode,
    paddingValues: PaddingValues,
    actions: SupportChatActions,
    onImageClick: (String) -> Unit,
    onDocumentClick: (SupportChatUiAttachment) -> Unit,
    scrollToNewestRequests: MutableIntState
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

    CmpColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CmpBox(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            SupportChatWallpaper(
                colors = colors,
                modifier = Modifier.fillMaxSize()
            )

            SupportChatHistory(
                state = state,
                colors = colors,
                columnMaxWidth = columnMaxWidth,
                contentPadding = contentPadding,
                actions = actions,
                onImageClick = onImageClick,
                onDocumentClick = onDocumentClick,
                scrollToNewestRequests = scrollToNewestRequests
            )

            // Статусы — поверх ленты: появление и исчезновение не сдвигают сообщения.
            SupportChatStatusOverlay(state = state, colors = colors)
        }

        SupportChatComposerSection(
            state = state,
            colors = colors,
            actions = actions,
            onSend = { scrollToNewestRequests.intValue++ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = contentPadding, vertical = 8.dp)
                .widthIn(max = columnMaxWidth)
        )
    }
}

@Composable
private fun BoxScope.SupportChatStatusOverlay(state: State<SupportChatScreenState>, colors: CmpThemeColors) {
    val isLoadingMore by remember(state) { derivedStateOf { state.value.isLoadingMore } }
    val banner by remember(state) { derivedStateOf { state.value.connectionBanner } }
    if (isLoadingMore) {
        CmpLinearProgressIndicator(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )
    }
    SupportChatConnectionStatus(
        banner = banner,
        colors = colors,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
    )
}

@Composable
private fun SupportChatComposerSection(
    state: State<SupportChatScreenState>,
    colors: CmpThemeColors,
    actions: SupportChatActions,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Черновик — локальное UI-состояние: каждый символ не проходит через state VM
    // и не рекомпозирует ленту. В VM текст попадает только в момент отправки.
    var draftText by rememberSaveable { mutableStateOf("") }
    // Только два поля state: индикатор набора, статусы и прогресс скачивания
    // композер не рекомпозируют.
    val pendingAttachments by remember(state) { derivedStateOf { state.value.pendingAttachments } }
    val isInputEnabled by remember(state) { derivedStateOf { state.value.isInputEnabled } }

    SupportChatComposer(
        draftText = draftText,
        onDraftChange = { draftText = it },
        onSend = {
            actions.onSend(draftText)
            draftText = ""
            onSend()
        },
        onAttachFile = actions.onAttachFile,
        onAttachPhoto = actions.onAttachPhoto,
        pendingAttachments = pendingAttachments,
        onRemoveAttachment = actions.onRemoveAttachment,
        colors = colors,
        modifier = modifier,
        enabled = isInputEnabled
    )
}

@Suppress("ktlint:compose:mutable-state-param-check")
@Composable
private fun SupportChatHistory(
    state: State<SupportChatScreenState>,
    colors: CmpThemeColors,
    columnMaxWidth: Dp,
    contentPadding: Dp,
    actions: SupportChatActions,
    onImageClick: (String) -> Unit,
    onDocumentClick: (SupportChatUiAttachment) -> Unit,
    scrollToNewestRequests: MutableIntState
) {
    when (val history = state.value.history) {
        is LCEState.None, is LCEState.Loading -> {
            CmpBox(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                SupportChatLoadingSkeleton(
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = columnMaxWidth)
                        .padding(horizontal = contentPadding)
                )
            }
        }

        is LCEState.Error -> {
            SupportChatErrorState(
                error = history.error,
                onRetry = actions.onHistoryRetry,
                colors = colors,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = columnMaxWidth)
            )
        }

        is LCEState.Content -> {
            SupportChatMessageList(
                state = state,
                colors = colors,
                columnMaxWidth = columnMaxWidth,
                contentPadding = contentPadding,
                actions = actions,
                onImageClick = onImageClick,
                onDocumentClick = onDocumentClick,
                scrollToNewestRequests = scrollToNewestRequests
            )
        }
    }
}

@Suppress("ktlint:compose:mutable-state-param-check")
@Composable
private fun SupportChatMessageList(
    state: State<SupportChatScreenState>,
    colors: CmpThemeColors,
    columnMaxWidth: Dp,
    contentPadding: Dp,
    actions: SupportChatActions,
    onImageClick: (String) -> Unit,
    onDocumentClick: (SupportChatUiAttachment) -> Unit,
    scrollToNewestRequests: MutableIntState
) {
    val listState = rememberLazyListState()
    val dateLabeler = rememberCmpDateLabeler()
    // Узкое чтение state: вложения композера, пагинация, связь ленту не перерисовывают. Набор и скачивание
    // читаются ниже — в построении списка и в элементе, а не в теле функции.
    val messages by remember(state) { derivedStateOf { state.value.messages } }
    val typingAuthor by remember(state) { derivedStateOf { state.value.typingAuthor } }
    val downloadingId by remember(state) { derivedStateOf { state.value.downloadingAttachmentId } }
    // Список мемоизируется только по сообщениям и меткам: индикатор набора —
    // отдельный item (index 0 при reverseLayout) и ленту не инвалидирует.
    val listItems = remember(messages, dateLabeler) {
        buildListItems(
            messages = messages,
            dateLabeler = dateLabeler,
            timeZone = TimeZone.currentSystemDefault()
        )
    }

    SupportChatFollowNewestEffect(
        listState = listState,
        newestKey = listItems.firstOrNull()?.key,
        scrollToNewestRequests = scrollToNewestRequests
    )
    SupportChatPaginationEffect(listState = listState, state = state, onLoadMore = actions.onLoadMore)
    SupportChatVisibilityEffect(listState = listState, onMessagesVisible = actions.onMessagesVisible)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        reverseLayout = true,
        contentPadding = PaddingValues(
            start = contentPadding,
            end = contentPadding,
            top = 8.dp,
            bottom = 16.dp
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (listItems.isEmpty()) {
            item {
                SupportChatEmptyState(
                    colors = colors,
                    modifier = Modifier
                        .fillParentMaxSize()
                        .widthIn(max = columnMaxWidth)
                )
            }
        } else {
            typingAuthor?.let { author ->
                item(key = "$TYPING_KEY_PREFIX$author") {
                    SupportChatTypingIndicator(
                        author = author,
                        colors = colors,
                        modifier = Modifier.widthIn(max = columnMaxWidth)
                    )
                }
            }
            items(
                count = listItems.size,
                key = { index -> listItems[index].key }
            ) { index ->
                when (val item = listItems[index]) {
                    is SupportChatListItem.DateDivider -> {
                        SupportChatDateDivider(
                            label = item.label,
                            colors = colors
                        )
                    }

                    is SupportChatListItem.Message -> {
                        SupportChatMessageBubble(
                            message = item.message,
                            colors = colors,
                            modifier = Modifier.widthIn(max = columnMaxWidth),
                            isDownloading = downloadingId != null &&
                                downloadingId == item.message.attachment?.id,
                            onRetry = actions.onRetry,
                            onDelete = actions.onDeleteFailed,
                            onImageClick = onImageClick,
                            onDocumentClick = onDocumentClick,
                            onDownloadAttachment = actions.onDownloadAttachment
                        )
                    }
                }
            }
        }
    }
}

/**
 * Новое сообщение внизу ленты. `LazyColumn` держит позицию по первому видимому
 * ключу, поэтому вставка в индекс 0 (reverseLayout) уходит под нижний край.
 *
 * - Своя отправка ([scrollToNewestRequests] растёт по кнопке «Отправить») — всегда
 *   возвращаем к последним сообщениям, где бы ни был пользователь. Триггер —
 *   счётчик, а не ключ: серверное эхо меняет ключ и отменило бы начатую прокрутку.
 * - Входящее — только если пользователь был у последних сообщений (порог 2: прежний
 *   нижний элемент после вставки сдвигается на индекс 1, при индикаторе набора — на 2);
 *   читающего историю не дёргаем.
 *
 * Анимация мягкая: издалека сначала мгновенный переход на несколько элементов от низа,
 * затем плавно до конца — без долгого пролёта через всю историю.
 */
@Suppress("ktlint:compose:mutable-state-param-check")
@Composable
private fun SupportChatFollowNewestEffect(
    listState: LazyListState,
    newestKey: String?,
    scrollToNewestRequests: MutableIntState
) {
    val sendRequests = scrollToNewestRequests.intValue
    LaunchedEffect(sendRequests) {
        if (sendRequests > 0) listState.softScrollToNewest()
    }
    LaunchedEffect(newestKey) {
        if (newestKey != null && listState.firstVisibleItemIndex <= FOLLOW_NEWEST_THRESHOLD) {
            listState.softScrollToNewest()
        }
    }
}

private suspend fun LazyListState.softScrollToNewest() {
    if (firstVisibleItemIndex > SMOOTH_SCROLL_DISTANCE) scrollToItem(SMOOTH_SCROLL_DISTANCE)
    animateScrollToItem(0)
}

/**
 * Пагинация: reverseLayout — «верх истории» это конец списка. Снапшот включает
 * условия загрузки, а не только `canScrollForward`: если страница не заполнила
 * экран, триггер сработает снова, когда изменится state (новая страница,
 * `isLoadingMore → false`), без ожидания скролла пользователя.
 *
 * После ошибки автоповтора нет — только когда пользователь заново доскроллил
 * до конца истории (переход `atHistoryEnd: false → true`).
 */
@Composable
private fun SupportChatPaginationEffect(
    listState: LazyListState,
    state: State<SupportChatScreenState>,
    onLoadMore: () -> Unit
) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(listState, state) {
        var wasAtHistoryEnd = false
        snapshotFlow {
            val current = state.value
            PaginationSnapshot(
                atHistoryEnd = !listState.canScrollForward,
                canLoad = current.isInputEnabled && current.hasMoreHistory && !current.isLoadingMore,
                isFailed = current.isHistoryPagingFailed,
                messageCount = current.messages.size
            )
        }
            .distinctUntilChanged()
            .collect { snapshot ->
                val userReachedEnd = snapshot.atHistoryEnd && !wasAtHistoryEnd
                wasAtHistoryEnd = snapshot.atHistoryEnd
                val mayRetry = !snapshot.isFailed || userReachedEnd
                if (snapshot.atHistoryEnd && snapshot.canLoad && mayRetry) {
                    currentOnLoadMore()
                }
            }
    }
}

private data class PaginationSnapshot(
    val atHistoryEnd: Boolean,
    val canLoad: Boolean,
    val isFailed: Boolean,
    val messageCount: Int
)

/**
 * Видимые сообщения — пачкой во VM (батч + debounce read-отметок там). Снапшот —
 * дешёвая пара индексов, а не список ключей на каждый кадр скролла; ключи
 * собираются только при смене видимого диапазона и фильтруются по префиксу
 * сообщений (разделители дат и индикатор набора — не сообщения).
 */
@Composable
private fun SupportChatVisibilityEffect(listState: LazyListState, onMessagesVisible: (List<String>) -> Unit) {
    val currentOnVisible by rememberUpdatedState(onMessagesVisible)
    LaunchedEffect(listState) {
        snapshotFlow {
            val visible = listState.layoutInfo.visibleItemsInfo
            (visible.firstOrNull()?.index ?: -1) to (visible.lastOrNull()?.index ?: -1)
        }
            .distinctUntilChanged()
            .collect {
                val messageIds = listState.layoutInfo.visibleItemsInfo.mapNotNull { info ->
                    (info.key as? String)
                        ?.takeIf { it.startsWith(MESSAGE_KEY_PREFIX) }
                        ?.removePrefix(MESSAGE_KEY_PREFIX)
                }
                if (messageIds.isNotEmpty()) currentOnVisible(messageIds)
            }
    }
}

internal sealed interface SupportChatListItem {
    val key: String

    data class DateDivider(val date: LocalDate, val label: String) : SupportChatListItem {
        override val key: String get() = "$DATE_KEY_PREFIX$date"
    }

    data class Message(val message: SupportChatUiMessage) : SupportChatListItem {
        override val key: String get() = "$MESSAGE_KEY_PREFIX${message.id}"
    }
}

/**
 * Элементы списка в отображаемом порядке (newest-first; экран использует
 * `reverseLayout = true`, поэтому index 0 — внизу, индикатор набора — первым).
 *
 * Порядок `messages` держит `SupportChatTimeline`: здесь только разворот и
 * перевод времени в пояс устройства, без сортировки на каждом построении.
 */
internal fun buildListItems(
    messages: List<SupportChatMessage>,
    dateLabeler: (LocalDate) -> String,
    timeZone: TimeZone
): ImmutableList<SupportChatListItem> {
    val result = mutableListOf<SupportChatListItem>()

    var groupDate: LocalDate? = null
    messages.asReversed().forEach { message ->
        val localTime = message.time.toLocalDateTime(timeZone)
        val date = localTime.date
        val closedGroupDate = groupDate
        if (closedGroupDate != null && date != closedGroupDate) {
            // Граница групп: разделитель закрывает более новую группу сверху.
            result.add(SupportChatListItem.DateDivider(date = closedGroupDate, label = dateLabeler(closedGroupDate)))
        }
        result.add(SupportChatListItem.Message(message.toUiMessage(localTime)))
        groupDate = date
    }
    groupDate?.let {
        result.add(SupportChatListItem.DateDivider(date = it, label = dateLabeler(it)))
    }

    return result.toPersistentList()
}

private enum class SupportChatLayoutMode {
    Compact,
    Medium,
    Expanded
}

private const val FOLLOW_NEWEST_THRESHOLD = 2
private const val SMOOTH_SCROLL_DISTANCE = 6
private const val DATE_KEY_PREFIX = "date_"
private const val MESSAGE_KEY_PREFIX = "msg_"
private const val TYPING_KEY_PREFIX = "typing_"
