package ru.alarmtrade.specmp.feature.techrequests.presentation.model

/** `testTag` для ui-test. */
internal object TechRequestsTags {
    const val CREATE = "techrequests_create"
    const val CONNECT = "techrequests_connect"
    const val RETRY = "techrequests_error_retry"
    const val CONFIRM_YES = "techrequests_confirm_yes"
    const val CONFIRM_NO = "techrequests_confirm_no"
    const val FORM_NAME = "techrequests_form_name"
    const val FORM_PHONE = "techrequests_form_phone"
    const val FORM_ISSUE = "techrequests_form_issue"
    const val FORM_SUBMIT = "techrequests_form_submit"
    const val FORM_CANCEL = "techrequests_form_cancel"
    const val SENT_OK = "techrequests_sent_ok"

    fun item(id: Long): String = "techrequests_item_$id"

    fun itemClose(id: Long): String = "techrequests_item_close_$id"

    fun itemCancel(id: Long): String = "techrequests_item_cancel_$id"
}
