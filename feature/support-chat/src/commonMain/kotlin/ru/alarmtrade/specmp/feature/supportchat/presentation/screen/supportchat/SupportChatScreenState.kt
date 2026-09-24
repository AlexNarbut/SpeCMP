package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPendingAttachment

/**
 * Состояние экрана чата.
 *
 * История и соединение — два независимых измерения (S-1): можно быть offline
 * с полной лентой (баннер сверху, лента под ним) и online с ошибкой истории
 * (полный экран ошибки, баннера нет).
 *
 * Ошибка первой загрузки → `history = Error` (полный экран с кнопкой «Повторить»);
 * ошибка последующей пагинации при живой ленте ленту не затирает — уходит тостом
 * (`SupportChatEffect.HistoryLoadFailed`). Payload ошибки UI не читает — причина
 * одна, экран рисует свой `stringResource`.
 */
@Immutable
data class SupportChatScreenState(
    // История: None/Loading → скелетон, Content → лента, Error → полный экран.
    val history: LCEState<List<SupportChatMessage>> = LCEState.None(),
    val isLoadingMore: Boolean = false,
    val hasMoreHistory: Boolean = true,

    // Соединение — измерение, независимое от истории.
    val isOffline: Boolean = false,

    val typingAuthor: String? = null,
    val draftText: String = "",
    val pendingAttachments: List<SupportChatPendingAttachment> = emptyList(),
    val downloadingAttachmentId: String? = null,
) {
    /**
     * Сообщения текущей ленты (пусто в None/Loading/Error).
     */
    val messages: List<SupportChatMessage>
        get() = (history as? LCEState.Content)?.content ?: emptyList()

    /**
     * Единый источник «можно отправлять»: композер гейтится только реальным
     * офлайном, загрузка истории отправку не блокирует.
     */
    val isComposerEnabled: Boolean
        get() = !isOffline && (draftText.isNotBlank() || pendingAttachments.isNotEmpty())
}
