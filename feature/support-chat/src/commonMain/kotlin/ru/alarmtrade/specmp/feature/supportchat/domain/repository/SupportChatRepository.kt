package ru.alarmtrade.specmp.feature.supportchat.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatPickedFile

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
    suspend fun loadNextMessages(): Response<Unit>
    suspend fun sendMessage(text: String): Response<Unit>
    suspend fun sendImage(file: SupportChatPickedFile): Response<Unit>
    suspend fun sendDocument(file: SupportChatPickedFile): Response<Unit>
    suspend fun downloadFile(url: String): Response<ByteArray>
    suspend fun markMessagesAsRead(ids: List<Long>): Response<Unit>
    suspend fun startWriting(): Response<Unit>
    suspend fun stopWriting(): Response<Unit>
}
