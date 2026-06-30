package ru.alarmtrade.specmp.feature.login.domain.usecase

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

class ResendActivationEmailUseCase(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(credentials: LoginCredentials): Response<Unit> {
        if (!credentials.isValid()) {
            return Response.Error(
                IllegalArgumentException(),
            )
        }
        return repository.resendActivationEmail(credentials)
    }
}
