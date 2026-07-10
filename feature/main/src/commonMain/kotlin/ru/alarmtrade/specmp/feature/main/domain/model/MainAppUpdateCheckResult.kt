package ru.alarmtrade.specmp.feature.main.domain.model

import androidx.compose.runtime.Immutable

sealed interface MainAppUpdateCheckResult {
    @Immutable
    data class Available(
        val versionName: String,
        val versionCode: Int,
    ) : MainAppUpdateCheckResult

    data object UpToDate : MainAppUpdateCheckResult
}
