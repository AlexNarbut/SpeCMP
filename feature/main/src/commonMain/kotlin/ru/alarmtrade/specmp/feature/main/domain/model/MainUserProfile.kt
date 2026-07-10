package ru.alarmtrade.specmp.feature.main.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class MainUserProfile(
    val name: String,
    val email: String,
)
