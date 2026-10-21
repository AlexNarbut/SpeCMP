package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget

sealed interface MainScreenEffect {
    data class Navigate(val target: MainNavigationTarget) : MainScreenEffect
}
