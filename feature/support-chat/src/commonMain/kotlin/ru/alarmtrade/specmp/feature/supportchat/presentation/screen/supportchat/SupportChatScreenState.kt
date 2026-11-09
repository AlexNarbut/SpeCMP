package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryCursor
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.timeline.SupportChatTimeline
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatConnection
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatConnectionBanner
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatError
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiMessage

/**
 * История и соединение независимы: offline с полной лентой (баннер) или online с ошибкой истории (полный экран).
 * `history = Error` — только когда показывать нечего; при живой ленте ошибка уходит тостом.
 */
@Immutable
data class SupportChatScreenState(
    val history: LCEState<SupportChatTimeline, SupportChatError> = LCEState.None,
    // Первая страница истории получена: пока false, подгрузка — это первичная загрузка.
    val isInitialHistoryLoaded: Boolean = false,
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
    val isChatAuthFailed: Boolean = false,

    val typingAuthor: String? = null,
    val pendingAttachments: ImmutableList<SupportChatUiAttachment> = persistentListOf(),
    val downloadingAttachmentId: String? = null,

    // Служебное VM: сессия собирается (иначе «Повторить» начинает новую), идёт загрузка любой страницы
    // истории (вторая не запускается), учёт прочтений.
    val isSessionActive: Boolean = false,
    val isLoadingHistory: Boolean = false,
    val readIds: SupportChatReadIds = SupportChatReadIds(),
) {
    val timeline: SupportChatTimeline
        get() = (history as? LCEState.Content)?.content ?: SupportChatTimeline.Empty

    val messages: List<SupportChatMessage>
        get() = timeline.messages

    val connectionBanner: SupportChatConnectionBanner?
        get() = when {
            connection == SupportChatConnection.Online -> null

            isChatAuthFailed -> SupportChatConnectionBanner.AuthFailed

            hasConnectionIssue || connection == SupportChatConnection.Offline ->
                SupportChatConnectionBanner.Reconnecting

            else -> SupportChatConnectionBanner.Connecting
        }

    /** Единственный источник «можно ли отправлять»: ввод только в Online. */
    val isInputEnabled: Boolean
        get() = connection == SupportChatConnection.Online
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

// Префиксы ключей элементов ленты (LazyColumn key); MESSAGE_KEY_PREFIX разбирает и экран — id из ключа.
private const val DATE_KEY_PREFIX = "date_"
internal const val MESSAGE_KEY_PREFIX = "msg_"
