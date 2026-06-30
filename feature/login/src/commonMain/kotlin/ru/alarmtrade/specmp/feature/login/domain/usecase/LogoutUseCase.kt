package ru.alarmtrade.specmp.feature.login.domain.usecase

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

class LogoutUseCase(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(): Response<Unit> = repository.logout()
}
