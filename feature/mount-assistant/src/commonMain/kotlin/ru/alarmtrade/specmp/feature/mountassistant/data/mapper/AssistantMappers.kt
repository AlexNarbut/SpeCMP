package ru.alarmtrade.specmp.feature.mountassistant.data.mapper

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title
import ru.alarmtrade.specmp.feature.device.settings.domain.model.IntegerSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OptionSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantCapability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantSettingValue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTransport
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CanFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SettingEditor

internal fun AlarmDevice.toFacts(
    transport: ConnectionTransport,
    online: Boolean,
    productSearch: Boolean,
): DeviceFacts = DeviceFacts(
    systemId = systemId,
    title = title,
    firmwareVersion = displayFirmwareVersion,
    transport = transport.toAssistant(),
    capabilities = features?.takeIf { modelInDatabase }?.mapNotNull { it.toCapability() }?.toSet(),
    can = can?.let { CanFacts(it.isFactory, it.code.ifBlank { null }, it.firmwareVersion.ifBlank { null }) },
    online = online,
    productSearch = productSearch,
)

private fun ConnectionTransport.toAssistant(): AssistantTransport = when (this) {
    ConnectionTransport.BLE -> AssistantTransport.BLE
    ConnectionTransport.USB -> AssistantTransport.USB
    ConnectionTransport.SERIAL -> AssistantTransport.SERIAL
}

private fun DeviceFeature.toCapability(): AssistantCapability? = when (this) {
    DeviceFeature.FIRMWARE_UPDATE -> AssistantCapability.FIRMWARE
    DeviceFeature.SETTINGS -> AssistantCapability.SETTINGS
    DeviceFeature.BYPASS_LEARNING -> AssistantCapability.BYPASS
    DeviceFeature.CLONE -> AssistantCapability.CLONE
    DeviceFeature.DYNAMIC_DEVICES -> AssistantCapability.DYNAMIC_DEVICES
    DeviceFeature.VOICE_FIRMWARE -> null
}

internal fun Setting.toFoundSetting(path: List<SettingsSection>, nameHighlights: List<IntRange>): FoundSetting =
    FoundSetting(
        id = id,
        name = name,
        path = path.map { it.name },
        nameHighlights = nameHighlights,
        value = value.toAssistantValue(),
        editor = toEditor(),
    )

private fun Setting.toEditor(): SettingEditor? = when (this) {
    is SwitchSetting -> SettingEditor.Switch(onTitle, offTitle)
    is OptionSetting -> SettingEditor.Options(options)
    is IntegerSetting -> SettingEditor.Number(range, step = 1, units = units)
    else -> null
}

private fun SettingValue.toAssistantValue(): AssistantSettingValue? = when (this) {
    is SettingValue.Switch -> AssistantSettingValue.Switch(enabled)
    is SettingValue.Option -> AssistantSettingValue.Option(index)
    is SettingValue.Integer -> AssistantSettingValue.Number(value)
    else -> null
}

internal fun AssistantSettingValue.toSettingValue(): SettingValue = when (this) {
    is AssistantSettingValue.Switch -> SettingValue.Switch(enabled)
    is AssistantSettingValue.Option -> SettingValue.Option(index)
    is AssistantSettingValue.Number -> SettingValue.Integer(value)
}
