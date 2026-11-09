package ru.alarmtrade.specmp.feature.login.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.interactors.auth.AuthInteractor
import ru.alarmtrade.specialist.interactors.auth.requests.LoginRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResendActivationEmailRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResetPasswordRequest
import ru.alarmtrade.specialist.interactors.auth.requests.SignUpRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.specialist.SpecialistInitializer

internal interface LoginDataSource {
    suspend fun syncClientInfo(): Response<Unit>
    suspend fun login(request: LoginRequest): SpecialistResponse<Unit>
    suspend fun signUp(request: SignUpRequest): SpecialistResponse<Unit>
    suspend fun resetPassword(request: ResetPasswordRequest): SpecialistResponse<Unit>
    suspend fun resendActivationEmail(request: ResendActivationEmailRequest): SpecialistResponse<Unit>
}

internal class SpecialistLoginDataSource(
    private val authInteractor: AuthInteractor,
    private val specialistInitializer: SpecialistInitializer,
) : LoginDataSource {
    override suspend fun syncClientInfo(): Response<Unit> = specialistInitializer.syncClientInfo()

    override suspend fun login(request: LoginRequest): SpecialistResponse<Unit> =
        authInteractor.loginUseCase.execute(request)

    override suspend fun signUp(request: SignUpRequest): SpecialistResponse<Unit> =
        authInteractor.signUpUseCase.execute(request)

    override suspend fun resetPassword(request: ResetPasswordRequest): SpecialistResponse<Unit> =
        authInteractor.resetPasswordUseCase.execute(request)

    override suspend fun resendActivationEmail(request: ResendActivationEmailRequest): SpecialistResponse<Unit> =
        authInteractor.resendActivationEmailUseCase.execute(request)
}
