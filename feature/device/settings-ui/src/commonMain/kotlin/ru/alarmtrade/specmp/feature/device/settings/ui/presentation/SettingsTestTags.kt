package ru.alarmtrade.specmp.feature.device.settings.ui.presentation

/** testTag экрана настроек для ui-test (`ui.sh`): id — `SettingNode.id` из меню устройства. */
internal object SettingsTestTags {
    const val WRITE = "settings_write"
    const val SEARCH_OPEN = "settings_search_open"
    const val SEARCH_FIELD = "settings_search_field"
    const val EDIT_DONE = "settings_edit_done"
    const val EDIT_RESET = "settings_edit_reset"
    const val EXIT_WRITE = "settings_exit_write"
    const val EXIT_DISCARD = "settings_exit_discard"
    const val TREE_RESIZE = "settings_tree_resize"
    const val CAN_CODE = "settings_can_code"
    const val CAN_FIND = "settings_can_find"
    const val CAN_PICK_VEHICLE = "settings_can_pick_vehicle"
    const val MENU = "settings_menu"
    const val MENU_RESET = "settings_menu_reset"
    const val RESET_CONFIRM = "settings_reset_confirm"
    const val CAN_SINGLE_UPDATE = "settings_can_single_update"
    const val DIALOG_CLOSE = "settings_dialog_close"

    fun row(id: Int) = "settings_row_$id"
    fun section(id: Int) = "settings_section_$id"
    fun tree(id: Int) = "settings_tree_$id"
    fun channelToggle(id: Int) = "settings_channel_toggle_$id"
    fun function(id: Int) = "settings_function_$id"
}
