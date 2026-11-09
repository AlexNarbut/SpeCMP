package ru.alarmtrade.specmp.feature.vehicle.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCard
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarTrim
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CatalogQuery
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter

/** Сервер отдаёт уровни целиком (без страниц и текстового поиска); без кэша и повторов. */
interface VehicleCatalogRepository {
    suspend fun brands(query: CatalogQuery): Response<List<CarBrand>>

    suspend fun models(query: CatalogQuery, brandId: Int): Response<List<CarModel>>

    suspend fun trims(query: CatalogQuery, modelId: Int): Response<List<CarTrim>>

    /** Карточка комплектации; [groups] — фильтр функций (пустой — все). */
    suspend fun card(carId: Int, groups: Set<CarFeatureGroup> = emptySet()): Response<CarCard>

    suspend fun findByCanCode(
        canCode: String,
        device: VehicleDeviceFilter?,
        groups: Set<CarFeatureGroup> = emptySet(),
    ): Response<List<CarSummary>>

    suspend fun summary(carId: Int): Response<CarSummary>
}
