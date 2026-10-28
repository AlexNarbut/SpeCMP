package ru.alarmtrade.specmp.feature.main.presentation.model

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.common.AppError

@Immutable
sealed interface MainDatabaseSyncBadge {
    data object CheckingVersion : MainDatabaseSyncBadge

    data object Downloading : MainDatabaseSyncBadge

    data object Applying : MainDatabaseSyncBadge

    data object Updated : MainDatabaseSyncBadge

    /** [error] — общая причина сбоя синхронизации (нет связи…); null — прочая ошибка. */
    data class UpdateFailed(val error: AppError?) : MainDatabaseSyncBadge

    data object AppUpdateRequired : MainDatabaseSyncBadge
}
