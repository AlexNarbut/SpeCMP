package ru.alarmtrade.specmp.feature.main.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile

interface MainRepository {
    suspend fun currentUserProfile(): Response<MainUserProfile>
    suspend fun cachedUserProfile(): Response<MainUserProfile?>
    suspend fun cachedUserAppFeatures(): Response<MainUserAppFeatures?>
    suspend fun cachedUserCommercial(): Response<MainUserCommercial?>
    suspend fun currentSettingsVersion(): Response<Long?>

    fun synchronizeSettingsVersion(): Flow<Response<MainDatabaseSyncStatus>>
    suspend fun currentUserAppFeatures(): Response<MainUserAppFeatures>
    suspend fun currentUserCommercial(): Response<MainUserCommercial>
    fun observeChatAvailability(): Flow<Response<Boolean>>
}
