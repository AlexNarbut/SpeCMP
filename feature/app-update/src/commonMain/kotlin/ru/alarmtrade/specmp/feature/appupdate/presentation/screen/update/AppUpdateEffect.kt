package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update

import kotlinx.collections.immutable.ImmutableList

sealed interface AppUpdateEffect {
    data class OpenStorePage(val urls: ImmutableList<String>) : AppUpdateEffect

    data object Saved : AppUpdateEffect

    data object SaveFailed : AppUpdateEffect

    data object Close : AppUpdateEffect
}
