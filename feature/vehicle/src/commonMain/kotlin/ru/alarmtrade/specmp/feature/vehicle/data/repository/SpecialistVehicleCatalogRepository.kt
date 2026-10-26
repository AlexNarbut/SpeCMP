package ru.alarmtrade.specmp.feature.vehicle.data.repository

import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemType
import ru.alarmtrade.specialist.interactors.cars.CarsInteractor
import ru.alarmtrade.specialist.interactors.cars.requests.CarBrandsRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarInformationByCanCodeRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarInformationRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarModelsRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarsRequest
import ru.alarmtrade.specialist.interactors.configuration.ConfigurationInteractor
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationBrandsRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationCarsRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationModelsRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.DeviceInformationInteractor
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceInformationRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfSuccess
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
 * [VehicleCatalogRepository] над SDK: каталог [VehicleCatalog.ALL] — `carsInteractor` (фильтр по подсистемам
 * устройства и группам функций), [VehicleCatalog.WITH_CONFIGURATIONS] — `configurationInteractor` (фильтр по
 * модели блока, группы не поддерживаются). Подсистемы модели блока — `deviceInformationInteractor`.
 * Интеракторы — лениво: недоступный на платформе не роняет создание репозитория.
 */
class SpecialistVehicleCatalogRepository(
    private val cars: () -> CarsInteractor,
    private val configurations: () -> ConfigurationInteractor,
    private val deviceInformation: () -> DeviceInformationInteractor
) : VehicleCatalogRepository {

    override suspend fun brands(query: CatalogQuery): Response<List<CarBrand>> = when (query.catalog) {
        VehicleCatalog.ALL -> withCars { interactor ->
            subsystems(query.device).mapIfSuccess { types ->
                interactor.getAllCarBrandsUseCase
                    .execute(CarBrandsRequest(subsystemTypes = types, featureGroupFilter = query.groups.toSdkFilter()))
                    .toResponse()
            }
        }

        VehicleCatalog.WITH_CONFIGURATIONS -> withConfigurations { interactor ->
            interactor.getAllCarBrandsUseCase
                .execute(ConfigurationBrandsRequest(alarmDeviceName = query.device.deviceName()))
                .toResponse()
        }
    }.map { brands -> brands.map { it.toDomain() }.sortedBy { it.name.lowercase() } }

    override suspend fun models(query: CatalogQuery, brandId: Int): Response<List<CarModel>> = when (query.catalog) {
        VehicleCatalog.ALL -> withCars { interactor ->
            subsystems(query.device).mapIfSuccess { types ->
                interactor.getCarModelsByBrandUseCase
                    .execute(CarModelsRequest(brandId, types, query.groups.toSdkFilter()))
                    .toResponse()
            }
        }

        VehicleCatalog.WITH_CONFIGURATIONS -> withConfigurations { interactor ->
            interactor.getCarModelsByBrandUseCase
                .execute(ConfigurationModelsRequest(brandId, query.device.deviceName()))
                .toResponse()
        }
    }.map { models -> models.map { it.toDomain() } }

    override suspend fun trims(query: CatalogQuery, modelId: Int): Response<List<CarTrim>> = when (query.catalog) {
        VehicleCatalog.ALL -> withCars { interactor ->
            subsystems(query.device).mapIfSuccess { types ->
                interactor.getCarsByModelUseCase
                    .execute(CarsRequest(modelId, types, query.groups.toSdkFilter()))
                    .toResponse()
            }
        }

        VehicleCatalog.WITH_CONFIGURATIONS -> withConfigurations { interactor ->
            interactor.getCarsByModelUseCase
                .execute(ConfigurationCarsRequest(modelId, query.device.deviceName()))
                .toResponse()
        }
    }.map { trims -> trims.map { it.toDomain() } }

    override suspend fun card(carId: Int, groups: Set<CarFeatureGroup>): Response<CarCard> = withCars { interactor ->
        interactor.getCarInformationUseCase
            .execute(CarInformationRequest(carId = carId, featureGroupFilter = groups.toSdkFilter()))
            .toResponse()
            .map { it.toCard() }
    }

    override suspend fun findByCanCode(
        canCode: String,
        device: VehicleDeviceFilter?,
        groups: Set<CarFeatureGroup>
    ): Response<List<CarSummary>> = withCars { interactor ->
        subsystems(device).mapIfSuccess { types ->
            interactor.getCarInformationByCanCodeUseCase
                .execute(CarInformationByCanCodeRequest(canCode, types, groups.toSdkFilter()))
                .toResponse()
        }.map { cars -> cars.map { it.toSummary() }.sortedWith(summaryOrder) }
    }

    override suspend fun summary(carId: Int): Response<CarSummary> = withCars { interactor ->
        interactor.getCarInformationUseCase.execute(CarInformationRequest(carId = carId)).toResponse()
            .map { it.toSummary() }
    }

    /** Подсистемы, под которые фильтруется каталог: тип — одна подсистема, модель блока — все её подсистемы. */
    private suspend fun subsystems(device: VehicleDeviceFilter?): Response<List<SubsystemType>> = when (device) {
        null -> Response.Success(emptyList())

        is VehicleDeviceFilter.Type -> Response.Success(listOf(device.type.toSubsystemType()))

        is VehicleDeviceFilter.Device -> {
            val interactor = deviceInformation.resolveOrNull()
            if (interactor == null) {
                interactorUnavailable()
            } else {
                interactor.getDeviceInformation.execute(GetDeviceInformationRequest(device.deviceName)).toResponse()
                    .map { information -> information.subsystems.map { it.type }.distinct() }
            }
        }
    }

    private inline fun <T> withCars(block: (CarsInteractor) -> Response<T>): Response<T> =
        cars.resolveOrNull()?.let(block) ?: interactorUnavailable()

    private inline fun <T> withConfigurations(block: (ConfigurationInteractor) -> Response<T>): Response<T> =
        configurations.resolveOrNull()?.let(block) ?: interactorUnavailable()

    private companion object {
        val summaryOrder =
            compareBy<CarSummary>({ it.brand.lowercase() }, { it.model.lowercase() }, { it.name.lowercase() })
    }
}

/** Модель блока для каталога конфигураций; пустая строка — конфигурации для всех моделей. */
private fun VehicleDeviceFilter?.deviceName(): String = (this as? VehicleDeviceFilter.Device)?.deviceName.orEmpty()
