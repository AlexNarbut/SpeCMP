package ru.alarmtrade.specmp.feature.login.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.model.ResetPasswordCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.SignUpCredentials

interface LoginRepository {
    suspend fun login(credentials: LoginCredentials): Response<LoginResult>
    suspend fun signUp(credentials: SignUpCredentials): Response<Unit>
    suspend fun resetPassword(credentials: ResetPasswordCredentials): Response<Unit>
    suspend fun resendActivationEmail(credentials: LoginCredentials): Response<Unit>
    suspend fun isLoggedIn(): Response<Boolean>
    suspend fun logout(): Response<Unit>
}
