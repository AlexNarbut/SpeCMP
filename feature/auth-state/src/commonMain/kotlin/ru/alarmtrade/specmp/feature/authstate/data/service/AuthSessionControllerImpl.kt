package ru.alarmtrade.specmp.feature.authstate.data.service

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository
import ru.alarmtrade.specmp.feature.authstate.domain.repository.SessionMarkRepository
import ru.alarmtrade.specmp.feature.authstate.domain.service.AuthSessionController
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.ReleaseAccountStateUseCase

internal class AuthSessionControllerImpl(
    private val authStateRepository: AuthStateRepository,
    private val sessionMarkRepository: SessionMarkRepository,
    private val releaseAccountState: ReleaseAccountStateUseCase,
    private val scope: CoroutineScope,
    private val dispatchers: AppDispatchers,
) : AuthSessionController {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    private val started = MutableStateFlow(false)

    override fun start() {
        if (!started.compareAndSet(expect = false, update = true)) return
        scope.launch(dispatchers.io) {
            authStateRepository.observeIsLoggedIn().collect { response ->
                if (response is Response.Success) onSession(loggedIn = response.value)
            }
        }
    }

    private suspend fun onSession(loggedIn: Boolean) {
        if (loggedIn) {
            sessionMarkRepository.mark().onError { logger.w(it) { "Failed to mark the session" } }
        } else {
            releaseAccountState().onError { logger.w(it) { "Failed to release the account state" } }
        }
    }
}
