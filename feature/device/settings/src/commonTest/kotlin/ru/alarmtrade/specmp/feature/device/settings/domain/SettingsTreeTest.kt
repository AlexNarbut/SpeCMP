package ru.alarmtrade.specmp.feature.device.settings.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SearchHighlights
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNameMatch
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.capabilities
import ru.alarmtrade.specmp.feature.device.settings.domain.model.findNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.localHighlights
import ru.alarmtrade.specmp.feature.device.settings.domain.model.pathTo
import ru.alarmtrade.specmp.feature.device.settings.domain.model.replace
import ru.alarmtrade.specmp.feature.device.settings.domain.model.reusing
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
        availableLogics = emptyList(), enabledLogics = emptyList(),
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
    fun `reusing keeps instances of unchanged nodes and returns old tree when nothing changed`() {
        // replace копирует все разделы: равное дерево из новых экземпляров
        val reread = root.replace(shock.copy())
        assertNotSame(root, reread)
        assertSame(root, reread.reusing(root))

        val changed = root.replace(tilt.copy(value = SettingValue.Switch(true)))
        val updated = changed.reusing(root)
        assertEquals(changed, updated)
        assertSame(inputs, updated.findNode(400))
        assertSame(shock, updated.findNode(32))
        assertNotSame(extra, updated.findNode(325))
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
    fun `local search highlights the matched name, number prefix and channel name`() {
        assertEquals(SearchHighlights(channel = listOf(0..2)), root.search("in1").single().highlights)
        assertEquals(SearchHighlights(number = listOf(0..2)), root.search("3.2").first { it.node.id == 32 }.highlights)
        assertEquals(listOf(7..13), root.search("наклона").single().highlights.name)
        assertEquals(SearchHighlights(name = listOf(0..1, 2..3)), switch(1, "", "аАаа").localHighlights("аа"))
    }

    @Test
    fun `sdk name matches go first in their order, local additions follow without duplicates`() {
        val hits = root.search(
            "3.2",
            listOf(
                SettingNameMatch(33, listOf(0..5)),
                SettingNameMatch(33, listOf(7..13)),
                SettingNameMatch(999, listOf(0..1)),
            ),
        )

        assertEquals(listOf(33, 320, 32, 325), hits.map { it.node.id })
        assertEquals(SearchHighlights(name = listOf(0..5), number = listOf(0..2)), hits.first().highlights)
        assertEquals(listOf(300, 320, 325), hits.first().path.map { it.id })
    }

    @Test
    fun `sdk match without highlights falls back to the local name match`() {
        val hit = root.search("удара", listOf(SettingNameMatch(32, emptyList()))).single()

        assertEquals(SearchHighlights(name = listOf(7..11)), hit.highlights)
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
        offTitle = null,
    )

    private fun section(id: Int, number: String, name: String, vararg children: SettingNode) =
        SettingsSection(id = id, number = number, name = name, description = null, children = children.toList())
}
