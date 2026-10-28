package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models

import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.SourceKey

internal sealed interface ModelsEffect {
    data class Open(val source: SourceKey) : ModelsEffect
}
