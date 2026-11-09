package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select

import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCard

/** Первая непустая вкладка карточки (`null` — вкладок нет). */
internal fun CarCard.firstTab(): CardTab? = tabs().firstOrNull()

/** Непустые вкладки карточки в порядке макета. */
internal fun CarCard.tabs(): List<CardTab> = buildList {
    if (features.isNotEmpty()) add(CardTab.FEATURES)
    if (mountPoints.isNotEmpty()) add(CardTab.MOUNT_POINTS)
    if (schemes.isNotEmpty()) add(CardTab.SCHEMES)
    if (mountingMaps.isNotEmpty()) add(CardTab.MAPS)
    if (userManuals.isNotEmpty()) add(CardTab.USER_MANUALS)
}
