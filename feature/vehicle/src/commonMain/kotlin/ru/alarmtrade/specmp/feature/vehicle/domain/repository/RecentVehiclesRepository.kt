package ru.alarmtrade.specmp.feature.vehicle.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog

/** Недавно открытые/выбранные автомобили — отдельно для каждого каталога, новые первыми. */
interface RecentVehiclesRepository {
    suspend fun recent(catalog: VehicleCatalog): Response<List<CarSummary>>

    /** Поднять [car] наверх (без дублей), хранится не больше [MAX_RECENT]; атомарно. Ответ — новый список. */
    suspend fun push(catalog: VehicleCatalog, car: CarSummary): Response<List<CarSummary>>

    /** Убрать автомобиль [carId] из недавних (нет в списке — ничего). */
    suspend fun remove(catalog: VehicleCatalog, carId: Int): Response<List<CarSummary>>

    companion object {
        const val MAX_RECENT = 5
    }
}
