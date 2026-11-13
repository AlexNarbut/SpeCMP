package ru.alarmtrade.specmp.feature.productinfo.presentation.model

/** `testTag` для ui-test. */
internal object ProductInfoTags {
    const val QUERY = "productinfo_query"
    const val SEARCH = "productinfo_search"
    const val CLEAR = "productinfo_clear"
    const val PRODUCT = "productinfo_product"
    const val KIT = "productinfo_kit"
    const val CONNECTED = "productinfo_connected"
    const val EXPAND_ALL = "productinfo_expand_all"
    const val ERROR = "productinfo_error"
    const val RETRY = "productinfo_retry"

    fun component(index: Int): String = "productinfo_component_$index"
}
