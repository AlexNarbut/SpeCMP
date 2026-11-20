package ru.alarmtrade.specmp.mountpassport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.configurator.testing.FakeLocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

class AppPassportConfigurationsRepositoryTest {

    private val local = FakeLocalConfigurationsRepository()
    private val repository = AppPassportConfigurationsRepository(local)

    @Test
    fun snapshotFileBecomesLocalConfigurationOfThatModel() = runTest {
        assertIs<Response.Success<Unit>>(
            repository.saveSettings("Toyota RAV4", "Pandora DX-91", "/data/passports/p1/settings.dxs"),
        )

        val (name, from) = local.createCalls.single()
        assertEquals("Toyota RAV4", name)
        assertEquals(
            SettingsSource.File(
                path = "/data/passports/p1/settings.dxs",
                deviceName = "Pandora DX-91",
                inPlace = false,
            ),
            from,
        )
    }

    @Test
    fun createErrorIsReturned() = runTest {
        local.createResult = Response.Error(IllegalStateException("db"))

        assertIs<Response.Error>(repository.saveSettings("x", "Pandora DX-91", "/p"))
    }
}
