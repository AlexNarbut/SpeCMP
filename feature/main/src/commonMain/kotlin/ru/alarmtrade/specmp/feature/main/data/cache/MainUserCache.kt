package ru.alarmtrade.specmp.feature.main.data.cache

import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile

internal interface MainUserCache : AuthCloseable {
    suspend fun profile(): MainUserProfile?
    suspend fun appFeatures(): MainUserAppFeatures?
    suspend fun commercial(): MainUserCommercial?

    suspend fun saveProfile(profile: MainUserProfile)
    suspend fun saveAppFeatures(features: MainUserAppFeatures)
    suspend fun saveCommercial(commercial: MainUserCommercial)
}
