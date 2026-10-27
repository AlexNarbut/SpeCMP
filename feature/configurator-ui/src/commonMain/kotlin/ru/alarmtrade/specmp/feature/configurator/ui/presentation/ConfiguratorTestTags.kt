package ru.alarmtrade.specmp.feature.configurator.ui.presentation

/** testTag экранов конфигуратора для ui-test (`ui.sh`). */
internal object ConfiguratorTestTags {
    const val SEARCH = "configurator_search"
    const val MODEL_FILTER = "configurator_model_filter"
    const val CREATE = "configurator_create"
    const val CREATE_SCRATCH = "configurator_create_scratch"
    const val CREATE_CLOUD = "configurator_create_cloud"
    const val CREATE_FILE = "configurator_create_file"
    const val MENU_RENAME = "configurator_menu_rename"
    const val MENU_COPY = "configurator_menu_copy"
    const val MENU_SAVE_FILE = "configurator_menu_save_file"
    const val MENU_WRITE = "configurator_menu_write"
    const val MENU_DELETE = "configurator_menu_delete"
    const val DELETE_CONFIRM = "configurator_delete_confirm"
    const val OPEN = "configurator_open"
    const val RETRY = "configurator_retry"
    const val MODELS_SEARCH = "configurator_models_search"
    const val CLOUD_ORDER_BEST = "configurator_cloud_order_best"
    const val CLOUD_ORDER_NEW = "configurator_cloud_order_new"
    const val CARD_OPEN = "configurator_card_open"
    const val CARD_LOAD = "configurator_card_load"
    const val CARD_SAVE_MINE = "configurator_card_save_mine"
    const val CARD_WRITE = "configurator_card_write"
    const val CARD_FIRMWARE = "configurator_card_firmware"
    const val CARD_MANUAL = "configurator_card_manual"

    fun mine(id: Long) = "configurator_mine_$id"
    fun mineMenu(id: Long) = "configurator_mine_menu_$id"
    fun model(name: String) = "configurator_model_$name"
    fun cloud(id: Int) = "configurator_cloud_$id"
    fun attachment(index: Int) = "configurator_attachment_$index"
}
