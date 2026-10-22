package ru.alarmtrade.specmp.feature.device.settings.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import ru.alarmtrade.common.settings.interaction.model.menu.SettingsPath
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.FloatSetting as SdkFloatSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.InputSetting as SdkInputSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.IntegerSetting as SdkIntegerSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.OutputSetting as SdkOutputSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.SettingsSubmenu as SdkSettingsSubmenu
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.SirenRingtoneSetting as SdkSirenRingtoneSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.SwitchSetting as SdkSwitchSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.CanCode as SdkCanCode
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.ChannelLogic as SdkChannelLogic
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.ChannelPolarity
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.DeviceWire
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.InputType
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.SystemInputValue
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.SystemOutputValue
import ru.alarmtrade.specialist.domain.alarmDevice.siren.SirenRingtoneEvent
import ru.alarmtrade.specialist.domain.alarmDevice.siren.SirenRingtoneType
import ru.alarmtrade.specialist.domain.alarmDevice.siren.SirenRingtoneValue
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingBooleanValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingCanCodeValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingInputValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingOptionValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingOutputSwitchValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingOutputValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingSirenIntValueRequest
import ru.alarmtrade.specmp.feature.device.settings.data.repository.DEFAULT_DECIMAL_STEP
import ru.alarmtrade.specmp.feature.device.settings.data.repository.toDomain
import ru.alarmtrade.specmp.feature.device.settings.data.repository.toPath
import ru.alarmtrade.specmp.feature.device.settings.data.repository.toRequest
import ru.alarmtrade.specmp.feature.device.settings.data.repository.toRootSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.DecimalSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.IntegerSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenEvent
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenTone
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenToneType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Wire

class SettingsMappersTest {

    private val path = SettingsPath.CurrentSessionDevice

    @Test
    fun `nested submenus keep their order and depth`() {
        val sdk = SdkSettingsSubmenu(
            id = 0,
            uuid = "",
            name = "root",
            number = "",
            privacy = 0,
            childItems = listOf(
                SdkSettingsSubmenu(
                    id = 1,
                    uuid = "",
                    name = "Охрана",
                    number = "3",
                    privacy = 0,
                    childItems = listOf(switch(id = 2, value = true))
                ),
                switch(id = 3, value = false)
            )
        )

        val root = sdk.toRootSection()

        assertEquals(listOf(1, 3), root.children.map { it.id })
        val section = assertIs<SettingsSection>(root.children[0])
        assertEquals("3", section.number)
        assertEquals(SettingValue.Switch(true), assertIs<SwitchSetting>(section.children.single()).value)
    }

    @Test
    fun `a single setting as root is wrapped into a section`() {
        val root = switch(id = 7, value = true).toRootSection()

        assertEquals(listOf(7), root.children.map { it.id })
    }

    @Test
    fun `integer setting keeps range and edge captions`() {
        val sdk = SdkIntegerSetting(
            id = 5, uuid = "", name = "Задержка", number = "3.2.4", privacy = 0, value = 30,
            minValue = 0, maxValue = 120, units = "с", minValueText = "Выкл", maxValueText = null,
            description = "d"
        )

        val setting = assertIs<IntegerSetting>(sdk.toDomain())

        assertEquals(0..120, setting.range)
        assertEquals(SettingValue.Integer(30), setting.value)
        assertEquals("Выкл", setting.minValueText)
        assertEquals("d", setting.description)
    }

    @Test
    fun `decimal setting without step gets the default step`() {
        val sdk = SdkFloatSetting(
            id = 1, uuid = "", name = "t", number = "", privacy = 0, value = -5f, minValue = -40f,
            maxValue = 20f, units = "°C", minValueText = null, maxValueText = null, step = null, description = null
        )

        assertEquals(DEFAULT_DECIMAL_STEP, assertIs<DecimalSetting>(sdk.toDomain()).step)
    }

    @Test
    fun `siren keeps one range for both tone types`() {
        val sdk = SdkSirenRingtoneSetting(
            id = 1, uuid = "", name = "s", number = "", privacy = 0,
            value = SirenRingtoneValue(SirenRingtoneType.MELODY, 3), minValue = 0, maxValue = 15, units = null,
            description = null, ringtoneEvent = SirenRingtoneEvent.ALARM
        )

        val setting = assertIs<SirenSetting>(sdk.toDomain())

        assertEquals(SettingValue.Siren(SirenTone(SirenToneType.MELODY, 3)), setting.value)
        assertEquals(0..15, setting.range)
        assertEquals(SirenEvent.ALARM, setting.event)
    }

