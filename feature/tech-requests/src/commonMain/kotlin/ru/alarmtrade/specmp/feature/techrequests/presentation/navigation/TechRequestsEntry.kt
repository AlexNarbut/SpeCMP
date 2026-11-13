@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.feature.techrequests.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_error_cancel
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_error_close
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_msg_canceled
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_msg_closed
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_msg_sending_stopped
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestFormActions
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsActions
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.textOf
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.DraftPrefill
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.RequestAction
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.TechRequestsEffect
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.TechRequestsScreen
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.TechRequestsViewModel

/**
 * Экран заявок поверх текущего; [prefill] — открыть сразу «Новую заявку» с описанием (ошибка обхода, Clone…).
 * Из расширенного монтажа без предзаполнения — тот же [TechRequestsEntry] в слоте графа.
 */
@Serializable
@SerialName("tech-requests.TechRequestsRoute")
data class TechRequestsRoute(val prefill: TechRequestPrefill? = null) : NavKey

/** [origin] — откуда открыта заявка («Бесключевой обход»), [text] — текст ошибки для описания; оба уже локализованы. */
@Serializable
@SerialName("tech-requests.TechRequestPrefill")
data class TechRequestPrefill(val origin: String, val text: String)

/** Список заявок и форма новой; [onConnectDevice] — к выбору блока, когда его нет. */
@Composable
fun TechRequestsEntry(
    onBack: () -> Unit,
    onConnectDevice: () -> Unit,
    onOpenChat: () -> Unit,
    prefill: TechRequestPrefill? = null,
) {
    val viewModel = koinViewModel<TechRequestsViewModel> { parametersOf(prefill?.toDraftPrefill()) }
    val state = viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnConnect by rememberUpdatedState(onConnectDevice)
    val currentOnChat by rememberUpdatedState(onOpenChat)

    viewModel.sideEffect.collectSideEffect { effect ->
        scope.launch { snackbar.showSnackbar(message = effect.message()) }
    }

    val actions = remember(viewModel) {
        TechRequestsActions(
            onBack = { currentOnBack() },
            onConnectDevice = { currentOnConnect() },
            onOpenChat = { currentOnChat() },
            onRetry = viewModel::onRetry,
            onLoadMore = viewModel::onLoadMore,
            onRetryMore = viewModel::onRetryMore,
            onCreate = viewModel::onCreateClick,
            onRequestAction = viewModel::onRequestAction,
            onConfirmAction = viewModel::onConfirmAction,
            onDismissDialog = viewModel::onDismissDialog,
            form = TechRequestFormActions(
                onNameChange = viewModel::onNameChange,
                onPhoneChange = viewModel::onPhoneChange,
                onIssueChange = viewModel::onIssueChange,
                onSubmit = viewModel::onSubmit,
                onCancelSending = viewModel::onCancelSending,
                onSentDone = viewModel::onSentDone,
                onClose = viewModel::onCloseForm,
                onDiscardDraft = viewModel::onDiscardDraft,
                onToList = viewModel::onToList,
            ),
        )
    }

    TechRequestsScreen(state = state, actions = actions, snackbar = snackbar)
}

private suspend fun TechRequestsEffect.message(): String = when (this) {
    is TechRequestsEffect.ActionDone -> getString(
        when (action) {
            RequestAction.Cancel -> Strings.string.techrequests_list_msg_canceled
            RequestAction.Close -> Strings.string.techrequests_list_msg_closed
        },
    )

    is TechRequestsEffect.ActionFailed -> getString(
        when (action) {
            RequestAction.Cancel -> Strings.string.techrequests_list_error_cancel
            RequestAction.Close -> Strings.string.techrequests_list_error_close
        },
        error.textOf(),
    )

    TechRequestsEffect.SendingStopped -> getString(Strings.string.techrequests_list_msg_sending_stopped)
}

private fun TechRequestPrefill.toDraftPrefill(): DraftPrefill = DraftPrefill(origin, text)
