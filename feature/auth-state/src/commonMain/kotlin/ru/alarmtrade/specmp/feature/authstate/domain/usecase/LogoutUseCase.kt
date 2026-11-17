package ru.alarmtrade.specmp.feature.authstate.domain.usecase

import co.touchlab.kermit.Logger
import kotlin.time.measureTimedValue
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository

/**
 * После успешного выхода сбрасывает состояние аккаунта сам, не дожидаясь наблюдателя сессии: к возврату сброс
 * закончен. Повторного сброса нет — [ReleaseAccountStateUseCase] срабатывает один раз на сессию.
 */
class LogoutUseCase(
    private val authStateRepository: AuthStateRepository,
    private val releaseAccountState: ReleaseAccountStateUseCase,
) {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    suspend operator fun invoke(): Response<Unit> = when (val response = timedLogout()) {
        is Response.Success -> releaseAccountState()
        is Response.Error -> response
    }

    // Время выхода в логе: SDK стирает токены сразу, сервер уведомляет в фоне — долгий выход здесь был бы виден.
    private suspend fun timedLogout(): Response<Unit> {
        val (response, elapsed) = measureTimedValue { authStateRepository.logout() }
        val result = if (response is Response.Success) "succeeded" else "failed"
        logger.i { "Logout $result in ${elapsed.inWholeMilliseconds} ms" }
        return response
    }
}
