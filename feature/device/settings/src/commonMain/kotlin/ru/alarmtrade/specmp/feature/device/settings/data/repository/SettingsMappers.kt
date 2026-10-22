package ru.alarmtrade.specmp.feature.device.settings.data.repository

import ru.alarmtrade.common.settings.interaction.model.menu.SettingsPath
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.AutoStartScheduleSetting as SdkAutoStartScheduleSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.CanCodeSetting as SdkCanCodeSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.FloatSetting as SdkFloatSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.InputSetting as SdkInputSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.IntegerSetting as SdkIntegerSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.OptionSetting as SdkOptionSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.OutputSetting as SdkOutputSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.SettingItem as SdkSettingItem
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.SettingsSubmenu as SdkSettingsSubmenu
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.SirenRingtoneSetting as SdkSirenRingtoneSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.SwitchOutputSetting as SdkSwitchOutputSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.SwitchSetting as SdkSwitchSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.TextInputType as SdkTextInputType
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.TextSetting as SdkTextSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.TimeSetting as SdkTimeSetting
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.AutoStartSchedule as SdkAutoStartSchedule
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.CanCode as SdkCanCode
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.ChannelLogic as SdkChannelLogic
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.ChannelPolarity
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.DayOfWeek
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.DeviceWire
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.InputType
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.SystemInputValue
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.SystemOutputValue
import ru.alarmtrade.specialist.domain.alarmDevice.settings.settingValues.TimeSettingValue
import ru.alarmtrade.specialist.domain.alarmDevice.siren.SirenRingtoneEvent
import ru.alarmtrade.specialist.domain.alarmDevice.siren.SirenRingtoneType
import ru.alarmtrade.specialist.domain.alarmDevice.siren.SirenRingtoneValue
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingAutostartScheduleValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingBooleanValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingCanCodeValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingFloatValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingInputValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingIntValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingOptionValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingOutputSwitchValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingOutputValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingSirenIntValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingStringValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingTimeValueRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingValueRequest
import ru.alarmtrade.specmp.feature.device.settings.domain.model.AutoStartSchedule
import ru.alarmtrade.specmp.feature.device.settings.domain.model.AutoStartScheduleSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.ChannelLogic
import ru.alarmtrade.specmp.feature.device.settings.domain.model.DecimalSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.IntegerSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OptionSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenEvent
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenTone
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenToneType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchOutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextInputType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TimeOfDay
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TimeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Weekday
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Wire

/** Шаг дробной настройки, если устройство его не задаёт (как в старом редакторе). */
internal const val DEFAULT_DECIMAL_STEP = 0.1f

/** Цвет-заглушка SDK «у провода нет цвета» (прозрачный белый). */
private const val NO_WIRE_COLOR = 0x00FFFFFF

private const val RGB_MASK = 0xFFFFFF

internal fun SettingsSource.toPath(): SettingsPath = when (this) {
    SettingsSource.Device -> SettingsPath.CurrentSessionDevice
    is SettingsSource.File -> SettingsPath.File(path, deviceName)
    is SettingsSource.LocalConfiguration -> SettingsPath.LocalConfiguration(id)
    is SettingsSource.RemoteConfiguration -> SettingsPath.RemoteConfiguration(id)
    is SettingsSource.Defaults -> SettingsPath.DefaultSettings(deviceName)
}

/** Корень меню; если SDK вернул не раздел, а одну настройку — оборачиваем в раздел без номера. */
internal fun SdkSettingItem.toRootSection(): SettingsSection = when (val node = toDomain()) {
    is SettingsSection -> node
    is Setting -> SettingsSection(id = 0, number = "", name = "", description = null, children = listOf(node))
}

