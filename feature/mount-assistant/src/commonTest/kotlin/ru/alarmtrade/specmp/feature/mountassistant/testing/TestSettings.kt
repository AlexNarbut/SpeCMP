package ru.alarmtrade.specmp.feature.mountassistant.testing

import ru.alarmtrade.specmp.feature.device.settings.domain.model.IntegerSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OptionSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextInputType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TimeOfDay
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TimeSetting

internal val autostartTemp =
    SwitchSetting(11, "1.1", "Автозапуск по температуре", null, SettingValue.Switch(false), null, null)
internal val warmupMinutes =
    IntegerSetting(12, "1.2", "Время прогрева", null, SettingValue.Integer(10), 5..30, "мин", null, null)
internal val sirenMode =
    OptionSetting(21, "2.1", "Режим сирены", null, SettingValue.Option(0), listOf("Тихо", "Громко"))
internal val phone =
    TextSetting(22, "2.2", "Телефон", null, SettingValue.Text(""), 16, TextInputType.entries.first(), null, null)
internal val startTime = TimeSetting(23, "2.3", "Время запуска", null, SettingValue.Time(TimeOfDay(7, 0)))

internal val autostartSection = SettingsSection(1, "1", "Автозапуск", null, listOf(autostartTemp, warmupMinutes))
internal val alarmSection = SettingsSection(2, "2", "Охрана", null, listOf(sirenMode, phone, startTime))
internal val testSettingsRoot = SettingsSection(0, "", "", null, listOf(autostartSection, alarmSection))
