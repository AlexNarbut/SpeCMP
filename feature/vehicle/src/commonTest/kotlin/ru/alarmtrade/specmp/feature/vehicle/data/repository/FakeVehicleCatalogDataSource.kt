package ru.alarmtrade.specmp.feature.vehicle.data.repository

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceInformation
import ru.alarmtrade.specialist.domain.cars.Car
import ru.alarmtrade.specialist.domain.cars.CarBrand
import ru.alarmtrade.specialist.domain.cars.CarInfo
import ru.alarmtrade.specialist.domain.cars.CarModel
import ru.alarmtrade.specialist.interactors.cars.requests.CarBrandsRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarInformationByCanCodeRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarInformationRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarModelsRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarsRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationBrandsRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationCarsRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationModelsRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceInformationRequest
import ru.alarmtrade.specmp.feature.vehicle.data.datasource.VehicleCatalogDataSource

internal class FakeVehicleCatalogDataSource : VehicleCatalogDataSource {
    var carsAvailable = true
    var brands: SpecialistResponse<List<CarBrand>> = SpecialistResponse.Success(emptyList())
    var models: SpecialistResponse<List<CarModel>> = SpecialistResponse.Success(emptyList())
    var trims: SpecialistResponse<List<Car>> = SpecialistResponse.Success(emptyList())
    var car: SpecialistResponse<CarInfo>? = null
    var carsByCanCode: SpecialistResponse<List<CarInfo>> = SpecialistResponse.Success(emptyList())
    var device: SpecialistResponse<DeviceInformation>? = null

    val requests = mutableListOf<Any>()

    override suspend fun brands(request: CarBrandsRequest) = cars(request) { brands }

    override suspend fun models(request: CarModelsRequest) = cars(request) { models }

    override suspend fun trims(request: CarsRequest) = cars(request) { trims }

    override suspend fun carInformation(request: CarInformationRequest) = cars(request) { car }

    override suspend fun carsByCanCode(request: CarInformationByCanCodeRequest) = cars(request) { carsByCanCode }

    override suspend fun configurationBrands(request: ConfigurationBrandsRequest) = record(request) { brands }

    override suspend fun configurationModels(request: ConfigurationModelsRequest) = record(request) { models }

    override suspend fun configurationTrims(request: ConfigurationCarsRequest) = record(request) { trims }

    override suspend fun deviceInformation(request: GetDeviceInformationRequest) = record(request) { device }

    private fun <T> cars(request: Any, response: () -> T?): T? = if (carsAvailable) record(request, response) else null

    private fun <T> record(request: Any, response: () -> T?): T? {
        requests += request
        return response()
    }
}
