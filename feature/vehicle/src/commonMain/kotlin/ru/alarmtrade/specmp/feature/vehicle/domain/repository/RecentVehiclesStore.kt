package ru.alarmtrade.specmp.feature.vehicle.domain.repository

import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog

/** Недавно открытые/выбранные автомобили — отдельно для каждого каталога, новые первыми. */
interface RecentVehiclesStore {
    fun recent(catalog: VehicleCatalog): List<CarSummary>

    /** Поднять [car] наверх (без дублей), хранится не больше [MAX_RECENT]. */
    fun push(catalog: VehicleCatalog, car: CarSummary)

    companion object {
        const val MAX_RECENT = 5
    }
}
