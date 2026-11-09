package ru.alarmtrade.specmp.feature.account.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.user.User
import ru.alarmtrade.specialist.interactors.user.UserInteractor

internal interface AccountDataSource {
    suspend fun currentUser(): SpecialistResponse<User>
}

internal class SpecialistAccountDataSource(private val userInteractor: UserInteractor) : AccountDataSource {
    override suspend fun currentUser(): SpecialistResponse<User> = userInteractor.getUserUseCase.execute()
}
