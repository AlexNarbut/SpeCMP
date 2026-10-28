package ru.alarmtrade.specmp.feature.main.presentation.model

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.common.NetworkFailure

@Immutable
sealed interface MainDatabaseSyncBadge {
    data object CheckingVersion : MainDatabaseSyncBadge

    data object Downloading : MainDatabaseSyncBadge

    data object Applying : MainDatabaseSyncBadge

    data object Updated : MainDatabaseSyncBadge

    /** [networkFailure] — синхронизация упала из-за связи; null — прочая ошибка. */
    data class UpdateFailed(val networkFailure: NetworkFailure?) : MainDatabaseSyncBadge

    data object AppUpdateRequired : MainDatabaseSyncBadge
}
