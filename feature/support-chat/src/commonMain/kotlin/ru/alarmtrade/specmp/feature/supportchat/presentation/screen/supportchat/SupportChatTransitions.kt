package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatConnectionState
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryPage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.domain.timeline.SupportChatTimeline
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatConnection
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatError

private const val LOCAL_ID_PREFIX = "local-"

internal fun SupportChatScreenState.withTimeline(timeline: SupportChatTimeline): SupportChatScreenState =
    copy(history = LCEState.Content(timeline))

internal fun SupportChatScreenState.withConnection(state: SupportChatConnectionState): SupportChatScreenState =
    when (state) {
        SupportChatConnectionState.Connecting -> copy(connection = SupportChatConnection.Connecting)

        SupportChatConnectionState.Ready -> copy(
            connection = SupportChatConnection.Online,
            hasConnectionIssue = false,
            isChatAuthFailed = false,
        )

        SupportChatConnectionState.Reconnecting,
        SupportChatConnectionState.WaitingForNetwork,
        SupportChatConnectionState.AuthFailed,
        -> copy(
            connection = SupportChatConnection.Offline,
            hasConnectionIssue = true,
            isChatAuthFailed = state == SupportChatConnectionState.AuthFailed,
        )
    }

internal fun SupportChatScreenState.withSessionUnavailable(): SupportChatScreenState = copy(
    connection = SupportChatConnection.Offline,
    hasConnectionIssue = true,
    history = if (history is LCEState.Content) history else LCEState.Error(SupportChatError.HistoryUnavailable),
)

/** Пока первая страница не получена — первичная загрузка (скелетон), дальше — тихое обновление. */
internal fun SupportChatScreenState.withFirstPageLoading(): SupportChatScreenState = copy(
    isLoadingMore = false,
    isLoadingHistory = true,
    // Лента (восстановленный outbox, live-сообщение) остаётся на экране.
    history = if (!isInitialHistoryLoaded && history !is LCEState.Content) LCEState.Loading else history,
)

internal fun SupportChatScreenState.withHistoryPage(
    page: SupportChatHistoryPage,
    isFirstPage: Boolean,
): SupportChatScreenState {
    // Повторная первая страница (после переподключения) пагинацию не сбрасывает:
    // курсор — номер страницы, новые сообщения сдвигают её только к дублям.
    val resetPaging = !isFirstPage || !isInitialHistoryLoaded
    return copy(
        isInitialHistoryLoaded = true,
        isLoadingMore = false,
        isLoadingHistory = false,
        isHistoryPagingFailed = false,
        historyCursor = if (resetPaging) page.nextCursor else historyCursor,
        hasMoreHistory = if (resetPaging) page.nextCursor != null else hasMoreHistory,
    )
}

/** При живой ленте ошибка уходит тостом, иначе — полноэкранная ошибка с «Повторить». */
internal fun SupportChatScreenState.withHistoryError(exception: Throwable): SupportChatScreenState = copy(
    isLoadingMore = false,
    isLoadingHistory = false,
    isHistoryPagingFailed = true,
    history = if (history is LCEState.Content) history else LCEState.Error(SupportChatError.from(exception)),
)

@OptIn(ExperimentalUuidApi::class)
internal fun optimisticMessage(
    timestamp: Instant,
    text: String? = null,
    attachment: SupportChatAttachment? = null,
): SupportChatMessage = SupportChatMessage(
    // Глобально уникален: переживает пересоздание VM и процесса (ключ outbox).
    id = "$LOCAL_ID_PREFIX${Uuid.random()}",
    serverId = null,
    text = text,
    attachment = attachment,
    time = timestamp,
    isMine = true,
    authorEmail = null,
    status = SupportChatMessageStatus.Sending,
)
