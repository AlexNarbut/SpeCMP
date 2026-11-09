package ru.alarmtrade.specmp.feature.vehicle.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceInformation
import ru.alarmtrade.specialist.domain.cars.Car
import ru.alarmtrade.specialist.domain.cars.CarBrand
import ru.alarmtrade.specialist.domain.cars.CarInfo
import ru.alarmtrade.specialist.domain.cars.CarModel
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
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе.
internal interface VehicleCatalogDataSource {
    suspend fun brands(request: CarBrandsRequest): SpecialistResponse<List<CarBrand>>?

    suspend fun models(request: CarModelsRequest): SpecialistResponse<List<CarModel>>?

    suspend fun trims(request: CarsRequest): SpecialistResponse<List<Car>>?

    suspend fun carInformation(request: CarInformationRequest): SpecialistResponse<CarInfo>?

    suspend fun carsByCanCode(request: CarInformationByCanCodeRequest): SpecialistResponse<List<CarInfo>>?

    suspend fun configurationBrands(request: ConfigurationBrandsRequest): SpecialistResponse<List<CarBrand>>?

    suspend fun configurationModels(request: ConfigurationModelsRequest): SpecialistResponse<List<CarModel>>?

    suspend fun configurationTrims(request: ConfigurationCarsRequest): SpecialistResponse<List<Car>>?

    suspend fun deviceInformation(request: GetDeviceInformationRequest): SpecialistResponse<DeviceInformation>?
}

internal class SpecialistVehicleCatalogDataSource(
    private val cars: () -> CarsInteractor,
    private val configurations: () -> ConfigurationInteractor,
    private val devices: () -> DeviceInformationInteractor,
) : VehicleCatalogDataSource {
    override suspend fun brands(request: CarBrandsRequest): SpecialistResponse<List<CarBrand>>? =
        cars.resolveOrNull()?.getAllCarBrandsUseCase?.execute(request)

    override suspend fun models(request: CarModelsRequest): SpecialistResponse<List<CarModel>>? =
        cars.resolveOrNull()?.getCarModelsByBrandUseCase?.execute(request)

    override suspend fun trims(request: CarsRequest): SpecialistResponse<List<Car>>? =
        cars.resolveOrNull()?.getCarsByModelUseCase?.execute(request)

    override suspend fun carInformation(request: CarInformationRequest): SpecialistResponse<CarInfo>? =
        cars.resolveOrNull()?.getCarInformationUseCase?.execute(request)

    override suspend fun carsByCanCode(request: CarInformationByCanCodeRequest): SpecialistResponse<List<CarInfo>>? =
        cars.resolveOrNull()?.getCarInformationByCanCodeUseCase?.execute(request)

    override suspend fun configurationBrands(
        request: ConfigurationBrandsRequest,
    ): SpecialistResponse<List<CarBrand>>? = configurations.resolveOrNull()?.getAllCarBrandsUseCase?.execute(request)

    override suspend fun configurationModels(
        request: ConfigurationModelsRequest,
    ): SpecialistResponse<List<CarModel>>? =
        configurations.resolveOrNull()?.getCarModelsByBrandUseCase?.execute(request)

    override suspend fun configurationTrims(request: ConfigurationCarsRequest): SpecialistResponse<List<Car>>? =
        configurations.resolveOrNull()?.getCarsByModelUseCase?.execute(request)

    override suspend fun deviceInformation(
        request: GetDeviceInformationRequest,
    ): SpecialistResponse<DeviceInformation>? = devices.resolveOrNull()?.getDeviceInformation?.execute(request)
}
