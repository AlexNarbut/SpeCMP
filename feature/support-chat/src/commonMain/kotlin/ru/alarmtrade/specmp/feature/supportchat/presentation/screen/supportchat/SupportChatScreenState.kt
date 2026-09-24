package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment

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
    // ImmutableList: стабильность для композера (U-12).
    val history: LCEState<ImmutableList<SupportChatMessage>> = LCEState.None(),
    val isLoadingMore: Boolean = false,
    val hasMoreHistory: Boolean = true,

    // Соединение — измерение, независимое от истории.
    val isOffline: Boolean = false,

    val typingAuthor: String? = null,
    // Вложения композера — сразу в presentation-модели, маппинг на границе VM.
    val pendingAttachments: ImmutableList<SupportChatUiAttachment> = persistentListOf(),
    val downloadingAttachmentId: String? = null,
) {
    /**
     * Сообщения текущей ленты (пусто в None/Loading/Error).
     */
    val messages: List<SupportChatMessage>
        get() = (history as? LCEState.Content)?.content ?: emptyList()
}
