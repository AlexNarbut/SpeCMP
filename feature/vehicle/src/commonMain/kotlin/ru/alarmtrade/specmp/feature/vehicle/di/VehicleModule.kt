package ru.alarmtrade.specmp.feature.vehicle.di

import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.vehicle.data.preferences.SettingsRecentVehiclesStore
import ru.alarmtrade.specmp.feature.vehicle.data.repository.SpecialistVehicleCatalogRepository
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.RecentVehiclesStore
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.VehicleCatalogRepository

private const val VEHICLE_PREFERENCES = "vehicles"

/**
 * Koin-модуль логической фичи `feature/vehicle`: каталог автомобилей и недавние.
 * SDK-интеракторы берутся из `core/specialist`.
 */
val vehicleModule = module {
    single<VehicleCatalogRepository> {
        // Ленивые провайдеры: недоступный на платформе интерактор не должен ронять создание репозитория.
        SpecialistVehicleCatalogRepository(cars = { get() }, configurations = { get() }, deviceInformation = { get() })
    }
    single<RecentVehiclesStore> { SettingsRecentVehiclesStore(get<AppSettingsFactory>().create(VEHICLE_PREFERENCES)) }
}
