package ru.alarmtrade.specmp.feature.device.settings.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.settings.interaction.model.errors.WrongSettingsSizeException
import ru.alarmtrade.common.settings.interaction.model.menu.AlarmDeviceSettingsMenu
import ru.alarmtrade.common.settings.interaction.model.menu.SettingsPath
import ru.alarmtrade.common.settings.interaction.model.search.SettingSearchResult
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.SettingsSubmenu as SdkSettingsSubmenu
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.SwitchSetting as SdkSwitchSetting
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNameMatch
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsMismatchException
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting

class SpecialistDeviceSettingsRepositoryTest {
    private val dataSource = FakeDeviceSettingsDataSource()
    private val repository = SpecialistDeviceSettingsRepository(dataSource)

    @Test
    fun `open maps the menu root and passes the source path`() = runTest {
        dataSource.menu = SpecialistResponse.Success(
            AlarmDeviceSettingsMenu(SettingsPath.CurrentSessionDevice, submenu(switch(id = 2, value = true))),
        )

        val section = assertIs<Response.Success<SettingsSection>>(
            repository.open(SettingsSource.Device, reload = true),
        ).value

        assertEquals(listOf(2), section.children.map { it.id })
        val request = dataSource.openRequests.single()
        assertEquals(SettingsPath.CurrentSessionDevice, request.path)
        assertTrue(request.reOpen)
    }

    @Test
    fun `single setting root is wrapped into a section`() = runTest {
        dataSource.menu = SpecialistResponse.Success(
            AlarmDeviceSettingsMenu(SettingsPath.CurrentSessionDevice, switch(id = 5, value = false)),
        )

        val section = assertIs<Response.Success<SettingsSection>>(repository.open(SettingsSource.Device, false)).value

        assertEquals(listOf(5), section.children.map { it.id })
    }

    @Test
    fun `set value returns the updated setting, a section is an error`() = runTest {
        dataSource.item = SpecialistResponse.Success(switch(id = 3, value = true))

        val setting = assertIs<Response.Success<*>>(
            repository.setValue(SettingsSource.Device, 3, SettingValue.Switch(true)),
        )
        assertEquals(SettingValue.Switch(true), assertIs<SwitchSetting>(setting.value).value)
        assertEquals(3, dataSource.valueRequests.single().settingId)

        dataSource.item = SpecialistResponse.Success(submenu())
        assertIs<Response.Error>(repository.setValue(SettingsSource.Device, 3, SettingValue.Switch(true)))
    }

    @Test
    fun `unsaved ids, save and close go to the source path`() = runTest {
        val file = SettingsSource.File(path = "/tmp/a.dxs", deviceName = "DX-9X")
        dataSource.unsaved = SpecialistResponse.Success(listOf(switch(1, true), switch(4, false)))

        assertEquals(Response.Success(setOf(1, 4)), repository.unsavedIds(file))
        assertEquals(Response.Success(Unit), repository.save(file))
        assertEquals(Response.Success(Unit), repository.close(file))

        assertEquals(SettingsPath.File("/tmp/a.dxs", "DX-9X"), dataSource.saveRequests.single().path)
        val close = dataSource.closeRequests.single()
        assertEquals(SettingsPath.File("/tmp/a.dxs", "DX-9X"), close.path)
        assertEquals(false, close.saveChanges)
    }

    @Test
    fun `copy of a different size is a settings mismatch`() = runTest {
        val sizeError = WrongSettingsSizeException(10, 12)
        dataSource.item = SpecialistResponse.Error.General(sizeError)

        val error = assertIs<Response.Error>(
            repository.copy(SettingsSource.Defaults("DX-9X"), SettingsSource.Device, saveChanges = false),
        )

        assertSame(sizeError, assertIs<SettingsMismatchException>(error.exception).cause)
        val request = dataSource.copyRequests.single()
        assertEquals(SettingsPath.DefaultSettings("DX-9X"), request.from)
        assertEquals(SettingsPath.CurrentSessionDevice, request.to)
    }

    @Test
    fun `other copy errors are kept`() = runTest {
        val failure = IllegalStateException("io")
        dataSource.item = SpecialistResponse.Error.General(failure)

        assertSame(
            failure,
            assertIs<Response.Error>(repository.copy(SettingsSource.Device, SettingsSource.Device, false)).exception,
        )
    }

    @Test
    fun `snapshot copies the source into a new scratch file and saves it`() = runTest {
        dataSource.item = SpecialistResponse.Success(submenu())

        val snapshot = assertIs<Response.Success<SettingsSource.File>>(
            repository.snapshot(SettingsSource.Device, deviceName = "DX-9X"),
        ).value

        assertTrue(snapshot.path.endsWith(".dxs"))
        assertEquals("DX-9X", snapshot.deviceName)
        val request = dataSource.copyRequests.single()
        assertEquals(SettingsPath.File(snapshot.path, "DX-9X"), request.to)
        assertTrue(request.saveChanges)
    }

    @Test
    fun `search keeps the sdk order and name highlights and passes the source path`() = runTest {
        dataSource.found = SpecialistResponse.Success(
            listOf(
                SettingSearchResult(switch(id = 7, value = false), listOf(0..1)),
                SettingSearchResult(switch(id = 3, value = true), listOf(0..0, 1..1)),
            ),
        )
        val file = SettingsSource.File(path = "/tmp/a.dxs", deviceName = "DX-9X")

        assertEquals(
            Response.Success(listOf(SettingNameMatch(7, listOf(0..1)), SettingNameMatch(3, listOf(0..0, 1..1)))),
            repository.search(file, "s7"),
        )
        val request = dataSource.searchRequests.single()
        assertEquals(SettingsPath.File("/tmp/a.dxs", "DX-9X"), request.path)
        assertEquals("s7", request.name)
    }

    @Test
    fun `search errors and an unavailable interactor are errors`() = runTest {
        val failure = IllegalStateException("closed")
        dataSource.found = SpecialistResponse.Error.General(failure)
        assertSame(failure, assertIs<Response.Error>(repository.search(SettingsSource.Device, "a")).exception)

        dataSource.found = null
        assertIs<Response.Error>(repository.search(SettingsSource.Device, "a"))
    }

    @Test
    fun `unavailable interactor is an error and reset goes to the device`() = runTest {
        assertIs<Response.Error>(repository.open(SettingsSource.Device, false))

        assertEquals(Response.Success(Unit), repository.resetDeviceToFactory())
        assertEquals(1, dataSource.resets)
    }

    private fun submenu(vararg children: SdkSwitchSetting) = SdkSettingsSubmenu(
        id = 0,
        uuid = "",
        name = "root",
        number = "",
        privacy = 0,
        childItems = children.toList(),
    )

    private fun switch(id: Int, value: Boolean) = SdkSwitchSetting(
        id = id, uuid = "", name = "s$id", number = "", privacy = 0, value = value, onTitle = null, offTitle = null,
        description = null,
    )
}
