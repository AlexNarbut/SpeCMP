package ru.alarmtrade.specmp.feature.device.settings.ui.presentation

import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.LoadSourceKind
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SaveTarget

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
    const val MENU_RESET = "settings_menu_item_reset"
    const val RESET_CONFIRM = "settings_reset_confirm"
    const val CAN_SINGLE_UPDATE = "settings_can_single_update"
    const val DIALOG_CLOSE = "settings_dialog_close"
    const val MENU_LOAD = "settings_menu_load"
    const val MENU_SAVE_TO = "settings_menu_item_save_to"
    const val MENU_SAVE_MINE = "settings_menu_item_save_mine"
    const val MENU_SAVE_FILE = "settings_menu_item_save_file"
    const val MENU_COPY = "settings_menu_item_copy"
    const val MENU_WRITE_DEVICE = "settings_menu_item_write_device"
    const val MISMATCH_CONTINUE = "settings_mismatch_continue"
    const val DIFF_LOAD = "settings_diff_load"
    const val DIFF_SEARCH = "settings_diff_placeholder_search"
    const val OVERWRITE_CONFIRM = "settings_overwrite_confirm"
    const val EXIT_SAVE = "settings_exit_save"

    fun row(id: Int) = "settings_row_$id"
    fun section(id: Int) = "settings_section_$id"
    fun tree(id: Int) = "settings_tree_$id"
    fun channelToggle(id: Int) = "settings_channel_toggle_$id"
    fun function(id: Int) = "settings_function_$id"
    fun loadSource(kind: LoadSourceKind) = "settings_load_${kind.name.lowercase()}"
    fun saveTarget(target: SaveTarget) = "settings_save_${target.name.lowercase()}"
    fun diffSection(id: Int) = "settings_diff_section_$id"
    fun diffRow(id: Int) = "settings_diff_row_$id"
}
