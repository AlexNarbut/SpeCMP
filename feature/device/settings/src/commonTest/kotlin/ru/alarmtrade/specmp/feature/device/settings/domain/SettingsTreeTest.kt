package ru.alarmtrade.specmp.feature.device.settings.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.capabilities
import ru.alarmtrade.specmp.feature.device.settings.domain.model.countIn
import ru.alarmtrade.specmp.feature.device.settings.domain.model.findNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.pathTo
import ru.alarmtrade.specmp.feature.device.settings.domain.model.replace
import ru.alarmtrade.specmp.feature.device.settings.domain.model.search
import ru.alarmtrade.specmp.feature.device.settings.domain.model.settings

class SettingsTreeTest {

    private val shock = switch(32, "3.2.1", "Датчик удара")
    private val tilt = switch(33, "3.2.5.1", "Датчик наклона")
    private val extra = section(325, "3.2.5", "Доп. датчики", tilt)
    private val sensors = section(320, "3.2", "Датчики", shock, extra)
    private val guard = section(300, "3", "Охрана", sensors)
    private val door = InputSetting(
        id = 41, number = "4.1", name = "Концевик дверей", description = null,
        value = SettingValue.Input(InputValue(emptyList(), InputContactType.NORMALLY_OPEN)),
        channel = Channel(1, "IN1", emptyList()), defaultPolarity = Polarity.MINUS,
        availableLogics = emptyList(), enabledLogics = emptyList()
    )
    private val inputs = section(400, "4", "Входы", door)
    private val root = section(0, "", "", guard, inputs)

    @Test
    fun `finds nodes at any depth`() {
        assertEquals(tilt, root.findNode(33))
        assertEquals(sensors, root.findNode(320))
        assertNull(root.findNode(999))
    }

    @Test
    fun `path lists sections from the first level to the parent`() {
        assertEquals(listOf(300, 320, 325), root.pathTo(33)?.map { it.id })
        assertEquals(emptyList(), root.pathTo(300)?.map { it.id })
        assertNull(root.pathTo(999))
    }

    @Test
    fun `replace swaps the setting deep in the tree and keeps the rest`() {
        val changed = tilt.copy(value = SettingValue.Switch(true))

        val updated = root.replace(changed)

        assertEquals(changed, updated.findNode(33))
        assertEquals(shock, updated.findNode(32))
        assertEquals(root.settings().size, updated.settings().size)
    }

    @Test
    fun `section counter counts changed settings at any depth`() {
        assertEquals(2, guard.countIn(setOf(32, 33, 41)))
        assertEquals(1, extra.countIn(setOf(32, 33, 41)))
        assertEquals(0, inputs.countIn(setOf(32)))
    }

    @Test
    fun `search matches name, number prefix and channel name with path`() {
        assertEquals(listOf(32, 33), root.search("ДАТЧИК ").map { it.node.id }.filter { it in setOf(32, 33) })
        assertEquals(listOf(320, 32, 325, 33), root.search("3.2").map { it.node.id })
        assertEquals(listOf(41), root.search("in1").map { it.node.id })
        assertEquals(listOf(300, 320, 325), root.search("наклона").single().path.map { it.id })
        assertTrue(root.search("  ").isEmpty())
    }

    @Test
    fun `only the device source follows the connection and can be reset`() {
        assertTrue(SettingsSource.Device.capabilities.followsDeviceConnection)
        assertTrue(SettingsSource.Device.capabilities.canResetToFactory)
        assertTrue(SettingsSource.File("p", "d").capabilities.canSave)
        assertEquals(false, SettingsSource.Defaults("d").capabilities.canSave)
        assertEquals(false, SettingsSource.RemoteConfiguration(1, "d", "t").capabilities.canSave)
    }

    private fun switch(id: Int, number: String, name: String) = SwitchSetting(
        id = id,
        number = number,
        name = name,
        description = null,
        value = SettingValue.Switch(false),
        onTitle = null,
        offTitle = null
    )

    private fun section(id: Int, number: String, name: String, vararg children: SettingNode) =
        SettingsSection(id = id, number = number, name = name, description = null, children = children.toList())
}
