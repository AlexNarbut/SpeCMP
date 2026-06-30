package ru.alarmtrade.specmp.feature.login.domain.usecase

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.login.domain.model.AuthValidationException
import ru.alarmtrade.specmp.feature.login.domain.model.SignUpCredentials
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

class SignUpUseCase(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(credentials: SignUpCredentials): Response<Unit> {
        credentials.validationError()?.let { error ->
            return Response.Error(AuthValidationException(error))
        }

        return repository.signUp(credentials)
    }
}
