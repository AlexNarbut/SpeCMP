package ru.alarmtrade.specmp.feature.login.data.repository

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.errors.UserIsNotConfirmed
import ru.alarmtrade.specialist.interactors.auth.AuthInteractor
import ru.alarmtrade.specialist.interactors.auth.requests.LoginRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResendActivationEmailRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResetPasswordRequest
import ru.alarmtrade.specialist.interactors.auth.requests.SignUpRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.core.specialist.SpecialistInitializer
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.model.ResetPasswordCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.SignUpCredentials
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

class SpecialistLoginRepository(
    private val authInteractor: AuthInteractor,
    private val specialistInitializer: SpecialistInitializer
) : LoginRepository {

    override suspend fun login(credentials: LoginCredentials): Response<LoginResult> =
        saveDeviceInformation().mapIfSuccess {
            when (val response = authInteractor.loginUseCase.execute(credentials.toLoginRequest())) {
                is SpecialistResponse.Success -> Response.Success(LoginResult.Authorized)

                is SpecialistResponse.Error -> {
                    if (response.exception is UserIsNotConfirmed) {
                        Response.Success(LoginResult.ActivationRequired)
                    } else {
                        response.toProjectError()
                    }
                }
            }
        }

    override suspend fun signUp(credentials: SignUpCredentials): Response<Unit> =
        when (val response = authInteractor.signUpUseCase.execute(credentials.toSignUpRequest())) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError()
        }

    override suspend fun resetPassword(credentials: ResetPasswordCredentials): Response<Unit> = when (
        val response = authInteractor.resetPasswordUseCase.execute(
            credentials.toResetPasswordRequest()
        )
    ) {
        is SpecialistResponse.Success -> Response.Success(Unit)
        is SpecialistResponse.Error -> response.toProjectError()
    }

    override suspend fun resendActivationEmail(credentials: LoginCredentials): Response<Unit> =
        saveDeviceInformation().mapIfSuccess {
            when (
                val response = authInteractor.resendActivationEmailUseCase.execute(
                    credentials.toResendActivationEmailRequest()
                )
            ) {
                is SpecialistResponse.Success -> Response.Success(Unit)
                is SpecialistResponse.Error -> response.toProjectError()
            }
        }

    private fun LoginCredentials.toLoginRequest(): LoginRequest = LoginRequest(
        username = normalizedLogin,
        password = password
    )

    private fun SignUpCredentials.toSignUpRequest(): SignUpRequest = SignUpRequest(
        email = normalizedEmail,
        password = password,
        fullName = normalizedFullName,
        phoneNumber = normalizedPhoneNumber
    )

    private fun ResetPasswordCredentials.toResetPasswordRequest(): ResetPasswordRequest = ResetPasswordRequest(
        login = normalizedEmail
    )

    private fun LoginCredentials.toResendActivationEmailRequest(): ResendActivationEmailRequest =
        ResendActivationEmailRequest(
            email = normalizedLogin,
            password = password
        )

    private suspend fun saveDeviceInformation(): Response<Unit> = specialistInitializer.syncClientInfo()

    private fun <T> SpecialistResponse.Error.toProjectError(): Response<T> = Response.Error(
        exception = exception,
        message = message ?: exception.message
    )
}
