package ru.alarmtrade.specmp.feature.login.domain.usecase

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

class LoginUseCase(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(credentials: LoginCredentials): Response<LoginResult> {
        if (!credentials.isValid()) {
            return Response.Error(
                IllegalArgumentException(),
            )
        }
        return repository.login(credentials)
    }
}