@Suppress("LongMethod", "CyclomaticComplexMethod") // плоский when по 13 типам SDK
internal fun SdkSettingItem.toDomain(): SettingNode = when (this) {
    is SdkSettingsSubmenu -> SettingsSection(id, number, name, description, childItems.map { it.toDomain() })

    is SdkSwitchSetting -> SwitchSetting(
        id,
        number,
        name,
        description,
        SettingValue.Switch(value),
        onTitle = onTitle,
        offTitle = offTitle
    )

    is SdkOptionSetting -> OptionSetting(id, number, name, description, SettingValue.Option(value), options)

    is SdkIntegerSetting -> IntegerSetting(
        id, number, name, description, SettingValue.Integer(value),
        range = minValue..maxValue,
        units = units,
        minValueText = minValueText,
        maxValueText = maxValueText
    )

    is SdkFloatSetting -> DecimalSetting(
        id, number, name, description, SettingValue.Decimal(value),
        minValue = minValue,
        maxValue = maxValue,
        step = step ?: DEFAULT_DECIMAL_STEP,
        units = units,
        minValueText = minValueText,
        maxValueText = maxValueText
    )

    is SdkTextSetting -> TextSetting(
        id, number, name, description, SettingValue.Text(value),
        maxLength = maxLength,
        inputType = inputType.toDomain(),
        validationPattern = validationPattern,
        validationErrorText = validationErrorText
    )

    is SdkTimeSetting -> TimeSetting(id, number, name, description, SettingValue.Time(value.toDomain()))

    is SdkAutoStartScheduleSetting -> AutoStartScheduleSetting(
        id,
        number,
        name,
        description,
        SettingValue.AutoStart(value.toDomain()),
        dayOfWeek.toDomain()
    )

    is SdkSirenRingtoneSetting -> SirenSetting(
        id,
        number,
        name,
        description,
        SettingValue.Siren(value.toDomain()),
        range = minValue..maxValue,
        units = units,
        event = ringtoneEvent.toDomain()
    )

    is SdkCanCodeSetting -> CanCodeSetting(id, number, name, description, SettingValue.Can(value.toDomain()))

    is SdkInputSetting -> InputSetting(
        id, number, name, description, SettingValue.Input(value.toDomain()),
        channel = Channel(channelNumber, channelName, wires.toDomain()),
        defaultPolarity = defaultPolarity.toDomain(),
        availableLogics = availableLogics.map { it.toDomain() },
        enabledLogics = enabledLogics.map { it.toDomain() }
    )

    is SdkOutputSetting -> OutputSetting(
        id, number, name, description, SettingValue.Output(value.toDomain()),
        channel = Channel(channelNumber, channelName, wires.toDomain()),
        canEditPolarity = canEditPolarity,
        defaultPolarity = defaultPolarity.toDomain(),
        availableLogics = availableLogics.map { it.toDomain() },
        enabledLogics = enabledLogics.map { it.toDomain() }
    )

    is SdkSwitchOutputSetting -> SwitchOutputSetting(
        id,
        number,
        name,
        description,
        SettingValue.SwitchOutput(value),
        channel = Channel(channelNumber, channelName, wires.toDomain()),
        polarity = polarity.toDomain(),
        enabledLogics = enabledLogics.map { it.toDomain() }
    )
}

/** Запрос SDK на изменение значения настройки [settingId] в сессии [path]. */
internal fun SettingValue.toRequest(path: SettingsPath, settingId: Int): SetDeviceSettingValueRequest = when (this) {
    is SettingValue.Switch -> SetDeviceSettingBooleanValueRequest(path, settingId, enabled)
    is SettingValue.Option -> SetDeviceSettingOptionValueRequest(path, settingId, index)
    is SettingValue.Integer -> SetDeviceSettingIntValueRequest(path, settingId, value)
    is SettingValue.Decimal -> SetDeviceSettingFloatValueRequest(path, settingId, value)
    is SettingValue.Text -> SetDeviceSettingStringValueRequest(path, settingId, value)
    is SettingValue.Time -> SetDeviceSettingTimeValueRequest(path, settingId, value.toSdk())
    is SettingValue.AutoStart -> SetDeviceSettingAutostartScheduleValueRequest(path, settingId, value.toSdk())
    is SettingValue.Siren -> SetDeviceSettingSirenIntValueRequest(path, settingId, value.toSdk())
    is SettingValue.Can -> SetDeviceSettingCanCodeValueRequest(path, settingId, value.toSdk())
    is SettingValue.Input -> SetDeviceSettingInputValueRequest(path, settingId, value.toSdk())
    is SettingValue.Output -> SetDeviceSettingOutputValueRequest(path, settingId, value.toSdk())
    is SettingValue.SwitchOutput -> SetDeviceSettingOutputSwitchValueRequest(path, settingId, enabled)
}

private fun SdkTextInputType.toDomain(): TextInputType = when (this) {
    SdkTextInputType.TEXT -> TextInputType.TEXT
    SdkTextInputType.NUMBER -> TextInputType.NUMBER
    SdkTextInputType.PHONE -> TextInputType.PHONE
}

private fun TimeSettingValue.toDomain() = TimeOfDay(hours, minutes)

private fun TimeOfDay.toSdk() = TimeSettingValue(hours, minutes)

