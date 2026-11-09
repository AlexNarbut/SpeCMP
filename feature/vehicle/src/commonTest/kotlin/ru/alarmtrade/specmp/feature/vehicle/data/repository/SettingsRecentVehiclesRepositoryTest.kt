package ru.alarmtrade.specmp.feature.vehicle.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.InMemorySettings
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog

class SettingsRecentVehiclesRepositoryTest {
    private val settings = InMemorySettings()
    private val repository = SettingsRecentVehiclesRepository(settings)

    @Test
    fun `recent keeps the latest first without duplicates and survives a new repository`() = runTest {
        repository.push(VehicleCatalog.ALL, car(1))
        repository.push(VehicleCatalog.ALL, car(2, imageUrl = "https://img/2.png"))
        repository.push(VehicleCatalog.ALL, car(1))

        assertEquals(
            Response.Success(listOf(car(1), car(2, imageUrl = "https://img/2.png"))),
            SettingsRecentVehiclesRepository(settings).recent(VehicleCatalog.ALL),
        )
    }

    @Test
    fun `each catalog has its own list`() = runTest {
        repository.push(VehicleCatalog.ALL, car(1))
        repository.push(VehicleCatalog.WITH_CONFIGURATIONS, car(2))

        assertEquals(Response.Success(listOf(car(1))), repository.recent(VehicleCatalog.ALL))
        assertEquals(Response.Success(listOf(car(2))), repository.recent(VehicleCatalog.WITH_CONFIGURATIONS))
    }

    @Test
    fun `recent holds at most the limit and removes by id`() = runTest {
        (1..7).forEach { repository.push(VehicleCatalog.ALL, car(it)) }

        assertEquals(listOf(7, 6, 5, 4, 3), ids(repository.recent(VehicleCatalog.ALL)))
        assertEquals(listOf(7, 5, 4, 3), ids(repository.remove(VehicleCatalog.ALL, carId = 6)))
    }

    @Test
    fun `corrupted list reads as empty`() = runTest {
        settings.putString("recent_all", "[{\"broken\"")

        assertEquals(Response.Success(emptyList()), repository.recent(VehicleCatalog.ALL))
    }

    private fun ids(response: Response<List<CarSummary>>) =
        assertIs<Response.Success<List<CarSummary>>>(response).value.map { it.id }
}

private fun car(id: Int, imageUrl: String? = null) =
    CarSummary(id, "Toyota", "Camry", "Trim $id", 2017..2023, "2154", imageUrl)
