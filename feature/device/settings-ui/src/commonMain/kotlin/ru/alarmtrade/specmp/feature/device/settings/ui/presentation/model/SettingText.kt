package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model

import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel

/** Имя настройки с каналом, если они различаются («IN1 · Концевик дверей»); без канала — имя. */
internal fun settingTitle(name: String, channel: Channel?): String = when {
    channel == null || channel.name.isBlank() || channel.name == name -> name
    name.isBlank() -> channel.name
    else -> "${channel.name} · $name"
}

/** Число с единицами измерения («30 с»); без единиц — число. */
internal fun withUnits(number: String, units: String?): String = if (units.isNullOrBlank()) number else "$number $units"

/** Подпись края диапазона («Выкл.», «Без ограничения»): значение на краю и подпись задана; иначе `null`. */
internal fun <T> edgeText(value: T, min: T, max: T, minText: String?, maxText: String?): String? = when (value) {
    min -> minText
    max -> maxText
    else -> null
}
