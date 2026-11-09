package ru.alarmtrade.specmp.feature.supportchat.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryCursor
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryPage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile

/** Соединением владеет SDK (подключение, переподключение, ожидание сети); на логауте его гасит сам SDK. */
interface SupportChatRepository {

    /**
     * Пока поток собирается, SDK держит соединение. Сам поток не завершается (обрывы — `Connection(state)`), кроме
     * `Response.Error` с `SupportChatUnavailableException`.
     */
    fun session(): Flow<Response<SupportChatEvent>>

    /** [cursor] `null` — самая новая страница; не зависит от состояния соединения. */
    suspend fun loadHistoryPage(cursor: SupportChatHistoryCursor?): Response<SupportChatHistoryPage>

    /** Вне `Ready` или без сети — `Response.Error` с `ApiError.NoConnection`. */
    suspend fun sendMessage(text: String): Response<Unit>
    suspend fun sendImage(file: SupportChatPickedFile): Response<Unit>
    suspend fun sendDocument(file: SupportChatPickedFile): Response<Unit>

    suspend fun downloadFile(url: String): Response<ByteArray>
    suspend fun markMessagesAsRead(ids: List<Long>): Response<Unit>
}
