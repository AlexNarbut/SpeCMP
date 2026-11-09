package ru.alarmtrade.specmp.feature.configurator.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.settings.LocalSettingsConfigurationInfo
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.CreateLocalConfigurationRequest
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.toSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

@OptIn(ExperimentalCoroutinesApi::class)
class SpecialistLocalConfigurationsRepositoryTest {
    private val dataSource = FakeLocalConfigurationsDataSource()
    private val settings = FakeDeviceSettingsRepository()
    private val repository = SpecialistLocalConfigurationsRepository(dataSource, settings)
    private val from = SettingsSource.Defaults("DX-9X")

    @Test
    fun `all reads every page and sorts by update date, newest first`() = runTest {
        dataSource.pages = listOf(
            SpecialistResponse.Success((1L..50L).map { info(it, day = 1) }),
            SpecialistResponse.Success(listOf(info(51, day = 3), info(52, day = 2))),
        )

        val result = assertIs<Response.Success<List<LocalConfiguration>>>(repository.all()).value

        assertEquals(listOf(0, 1), dataSource.pageRequests.map { it.page })
        assertEquals(setOf(50), dataSource.pageRequests.map { it.count }.toSet())
        assertEquals(52, result.size)
        assertEquals(listOf(51L, 52L), result.take(2).map { it.id })
    }

    @Test
    fun `failed page fails the whole list`() = runTest {
        dataSource.pages = listOf(SpecialistResponse.Error.Network(NetworkIsNotAvailableException()))

        assertEquals(ApiError.NoConnection, assertIs<Response.Error>(repository.all()).exception.appError)
    }

    @Test
    fun `unavailable interactor is an error`() = runTest {
        dataSource.available = false

        assertIs<IllegalStateException>(assertIs<Response.Error>(repository.all()).exception)
    }

    @Test
    fun `create copies the source settings into the new record and signals a change`() = runTest {
        val changes = collectChanges()

        val result = repository.create(name = "Охрана", description = "Описание", deviceName = "DX-9X", from = from)

        val target = SettingsSource.LocalConfiguration(id = 100, deviceName = "DX-9X", title = "Охрана")
        assertEquals(Response.Success(target), result)
        assertEquals(
            listOf(CreateLocalConfigurationRequest(name = "Охрана", deviceName = "DX-9X", description = "Описание")),
            dataSource.created,
        )
        assertEquals(listOf(Triple<SettingsSource, SettingsSource, Boolean>(from, target, true)), settings.copies)
        assertEquals(1, changes.size)
    }

    @Test
    fun `failed copy deletes the new record and returns the copy error`() = runTest {
        val changes = collectChanges()
        val failure = IllegalStateException("mismatch")
        settings.copyResult = Response.Error(failure)

        val result = assertIs<Response.Error>(repository.create("Охрана", "", "DX-9X", from))

        assertSame(failure, result.exception)
        assertEquals(listOf(100L), dataSource.deleted)
        assertTrue(changes.isEmpty())
    }

    @Test
    fun `failed SDK create does not copy settings`() = runTest {
        dataSource.createResult = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val result = assertIs<Response.Error>(repository.create("Охрана", "", "DX-9X", from))

        assertEquals(ApiError.NoConnection, result.exception.appError)
        assertTrue(settings.copies.isEmpty())
    }

    @Test
    fun `update info makes a renamed copy, closes the old session and deletes the old record`() = runTest {
        val old = configuration(id = 7)

        val result = repository.updateInfo(old, name = "Новое", description = "")

        val copy = SettingsSource.LocalConfiguration(id = 100, deviceName = "DX-9X", title = "Новое")
        assertEquals(Response.Success(copy), result)
        assertEquals(old.toSource(), settings.copies.single().first)
        assertEquals(listOf<SettingsSource>(old.toSource()), settings.closed)
        assertEquals(listOf(7L), dataSource.deleted)
    }

    @Test
    fun `update info removes the copy when the old record is not deleted`() = runTest {
        dataSource.deleteResults[7] = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val result = assertIs<Response.Error>(repository.updateInfo(configuration(id = 7), "Новое", ""))

        assertEquals(ApiError.NoConnection, result.exception.appError)
        assertEquals(listOf(7L, 100L), dataSource.deleted)
    }

    @Test
    fun `delete signals a change only on success`() = runTest {
        val changes = collectChanges()
        dataSource.deleteResults[8] = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        assertEquals(Response.Success(Unit), repository.delete(7))
        assertIs<Response.Error>(repository.delete(8))

        assertEquals(1, changes.size)
    }

    private fun TestScope.collectChanges(): List<Response<Unit>> {
        val changes = mutableListOf<Response<Unit>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { repository.changes.collect { changes += it } }
        return changes
    }
}

private fun date(day: Int) = LocalDateTime(year = 2026, monthNumber = 1, dayOfMonth = day, hour = 0, minute = 0)

private fun info(id: Long, day: Int) =
    LocalSettingsConfigurationInfo(id, "Конфигурация $id", "", "DX-9X", date(day), date(day))

private fun configuration(id: Long) = LocalConfiguration(id, "Старое", "", "DX-9X", date(1), date(1))
