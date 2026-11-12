package ru.alarmtrade.specmp.feature.main.data.repository

import ru.alarmtrade.specmp.feature.main.data.cache.MainUserCache
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile

internal class FakeMainUserCache : MainUserCache {
    var profile: MainUserProfile? = null
    var features: MainUserAppFeatures? = null
    var commercial: MainUserCommercial? = null

    override suspend fun profile(): MainUserProfile? = profile
    override suspend fun appFeatures(): MainUserAppFeatures? = features
    override suspend fun commercial(): MainUserCommercial? = commercial

    override suspend fun saveProfile(profile: MainUserProfile) {
        this.profile = profile
    }

    override suspend fun saveAppFeatures(features: MainUserAppFeatures) {
        this.features = features
    }

    override suspend fun saveCommercial(commercial: MainUserCommercial) {
        this.commercial = commercial
    }

    override suspend fun release() {
        profile = null
        features = null
        commercial = null
    }
}
