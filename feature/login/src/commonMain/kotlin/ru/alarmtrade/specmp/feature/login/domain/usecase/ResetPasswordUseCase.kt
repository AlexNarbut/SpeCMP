package ru.alarmtrade.specmp.feature.login.domain.usecase

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.login.domain.model.AuthValidationException
import ru.alarmtrade.specmp.feature.login.domain.model.ResetPasswordCredentials
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

class ResetPasswordUseCase(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(credentials: ResetPasswordCredentials): Response<Unit> {
        credentials.validationError()?.let { error ->
            return Response.Error(AuthValidationException(error))
        }

        return repository.resetPassword(credentials)
    }
}
