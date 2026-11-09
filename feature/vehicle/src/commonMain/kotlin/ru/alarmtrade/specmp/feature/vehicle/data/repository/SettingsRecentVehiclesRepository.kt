package ru.alarmtrade.specmp.feature.vehicle.data.repository

import com.russhwolf.settings.Settings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import ru.alarmtrade.specmp.core.common.RecentList
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.RecentVehiclesRepository

/**
 * [RecentVehiclesRepository] в именованном хранилище приложения: JSON-список на каталог. Битые данные — пустой
 * список.
 */
internal class SettingsRecentVehiclesRepository(settings: Settings) : RecentVehiclesRepository {

    // Отдельный список (и замок) на каталог.
    private val lists = VehicleCatalog.entries.associateWith { catalog ->
        RecentList<CarSummary, Int>(
            max = RecentVehiclesRepository.MAX_RECENT,
            key = { it.id },
            read = {
                settings.getStringOrNull(catalog.key())
                    ?.let { raw -> safeRun { json.decodeFromString(serializer, raw) }.getValueIfSuccess() }
                    .orEmpty()
                    .map { it.toDomain() }
            },
            write = { cars ->
                settings.putString(catalog.key(), json.encodeToString(serializer, cars.map { it.toStored() }))
            },
        )
    }

    override suspend fun recent(catalog: VehicleCatalog): Response<List<CarSummary>> =
        safeRun { lists.getValue(catalog).all() }

    override suspend fun push(catalog: VehicleCatalog, car: CarSummary): Response<List<CarSummary>> =
        safeRun { lists.getValue(catalog).push(car) }

    override suspend fun remove(catalog: VehicleCatalog, carId: Int): Response<List<CarSummary>> =
        safeRun { lists.getValue(catalog).remove(carId) }

    private fun VehicleCatalog.key() = "recent_${name.lowercase()}"

    @Serializable
    @SerialName("vehicle.SettingsRecentVehiclesStore.StoredCar")
    private data class StoredCar(
        val id: Int,
        val brand: String,
        val model: String,
        val name: String,
        val yearFrom: Int,
        val yearTo: Int,
        val canCode: String,
        val imageUrl: String? = null,
    ) {
        fun toDomain() = CarSummary(id, brand, model, name, yearFrom..yearTo, canCode, imageUrl)
    }

    private fun CarSummary.toStored() = StoredCar(id, brand, model, name, years.first, years.last, canCode, imageUrl)

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
        val serializer = ListSerializer(StoredCar.serializer())
    }
}
