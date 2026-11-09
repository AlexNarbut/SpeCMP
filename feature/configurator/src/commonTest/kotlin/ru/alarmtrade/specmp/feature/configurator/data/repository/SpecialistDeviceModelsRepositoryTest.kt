package ru.alarmtrade.specmp.feature.configurator.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.testing.InMemorySettings
import ru.alarmtrade.specmp.feature.configurator.data.datasource.DeviceModelsDataSource

class SpecialistDeviceModelsRepositoryTest {
    private var names: SpecialistResponse<List<String>>? = SpecialistResponse.Success(emptyList())
    private val dataSource = object : DeviceModelsDataSource {
        override suspend fun deviceNames(): SpecialistResponse<List<String>>? = names
    }
    private val settings = InMemorySettings()
    private val repository = SpecialistDeviceModelsRepository(dataSource, settings)

    @Test
    fun `models drop blanks and duplicates and sort ignoring case`() = runTest {
        names = SpecialistResponse.Success(listOf("dx-91", "", "A-93", "DX-9X", "A-93", " "))

        assertEquals(Response.Success(listOf("A-93", "dx-91", "DX-9X")), repository.models())
    }

    @Test
    fun `network failure becomes a common no-connection error`() = runTest {
        names = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        assertEquals(ApiError.NoConnection, assertIs<Response.Error>(repository.models()).exception.appError)
    }

    @Test
    fun `unavailable interactor is an error`() = runTest {
        names = null

        assertIs<IllegalStateException>(assertIs<Response.Error>(repository.models()).exception)
    }

    @Test
    fun `recent keeps the latest first without duplicates and survives a new repository`() = runTest {
        repository.pushRecent("DX-9X")
        repository.pushRecent("A-93")
        repository.pushRecent("DX-9X")
        repository.removeRecent("missing")

        val reopened = SpecialistDeviceModelsRepository(dataSource, settings)
        assertEquals(Response.Success(listOf("DX-9X", "A-93")), reopened.recent())
    }

    @Test
    fun `recent holds at most the limit and removes by name`() = runTest {
        (1..7).forEach { repository.pushRecent("M-$it") }

        assertEquals(Response.Success(listOf("M-7", "M-6", "M-5", "M-4", "M-3")), repository.recent())
        assertEquals(Response.Success(listOf("M-7", "M-5", "M-4", "M-3")), repository.removeRecent("M-6"))
    }

    @Test
    fun `corrupted recent list reads as empty`() = runTest {
        settings.putString("recent_models", "{not json")

        assertEquals(Response.Success(emptyList()), repository.recent())
    }
}
