package ru.alarmtrade.specmp.feature.login.data.repository

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.interactors.auth.requests.LoginRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResendActivationEmailRequest
import ru.alarmtrade.specialist.interactors.auth.requests.ResetPasswordRequest
import ru.alarmtrade.specialist.interactors.auth.requests.SignUpRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.login.data.datasource.LoginDataSource

internal class FakeLoginDataSource : LoginDataSource {
    var syncResult: Response<Unit> = Response.Success(Unit)
    var result: SpecialistResponse<Unit> = SpecialistResponse.Success(Unit)

    val requests = mutableListOf<Any>()
    var syncCalls = 0
        private set

    override suspend fun syncClientInfo(): Response<Unit> {
        syncCalls++
        return syncResult
    }

    override suspend fun login(request: LoginRequest): SpecialistResponse<Unit> = record(request)

    override suspend fun signUp(request: SignUpRequest): SpecialistResponse<Unit> = record(request)

    override suspend fun resetPassword(request: ResetPasswordRequest): SpecialistResponse<Unit> = record(request)

    override suspend fun resendActivationEmail(request: ResendActivationEmailRequest): SpecialistResponse<Unit> =
        record(request)

    private fun record(request: Any): SpecialistResponse<Unit> {
        requests += request
        return result
    }
}
