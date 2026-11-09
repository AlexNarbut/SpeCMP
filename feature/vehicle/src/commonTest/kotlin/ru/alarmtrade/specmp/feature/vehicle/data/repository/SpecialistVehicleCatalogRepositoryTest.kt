package ru.alarmtrade.specmp.feature.vehicle.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceInformation
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceType
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemInformation
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemType
import ru.alarmtrade.specialist.domain.cars.Car
import ru.alarmtrade.specialist.domain.cars.CarBrand as SdkCarBrand
import ru.alarmtrade.specialist.domain.cars.CarFeatureGroup as SdkCarFeatureGroup
import ru.alarmtrade.specialist.domain.cars.CarInfo
import ru.alarmtrade.specialist.domain.cars.CarModel as SdkCarModel
import ru.alarmtrade.specialist.domain.cars.EngineBlockByCanFeature
import ru.alarmtrade.specialist.domain.cars.ImmobilizerBypassFeature
import ru.alarmtrade.specialist.interactors.cars.requests.CarBrandsRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarInformationByCanCodeRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarInformationRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarModelsRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationBrandsRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationCarsRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.requests.GetDeviceInformationRequest
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCapability
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCard
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarTrim
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CatalogQuery
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType

class SpecialistVehicleCatalogRepositoryTest {
    private val dataSource = FakeVehicleCatalogDataSource()
    private val repository = SpecialistVehicleCatalogRepository(dataSource)
    private val device = VehicleDeviceFilter.Device("DX-9X")

    @Test
    fun `brands of a device model filter by its distinct subsystems and sort by name`() = runTest {
        dataSource.device = SpecialistResponse.Success(
            DeviceInformation(
                id = 1,
                name = "DX-9X",
                type = DeviceType.ALARM,
                subsystems = listOf(
                    subsystem(SubsystemType.ALARM_MAIN),
                    subsystem(SubsystemType.BLUETOOTH_MODULE),
                    subsystem(SubsystemType.ALARM_MAIN),
                ),
                imageUrl = null,
            ),
        )
        dataSource.brands = SpecialistResponse.Success(
            listOf(SdkCarBrand(1, "toyota", ""), SdkCarBrand(2, "Audi", "https://img/audi.png")),
        )

        val result = repository.brands(CatalogQuery(VehicleCatalog.ALL, device, setOf(CarFeatureGroup.MAIN)))

        assertEquals(
            Response.Success(listOf(CarBrand(2, "Audi", "https://img/audi.png"), CarBrand(1, "toyota", null))),
            result,
        )
        assertEquals("DX-9X", assertIs<GetDeviceInformationRequest>(dataSource.requests.first()).name)
        assertEquals(
            CarBrandsRequest(
                subsystemTypes = listOf(SubsystemType.ALARM_MAIN, SubsystemType.BLUETOOTH_MODULE),
                featureGroupFilter = listOf(SdkCarFeatureGroup.MAIN),
            ),
            dataSource.requests.last(),
        )
    }

    @Test
    fun `device type filters by its own subsystem without asking the server`() = runTest {
        val query = CatalogQuery(VehicleCatalog.ALL, VehicleDeviceFilter.Type(VehicleDeviceType.MICROBOT))

        repository.models(query, brandId = 3)

        assertEquals(listOf<Any>(CarModelsRequest(3, listOf(SubsystemType.MICROBOT_MAIN), null)), dataSource.requests)
    }

    @Test
    fun `failed device information stops the catalog request`() = runTest {
        dataSource.device = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val result = assertIs<Response.Error>(repository.brands(CatalogQuery(VehicleCatalog.ALL, device)))

        assertEquals(ApiError.NoConnection, result.exception.appError)
        assertEquals(1, dataSource.requests.size)
    }

    @Test
    fun `catalog with configurations filters by the device model, empty without a model`() = runTest {
        dataSource.models = SpecialistResponse.Success(listOf(SdkCarModel(4, "Camry", "", 2017..2023, emptyList())))
        dataSource.trims = SpecialistResponse.Success(listOf(Car(5, "2.5 AT")))

        repository.brands(CatalogQuery(VehicleCatalog.WITH_CONFIGURATIONS, device = null))
        val models = repository.models(CatalogQuery(VehicleCatalog.WITH_CONFIGURATIONS, device), brandId = 1)
        val trims = repository.trims(CatalogQuery(VehicleCatalog.WITH_CONFIGURATIONS, device), modelId = 4)

        assertEquals(Response.Success(listOf(CarModel(4, "Camry", null, 2017..2023))), models)
        assertEquals(Response.Success(listOf(CarTrim(5, "2.5 AT"))), trims)
        assertEquals(ConfigurationBrandsRequest(alarmDeviceName = ""), dataSource.requests[0])
        assertEquals(ConfigurationCarsRequest(4, "DX-9X"), dataSource.requests[2])
    }

    @Test
    fun `search by CAN code sorts by brand, model and trim`() = runTest {
        dataSource.carsByCanCode = SpecialistResponse.Success(
            listOf(
                carInfo(1, "Toyota", "Camry", "3.5"),
                carInfo(2, "audi", "A6", "2.0"),
                carInfo(3, "Toyota", "Camry", "2.5"),
            ),
        )

        val result = assertIs<Response.Success<List<CarSummary>>>(repository.findByCanCode("2154", null, emptySet()))

        assertEquals(listOf(2, 3, 1), result.value.map { it.id })
        assertEquals(listOf<Any>(CarInformationByCanCodeRequest("2154", emptyList(), null)), dataSource.requests)
    }

    @Test
    fun `card asks for the requested groups and maps the summary`() = runTest {
        dataSource.car = SpecialistResponse.Success(carInfo(7, "Toyota", "Camry", "2.5 AT"))

        val card = assertIs<Response.Success<CarCard>>(repository.card(7, setOf(CarFeatureGroup.TRAILER))).value

        assertEquals("Camry", card.summary.model)
        assertEquals(CarCapability.NoData, card.engineBlockByCan)
        assertEquals(
            CarInformationRequest(carId = 7, featureGroupFilter = listOf(SdkCarFeatureGroup.TRAILER)),
            dataSource.requests.single(),
        )
        assertEquals(
            Response.Success(CarSummary(7, "Toyota", "Camry", "2.5 AT", 2017..2023, "2154", null)),
            repository.summary(7),
        )
    }

    @Test
    fun `unavailable cars interactor is an error`() = runTest {
        dataSource.carsAvailable = false

        assertIs<IllegalStateException>(assertIs<Response.Error>(repository.summary(7)).exception)
    }
}

private fun subsystem(type: SubsystemType) =
    SubsystemInformation(systemName = type.name, title = "", id = 1, type = type)

private fun carInfo(id: Int, brand: String, model: String, name: String) = CarInfo(
    carId = id,
    carBrand = brand,
    carModel = model,
    carName = name,
    carImageUrl = "",
    productionYears = 2017..2023,
    engineBlockByCan = EngineBlockByCanFeature.NotAvailable,
    immobilizerBypass = ImmobilizerBypassFeature.NotAvailable,
    description1 = null,
    description2 = null,
    canCode = "2154",
    features = emptyList(),
    mountingGuides = emptyList(),
    mountingSchemes = emptyList(),
    mountingMaps = emptyList(),
    userManualsExist = false,
    userManuals = emptyList(),
)
