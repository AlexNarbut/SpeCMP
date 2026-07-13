package ru.alarmtrade.specmp.feature.main.domain.model

sealed interface MainAppUpdateCheckResult {
    data class Available(
        val versionName: String,
    ) : MainAppUpdateCheckResult

    data object UpToDate : MainAppUpdateCheckResult
}