    @Test
    fun `input maps contact type, logics and wires`() {
        val sdk = SdkInputSetting(
            id = 41, uuid = "", name = "Концевик", number = "4.1", privacy = 0,
            value = SystemInputValue(listOf(false, true), InputType.NC),
            defaultPolarity = ChannelPolarity.MINUS,
            availableLogics = listOf(SdkChannelLogic(0, "Двери"), SdkChannelLogic(1, "Багажник", "desc")),
            enabledLogics = listOf(SdkChannelLogic(1, "Багажник", "desc")),
            wires = listOf(DeviceWire("Жёлто-чёрный", intArrayOf(0xFFE0B400.toInt(), 0x1B1B1B), 5)),
            channelName = "IN1", channelNumber = 1, description = null
        )

        val setting = assertIs<InputSetting>(sdk.toDomain())

        assertEquals(InputValue(listOf(false, true), InputContactType.NORMALLY_CLOSED), setting.value.value)
        assertEquals(Polarity.MINUS, setting.defaultPolarity)
        assertEquals(listOf("Багажник"), setting.enabledLogics.map { it.name })
        assertEquals("IN1", setting.channel.name)
        // Альфа-канал отбрасывается: цвета — RGB.
        assertEquals(listOf(Wire("Жёлто-чёрный", listOf(0xE0B400, 0x1B1B1B), 5)), setting.channel.wires)
    }

    @Test
    fun `wire placeholder without color and pin means no wire, uncolored wire with pin stays`() {
        val sdk = output(
            wires = listOf(
                DeviceWire("", intArrayOf(0x00FFFFFF), 0),
                DeviceWire("Без цвета", intArrayOf(0x00FFFFFF), 13)
            )
        )

        val setting = assertIs<OutputSetting>(sdk.toDomain())

        assertEquals(listOf(Wire("Без цвета", emptyList(), 13)), setting.channel.wires)
    }

    @Test
    fun `undefined polarity is kept as undefined`() {
        val setting = assertIs<OutputSetting>(output(polarity = ChannelPolarity.UNDEF).toDomain())

        assertEquals(Polarity.UNDEFINED, setting.value.value.polarity)
    }

    @Test
    fun `values map to the request of their type`() {
        assertIs<SetDeviceSettingBooleanValueRequest>(SettingValue.Switch(true).toRequest(path, 1))
        assertIs<SetDeviceSettingOptionValueRequest>(SettingValue.Option(2).toRequest(path, 1))
        assertIs<SetDeviceSettingOutputSwitchValueRequest>(SettingValue.SwitchOutput(true).toRequest(path, 1))

        val siren = assertIs<SetDeviceSettingSirenIntValueRequest>(
            SettingValue.Siren(SirenTone(SirenToneType.TONE, 4)).toRequest(path, 9)
        )
        assertEquals(9, siren.settingId)
        assertEquals(SirenRingtoneValue(SirenRingtoneType.TONE, 4), siren.value)

        val can =
            assertIs<SetDeviceSettingCanCodeValueRequest>(SettingValue.Can(CanCode("2154", 17)).toRequest(path, 1))
        assertEquals(SdkCanCode("2154", 17), can.value)

        val input = assertIs<SetDeviceSettingInputValueRequest>(
            SettingValue.Input(InputValue(listOf(true), InputContactType.NORMALLY_OPEN)).toRequest(path, 1)
        )
        assertEquals(SystemInputValue(listOf(true), InputType.NO), input.value)

        val output = assertIs<SetDeviceSettingOutputValueRequest>(
            SettingValue.Output(OutputValue(listOf(false), Polarity.PLUS)).toRequest(path, 1)
        )
        assertEquals(SystemOutputValue(listOf(false), ChannelPolarity.PLUS), output.value)
        assertTrue(output.path == path)
    }

    @Test
    fun `sources map to sdk paths`() {
        assertEquals(SettingsPath.CurrentSessionDevice, SettingsSource.Device.toPath())
        assertEquals(SettingsPath.File("/tmp/a.dxs", "DX-9X"), SettingsSource.File("/tmp/a.dxs", "DX-9X").toPath())
        assertEquals(SettingsPath.LocalConfiguration(3L), SettingsSource.LocalConfiguration(3L, "DX", "t").toPath())
        assertEquals(SettingsPath.RemoteConfiguration(4), SettingsSource.RemoteConfiguration(4, "DX", "t").toPath())
        assertEquals(SettingsPath.DefaultSettings("DX"), SettingsSource.Defaults("DX").toPath())
    }

    private fun switch(id: Int, value: Boolean) = SdkSwitchSetting(
        id = id, uuid = "", name = "s$id", number = "", privacy = 0, value = value, onTitle = null, offTitle = null,
        description = null
    )

    private fun output(wires: List<DeviceWire> = emptyList(), polarity: ChannelPolarity = ChannelPolarity.MINUS) =
        SdkOutputSetting(
            id = 51, uuid = "", name = "Реле", number = "5.4", privacy = 0,
            value = SystemOutputValue(emptyList(), polarity), canEditPolarity = false,
            defaultPolarity = ChannelPolarity.MINUS, availableLogics = emptyList(), enabledLogics = emptyList(),
            wires = wires, channelName = "R1", channelNumber = 1, description = null
        )
}
