package ru.alarmtrade.specmp.feature.supportchat.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryPage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile

/**
 * Контракт чат-сессии техподдержки.
 *
 * Реализация — app-scoped синглтон и наследует [AuthCloseable]: сессия обязана
 * закрываться при логауте через `release()` (`LogoutUseCase`), а не переживать
 * смену аккаунта со своим состоянием.
 */
interface SupportChatRepository : AuthCloseable {
    fun observeEvents(): Flow<Response<SupportChatEvent>>
    suspend fun openChat(): Response<Unit>
    suspend fun closeChat(): Response<Unit>

    /**
     * Загружает следующую страницу истории и возвращает её вызывающему.
     *
     * Гарантированный финал: успех означает «страница получена», ошибка или
     * таймаут возвращаются как `Response.Error` — не бывает состояния «запрос
     * ушёл, жди событие». SDK публикует страницу асинхронно в общий поток
     * событий; репозиторий маршрутизирует её в этот вызов (с таймаутом).
     */
    suspend fun loadNextMessages(): Response<SupportChatHistoryPage>
    suspend fun sendMessage(text: String): Response<Unit>
    suspend fun sendImage(file: SupportChatPickedFile): Response<Unit>
    suspend fun sendDocument(file: SupportChatPickedFile): Response<Unit>
    suspend fun downloadFile(url: String): Response<ByteArray>
    suspend fun markMessagesAsRead(ids: List<Long>): Response<Unit>
}
