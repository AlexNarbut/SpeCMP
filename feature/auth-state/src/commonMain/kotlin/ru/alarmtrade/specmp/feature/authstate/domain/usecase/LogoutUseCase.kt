package ru.alarmtrade.specmp.feature.authstate.domain.usecase

import co.touchlab.kermit.Logger
import kotlin.time.measureTimedValue
import kotlinx.coroutines.CancellationException
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
class LogoutUseCase(private val authStateRepository: AuthStateRepository, private val closeables: List<AuthCloseable>) {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    // Сбой release() одного AuthCloseable не должен сорвать выход: ловим всё, кроме отмены, и логируем
    @Suppress("TooGenericExceptionCaught")
    suspend fun execute(): Response<Unit> = when (val response = timedLogout()) {
        is Response.Success -> {
            closeables.forEach { closeable ->
                try {
                    closeable.release()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.d(e) { "Failed to release ${closeable::class.simpleName} on logout" }
                }
            }
            Response.Success(Unit)
        }

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
