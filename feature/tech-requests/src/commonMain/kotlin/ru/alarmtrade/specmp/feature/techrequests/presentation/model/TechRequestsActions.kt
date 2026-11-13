package ru.alarmtrade.specmp.feature.techrequests.presentation.model

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.RequestAction

@Immutable
internal data class TechRequestsActions(
    val onBack: () -> Unit,
    val onConnectDevice: () -> Unit,
    val onOpenChat: () -> Unit,
    val onRetry: () -> Unit,
    val onLoadMore: () -> Unit,
    val onRetryMore: () -> Unit,
    val onCreate: () -> Unit,
    val onRequestAction: (RequestAction, Long) -> Unit,
    val onConfirmAction: () -> Unit,
    val onDismissDialog: () -> Unit,
    val form: TechRequestFormActions,
)

@Immutable
internal data class TechRequestFormActions(
    val onNameChange: (String) -> Unit,
    val onPhoneChange: (String) -> Unit,
    val onIssueChange: (String) -> Unit,
    val onSubmit: () -> Unit,
    val onCancelSending: () -> Unit,
    val onSentDone: () -> Unit,
    val onClose: () -> Unit,
    val onDiscardDraft: () -> Unit,
    val onToList: () -> Unit,
)
