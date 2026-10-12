package ru.alarmtrade.specmp.feature.supportchat.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.specialist.account.AccountProfile
import ru.alarmtrade.specmp.core.specialist.account.AccountRepository
import ru.alarmtrade.specmp.feature.supportchat.data.mapper.SupportChatMessageMapper
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatConnectionState
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryCursor
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryPage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatNotConnectedException
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specialist.domain.chat.ChatConnectionState
import ru.alarmtrade.specialist.domain.chat.ChatEvent
import ru.alarmtrade.specialist.interactors.chat.ChatInteractor
import ru.alarmtrade.specialist.interactors.chat.requests.DownloadChatFileRequest
import ru.alarmtrade.specialist.interactors.chat.requests.LoadChatMessagesRequest
import ru.alarmtrade.specialist.interactors.chat.requests.MarkMessagesAsReadRequest
import ru.alarmtrade.specialist.interactors.chat.requests.UploadChatFileRequest
import ru.alarmtrade.specialist.interactors.chat.requests.WriteMessageRequest
import ru.alarmtrade.specialist.network.errors.NetworkNotConnectedError

class SpecialistSupportChatRepository(
    private val chatInteractor: ChatInteractor,
    private val accountRepository: AccountRepository,
) : SupportChatRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    private val messageMapper = SupportChatMessageMapper()

    override fun session(): Flow<SupportChatEvent> = channelFlow {
        send(SupportChatEvent.Connection(SupportChatConnectionState.Connecting))

        // Профиль — часть сессии: нужен маппингу «моё / чужое», получаем один раз.
        val profile = when (val response = accountRepository.currentProfile()) {
            is Response.Success -> response.value
            is Response.Error -> {
                logger.d(response.exception) { "Failed to resolve chat profile" }
                send(SupportChatEvent.Connection(SupportChatConnectionState.Unavailable))
                return@channelFlow
            }
        }

        launch {
            chatInteractor.getChatEventsFlowUseCase.execute().collect { response ->
                when (response) {
                    is SpecialistResponse.Success -> response.value.toSupportChatEvent(profile)?.let { send(it) }
                    // Обрывы отражает состояние соединения; ошибка в потоке событий — только лог.
                    is SpecialistResponse.Error -> logger.d(response.exception) { "Chat event error" }
                }
            }
        }

        // Заявка на соединение живёт, пока собирается этот поток.
        chatInteractor.chatSessionFlowUseCase.execute().collect { response ->
            when (response) {
                is SpecialistResponse.Success -> response.value.toDomain()?.let {
                    send(SupportChatEvent.Connection(it))
                }
                is SpecialistResponse.Error -> logger.d(response.exception) { "Chat connection state error" }
            }
        }
    }

    override suspend fun loadHistoryPage(cursor: SupportChatHistoryCursor?): Response<SupportChatHistoryPage> {
        val profile = when (val response = accountRepository.currentProfile()) {
            is Response.Success -> response.value
            is Response.Error -> return response
        }
        val request = LoadChatMessagesRequest(cursor = cursor?.token, pageSize = HISTORY_PAGE_SIZE)
        return when (val response = chatInteractor.loadChatMessagesUseCase.execute(request)) {
            is SpecialistResponse.Success -> Response.Success(
                SupportChatHistoryPage(
                    messages = response.value.messages.mapNotNull { messageMapper.map(it, profile.email) },
                    nextCursor = response.value.nextCursor?.let { SupportChatHistoryCursor(it.token) },
                )
            )
            is SpecialistResponse.Error -> response.toProject()
        }
    }

    override suspend fun sendMessage(text: String): Response<Unit> {
        return chatInteractor.writeMessageUseCase.execute(WriteMessageRequest(text)).toProject()
    }

    override suspend fun sendImage(file: SupportChatPickedFile): Response<Unit> {
        return chatInteractor.attachImageUseCase.execute(UploadChatFileRequest(file.path)).toProject()
    }

    override suspend fun sendDocument(file: SupportChatPickedFile): Response<Unit> {
        return chatInteractor.attachDocumentUseCase.execute(UploadChatFileRequest(file.path)).toProject()
    }

    override suspend fun downloadFile(url: String): Response<ByteArray> {
        // Лимит размера (20 МБ) проверяет SDK.
        return chatInteractor.downloadChatFileUseCase.execute(DownloadChatFileRequest(url)).toProject().also { response ->
            if (response is Response.Error) logger.d(response.exception) { "Failed to download chat file" }
        }
    }

    override suspend fun markMessagesAsRead(ids: List<Long>): Response<Unit> {
        return chatInteractor.markMessagesAsReadUseCase.execute(MarkMessagesAsReadRequest(ids)).toProject()
    }

    // Соединение закрывает сам SDK на логауте: заявки, взятые до него, гасятся.
    override suspend fun release() = Unit

    private fun ChatConnectionState.toDomain(): SupportChatConnectionState? {
        return when (this) {
            // Заявка взята, но SDK ещё не начал подключение — для экрана это то же «подключение».
            ChatConnectionState.Idle, ChatConnectionState.Connecting -> SupportChatConnectionState.Connecting
            ChatConnectionState.Ready -> SupportChatConnectionState.Ready
            is ChatConnectionState.Reconnecting -> SupportChatConnectionState.Reconnecting
            ChatConnectionState.WaitingForNetwork -> SupportChatConnectionState.WaitingForNetwork
            ChatConnectionState.AuthFailed -> SupportChatConnectionState.AuthFailed
        }
    }

    private fun ChatEvent.toSupportChatEvent(profile: AccountProfile): SupportChatEvent? {
        return when (this) {
            is ChatEvent.Chat.Message -> {
                val message = messageMapper.map(this, profile.email)
                if (message == null) logger.d { "Ignored unsupported chat message: ${this::class.simpleName}" }
                message?.let(SupportChatEvent::MessageReceived)
            }
            is ChatEvent.Chat.MarkMessageAsRead -> SupportChatEvent.MessagesRead(ids = messageIds)
            is ChatEvent.Chat.Writing.Start ->
                SupportChatEvent.TypingChanged(userLogin, isTyping = true, isMine = userLogin == profile.email)
            is ChatEvent.Chat.Writing.Stop ->
                SupportChatEvent.TypingChanged(userLogin, isTyping = false, isMine = userLogin == profile.email)
            // Состояние соединения — из chatSessionFlowUseCase.
            is ChatEvent.Connection, is ChatEvent.ChatRoom, is ChatEvent.Error -> null
            else -> {
                logger.d { "Ignored unsupported chat event: $this" }
                null
            }
        }
    }

    private fun <T> SpecialistResponse<T>.toProject(): Response<T> {
        return when (this) {
            is SpecialistResponse.Success -> Response.Success(value)
            is SpecialistResponse.Error -> {
                val error = if (exception is NetworkNotConnectedError) SupportChatNotConnectedException(exception) else exception
                Response.Error(exception = error, message = message ?: exception.message)
            }
        }
    }

    private companion object {
        // Не менять между вызовами одной пагинации: курсор SDK — номер страницы.
        const val HISTORY_PAGE_SIZE = 20
    }
}
