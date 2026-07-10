package ru.alarmtrade.specmp.feature.main.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateCheckResult
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppVersion
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile

interface MainRepository {
    val appVersion: MainAppVersion

    suspend fun currentUserProfile(): Response<MainUserProfile>
    suspend fun currentSettingsVersion(): Response<Long?>
    suspend fun synchronizeSettingsVersion(): Response<Long?>
    suspend fun checkAppUpdate(): Response<MainAppUpdateCheckResult>
    suspend fun currentUserAppFeatures(): Response<MainUserAppFeatures>
    suspend fun currentUserCommercial(): Response<MainUserCommercial>
    suspend fun chatAvailability(): Response<Boolean>
    suspend fun unreadMessagesCount(): Response<Int>
}
