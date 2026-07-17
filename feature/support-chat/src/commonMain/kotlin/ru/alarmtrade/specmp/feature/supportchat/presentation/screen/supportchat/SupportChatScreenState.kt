package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPendingAttachment

@Immutable
data class SupportChatScreenState(
    val messages: List<SupportChatMessage> = emptyList(),
    val isLoadingHistory: Boolean = true,
    val hasMoreHistory: Boolean = true,
    val isOffline: Boolean = false,
    val typingAuthor: String? = null,
    val draftText: String = "",
    val pendingAttachments: List<SupportChatPendingAttachment> = emptyList(),
    val downloadingAttachmentId: String? = null,
) {
    val isComposerEnabled: Boolean
        get() = draftText.isNotBlank() || pendingAttachments.isNotEmpty()
}
