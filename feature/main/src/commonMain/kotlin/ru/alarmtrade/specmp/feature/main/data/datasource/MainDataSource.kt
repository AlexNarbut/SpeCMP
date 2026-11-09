package ru.alarmtrade.specmp.feature.main.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.LongWrapper
import ru.alarmtrade.specialist.domain.appUpdates.AppUpdatesResponse
import ru.alarmtrade.specialist.domain.appUpdates.SettingsUpdatingStatus
import ru.alarmtrade.specialist.domain.chat.ChatAvailability
import ru.alarmtrade.specialist.domain.user.UserAppFeature
import ru.alarmtrade.specialist.domain.user.UserCommercial
import ru.alarmtrade.specialist.interactors.appUpdates.AppUpdatesInteractor
import ru.alarmtrade.specialist.interactors.appUpdates.requests.AppUpdateRequest
import ru.alarmtrade.specialist.interactors.chat.ChatInteractor
import ru.alarmtrade.specialist.interactors.user.UserInteractor

internal interface MainDataSource {
    suspend fun currentSettingsVersion(): SpecialistResponse<LongWrapper>
    fun synchronizeSettingsVersion(): Flow<SpecialistResponse<SettingsUpdatingStatus>>
    suspend fun checkForAppUpdates(request: AppUpdateRequest): SpecialistResponse<AppUpdatesResponse>
    suspend fun userAppFeatures(): SpecialistResponse<List<UserAppFeature>>
    suspend fun userCommercial(): SpecialistResponse<UserCommercial?>
    suspend fun chatAvailability(): SpecialistResponse<ChatAvailability>
}

internal class SpecialistMainDataSource(
    private val appUpdatesInteractor: AppUpdatesInteractor,
    private val userInteractor: UserInteractor,
    private val chatInteractor: ChatInteractor,
) : MainDataSource {
    override suspend fun currentSettingsVersion(): SpecialistResponse<LongWrapper> =
        appUpdatesInteractor.getCurrentSettingsVersionUseCase.execute()

    override fun synchronizeSettingsVersion(): Flow<SpecialistResponse<SettingsUpdatingStatus>> = flow {
        emitAll(appUpdatesInteractor.synchronizeSettingsVersionUseCase.execute())
    }

    override suspend fun checkForAppUpdates(request: AppUpdateRequest): SpecialistResponse<AppUpdatesResponse> =
        appUpdatesInteractor.checkForAppUpdatesUseCase.execute(request)

    override suspend fun userAppFeatures(): SpecialistResponse<List<UserAppFeature>> =
        userInteractor.getUserAppFeaturesUseCase.execute()

    override suspend fun userCommercial(): SpecialistResponse<UserCommercial?> =
        userInteractor.getUserCommercialUseCase.execute()

    override suspend fun chatAvailability(): SpecialistResponse<ChatAvailability> =
        chatInteractor.isAvailableUseCase.execute()
}
