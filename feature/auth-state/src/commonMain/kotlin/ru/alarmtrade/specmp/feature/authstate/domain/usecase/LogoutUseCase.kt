package ru.alarmtrade.specmp.feature.authstate.domain.usecase

import co.touchlab.kermit.Logger
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository

/**
 * Точка очистки состояния при логауте.
 *
 * Выполняет logout и только после успеха проходит по всем зарегистрированным
 * в DI [AuthCloseable] (app-scoped синглтоны: чат-репозиторий и будущие
 * холдеры сессий), вызывая [AuthCloseable.release]. Ошибки очистки не
 * отменяют logout: они логируются и проглатываются — сессия уже закрыта.
 */
class LogoutUseCase(
    private val authStateRepository: AuthStateRepository,
    private val closeables: List<AuthCloseable>,
) {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    suspend fun execute(): Response<Unit> {
        return when (val response = authStateRepository.logout()) {
            is Response.Success -> {
                closeables.forEach { closeable ->
                    try {
                        closeable.release()
                    } catch (e: Exception) {
                        logger.d(e) { "Failed to release ${closeable::class.simpleName} on logout" }
                    }
                }
                Response.Success(Unit)
            }

            is Response.Error -> response
        }
    }
}
