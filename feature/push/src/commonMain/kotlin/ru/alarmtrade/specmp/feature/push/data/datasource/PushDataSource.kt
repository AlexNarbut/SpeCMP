package ru.alarmtrade.specmp.feature.push.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.interactors.user.UserInteractor
import ru.alarmtrade.specialist.interactors.user.requests.SetPushRulesRequest
import ru.alarmtrade.specialist.interactors.user.requests.UpdatePushTokenRequest

internal interface PushDataSource {
    suspend fun updatePushToken(request: UpdatePushTokenRequest): SpecialistResponse<Unit>

    suspend fun getPushRules(): SpecialistResponse<Set<Int>>

    suspend fun setPushRules(request: SetPushRulesRequest): SpecialistResponse<Unit>
}

internal class SpecialistPushDataSource(private val userInteractor: UserInteractor) : PushDataSource {
    override suspend fun updatePushToken(request: UpdatePushTokenRequest): SpecialistResponse<Unit> =
        userInteractor.updatePushTokenUseCase.execute(request)

    override suspend fun getPushRules(): SpecialistResponse<Set<Int>> = userInteractor.getPushRulesUseCase.execute()

    override suspend fun setPushRules(request: SetPushRulesRequest): SpecialistResponse<Unit> =
        userInteractor.setPushRulesUseCase.execute(request)
}
