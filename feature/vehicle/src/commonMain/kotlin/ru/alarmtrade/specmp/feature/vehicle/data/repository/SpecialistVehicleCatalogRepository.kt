package ru.alarmtrade.specmp.feature.vehicle.data.repository

import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemType
import ru.alarmtrade.specialist.interactors.cars.requests.CarBrandsRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarInformationByCanCodeRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarInformationRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarModelsRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarsRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationBrandsRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationCarsRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationModelsRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceInformationRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.core.specialist.sdk.toResponseOrUnavailable
import ru.alarmtrade.specmp.feature.vehicle.data.datasource.VehicleCatalogDataSource
import ru.alarmtrade.specmp.feature.vehicle.data.mapper.toCard
import ru.alarmtrade.specmp.feature.vehicle.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.vehicle.data.mapper.toSdkFilter
import ru.alarmtrade.specmp.feature.vehicle.data.mapper.toSubsystemType
import ru.alarmtrade.specmp.feature.vehicle.data.mapper.toSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCard
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarTrim
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CatalogQuery
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.VehicleCatalogRepository

/**
 * [VehicleCatalogRepository] над SDK ([VehicleCatalogDataSource]): каталог [VehicleCatalog.ALL] — `carsInteractor`
 * (фильтр по подсистемам устройства и группам функций), [VehicleCatalog.WITH_CONFIGURATIONS] —
 * `configurationInteractor` (фильтр по модели блока, группы не поддерживаются). Подсистемы модели блока —
 * `deviceInformationInteractor`.
 */
internal class SpecialistVehicleCatalogRepository(private val dataSource: VehicleCatalogDataSource) :
    VehicleCatalogRepository {

    override suspend fun brands(query: CatalogQuery): Response<List<CarBrand>> = when (query.catalog) {
        VehicleCatalog.ALL -> subsystems(query.device).mapIfSuccess { types ->
            dataSource.brands(CarBrandsRequest(subsystemTypes = types, featureGroupFilter = query.groups.toSdkFilter()))
                .toResponseOrUnavailable()
        }

        VehicleCatalog.WITH_CONFIGURATIONS ->
            dataSource.configurationBrands(ConfigurationBrandsRequest(alarmDeviceName = query.device.deviceName()))
                .toResponseOrUnavailable()
    }.map { brands ->
        // Ключ сортировки — один раз на марку, а не на каждое сравнение (сотни марок).
        brands.map { it.toDomain() }.map { it.name.lowercase() to it }.sortedBy { it.first }.map { it.second }
    }

    override suspend fun models(query: CatalogQuery, brandId: Int): Response<List<CarModel>> = when (query.catalog) {
        VehicleCatalog.ALL -> subsystems(query.device).mapIfSuccess { types ->
            dataSource.models(CarModelsRequest(brandId, types, query.groups.toSdkFilter())).toResponseOrUnavailable()
        }

        VehicleCatalog.WITH_CONFIGURATIONS ->
            dataSource.configurationModels(ConfigurationModelsRequest(brandId, query.device.deviceName()))
                .toResponseOrUnavailable()
    }.map { models -> models.map { it.toDomain() } }

    override suspend fun trims(query: CatalogQuery, modelId: Int): Response<List<CarTrim>> = when (query.catalog) {
        VehicleCatalog.ALL -> subsystems(query.device).mapIfSuccess { types ->
            dataSource.trims(CarsRequest(modelId, types, query.groups.toSdkFilter())).toResponseOrUnavailable()
        }

        VehicleCatalog.WITH_CONFIGURATIONS ->
            dataSource.configurationTrims(ConfigurationCarsRequest(modelId, query.device.deviceName()))
                .toResponseOrUnavailable()
    }.map { trims -> trims.map { it.toDomain() } }

    override suspend fun card(carId: Int, groups: Set<CarFeatureGroup>): Response<CarCard> = dataSource
        .carInformation(CarInformationRequest(carId = carId, featureGroupFilter = groups.toSdkFilter()))
        .toResponseOrUnavailable()
        .map { it.toCard() }

    override suspend fun findByCanCode(
        canCode: String,
        device: VehicleDeviceFilter?,
        groups: Set<CarFeatureGroup>,
    ): Response<List<CarSummary>> = subsystems(device).mapIfSuccess { types ->
        dataSource.carsByCanCode(CarInformationByCanCodeRequest(canCode, types, groups.toSdkFilter()))
            .toResponseOrUnavailable()
    }.map { cars -> cars.map { it.toSummary() }.sortedWith(summaryOrder) }

    override suspend fun summary(carId: Int): Response<CarSummary> =
        dataSource.carInformation(CarInformationRequest(carId = carId)).toResponseOrUnavailable().map { it.toSummary() }

    /** Подсистемы, под которые фильтруется каталог: тип — одна подсистема, модель блока — все её подсистемы. */
    private suspend fun subsystems(device: VehicleDeviceFilter?): Response<List<SubsystemType>> = when (device) {
        null -> Response.Success(emptyList())

        is VehicleDeviceFilter.Type -> Response.Success(listOf(device.type.toSubsystemType()))

        is VehicleDeviceFilter.Device ->
            dataSource.deviceInformation(GetDeviceInformationRequest(device.deviceName)).toResponseOrUnavailable()
                .map { information -> information.subsystems.map { it.type }.distinct() }
    }

    private companion object {
        val summaryOrder =
            compareBy<CarSummary>({ it.brand.lowercase() }, { it.model.lowercase() }, { it.name.lowercase() })
    }
}

/** Модель блока для каталога конфигураций; пустая строка — конфигурации для всех моделей. */
private fun VehicleDeviceFilter?.deviceName(): String = (this as? VehicleDeviceFilter.Device)?.deviceName.orEmpty()
