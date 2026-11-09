package ru.alarmtrade.specmp.feature.login.data.repository

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.errors.UserIsNotConfirmed
import ru.alarmtrade.specialist.interactors.auth.requests.LoginRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResendActivationEmailRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResetPasswordRequest
import ru.alarmtrade.specialist.interactors.auth.requests.SignUpRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.login.data.datasource.LoginDataSource
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.model.ResetPasswordCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.SignUpCredentials
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

internal class SpecialistLoginRepository(private val dataSource: LoginDataSource) : LoginRepository {

    override suspend fun login(credentials: LoginCredentials): Response<LoginResult> =
        dataSource.syncClientInfo().mapIfSuccess {
            when (val response = dataSource.login(credentials.toLoginRequest())) {
                is SpecialistResponse.Success -> Response.Success(LoginResult.Authorized)

                is SpecialistResponse.Error -> {
                    if (response.exception is UserIsNotConfirmed) {
                        Response.Success(LoginResult.ActivationRequired)
                    } else {
                        response.toResponse()
                    }
                }
            }
        }

    override suspend fun signUp(credentials: SignUpCredentials): Response<Unit> =
        dataSource.signUp(credentials.toSignUpRequest()).toResponse()

    override suspend fun resetPassword(credentials: ResetPasswordCredentials): Response<Unit> =
        dataSource.resetPassword(credentials.toResetPasswordRequest()).toResponse()

    override suspend fun resendActivationEmail(credentials: LoginCredentials): Response<Unit> =
        dataSource.syncClientInfo().mapIfSuccess {
            dataSource.resendActivationEmail(credentials.toResendActivationEmailRequest()).toResponse()
        }

    private fun LoginCredentials.toLoginRequest(): LoginRequest = LoginRequest(
        username = normalizedLogin,
        password = password,
    )

    private fun SignUpCredentials.toSignUpRequest(): SignUpRequest = SignUpRequest(
        email = normalizedEmail,
        password = password,
        fullName = normalizedFullName,
        phoneNumber = normalizedPhoneNumber,
    )

    private fun ResetPasswordCredentials.toResetPasswordRequest(): ResetPasswordRequest = ResetPasswordRequest(
        login = normalizedEmail,
    )

    private fun LoginCredentials.toResendActivationEmailRequest(): ResendActivationEmailRequest =
        ResendActivationEmailRequest(
            email = normalizedLogin,
            password = password,
        )
}
