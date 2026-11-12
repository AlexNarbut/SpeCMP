package ru.alarmtrade.specmp.feature.mountassistant.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNameMatch
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsWrongPinException
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantSettingValue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantWriteException
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SettingEditor
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.WriteFailure
import ru.alarmtrade.specmp.feature.mountassistant.testing.FakeDeviceSettingsRepository

class AssistantSettingsRepositoryImplTest {
    private val settings = FakeDeviceSettingsRepository()
    private val repository = AssistantSettingsRepositoryImpl(settings)

    @Test
    fun `find opens the device session without reload and keeps sdk order first`() = runTest {
        settings.searchResult = Response.Success(listOf(SettingNameMatch(12, listOf(0..3))))

        val found = (repository.find("автозапуск") as Response.Success).value

        assertEquals(listOf<Pair<SettingsSource, Boolean>>(SettingsSource.Device to false), settings.opens)
        assertEquals(listOf(12, 11), found.map { it.id })
        assertEquals(listOf("Автозапуск"), found[1].path)
        assertEquals(listOf(0..3), found[0].nameHighlights)
        assertEquals(0, settings.closes)
    }

    @Test
    fun `editors exist for switch, list and number only`() = runTest {
        val found = (repository.find("е") as Response.Success).value.associateBy(FoundSetting::id)

        assertIs<SettingEditor.Switch>(found.getValue(11).editor)
        assertEquals(SettingEditor.Number(5..30, 1, "мин"), found.getValue(12).editor)
        assertEquals(SettingEditor.Options(listOf("Тихо", "Громко")), found.getValue(21).editor)
        assertNull(found.getValue(23).editor)
        assertEquals(AssistantSettingValue.Number(10), found.getValue(12).value)
    }

    @Test
    fun `sdk search error falls back to local matches, open error is the result`() = runTest {
        settings.searchResult = Response.Error(IllegalStateException("sdk"))
        assertEquals(listOf(21), (repository.find("сирен") as Response.Success).value.map { it.id })

        settings.openResult = Response.Error(IllegalStateException("link"))
        assertIs<Response.Error>(repository.find("сирен"))
    }

    @Test
    fun `setting is found by id with its path`() = runTest {
        val setting = (repository.setting(21) as Response.Success).value
        assertEquals(listOf("Охрана"), setting.path)
        assertIs<Response.Error>(repository.setting(999))
    }

    @Test
    fun `write sets the value and saves the session`() = runTest {
        assertEquals(Response.Success(Unit), repository.write(11, AssistantSettingValue.Switch(true)))
        assertEquals(listOf<Pair<Int, SettingValue>>(11 to SettingValue.Switch(true)), settings.values)
        assertEquals(1, settings.saves)
    }

    @Test
    fun `wrong pin and other save errors are typed`() = runTest {
        settings.saveResults += listOf(
            Response.Error(SettingsWrongPinException(IllegalStateException())),
            Response.Error(IllegalStateException("io")),
        )
        val pin = assertIs<Response.Error>(repository.write(11, AssistantSettingValue.Switch(true)))
        assertEquals(WriteFailure.WRONG_PIN, assertIs<AssistantWriteException>(pin.exception).failure)
        val failed = assertIs<Response.Error>(repository.save())
        assertEquals(WriteFailure.FAILED, assertIs<AssistantWriteException>(failed.exception).failure)
    }

    @Test
    fun `other unsaved excludes the setting itself and revert sets the old value`() = runTest {
        settings.unsaved = setOf(11, 12, 21)
        assertEquals(Response.Success(2), repository.otherUnsaved(11))
        repository.revert(11, AssistantSettingValue.Switch(false))
        assertEquals<Pair<Int, SettingValue>>(11 to SettingValue.Switch(false), settings.values.last())
        assertEquals(0, settings.saves)
    }
}
