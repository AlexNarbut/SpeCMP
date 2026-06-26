package ru.alarmtrade.specmp.feature.login.data.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

class UnavailableLoginRepository : LoginRepository {
    override suspend fun login(credentials: LoginCredentials): Response<LoginResult> {
        return unavailable()
    }

    override suspend fun resendActivationEmail(credentials: LoginCredentials): Response<Unit> {
        return unavailable()
    }

    override suspend fun isLoggedIn(): Response<Boolean> {
        return Response.Success(false)
    }

    private fun <T> unavailable(): Response<T> {
        return Response.Error(
            exception = UnsupportedOperationException("Login is not available on this platform"),
            message = "Авторизация недоступна на этой платформе",
        )
    }
}
