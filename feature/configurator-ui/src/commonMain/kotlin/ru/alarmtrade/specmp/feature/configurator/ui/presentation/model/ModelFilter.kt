package ru.alarmtrade.specmp.feature.configurator.ui.presentation.model

internal fun <T> List<T>.modelOptions(deviceName: (T) -> String): List<String> =
    map(deviceName).distinct().sortedBy { it.lowercase() }

internal fun matchesModel(deviceName: String, filter: String?): Boolean =
    filter == null || deviceName.equals(filter, ignoreCase = true)
