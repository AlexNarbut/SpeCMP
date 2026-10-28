package ru.alarmtrade.specmp.feature.vehicle.data.preferences

import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.RecentVehiclesStore

/** [RecentVehiclesStore] в именованном хранилище приложения: JSON-список на каталог. Битые данные — пустой список. */
class SettingsRecentVehiclesStore(private val settings: Settings) : RecentVehiclesStore {

    override fun recent(catalog: VehicleCatalog): List<CarSummary> {
        val raw = settings.getStringOrNull(catalog.key()) ?: return emptyList()
        return runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(emptyList()).map { it.toDomain() }
    }

    override fun push(catalog: VehicleCatalog, car: CarSummary) {
        val updated = (listOf(car) + recent(catalog).filter { it.id != car.id }).take(RecentVehiclesStore.MAX_RECENT)
        store(catalog, updated)
    }

    override fun remove(catalog: VehicleCatalog, carId: Int) {
        store(catalog, recent(catalog).filter { it.id != carId })
    }

    private fun store(catalog: VehicleCatalog, cars: List<CarSummary>) {
        settings.putString(catalog.key(), json.encodeToString(serializer, cars.map { it.toStored() }))
    }

    private fun VehicleCatalog.key() = "recent_${name.lowercase()}"

    @Serializable
    private data class StoredCar(
        val id: Int,
        val brand: String,
        val model: String,
        val name: String,
        val yearFrom: Int,
        val yearTo: Int,
        val canCode: String,
        val imageUrl: String? = null
    ) {
        fun toDomain() = CarSummary(id, brand, model, name, yearFrom..yearTo, canCode, imageUrl)
    }

    private fun CarSummary.toStored() = StoredCar(id, brand, model, name, years.first, years.last, canCode, imageUrl)

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
        val serializer = ListSerializer(StoredCar.serializer())
    }
}
