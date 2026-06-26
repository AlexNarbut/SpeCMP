package ru.alarmtrade.specmp.feature.login.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult

interface LoginRepository {
    suspend fun login(credentials: LoginCredentials): Response<LoginResult>
    suspend fun resendActivationEmail(credentials: LoginCredentials): Response<Unit>
    suspend fun isLoggedIn(): Response<Boolean>
}
