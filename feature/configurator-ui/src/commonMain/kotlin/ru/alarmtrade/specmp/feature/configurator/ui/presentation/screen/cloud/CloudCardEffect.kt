package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

internal sealed interface CloudCardEffect {
    data class OpenUrl(val url: String) : CloudCardEffect
    data class SavedToMine(val configuration: SettingsSource.LocalConfiguration) : CloudCardEffect
    data object SaveFailed : CloudCardEffect
}
