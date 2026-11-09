package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine

import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

internal sealed interface MineEffect {
    data class Message(val message: MineMessage) : MineEffect

    data class Copied(val configuration: SettingsSource.LocalConfiguration) : MineEffect

    data class ExportFile(val path: String, val fileName: String) : MineEffect

    data class FileSaved(val name: String) : MineEffect
}
