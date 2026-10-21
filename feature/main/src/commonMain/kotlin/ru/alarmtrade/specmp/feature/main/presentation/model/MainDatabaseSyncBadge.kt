package ru.alarmtrade.specmp.feature.main.presentation.model

enum class MainDatabaseSyncBadge {
    CheckingVersion,
    Downloading,
    Applying,
    Updated,
    UpdateFailed,
    AppUpdateRequired
}
