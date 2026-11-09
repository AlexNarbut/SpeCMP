package ru.alarmtrade.specmp.feature.vehicle.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog

interface RecentVehiclesRepository {
    suspend fun recent(catalog: VehicleCatalog): Response<List<CarSummary>>

    /** Без дублей, не больше [MAX_RECENT]; атомарно. Ответ — новый список. */
    suspend fun push(catalog: VehicleCatalog, car: CarSummary): Response<List<CarSummary>>

    suspend fun remove(catalog: VehicleCatalog, carId: Int): Response<List<CarSummary>>

    companion object {
        const val MAX_RECENT = 5
    }
}
