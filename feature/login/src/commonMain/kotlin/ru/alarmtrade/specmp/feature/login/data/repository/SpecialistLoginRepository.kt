package ru.alarmtrade.specmp.feature.login.data.repository

import ru.alarmtrade.common.app.interactors.appInformation.requests.SaveAppDeviceInformationRequest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.core.specialist.SpecialistDeviceInformationProvider
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.model.ResetPasswordCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.SignUpCredentials
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository
import ru.alarmtrade.specialist.domain.errors.UserIsNotConfirmed
import ru.alarmtrade.specialist.interactors.appDevice.AppDeviceInformationInteractor
import ru.alarmtrade.specialist.interactors.auth.AuthInteractor
import ru.alarmtrade.specialist.interactors.auth.requests.LoginRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResendActivationEmailRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResetPasswordRequest
import ru.alarmtrade.specialist.interactors.auth.requests.SignUpRequest

class SpecialistLoginRepository(
    private val authInteractor: AuthInteractor,
    private val appDeviceInformationInteractor: AppDeviceInformationInteractor,
    private val deviceInformationProvider: SpecialistDeviceInformationProvider,
) : LoginRepository {

    override suspend fun login(credentials: LoginCredentials): Response<LoginResult> {
        return saveDeviceInformation().mapIfSuccess {
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
    }

    override suspend fun signUp(credentials: SignUpCredentials): Response<Unit> {
        return when (val response = authInteractor.signUpUseCase.execute(credentials.toSignUpRequest())) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    override suspend fun resetPassword(credentials: ResetPasswordCredentials): Response<Unit> {
        return when (
            val response = authInteractor.resetPasswordUseCase.execute(
                credentials.toResetPasswordRequest()
            )
        ) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    override suspend fun resendActivationEmail(credentials: LoginCredentials): Response<Unit> {
        return saveDeviceInformation().mapIfSuccess {
            when (
                val response = authInteractor.resendActivationEmailUseCase.execute(
                    credentials.toResendActivationEmailRequest()
                )
            ) {
                is SpecialistResponse.Success -> Response.Success(Unit)
                is SpecialistResponse.Error -> response.toProjectError()
            }
        }
    }

    private fun LoginCredentials.toLoginRequest(): LoginRequest {
        return LoginRequest(
            username = normalizedLogin,
            password = password,
        )
    }

    private fun SignUpCredentials.toSignUpRequest(): SignUpRequest {
        return SignUpRequest(
            email = normalizedEmail,
            password = password,
            fullName = normalizedFullName,
            phoneNumber = normalizedPhoneNumber,
        )
    }

    private fun ResetPasswordCredentials.toResetPasswordRequest(): ResetPasswordRequest {
        return ResetPasswordRequest(
            login = normalizedEmail,
        )
    }

    private fun LoginCredentials.toResendActivationEmailRequest(): ResendActivationEmailRequest {
        return ResendActivationEmailRequest(
            email = normalizedLogin,
            password = password,
        )
    }

    private suspend fun saveDeviceInformation(): Response<Unit> {
        return when (
            val response = appDeviceInformationInteractor.saveAppDeviceInformationUseCase.execute(
                SaveAppDeviceInformationRequest(deviceInformationProvider.create())
            )
        ) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    private fun <T> SpecialistResponse.Error.toProjectError(): Response<T> {
        return Response.Error(
            exception = exception,
            message = message ?: exception.message,
        )
    }
}
