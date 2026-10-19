package ru.alarmtrade.specmp.feature.supportchat.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryCursor
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryPage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile

/**
 * Контракт чата техподдержки — тонкая обёртка над SDK. Соединением (подключение, подписка
 * комнаты, переподключение, ожидание сети) владеет SDK; вызывающий решает, когда чат нужен.
 *
 * Реализация — app-scoped синглтон и наследует [AuthCloseable] (`LogoutUseCase`).
 */
interface SupportChatRepository : AuthCloseable {

    /**
     * Чат-сессия: пока поток собирается, SDK держит соединение; отмена сбора его отпускает.
     *
     * Поток не завершается сам: обрывы и переподключения приходят как `Connection(state)`.
     * Исключение — `Connection(Unavailable)` (сессию не удалось начать): финал, поток завершается.
     */
    fun session(): Flow<SupportChatEvent>

    /**
     * Страница истории: [cursor] `null` — самая новая. Не зависит от состояния соединения.
     */
    suspend fun loadHistoryPage(cursor: SupportChatHistoryCursor?): Response<SupportChatHistoryPage>

    /**
     * Вне `Ready` — `Response.Error` с
     * [ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatNotConnectedException].
     */
    suspend fun sendMessage(text: String): Response<Unit>
    suspend fun sendImage(file: SupportChatPickedFile): Response<Unit>
    suspend fun sendDocument(file: SupportChatPickedFile): Response<Unit>

    suspend fun downloadFile(url: String): Response<ByteArray>
    suspend fun markMessagesAsRead(ids: List<Long>): Response<Unit>
}
