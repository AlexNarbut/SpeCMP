package ru.alarmtrade.specmp.feature.main.domain.model

/** Поток синхронизации проходит [InProgress] и всегда завершается одним из [Finished]. */
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
