package ru.alarmtrade.specmp.feature.main.domain.model

/**
 * Обобщённые под-состояния процесса синхронизации базы настроек.
 *
 * Поток синхронизации проходит стадии [InProgress] и всегда завершается
 * одним из [Finished]-состояний (SDK-стадии маппятся в data-слое).
 */
sealed interface MainDatabaseSyncStatus {

    sealed interface InProgress : MainDatabaseSyncStatus {
        data object CheckingVersion : InProgress
        data object Downloading : InProgress
        data object Applying : InProgress
    }

    sealed interface Finished : MainDatabaseSyncStatus {
        data class Success(val newVersion: Long) : Finished
        data object NoUpdates : Finished
        data class NotCompatible(val minRequiredAppVersion: Int) : Finished
    }
}
