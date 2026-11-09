package ru.alarmtrade.specmp.feature.vehicle.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.vehicle.data.datasource.SpecialistVehicleCatalogDataSource
import ru.alarmtrade.specmp.feature.vehicle.data.datasource.VehicleCatalogDataSource
import ru.alarmtrade.specmp.feature.vehicle.data.repository.SettingsRecentVehiclesRepository
import ru.alarmtrade.specmp.feature.vehicle.data.repository.SpecialistVehicleCatalogRepository
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.RecentVehiclesRepository
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.VehicleCatalogRepository

private const val VEHICLE_PREFERENCES = "vehicles"

/**
 * Koin-модуль логической фичи `feature/vehicle`: каталог автомобилей и недавние.
 * SDK-интеракторы берутся из `core/specialist`.
 */
val vehicleModule = module {
    single<VehicleCatalogDataSource> {
        // Ленивые провайдеры: недоступный на платформе интерактор не должен ронять создание источника.
        SpecialistVehicleCatalogDataSource(cars = { get() }, configurations = { get() }, devices = { get() })
    }
    singleOf(::SpecialistVehicleCatalogRepository) bind VehicleCatalogRepository::class
    single<RecentVehiclesRepository> {
        SettingsRecentVehiclesRepository(get<AppSettingsFactory>().create(VEHICLE_PREFERENCES))
    }
}
