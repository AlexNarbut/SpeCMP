package ru.alarmtrade.specmp.feature.mountpassport.presentation.model

/** `testTag` для ui-test. */
internal object MountPassportTags {
    const val SEARCH = "mountpassport_search"
    const val NEW = "mountpassport_new"
    const val NEW_WITHOUT_UNIT = "mountpassport_new_without_unit"
    const val PICK_CAR = "mountpassport_pick_car"
    const val LIST = "mountpassport_list"
    const val RETRY = "mountpassport_retry"
    const val DELETE_CONFIRM = "mountpassport_delete_confirm"
    const val EDITOR = "mountpassport_editor"
    const val SAVE_STATUS = "mountpassport_save_status"
    const val PDF = "mountpassport_pdf"
    const val ADD_PHOTO = "mountpassport_add_photo"
    const val READ_UNIT = "mountpassport_read_unit"
    const val READ = "mountpassport_read"
    const val READ_CANCEL = "mountpassport_read_cancel"
    const val READ_RETRY = "mountpassport_read_retry"
    const val READ_CONTINUE = "mountpassport_read_continue"
    const val READ_SKIP = "mountpassport_read_skip"
    const val READY = "mountpassport_ready"
    const val READY_SHARE = "mountpassport_ready_share"
    const val READY_SAVE = "mountpassport_ready_save"
    const val READY_OPEN = "mountpassport_ready_open"
    const val READY_EDIT = "mountpassport_ready_edit"
    const val READY_MORE = "mountpassport_ready_more"
    const val READY_RETRY = "mountpassport_ready_retry"
    const val PREVIEW = "mountpassport_preview"
    const val LOOKUP = "mountpassport_lookup"
    const val LOOKUP_ID = "mountpassport_lookup_id"
    const val LOOKUP_FIND = "mountpassport_lookup_find"
    const val LOOKUP_SCAN = "mountpassport_lookup_scan"
    const val LOOKUP_MODEL = "mountpassport_lookup_model"
    const val LOOKUP_CUSTOM = "mountpassport_lookup_custom"
    const val LOOKUP_NEXT = "mountpassport_lookup_next"

    fun row(id: String): String = "mountpassport_row_$id"

    fun finish(id: String): String = "mountpassport_finish_$id"

    fun section(name: String): String = "mountpassport_section_$name"

    fun reread(stage: String): String = "mountpassport_reread_$stage"

    fun photo(index: Int): String = "mountpassport_photo_$index"

    fun readStep(step: String): String = "mountpassport_read_step_$step"

    fun page(index: Int): String = "mountpassport_page_$index"
}
