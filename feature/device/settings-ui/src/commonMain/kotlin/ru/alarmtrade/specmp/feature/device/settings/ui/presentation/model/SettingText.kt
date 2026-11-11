package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model

import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel

internal fun settingTitle(name: String, channel: Channel?): String =
    settingTitleParts(name, channel).joinToString("") { it.text }

internal fun withUnits(number: String, units: String?): String = if (units.isNullOrBlank()) number else "$number $units"

internal fun <T> edgeText(value: T, min: T, max: T, minText: String?, maxText: String?): String? = when (value) {
    min -> minText
    max -> maxText
    else -> null
}
