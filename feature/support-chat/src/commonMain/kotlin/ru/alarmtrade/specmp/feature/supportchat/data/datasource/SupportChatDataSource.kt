package ru.alarmtrade.specmp.feature.supportchat.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.chat.ChatConnectionState
import ru.alarmtrade.specialist.domain.chat.ChatEvent
import ru.alarmtrade.specialist.domain.chat.ChatMessagesPage
import ru.alarmtrade.specialist.interactors.chat.ChatInteractor
import ru.alarmtrade.specialist.interactors.chat.requests.DownloadChatFileRequest
import ru.alarmtrade.specialist.interactors.chat.requests.LoadChatMessagesRequest
import ru.alarmtrade.specialist.interactors.chat.requests.MarkMessagesAsReadRequest
import ru.alarmtrade.specialist.interactors.chat.requests.UploadChatFileRequest
import ru.alarmtrade.specialist.interactors.chat.requests.WriteMessageRequest

internal interface SupportChatDataSource {
    fun chatEvents(): Flow<SpecialistResponse<ChatEvent>>
    fun chatSession(): Flow<SpecialistResponse<ChatConnectionState>>
    suspend fun loadMessages(request: LoadChatMessagesRequest): SpecialistResponse<ChatMessagesPage>
    suspend fun writeMessage(request: WriteMessageRequest): SpecialistResponse<Unit>
    suspend fun attachImage(request: UploadChatFileRequest): SpecialistResponse<Unit>
    suspend fun attachDocument(request: UploadChatFileRequest): SpecialistResponse<Unit>
    suspend fun downloadFile(request: DownloadChatFileRequest): SpecialistResponse<ByteArray>
    suspend fun markMessagesAsRead(request: MarkMessagesAsReadRequest): SpecialistResponse<Unit>
}

internal class SpecialistSupportChatDataSource(private val chatInteractor: ChatInteractor) : SupportChatDataSource {
    override fun chatEvents(): Flow<SpecialistResponse<ChatEvent>> = flow {
        emitAll(chatInteractor.getChatEventsFlowUseCase.execute())
    }

    override fun chatSession(): Flow<SpecialistResponse<ChatConnectionState>> = flow {
        emitAll(chatInteractor.chatSessionFlowUseCase.execute())
    }

    override suspend fun loadMessages(request: LoadChatMessagesRequest): SpecialistResponse<ChatMessagesPage> =
        chatInteractor.loadChatMessagesUseCase.execute(request)

    override suspend fun writeMessage(request: WriteMessageRequest): SpecialistResponse<Unit> =
        chatInteractor.writeMessageUseCase.execute(request)

    override suspend fun attachImage(request: UploadChatFileRequest): SpecialistResponse<Unit> =
        chatInteractor.attachImageUseCase.execute(request)

    override suspend fun attachDocument(request: UploadChatFileRequest): SpecialistResponse<Unit> =
        chatInteractor.attachDocumentUseCase.execute(request)

    override suspend fun downloadFile(request: DownloadChatFileRequest): SpecialistResponse<ByteArray> =
        chatInteractor.downloadChatFileUseCase.execute(request)

    override suspend fun markMessagesAsRead(request: MarkMessagesAsReadRequest): SpecialistResponse<Unit> =
        chatInteractor.markMessagesAsReadUseCase.execute(request)
}
