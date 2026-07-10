package ru.alarmtrade.specmp.feature.main.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class MainAppVersion(
    val name: String,
    val code: Int,
)
