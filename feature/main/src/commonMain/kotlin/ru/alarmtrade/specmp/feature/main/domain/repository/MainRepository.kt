package ru.alarmtrade.specmp.feature.main.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateCheckResult
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppVersion
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile

interface MainRepository {
    val appVersion: MainAppVersion

    suspend fun currentUserProfile(): Response<MainUserProfile>
    suspend fun currentSettingsVersion(): Response<Long?>

    /**
     * Процесс синхронизации базы настроек: стадии [MainDatabaseSyncStatus.InProgress],
     * завершается одним из [MainDatabaseSyncStatus.Finished].
     */
    fun synchronizeSettingsVersion(): Flow<Response<MainDatabaseSyncStatus>>
    suspend fun checkAppUpdate(): Response<MainAppUpdateCheckResult>
    suspend fun currentUserAppFeatures(): Response<MainUserAppFeatures>
    suspend fun currentUserCommercial(): Response<MainUserCommercial>
    fun observeChatAvailability(): Flow<Response<Boolean>>
    fun observeUnreadMessagesCount(): Flow<Response<Int>>
}
