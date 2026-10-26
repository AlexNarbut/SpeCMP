package ru.alarmtrade.specmp.feature.vehicle.ui.presentation

/** `testTag` экрана выбора автомобиля для UI-тестов (id — id сервера). */
internal object VehicleTestTags {
    const val TYPE_CHIP = "vehicle_type_chip"
    const val FILTER = "vehicle_filter"
    const val FILTER_APPLY = "vehicle_filter_apply"
    const val FILTER_RESET = "vehicle_filter_reset"
    const val FILTER_CLEAR = "vehicle_filter_clear"
    const val SEARCH = "vehicle_search"
    const val CAN_INPUT = "vehicle_can_input"
    const val RETRY = "vehicle_retry"
    const val CARD = "vehicle_card"
    const val CARD_PICK = "vehicle_card_pick"
    const val CAN_COPY = "vehicle_can_copy"
    const val NOTES_TOGGLE = "vehicle_notes_toggle"
    const val PHOTO_FULL = "vehicle_photo_full"
    const val MISMATCH_CONFIRM = "vehicle_mismatch_confirm"

    fun type(type: String) = "vehicle_type_$type"
    fun mode(mode: String) = "vehicle_mode_$mode"
    fun filterGroup(group: String) = "vehicle_filter_group_$group"
    fun brand(id: Int) = "vehicle_brand_$id"
    fun model(id: Int) = "vehicle_model_$id"
    fun trim(id: Int) = "vehicle_trim_$id"
    fun trimPick(id: Int) = "vehicle_trim_pick_$id"
    fun canResult(id: Int) = "vehicle_can_result_$id"
    fun recent(id: Int) = "vehicle_recent_$id"
    fun capability(kind: String) = "vehicle_cap_$kind"
    fun tab(tab: String) = "vehicle_tab_$tab"
    fun point(index: Int) = "vehicle_point_$index"
}
