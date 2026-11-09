package ru.alarmtrade.specmp.feature.authstate.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import ru.alarmtrade.common.auth.domain.AuthState as SdkAuthState
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.interactors.auth.AuthInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository

internal class SpecialistAuthStateRepository(private val authInteractor: AuthInteractor) : AuthStateRepository {

    // Состояние сессии SDK держит в хранилище токенов: вход и выход меняют его сразу, без опроса.
    override fun observeIsLoggedIn(): Flow<Response<Boolean>> = flow {
        emitAll(
            authInteractor.listenAuthFlowUseCase.execute().mapNotNull { response ->
                when (response) {
                    is SpecialistResponse.Success -> when (response.value) {
                        is SdkAuthState.Authed -> Response.Success(true)
                        SdkAuthState.NoAuthed -> Response.Success(false)
                        SdkAuthState.Waiting -> null
                    }

                    is SpecialistResponse.Error -> response.toResponse()
                }
            },
        )
    }

    override suspend fun logout(): Response<Unit> = authInteractor.logoutUseCase.execute().toResponse().map { }
}
