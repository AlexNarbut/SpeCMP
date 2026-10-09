package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryCursor
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.timeline.SupportChatTimeline
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatConnection
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatConnectionBanner
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment

/**
 * Состояние экрана чата.
 *
 * История и соединение — два независимых измерения: можно быть offline с полной
 * лентой (баннер сверху, лента под ним) и online с ошибкой истории (полный экран
 * ошибки, баннера нет).
 *
 * `history = Error` возникает только когда показывать нечего; при живой ленте
 * (восстановленный outbox, live-сообщение) ошибка загрузки уходит тостом
 * (`SupportChatEffect.HistoryLoadFailed`).
 */
@Immutable
data class SupportChatScreenState(
    // None/Loading → скелетон, Content → лента, Error → полный экран.
    val history: LCEState<SupportChatTimeline> = LCEState.None(),
    // Первая страница истории получена: пока false, подгрузка — это первичная загрузка.
    val isInitialHistoryLoaded: Boolean = false,
    // Курсор следующей (более старой) страницы истории.
    val historyCursor: SupportChatHistoryCursor? = null,
    val isLoadingMore: Boolean = false,
    // Последняя загрузка страницы упала: автоповтор по триггеру пагинации
    // выключен (иначе цикл «ошибка → тост → повтор»), повтор — жестом пользователя.
    val isHistoryPagingFailed: Boolean = false,
    val hasMoreHistory: Boolean = true,

    val connection: SupportChatConnection = SupportChatConnection.Connecting,
    // Соединение уже терялось и ещё не восстановлено: плашка — «нет соединения», а не
    // «Подключение…» (переподключение, ожидание сети, повтор сессии).
    val hasConnectionIssue: Boolean = false,
    // Сервер отверг авторизацию: SDK подключится сам после повторного входа.
    val isChatAuthFailed: Boolean = false,

    val typingAuthor: String? = null,
    // Вложения композера — сразу в presentation-модели, маппинг на границе VM.
    val pendingAttachments: ImmutableList<SupportChatUiAttachment> = persistentListOf(),
    val downloadingAttachmentId: String? = null,
) {
    /** Лента текущего состояния; пустая в None/Loading/Error. */
    val timeline: SupportChatTimeline
        get() = (history as? LCEState.Content)?.content ?: SupportChatTimeline.Empty

    /** Сообщения ленты по возрастанию времени (инвариант держит [SupportChatTimeline]). */
    val messages: List<SupportChatMessage>
        get() = timeline.messages

    /** Что показывает плашка соединения (null — ничего). */
    val connectionBanner: SupportChatConnectionBanner?
        get() = when {
            connection == SupportChatConnection.Online -> null
            isChatAuthFailed -> SupportChatConnectionBanner.AuthFailed
            hasConnectionIssue || connection == SupportChatConnection.Offline -> SupportChatConnectionBanner.Reconnecting
            else -> SupportChatConnectionBanner.Connecting
        }

    /** Единственный источник «можно ли отправлять»: ввод только в Online. */
    val isInputEnabled: Boolean
        get() = connection == SupportChatConnection.Online
}
