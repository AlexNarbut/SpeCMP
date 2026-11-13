package ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests

import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.techrequests.domain.list.TechRequestList
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequest
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestPage
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestStatus
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsError

internal fun TechRequestsState.withFirstPage(page: TechRequestPage): TechRequestsState = copy(
    requests = LCEState.Content(TechRequestList.of(page.items)),
    hasMore = page.hasMore,
    loadingMore = false,
    moreError = null,
)

internal fun TechRequestsState.withNextPage(page: TechRequestPage): TechRequestsState = copy(
    requests = LCEState.Content(loaded().merge(page.items)),
    hasMore = page.hasMore,
    loadingMore = false,
    moreError = null,
)

/** Ошибка при показанном списке — строка «Повторить» внизу; без списка — полноэкранная ошибка. */
internal fun TechRequestsState.withPageError(error: TechRequestsError): TechRequestsState =
    if (requests is LCEState.Content) {
        copy(loadingMore = false, moreError = error)
    } else {
        copy(requests = LCEState.Error(error), loadingMore = false)
    }

/** Обновления до первой страницы не применяются: первая страница их и так содержит. */
internal fun TechRequestsState.withUpdates(portion: List<TechRequest>): TechRequestsState =
    if (requests is LCEState.Content) copy(requests = LCEState.Content(loaded().merge(portion))) else this

internal fun TechRequestsState.withStatus(id: Long, status: TechRequestStatus): TechRequestsState {
    val request = (requests as? LCEState.Content)?.content?.find(id) ?: return this
    return withUpdates(listOf(request.copy(status = status)))
}

internal fun TechRequestsState.withCreated(request: TechRequest, phoneDigits: String): TechRequestsState = copy(
    requests = LCEState.Content(loaded().merge(listOf(request))),
    sending = TechRequestSending.Sent(request.id, phoneDigits),
    highlightedId = request.id,
)

internal fun TechRequestsState.withForm(transform: TechRequestForm.() -> TechRequestForm): TechRequestsState {
    val edited = form.transform()
    return copy(form = if (edited.attempted) edited.copy(errors = edited.draft.fieldErrors()) else edited)
}

/** Контакт из профиля подставляется только в пустые поля: введённое пользователем не затирается. */
internal fun TechRequestsState.withPrefill(prefill: ContactPrefill): TechRequestsState = copy(
    prefill = prefill,
    form = form.copy(
        name = form.name.ifBlank { prefill.name },
        phoneDigits = form.phoneDigits.ifEmpty { prefill.phoneDigits },
    ),
)

internal fun TechRequestsState.withClearedForm(): TechRequestsState = copy(
    form = TechRequestForm(name = prefill.name, phoneDigits = prefill.phoneDigits),
    sending = TechRequestSending.Idle,
    formOpen = false,
    origin = null,
)

private fun TechRequestsState.loaded(): TechRequestList =
    (requests as? LCEState.Content)?.content ?: TechRequestList.Empty
