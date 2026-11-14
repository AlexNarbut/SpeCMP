package ru.alarmtrade.specmp.feature.main.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.LongWrapper
import ru.alarmtrade.specialist.domain.appUpdates.SettingsUpdatingStatus
import ru.alarmtrade.specialist.domain.chat.ChatAvailability
import ru.alarmtrade.specialist.domain.user.UserAppFeature
import ru.alarmtrade.specialist.domain.user.UserCommercial
import ru.alarmtrade.specmp.feature.main.data.datasource.MainDataSource

internal class FakeMainDataSource : MainDataSource {
    var settingsVersion: SpecialistResponse<LongWrapper> = SpecialistResponse.Success(LongWrapper(1))
    var synchronization: Flow<SpecialistResponse<SettingsUpdatingStatus>> = flowOf()
    var appFeatures: SpecialistResponse<List<UserAppFeature>> = SpecialistResponse.Success(emptyList())
    var commercial: SpecialistResponse<UserCommercial?> = SpecialistResponse.Success(null)
    val chatAvailabilities = ArrayDeque<SpecialistResponse<ChatAvailability>>()

    override suspend fun currentSettingsVersion(): SpecialistResponse<LongWrapper> = settingsVersion

    override fun synchronizeSettingsVersion(): Flow<SpecialistResponse<SettingsUpdatingStatus>> = synchronization

    override suspend fun userAppFeatures(): SpecialistResponse<List<UserAppFeature>> = appFeatures

    override suspend fun userCommercial(): SpecialistResponse<UserCommercial?> = commercial

    override suspend fun chatAvailability(): SpecialistResponse<ChatAvailability> = chatAvailabilities.removeFirst()
}
