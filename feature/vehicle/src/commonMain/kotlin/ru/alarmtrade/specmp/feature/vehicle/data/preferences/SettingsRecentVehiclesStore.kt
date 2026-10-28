package ru.alarmtrade.specmp.feature.vehicle.data.preferences

import com.russhwolf.settings.Settings
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.RecentVehiclesStore

/** [RecentVehiclesStore] в именованном хранилище приложения: JSON-список на каталог. Битые данные — пустой список. */
class SettingsRecentVehiclesStore(private val settings: Settings) : RecentVehiclesStore {

    private val lock = Mutex()

    override fun recent(catalog: VehicleCatalog): List<CarSummary> {
        val raw = settings.getStringOrNull(catalog.key()) ?: return emptyList()
        return safeRun { json.decodeFromString(serializer, raw) }.getValueIfSuccess().orEmpty().map { it.toDomain() }
    }

    override suspend fun push(catalog: VehicleCatalog, car: CarSummary): List<CarSummary> = update(catalog) { recent ->
        (listOf(car) + recent.filter { it.id != car.id }).take(RecentVehiclesStore.MAX_RECENT)
    }

    override suspend fun remove(catalog: VehicleCatalog, carId: Int): List<CarSummary> = update(catalog) { recent ->
        recent.filter { it.id != carId }
    }

    // Settings не атомарен: две быстрые правки без замка потеряли бы одну.
    private suspend fun update(catalog: VehicleCatalog, change: (List<CarSummary>) -> List<CarSummary>) =
        lock.withLock {
            change(recent(catalog)).also { cars ->
                settings.putString(catalog.key(), json.encodeToString(serializer, cars.map { it.toStored() }))
            }
        }

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
