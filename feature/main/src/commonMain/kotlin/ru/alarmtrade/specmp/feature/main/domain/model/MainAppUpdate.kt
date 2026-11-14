package ru.alarmtrade.specmp.feature.main.domain.model

/** Обновление приложения для входов на главном; [installsItself] — desktop (качает и ставит сам). */
data class MainAppUpdateStatus(val installsItself: Boolean, val update: MainAppUpdate)

sealed interface MainAppUpdate {
    data object None : MainAppUpdate

    data object Checking : MainAppUpdate

    data class Available(val version: String) : MainAppUpdate

    data class Downloading(val version: String, val bytes: Long) : MainAppUpdate

    data class Downloaded(val version: String) : MainAppUpdate

    data object Installing : MainAppUpdate
}
