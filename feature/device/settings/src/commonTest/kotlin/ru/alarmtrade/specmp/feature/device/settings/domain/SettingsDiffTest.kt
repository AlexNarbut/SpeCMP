package ru.alarmtrade.specmp.feature.device.settings.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.ChannelLogic
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.diffTo
import ru.alarmtrade.specmp.feature.device.settings.domain.model.logicsChange
import ru.alarmtrade.specmp.feature.device.settings.domain.model.replace

class SettingsDiffTest {

    private val hood = ChannelLogic(0, "Капот", null)
    private val trunk = ChannelLogic(1, "Багажник", null)
    private val doors = ChannelLogic(2, "Двери", null)

    private val rootSwitch = switch(5, "0.1", "Корневая")
    private val shock = switch(32, "3.2.1", "Датчик удара")
    private val tilt = switch(33, "3.2.5.1", "Датчик наклона")
    private val guard = section(300, "3", "Охрана", section(320, "3.2", "Датчики", shock, tilt))
    private val input = InputSetting(
        id = 41, number = "4.1", name = "Вход 1", description = null,
        value = SettingValue.Input(InputValue(listOf(true, false, true), InputContactType.NORMALLY_OPEN)),
        channel = Channel(1, "IN1", emptyList()), defaultPolarity = Polarity.MINUS,
        availableLogics = listOf(hood, trunk, doors), enabledLogics = listOf(hood, doors),
    )
    private val inputs = section(400, "4", "Входы", input)
    private val can = CanCodeSetting(61, "6.1", "CAN-код", null, SettingValue.Can(CanCode("1032", null)))
    private val canSection = section(600, "6", "CAN", can)
    private val root = section(0, "", "", rootSwitch, guard, inputs, canSection)

    @Test
    fun `equal menus have no changes`() {
        val diff = root.diffTo(root)

        assertTrue(diff.isEmpty)
        assertEquals(0, diff.sectionCount)
    }

    @Test
    fun `changes are grouped by first level sections with root settings first`() {
        val next = root
            .replace(tilt.copy(value = SettingValue.Switch(true)))
            .replace(shock.copy(value = SettingValue.Switch(true)))
            .replace(rootSwitch.copy(value = SettingValue.Switch(true)))

        val diff = root.diffTo(next)

        assertEquals(3, diff.total)
        assertEquals(listOf(null, 300), diff.sections.map { it.section?.id })
        assertEquals(listOf(32, 33), diff.sections[1].changes.map { it.after.id })
        assertEquals(SettingValue.Switch(false), diff.sections[1].changes[0].before.value)
        assertEquals(2, diff.sectionCount)
    }

    @Test
    fun `can code change goes first and still counts its section`() {
        val next = root
            .replace(can.copy(value = SettingValue.Can(CanCode("2154", 7))))
            .replace(shock.copy(value = SettingValue.Switch(true)))

        val diff = root.diffTo(next)

        assertEquals(61, diff.canCode?.after?.id)
        assertEquals(listOf(300), diff.sections.map { it.section?.id })
        assertEquals(2, diff.total)
        assertEquals(2, diff.sectionCount)
    }

    @Test
    fun `settings missing in one of the menus are skipped`() {
        val next = section(0, "", "", section(300, "3", "Охрана", shock.copy(value = SettingValue.Switch(true))))

        val diff = root.diffTo(next)

        assertEquals(1, diff.total)
        assertNull(diff.canCode)
    }

    @Test
    fun `channel change lists added and removed functions`() {
        val next = root.replace(
            input.copy(
                value = SettingValue.Input(InputValue(listOf(false, true, true), InputContactType.NORMALLY_CLOSED)),
            ),
        )

        val change = root.diffTo(next).sections.single().changes.single()
        val logics = change.logicsChange()

        assertEquals(listOf(trunk), logics?.added)
        assertEquals(listOf(hood), logics?.removed)
        assertNull(
            root.diffTo(root.replace(shock.copy(value = SettingValue.Switch(true))))
                .sections.single().changes.single().logicsChange(),
        )
    }

    private fun switch(id: Int, number: String, name: String) =
        SwitchSetting(id, number, name, null, SettingValue.Switch(false), null, null)

    private fun section(id: Int, number: String, name: String, vararg children: SettingNode) =
        SettingsSection(id, number, name, null, children.toList())
}
