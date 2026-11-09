package ru.alarmtrade.specmp.feature.supportchat.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.chat.ChatConnectionState
import ru.alarmtrade.specialist.domain.chat.ChatEvent
import ru.alarmtrade.specialist.domain.chat.ChatMessagesPage
import ru.alarmtrade.specialist.interactors.chat.requests.DownloadChatFileRequest
import ru.alarmtrade.specialist.interactors.chat.requests.LoadChatMessagesRequest
import ru.alarmtrade.specialist.interactors.chat.requests.MarkMessagesAsReadRequest
import ru.alarmtrade.specialist.interactors.chat.requests.UploadChatFileRequest
import ru.alarmtrade.specialist.interactors.chat.requests.WriteMessageRequest
import ru.alarmtrade.specmp.feature.supportchat.data.datasource.SupportChatDataSource

internal class FakeSupportChatDataSource : SupportChatDataSource {
    var events: Flow<SpecialistResponse<ChatEvent>> = emptyFlow()
    var session: Flow<SpecialistResponse<ChatConnectionState>> = emptyFlow()
    var page: SpecialistResponse<ChatMessagesPage> =
        SpecialistResponse.Success(ChatMessagesPage(emptyList(), hasMore = false, total = 0, nextCursor = null))
    var sendResult: SpecialistResponse<Unit> = SpecialistResponse.Success(Unit)
    var download: SpecialistResponse<ByteArray> = SpecialistResponse.Success(ByteArray(0))

    val pageRequests = mutableListOf<LoadChatMessagesRequest>()
    val sent = mutableListOf<String>()
    val readRequests = mutableListOf<List<Long>>()
    var sessionSubscriptions = 0

    override fun chatEvents(): Flow<SpecialistResponse<ChatEvent>> = events

    override fun chatSession(): Flow<SpecialistResponse<ChatConnectionState>> {
        sessionSubscriptions++
        return session
    }

    override suspend fun loadMessages(request: LoadChatMessagesRequest): SpecialistResponse<ChatMessagesPage> {
        pageRequests += request
        return page
    }

    override suspend fun writeMessage(request: WriteMessageRequest): SpecialistResponse<Unit> {
        sent += "text:${request.text}"
        return sendResult
    }

    override suspend fun attachImage(request: UploadChatFileRequest): SpecialistResponse<Unit> {
        sent += "image:${request.filePath}"
        return sendResult
    }

    override suspend fun attachDocument(request: UploadChatFileRequest): SpecialistResponse<Unit> {
        sent += "document:${request.filePath}"
        return sendResult
    }

    override suspend fun downloadFile(request: DownloadChatFileRequest): SpecialistResponse<ByteArray> = download

    override suspend fun markMessagesAsRead(request: MarkMessagesAsReadRequest): SpecialistResponse<Unit> {
        readRequests += request.messageIds
        return sendResult
    }
}
