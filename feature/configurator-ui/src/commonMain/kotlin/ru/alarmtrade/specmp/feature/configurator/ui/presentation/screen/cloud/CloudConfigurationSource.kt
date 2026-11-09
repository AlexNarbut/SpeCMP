package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

internal fun CloudConfiguration.toSource(): SettingsSource.RemoteConfiguration =
    SettingsSource.RemoteConfiguration(id = summary.id, deviceName = summary.deviceName, title = summary.name)
