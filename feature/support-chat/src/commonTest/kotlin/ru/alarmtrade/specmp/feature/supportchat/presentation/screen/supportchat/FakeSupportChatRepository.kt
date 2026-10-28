package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatConnectionState
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryCursor
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryPage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository

/**
 * Фейк репозитория. Сессия ведёт себя как заявка SDK: живёт, пока собирается, не завершается
 * сама; состояние соединения тест переключает вручную ([connect] / [disconnect]).
 */
class FakeSupportChatRepository : SupportChatRepository {

    // События внутри активной сессии (сообщения, прочтение, набор, смены состояния соединения).
    private val events = MutableSharedFlow<SupportChatEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    // false — сессия остаётся в Connecting, пока тест не вызовет connect().
    var autoConnect = true

    // true — сессию не удаётся начать (Unavailable, поток завершается).
    var sessionUnavailable = false

    // Страница истории на любой запрос; null — ответ loadHistoryError.
    var historyPageOnLoad: List<SupportChatMessage>? = emptyList()
    var loadHistoryError: Exception = Exception("History load failed")
    var sendMessageResult: Response<Unit> = Response.Success(Unit)
    var sendImageResult: Response<Unit> = Response.Success(Unit)
    var sendDocumentResult: Response<Unit> = Response.Success(Unit)
    var markMessagesAsReadResult: Response<Unit> = Response.Success(Unit)
    var downloadFileResult: Response<ByteArray> = Response.Success(byteArrayOf(1, 2, 3))
    var outgoingDelayMillis = 0L

    // Следующая отправка «обрывается»: соединение теряется посреди запроса, вызов — ошибка.
    var disconnectDuringNextSend = false

    // Задержка ответа loadHistoryPage (гонки пагинации с первичной загрузкой).
    var loadDelayMillis = 0L

    val sentMessages = mutableListOf<String>()
    val sentImages = mutableListOf<SupportChatPickedFile>()
    val sentDocuments = mutableListOf<SupportChatPickedFile>()
    val markedAsRead = mutableListOf<List<Long>>()
    val connectionCalls = mutableListOf<String>()
    val historyCursors = mutableListOf<SupportChatHistoryCursor?>()
    val loadHistoryCallCount: Int
        get() = historyCursors.size
    var downloadFileCallCount = 0
        private set

    // Число начатых сессий (заявок) и отпущенных.
    var sessionCount = 0
        private set
    var releasedSessionCount = 0
        private set
    var maxConcurrentOutgoingCalls = 0
        private set
    private var activeOutgoingCalls = 0

    override fun session(): Flow<SupportChatEvent> = channelFlow {
        sessionCount++
        connectionCalls.add(ACQUIRE_CALL)
        try {
            send(SupportChatEvent.Connection(SupportChatConnectionState.Connecting))
            if (sessionUnavailable) {
                send(SupportChatEvent.Connection(SupportChatConnectionState.Unavailable))
                return@channelFlow
            }
            launch(start = CoroutineStart.UNDISPATCHED) { events.collect { send(it) } }
            if (autoConnect) send(SupportChatEvent.Connection(SupportChatConnectionState.Ready))
            awaitCancellation()
        } finally {
            releasedSessionCount++
            connectionCalls.add(RELEASE_CALL)
        }
    }

    override suspend fun loadHistoryPage(cursor: SupportChatHistoryCursor?): Response<SupportChatHistoryPage> {
        historyCursors.add(cursor)
        if (loadDelayMillis > 0) delay(loadDelayMillis)
        val page = historyPageOnLoad ?: return Response.Error(loadHistoryError)
        val nextCursor = if (page.isEmpty()) null else SupportChatHistoryCursor((cursor?.token ?: 1) + 1)
        return Response.Success(SupportChatHistoryPage(messages = page, nextCursor = nextCursor))
    }

    override suspend fun sendMessage(text: String): Response<Unit> {
        interruptIfRequested()?.let { return it }
        return trackOutgoingCall {
            sentMessages.add(text)
            sendMessageResult
        }
    }

    override suspend fun sendImage(file: SupportChatPickedFile): Response<Unit> {
        interruptIfRequested()?.let { return it }
        return trackOutgoingCall {
            sentImages.add(file)
            sendImageResult
        }
    }

    override suspend fun sendDocument(file: SupportChatPickedFile): Response<Unit> = trackOutgoingCall {
        sentDocuments.add(file)
        sendDocumentResult
    }

    override suspend fun downloadFile(url: String): Response<ByteArray> {
        downloadFileCallCount++
        return downloadFileResult
    }

    override suspend fun markMessagesAsRead(ids: List<Long>): Response<Unit> {
        markedAsRead.add(ids)
        if (markMessagesAsReadResult is Response.Success) {
            events.tryEmit(SupportChatEvent.MessagesRead(ids))
        }
        return markMessagesAsReadResult
    }

    /** Событие внутри активной сессии. */
    fun emitEvent(event: SupportChatEvent) {
        val emitted = events.tryEmit(event)
        check(emitted) { "Failed to emit event: $event" }
    }

    /** Соединение готово (подписка комнаты). */
    fun connect() = emitEvent(SupportChatEvent.Connection(SupportChatConnectionState.Ready))

    /** Соединение потеряно: SDK переподключается / ждёт сеть / авторизация отвергнута. */
    fun disconnect(state: SupportChatConnectionState = SupportChatConnectionState.Reconnecting) =
        emitEvent(SupportChatEvent.Connection(state))

    private suspend fun interruptIfRequested(): Response<Unit>? {
        if (!disconnectDuringNextSend) return null
        disconnectDuringNextSend = false
        disconnect()
        yield()
        return Response.Error(AppException(ApiError.NoConnection, IllegalStateException("disconnected")))
    }

    private suspend fun trackOutgoingCall(block: () -> Response<Unit>): Response<Unit> {
        activeOutgoingCalls++
        maxConcurrentOutgoingCalls = maxOf(maxConcurrentOutgoingCalls, activeOutgoingCalls)
        return try {
            if (outgoingDelayMillis > 0) delay(outgoingDelayMillis)
            block()
        } finally {
            activeOutgoingCalls--
        }
    }

    companion object {
        const val ACQUIRE_CALL = "acquire"
        const val RELEASE_CALL = "release"
    }
}
