package ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.techrequests.domain.list.TechRequestList
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestDraft
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestField
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsError

/**
 * [device] — подключённый блок в рабочем режиме: без него заявку не создать (SDK читает его настройки).
 * [formOpen] — форма открыта на узком экране; на широком она всегда в панели справа.
 */
@Immutable
internal data class TechRequestsState(
    val requests: LCEState<TechRequestList, TechRequestsError> = LCEState.Loading,
    val hasMore: Boolean = false,
    val loadingMore: Boolean = false,
    val moreError: TechRequestsError? = null,
    val device: AttachedDevice? = null,
    val language: AppLocale = AppLocale.English,
    val form: TechRequestForm = TechRequestForm(),
    val formOpen: Boolean = false,
    val sending: TechRequestSending = TechRequestSending.Idle,
    val highlightedId: Long? = null,
    val pendingActionId: Long? = null,
    val pendingAction: RequestAction? = null,
    val dialog: TechRequestsDialog? = null,
    val prefill: ContactPrefill = ContactPrefill(),
    /** Откуда открыта заявка (плашка над полями); сбрасывается вместе с формой. */
    val origin: String? = null,
) {
    val canCreate: Boolean get() = device != null
    val canLoadMore: Boolean get() = requests is LCEState.Content && hasMore && !loadingMore && moreError == null
}

/** Модель и прошивка блока для блока «Что отправим». */
@Immutable
internal data class AttachedDevice(val model: String, val firmware: String)

/** Заявка из другого сценария: форма открыта, описание начинается с текста ошибки. */
@Immutable
internal data class DraftPrefill(val origin: String?, val text: String)

@Immutable
internal data class ContactPrefill(val name: String = "", val phoneDigits: String = "")

/** [errors] показываются только после первой попытки отправить ([attempted]), дальше обновляются при вводе. */
@Immutable
internal data class TechRequestForm(
    val name: String = "",
    val phoneDigits: String = "",
    val issue: String = "",
    val attempted: Boolean = false,
    val errors: Set<TechRequestField> = emptySet(),
) {
    val draft: TechRequestDraft get() = TechRequestDraft(name, phoneDigits, issue)
    val hasDraft: Boolean get() = issue.isNotBlank()
}

internal enum class SubmitStage { ReadingDevice, Sending }

@Immutable
internal sealed interface TechRequestSending {
    data object Idle : TechRequestSending

    data class InProgress(val stage: SubmitStage) : TechRequestSending

    data class Sent(val id: Long, val phoneDigits: String) : TechRequestSending

    data class Failed(val error: TechRequestsError) : TechRequestSending
}

internal enum class RequestAction { Cancel, Close }

@Immutable
internal sealed interface TechRequestsDialog {
    data class Confirm(val action: RequestAction, val id: Long) : TechRequestsDialog

    data object DiscardDraft : TechRequestsDialog

    data object NoDevice : TechRequestsDialog
}

internal fun DraftPrefill?.initialState(language: AppLocale): TechRequestsState = TechRequestsState(
    language = language,
    form = TechRequestForm(issue = this?.text?.takeIf { it.isNotBlank() }?.let { "$it " }.orEmpty()),
    formOpen = this != null,
    origin = this?.origin,
)
