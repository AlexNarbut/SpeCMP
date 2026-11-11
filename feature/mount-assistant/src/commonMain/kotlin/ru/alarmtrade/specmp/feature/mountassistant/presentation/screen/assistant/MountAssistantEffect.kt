package ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant

import ru.alarmtrade.specmp.feature.mountassistant.presentation.navigation.AssistantNavigationTarget

sealed interface MountAssistantEffect {
    data class Navigate(val target: AssistantNavigationTarget) : MountAssistantEffect

    data object FocusSearch : MountAssistantEffect
}
