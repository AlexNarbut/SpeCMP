package ru.alarmtrade.specmp.feature.mountpassport.presentation.model

/** `testTag` для ui-test. */
internal object MountPassportTags {
    const val SEARCH = "mountpassport_search"
    const val NEW = "mountpassport_new"
    const val NEW_WITHOUT_UNIT = "mountpassport_new_without_unit"
    const val LIST = "mountpassport_list"
    const val RETRY = "mountpassport_retry"
    const val DELETE_CONFIRM = "mountpassport_delete_confirm"
    const val EDITOR = "mountpassport_editor"
    const val SAVE_STATUS = "mountpassport_save_status"
    const val PDF = "mountpassport_pdf"
    const val ADD_PHOTO = "mountpassport_add_photo"
    const val READ_UNIT = "mountpassport_read_unit"

    fun row(id: String): String = "mountpassport_row_$id"

    fun finish(id: String): String = "mountpassport_finish_$id"

    fun section(name: String): String = "mountpassport_section_$name"

    fun reread(stage: String): String = "mountpassport_reread_$stage"

    fun photo(index: Int): String = "mountpassport_photo_$index"
}
