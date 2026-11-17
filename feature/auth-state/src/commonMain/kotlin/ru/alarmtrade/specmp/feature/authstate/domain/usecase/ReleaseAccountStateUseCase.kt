package ru.alarmtrade.specmp.feature.authstate.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.feature.authstate.domain.repository.SessionMarkRepository

/**
 * Сбрасывает состояние аккаунта ([AuthCloseable.release]) один раз на сессию: только если стоит отметка сессии, и
 * снимает её до сброса. Повторный вызов (кнопка «Выйти» и наблюдатель сессии) ничего не делает. Ошибка чтения
 * отметки — сбрасываем: лишний сброс безопаснее чужого токена. Сбой release() одного не мешает остальным.
 */
class ReleaseAccountStateUseCase(
    private val sessionMarkRepository: SessionMarkRepository,
    private val closeables: List<AuthCloseable>,
) {
    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    private val mutex = Mutex()

    suspend operator fun invoke(): Response<Unit> = mutex.withLock {
        if (sessionMarkRepository.isMarked().getValueIfSuccess() == false) return Response.Success(Unit)
        sessionMarkRepository.clear().onError { logger.w(it) { "Failed to clear the session mark" } }
        closeables.forEach { release(it) }
        logger.i { "Account state released" }
        Response.Success(Unit)
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun release(closeable: AuthCloseable) {
        try {
            closeable.release()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.w(e) { "Failed to release ${closeable::class.simpleName}" }
        }
    }
}