private fun SdkAutoStartSchedule.toDomain() = AutoStartSchedule(
    isMorningStartEnabled = isMorningStartEnabled,
    morningStartTime = morningStartTime.toDomain(),
    isEveningStartEnabled = isEveningStartEnabled,
    eveningStartTime = eveningStartTime.toDomain()
)

private fun AutoStartSchedule.toSdk() = SdkAutoStartSchedule(
    isMorningStartEnabled = isMorningStartEnabled,
    morningStartTime = morningStartTime.toSdk(),
    isEveningStartEnabled = isEveningStartEnabled,
    eveningStartTime = eveningStartTime.toSdk()
)

private fun DayOfWeek.toDomain(): Weekday = when (this) {
    DayOfWeek.MONDAY -> Weekday.MONDAY
    DayOfWeek.TUESDAY -> Weekday.TUESDAY
    DayOfWeek.WEDNESDAY -> Weekday.WEDNESDAY
    DayOfWeek.THURSDAY -> Weekday.THURSDAY
    DayOfWeek.FRIDAY -> Weekday.FRIDAY
    DayOfWeek.SATURDAY -> Weekday.SATURDAY
    DayOfWeek.SUNDAY -> Weekday.SUNDAY
}

private fun SirenRingtoneValue.toDomain() = SirenTone(
    type = when (type) {
        SirenRingtoneType.TONE -> SirenToneType.TONE
        SirenRingtoneType.MELODY -> SirenToneType.MELODY
    },
    number = value
)

private fun SirenTone.toSdk() = SirenRingtoneValue(
    type = when (type) {
        SirenToneType.TONE -> SirenRingtoneType.TONE
        SirenToneType.MELODY -> SirenRingtoneType.MELODY
    },
    value = number
)

private fun SirenRingtoneEvent.toDomain(): SirenEvent = when (this) {
    SirenRingtoneEvent.ARM -> SirenEvent.ARM
    SirenRingtoneEvent.DISARM -> SirenEvent.DISARM
    SirenRingtoneEvent.WARNING -> SirenEvent.WARNING
    SirenRingtoneEvent.ALARM -> SirenEvent.ALARM
}

private fun SdkCanCode.toDomain() = CanCode(code = canCode, carId = carId)

private fun CanCode.toSdk() = SdkCanCode(canCode = code, carId = carId)

private fun ChannelPolarity.toDomain(): Polarity = when (this) {
    ChannelPolarity.PLUS -> Polarity.PLUS
    ChannelPolarity.MINUS -> Polarity.MINUS
    ChannelPolarity.UNDEF -> Polarity.UNDEFINED
}

private fun Polarity.toSdk(): ChannelPolarity = when (this) {
    Polarity.PLUS -> ChannelPolarity.PLUS
    Polarity.MINUS -> ChannelPolarity.MINUS
    Polarity.UNDEFINED -> ChannelPolarity.UNDEF
}

private fun InputType.toDomain(): InputContactType = when (this) {
    InputType.NO -> InputContactType.NORMALLY_OPEN
    InputType.NC -> InputContactType.NORMALLY_CLOSED
    InputType.UNDEF -> InputContactType.UNDEFINED
}

private fun InputContactType.toSdk(): InputType = when (this) {
    InputContactType.NORMALLY_OPEN -> InputType.NO
    InputContactType.NORMALLY_CLOSED -> InputType.NC
    InputContactType.UNDEFINED -> InputType.UNDEF
}

private fun SystemInputValue.toDomain() = InputValue(logicsState = logicsState, type = type.toDomain())

private fun InputValue.toSdk() = SystemInputValue(logicsState = logicsState, type = type.toSdk())

private fun SystemOutputValue.toDomain() = OutputValue(logicsState = logicsState, polarity = polarity.toDomain())

private fun OutputValue.toSdk() = SystemOutputValue(logicsState = logicsState, polarity = polarity.toSdk())

private fun SdkChannelLogic.toDomain() = ChannelLogic(id = id, name = name, description = description)

/**
 * Провода канала. Цвет-заглушка SDK означает «без цвета»; провод без цвета и без пина — это отсутствие
 * провода (так SDK описывает канал без жгута, например реле R1), такие не показываем.
 */
internal fun List<DeviceWire>.toDomain(): List<Wire> = mapNotNull { wire ->
    val colors = wire.colors.filter { it != NO_WIRE_COLOR }.map { it and RGB_MASK }
    if (colors.isEmpty() && wire.pinNumber == 0) null else Wire(name = wire.name, colors = colors, pin = wire.pinNumber)
}
