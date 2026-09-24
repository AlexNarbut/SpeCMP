package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.yield
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository

class FakeSupportChatRepository : SupportChatRepository {

    private val events = MutableSharedFlow<Response<SupportChatEvent>>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    var openChatResult: Response<Unit> = Response.Success(Unit)
    var closeChatResult: Response<Unit> = Response.Success(Unit)
    var loadNextMessagesResult: Response<Unit> = Response.Success(Unit)
    var sendMessageResult: Response<Unit> = Response.Success(Unit)
    var sendImageResult: Response<Unit> = Response.Success(Unit)
    var sendDocumentResult: Response<Unit> = Response.Success(Unit)
    var markMessagesAsReadResult: Response<Unit> = Response.Success(Unit)
    var downloadFileResult: Response<ByteArray> = Response.Success(byteArrayOf(1, 2, 3))
    var startWritingResult: Response<Unit> = Response.Success(Unit)
    var stopWritingResult: Response<Unit> = Response.Success(Unit)
    var historyPageOnLoad: List<SupportChatMessage>? = emptyList()
    var emitConnectedDuringNextOpenChat = false
    var outgoingDelayMillis = 0L

    val sentMessages = mutableListOf<String>()
    val sentImages = mutableListOf<SupportChatPickedFile>()
    val sentDocuments = mutableListOf<SupportChatPickedFile>()
    val markedAsRead = mutableListOf<List<Long>>()
    val connectionCalls = mutableListOf<String>()
    var loadNextMessagesCallCount = 0
        private set
    var openChatCallCount = 0
        private set
    var closeChatCallCount = 0
        private set
    var maxConcurrentOutgoingCalls = 0
        private set
    private var activeOutgoingCalls = 0

    override fun observeEvents(): Flow<Response<SupportChatEvent>> = events

    override suspend fun openChat(): Response<Unit> {
        openChatCallCount++
        connectionCalls.add(OPEN_CALL)
        if (emitConnectedDuringNextOpenChat && openChatResult is Response.Success) {
            emitConnectedDuringNextOpenChat = false
            emitEvent(SupportChatEvent.ConnectionChanged(true))
            yield()
        }
        return openChatResult
    }

    override suspend fun closeChat(): Response<Unit> {
        closeChatCallCount++
        connectionCalls.add(CLOSE_CALL)
        return closeChatResult
    }

    override suspend fun loadNextMessages(): Response<Unit> {
        loadNextMessagesCallCount++
        if (loadNextMessagesResult is Response.Success) {
            historyPageOnLoad?.let { page ->
                emitEvent(SupportChatEvent.HistoryPageReceived(page))
            }
        }
        return loadNextMessagesResult
    }

    override suspend fun sendMessage(text: String): Response<Unit> {
        return trackOutgoingCall {
            sentMessages.add(text)
            sendMessageResult
        }
    }

    override suspend fun sendImage(file: SupportChatPickedFile): Response<Unit> {
        return trackOutgoingCall {
            sentImages.add(file)
            sendImageResult
        }
    }

    override suspend fun sendDocument(file: SupportChatPickedFile): Response<Unit> {
        return trackOutgoingCall {
            sentDocuments.add(file)
            sendDocumentResult
        }
    }

    override suspend fun downloadFile(url: String): Response<ByteArray> {
        return downloadFileResult
    }

    override suspend fun markMessagesAsRead(ids: List<Long>): Response<Unit> {
        markedAsRead.add(ids)
        if (markMessagesAsReadResult is Response.Success) {
            events.tryEmit(Response.Success(SupportChatEvent.MessagesRead(ids)))
        }
        return markMessagesAsReadResult
    }

    override suspend fun startWriting(): Response<Unit> = startWritingResult
    override suspend fun stopWriting(): Response<Unit> = stopWritingResult

    override suspend fun release() {
        closeChat()
    }

    fun emitEvent(event: SupportChatEvent) {
        val emitted = events.tryEmit(Response.Success(event))
        check(emitted) { "Failed to emit event: $event" }
    }

    fun emitError(exception: Throwable) {
        events.tryEmit(Response.Error(exception, exception.message))
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
        const val OPEN_CALL = "open"
        const val CLOSE_CALL = "close"
    }
}
