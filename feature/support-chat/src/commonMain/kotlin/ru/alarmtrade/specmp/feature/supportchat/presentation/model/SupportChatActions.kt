package ru.alarmtrade.specmp.feature.supportchat.presentation.model

import androidx.compose.runtime.Immutable

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
