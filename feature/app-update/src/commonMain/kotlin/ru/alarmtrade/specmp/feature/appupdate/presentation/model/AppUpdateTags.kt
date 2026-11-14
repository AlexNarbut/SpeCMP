package ru.alarmtrade.specmp.feature.appupdate.presentation.model

/** `testTag` для ui-test. */
internal object AppUpdateTags {
    const val CURRENT = "appupdate_current"
    const val STAGE = "appupdate_stage"
    const val UPDATE = "appupdate_update"
    const val SAVE = "appupdate_save"
    const val LATER = "appupdate_later"
    const val STORE = "appupdate_store"
    const val PROGRESS = "appupdate_progress"
    const val CANCEL = "appupdate_cancel"
    const val INSTALL = "appupdate_install"
    const val RETRY = "appupdate_retry"
    const val NOTES = "appupdate_notes_toggle"
    const val CHANNELS = "appupdate_channels"
    const val DIALOG_CONFIRM = "appupdate_dialog_confirm"
    const val DIALOG_CANCEL = "appupdate_dialog_cancel"

    fun channel(id: Int): String = "appupdate_channel_$id"
}
