package ru.alarmtrade.specmp.feature.authstate.data.repository

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository
import ru.alarmtrade.specialist.interactors.auth.AuthInteractor
import kotlin.time.Duration.Companion.seconds

class SpecialistAuthStateRepository(
    private val authInteractor: AuthInteractor,
) : AuthStateRepository {

    // Заглушка: Specialist SDK пока не отдаёт Flow состояния авторизации,
    // поэтому наблюдение эмулируется периодическим опросом one-shot use case.
    // Когда в SDK появится flow-метод, заменить опрос на подписку.
    override fun observeIsLoggedIn(): Flow<Response<Boolean>> {
        return flow {
            while (currentCoroutineContext().isActive) {
                emit(checkIsLoggedIn())
                delay(POLL_INTERVAL)
            }
        }
    }

    override suspend fun logout(): Response<Unit> {
        return when (val response = authInteractor.logoutUseCase.execute()) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    private suspend fun checkIsLoggedIn(): Response<Boolean> {
        return when (val response = authInteractor.checkIfUserLoggedInUseCase.execute()) {
            is SpecialistResponse.Success -> Response.Success(response.value.boolean)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    private fun <T> SpecialistResponse.Error.toProjectError(): Response<T> {
        return Response.Error(
            exception = exception,
            message = message ?: exception.message,
        )
    }

    private companion object {
        val POLL_INTERVAL = 2.seconds
    }
}
