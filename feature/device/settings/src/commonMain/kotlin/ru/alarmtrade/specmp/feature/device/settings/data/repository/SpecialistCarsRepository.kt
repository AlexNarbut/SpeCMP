package ru.alarmtrade.specmp.feature.device.settings.data.repository

import ru.alarmtrade.specialist.domain.cars.CarInfo
import ru.alarmtrade.specialist.interactors.cars.CarsInteractor
import ru.alarmtrade.specialist.interactors.cars.requests.CarInformationByCanCodeRequest
import ru.alarmtrade.specialist.interactors.cars.requests.CarInformationRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Car
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.CarsRepository

/**
 * [CarsRepository] над `carsInteractor` SDK. Поиск по CAN-коду — без фильтра по подсистемам блока (`deviceTypes`
 * пустой): нужен только автомобиль для подписи и `carId` к коду.
 */
class SpecialistCarsRepository(private val carsInteractor: () -> CarsInteractor) : CarsRepository {

    override suspend fun findByCanCode(canCode: String): Response<List<Car>> {
        val interactor = carsInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.getCarInformationByCanCodeUseCase
            .execute(CarInformationByCanCodeRequest(canCode = canCode, deviceTypes = emptyList()))
            .toResponse()
            .map { cars -> cars.map { it.toDomain() } }
    }

    override suspend fun car(id: Int): Response<Car> {
        val interactor = carsInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.getCarInformationUseCase.execute(CarInformationRequest(carId = id)).toResponse()
            .map { it.toDomain() }
    }
}

internal fun CarInfo.toDomain() = Car(
    id = carId,
    brand = carBrand,
    model = carModel,
    name = carName,
    years = productionYears
)
