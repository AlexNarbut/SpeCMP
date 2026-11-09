package ru.alarmtrade.specmp.feature.authstate.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.auth.domain.AuthState as SdkAuthState
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.interactors.auth.AuthInteractor

internal interface AuthStateDataSource {
    fun authStates(): Flow<SpecialistResponse<SdkAuthState>>
    suspend fun logout(): SpecialistResponse<Unit>
}

internal class SpecialistAuthStateDataSource(private val authInteractor: AuthInteractor) : AuthStateDataSource {
    override fun authStates(): Flow<SpecialistResponse<SdkAuthState>> = flow {
        emitAll(authInteractor.listenAuthFlowUseCase.execute())
    }

    override suspend fun logout(): SpecialistResponse<Unit> = authInteractor.logoutUseCase.execute()
}
