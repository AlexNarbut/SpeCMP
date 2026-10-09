package ru.alarmtrade.specmp.feature.supportchat.presentation.model

import androidx.compose.runtime.Immutable

/**
 * Интенты экрана чата в одном объекте: сигнатуры Screen/Scaffold/Body не растут
 * на каждый новый интент. Создаётся один раз в entry-точке (`remember(viewModel)`).
 */
@Immutable
data class SupportChatActions(
    val onBack: () -> Unit,
    val onAttachFile: () -> Unit,
    val onAttachPhoto: () -> Unit,
    val onRemoveAttachment: (String) -> Unit,
    val onSend: (String) -> Unit,
    val onRetry: (String) -> Unit,
    val onDeleteFailed: (String) -> Unit,
    val onHistoryRetry: () -> Unit,
    val onLoadMore: () -> Unit,
    val onMessagesVisible: (List<String>) -> Unit,
    val onDownloadAttachment: (SupportChatUiAttachment) -> Unit,
    val onOpenAttachment: (SupportChatUiAttachment) -> Unit,
)
