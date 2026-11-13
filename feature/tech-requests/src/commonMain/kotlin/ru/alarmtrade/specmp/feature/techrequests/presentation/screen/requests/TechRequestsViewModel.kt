package ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.PlatformError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.phone.PhoneNumbers
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.account.domain.repository.AccountRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestDraft
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestStatus
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestSubmitStep
import ru.alarmtrade.specmp.feature.techrequests.domain.repository.TechRequestsRepository
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsError

/**
 * Список заявок и форма новой. Список и отмена/закрытие от блока не зависят (вход и с главного экрана);
 * отправка читает подключённый блок — `launchOnDevice`, обрыв связи её отменяет.
 */
internal class TechRequestsViewModel(
    deviceSession: DeviceSession,
    private val repository: TechRequestsRepository,
    private val accountRepository: AccountRepository,
    private val dispatchers: AppDispatchers,
    localeStore: AppLocaleStore,
) : BaseDeviceViewModel<TechRequestsState, TechRequestsEffect>(
    deviceSession,
    TechRequestsState(language = localeStore.current()),
) {

    override suspend fun onStart() {
        launchReplacing(UPDATES) { collectUpdates() }
        connectedDevice
            .map { device ->
                device?.takeIf { it.mode == DeviceOperationMode.MAIN }
                    ?.let { AttachedDevice(it.systemName, it.displayFirmwareVersion) }
            }
            .distinctUntilChanged()
            .collectIntoState { device -> copy(device = device) }
        // Отправку уже отменил launchOnDevice; без этого финала экран застрял бы в «Отправляем…».
        onDeviceLost {
            reduce {
                if (sending is TechRequestSending.InProgress) {
                    copy(
                        sending = TechRequestSending.Failed(TechRequestsError.Common(PlatformError.DeviceDisconnected)),
                    )
                } else {
                    this
                }
            }
        }
        loadFirstPage()
        launchReplacing(PREFILL) { loadPrefill() }
    }

    fun onRetry() {
        intent {
            reduce { copy(requests = LCEState.Loading, moreError = null) }
            loadFirstPage()
        }
    }

    fun onLoadMore() {
        intent {
            if (!currentState.canLoadMore) return@intent
            reduce { copy(loadingMore = true) }
            launchReplacing(PAGE) { loadNextPage() }
        }
    }

    fun onRetryMore() {
        intent {
            if (currentState.moreError == null || currentState.loadingMore) return@intent
            reduce { copy(moreError = null, loadingMore = true) }
            launchReplacing(PAGE) { loadNextPage() }
        }
    }

    fun onCreateClick() {
        intent {
            reduce { if (canCreate) copy(formOpen = true) else copy(dialog = TechRequestsDialog.NoDevice) }
        }
    }

    fun onNameChange(value: String) {
        reduce { withForm { copy(name = value) } }
    }

    fun onPhoneChange(digits: String) {
        reduce { withForm { copy(phoneDigits = digits) } }
    }

    fun onIssueChange(value: String) {
        reduce { withForm { copy(issue = value.take(TechRequestDraft.ISSUE_MAX_LENGTH)) } }
    }

    fun onSubmit() {
        intent {
            if (currentState.sending is TechRequestSending.InProgress) return@intent
            reduce { withForm { copy(attempted = true) } }
            if (currentState.form.errors.isNotEmpty()) return@intent
            if (!currentState.canCreate) {
                reduce { copy(dialog = TechRequestsDialog.NoDevice) }
                return@intent
            }
            val draft = currentState.form.draft
            reduce { copy(sending = TechRequestSending.InProgress(SubmitStage.ReadingDevice)) }
            launchOnDevice(SUBMIT) { submit(draft) } ?: reduce {
                copy(sending = TechRequestSending.Failed(TechRequestsError.Common(PlatformError.DeviceDisconnected)))
            }
        }
    }

    /** Заявка могла уже уйти: перечитываем список, чтобы показать фактическое. */
    fun onCancelSending() {
        intent {
            if (currentState.sending !is TechRequestSending.InProgress) return@intent
            cancelLaunched(SUBMIT)
            reduce { copy(sending = TechRequestSending.Idle) }
            postSideEffect(TechRequestsEffect.SendingStopped)
            loadFirstPage()
        }
    }

    fun onSentDone() {
        reduce { withClearedForm() }
    }

    fun onCloseForm() {
        intent {
            when {
                currentState.sending is TechRequestSending.InProgress -> Unit
                currentState.sending is TechRequestSending.Sent -> reduce { withClearedForm() }
                currentState.form.hasDraft -> reduce { copy(dialog = TechRequestsDialog.DiscardDraft) }
                else -> reduce { copy(formOpen = false) }
            }
        }
    }

    fun onDiscardDraft() {
        reduce { withClearedForm().copy(dialog = null) }
    }

    /** Лимит незакрытых заявок: к списку, черновик остаётся. */
    fun onToList() {
        reduce { copy(formOpen = false, sending = TechRequestSending.Idle) }
    }

    fun onRequestAction(action: RequestAction, id: Long) {
        reduce { copy(dialog = TechRequestsDialog.Confirm(action, id)) }
    }

    fun onConfirmAction() {
        intent {
            val confirm = currentState.dialog as? TechRequestsDialog.Confirm ?: return@intent
            if (currentState.pendingActionId != null) return@intent
            reduce { copy(dialog = null, pendingActionId = confirm.id) }
            val response = withContext(dispatchers.io) {
                when (confirm.action) {
                    RequestAction.Cancel -> repository.cancel(confirm.id)
                    RequestAction.Close -> repository.close(confirm.id)
                }
            }
            when (response) {
                is Response.Success -> {
                    reduce { copy(pendingActionId = null).withStatus(confirm.id, confirm.action.resultStatus) }
                    postSideEffect(TechRequestsEffect.ActionDone(confirm.action))
                }

                is Response.Error -> {
                    reduce { copy(pendingActionId = null) }
                    postSideEffect(
                        TechRequestsEffect.ActionFailed(confirm.action, TechRequestsError.from(response.exception)),
                    )
                }
            }
        }
    }

    fun onDismissDialog() {
        reduce { copy(dialog = null) }
    }

    private suspend fun collectUpdates() {
        repository.updates().flowOn(dispatchers.io).collect { response ->
            when (response) {
                is Response.Success -> reduce { withUpdates(response.value) }
                is Response.Error -> logger.w(response.exception) { "Tech request update failed" }
            }
        }
    }

    private fun loadFirstPage() {
        launchReplacing(PAGE) {
            val response = withContext(dispatchers.io) { repository.loadFirstPage() }
            reduce {
                when (response) {
                    is Response.Success -> withFirstPage(response.value)
                    is Response.Error -> withPageError(TechRequestsError.from(response.exception))
                }
            }
        }
    }

    private suspend fun loadNextPage() {
        val response = withContext(dispatchers.io) { repository.loadNextPage() }
        reduce {
            when (response) {
                is Response.Success -> withNextPage(response.value)
                is Response.Error -> withPageError(TechRequestsError.from(response.exception))
            }
        }
    }

    private suspend fun loadPrefill() {
        when (val profile = withContext(dispatchers.io) { accountRepository.currentProfile() }) {
            is Response.Success -> reduce {
                withPrefill(ContactPrefill(profile.value.name, PhoneNumbers.fromRaw(profile.value.phone.orEmpty())))
            }

            is Response.Error -> logger.w(profile.exception) { "Profile for the tech request form is unavailable" }
        }
    }

    private suspend fun submit(draft: TechRequestDraft) {
        repository.submit(draft).flowOn(dispatchers.io).collect { response ->
            when (response) {
                is Response.Success -> onSubmitStep(response.value, draft)

                is Response.Error -> reduce {
                    copy(sending = TechRequestSending.Failed(TechRequestsError.from(response.exception)))
                }
            }
        }
        // Поток кончился без финала — не оставляем экран в «Отправляем…».
        reduce {
            if (sending is TechRequestSending.InProgress) {
                copy(sending = TechRequestSending.Failed(TechRequestsError.Unknown))
            } else {
                this
            }
        }
    }

    private fun onSubmitStep(step: TechRequestSubmitStep, draft: TechRequestDraft) {
        when (step) {
            TechRequestSubmitStep.ReadingDevice -> reduce {
                copy(sending = TechRequestSending.InProgress(SubmitStage.ReadingDevice))
            }

            TechRequestSubmitStep.Sending -> reduce {
                copy(sending = TechRequestSending.InProgress(SubmitStage.Sending))
            }

            is TechRequestSubmitStep.Created -> {
                reduce { withCreated(step.request, draft.phoneDigits) }
                highlight(step.request.id)
            }
        }
    }

    private fun highlight(id: Long) {
        launchReplacing(HIGHLIGHT) {
            delay(HIGHLIGHT_MILLIS)
            reduce { if (highlightedId == id) copy(highlightedId = null) else this }
        }
    }

    private val RequestAction.resultStatus: TechRequestStatus
        get() = when (this) {
            RequestAction.Cancel -> TechRequestStatus.CANCELED_BY_USER
            RequestAction.Close -> TechRequestStatus.FINISHED_BY_USER
        }

    private companion object {
        const val UPDATES = "updates"
        const val PAGE = "page"
        const val PREFILL = "prefill"
        const val SUBMIT = "submit"
        const val HIGHLIGHT = "highlight"
        const val HIGHLIGHT_MILLIS = 4_000L
    }
}
