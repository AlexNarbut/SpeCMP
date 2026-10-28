package ru.alarmtrade.specmp.feature.configurator.ui.presentation.model

/** Варианты фильтра по модели блока: модели элементов списка без повторов, по алфавиту. */
internal fun <T> List<T>.modelOptions(deviceName: (T) -> String): List<String> =
    map(deviceName).distinct().sortedBy { it.lowercase() }

/** Подходит ли модель [deviceName] под фильтр [filter] (`null` — все модели). */
internal fun matchesModel(deviceName: String, filter: String?): Boolean =
    filter == null || deviceName.equals(filter, ignoreCase = true)
