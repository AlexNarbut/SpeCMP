package ru.alarmtrade.specmp.feature.login.presentation.screen

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.model.ResetPasswordCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.SignUpCredentials
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

class FakeLoginRepository : LoginRepository {
    var loginResponse: Response<LoginResult> = Response.Success(LoginResult.Authorized)
    var signUpResponse: Response<Unit> = Response.Success(Unit)
    var resetPasswordResponse: Response<Unit> = Response.Success(Unit)
    var resendResponse: Response<Unit> = Response.Success(Unit)

    val calls = mutableListOf<Any>()

    override suspend fun login(credentials: LoginCredentials): Response<LoginResult> {
        calls += credentials
        return loginResponse
    }

    override suspend fun signUp(credentials: SignUpCredentials): Response<Unit> {
        calls += credentials
        return signUpResponse
    }

    override suspend fun resetPassword(credentials: ResetPasswordCredentials): Response<Unit> {
        calls += credentials
        return resetPasswordResponse
    }

    override suspend fun resendActivationEmail(credentials: LoginCredentials): Response<Unit> {
        calls += RESEND to credentials
        return resendResponse
    }

    companion object {
        const val RESEND = "resend"
    }
}
