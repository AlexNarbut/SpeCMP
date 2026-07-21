package ru.alarmtrade.specmp.feature.supportchat.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.asResponse
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specialist.domain.chat.ChatEvent
import ru.alarmtrade.specialist.domain.chat.ChatMessageContent
import ru.alarmtrade.specialist.interactors.chat.ChatInteractor
import ru.alarmtrade.specialist.domain.chat.ChatType
import ru.alarmtrade.specialist.interactors.chat.requests.ConnectChatRequest
import ru.alarmtrade.specialist.interactors.chat.requests.MarkMessagesAsReadRequest
import ru.alarmtrade.specialist.interactors.chat.requests.SetChatTypeRequest
import ru.alarmtrade.specialist.interactors.chat.requests.UploadChatFileRequest
import ru.alarmtrade.specialist.interactors.chat.requests.WriteMessageRequest
import ru.alarmtrade.specialist.interactors.files.RemoteFilesInteractor
import ru.alarmtrade.specialist.interactors.files.requests.DownloadFileRequest
import ru.alarmtrade.specialist.interactors.user.UserInteractor

class SpecialistSupportChatRepository(
    private val chatInteractor: ChatInteractor,
    private val userInteractor: UserInteractor,
    private val filesInteractor: RemoteFilesInteractor,
) : SupportChatRepository {

    private val logger = Logger.withTag("SpecialistSupportChatRepository")

    // Сериализует open/close: переплетение closeChat прошлой сессии с openChat
    // новой закрыло бы свежее соединение.
    private val connectionMutex = Mutex()

    private var currentUserEmail: String? = null

    override fun observeEvents(): Flow<Response<SupportChatEvent>> {
        return flow {
            chatInteractor.getChatEventsFlowUseCase.execute()
                .map { response ->
                    when (response) {
                        is SpecialistResponse.Success -> mapChatEvent(response.value)?.asResponse()
                        is SpecialistResponse.Error -> Response.Error(
                            exception = response.exception,
                            message = response.message ?: response.exception.message,
                        )
                    }
                }
                .filterNotNull()
                .catch { e -> emit(Response.Error(e, e.message)) }
                .collect { emit(it) }
        }.catch { e -> emit(Response.Error(e, e.message)) }
    }

    override suspend fun openChat(): Response<Unit> {
        return connectionMutex.withLock {
            // Репозиторий — синглтон и переживает смену аккаунта, поэтому identity
            // перечитывается на каждую новую чат-сессию.
            currentUserEmail = null
            when (val typeResponse = chatInteractor.setChatTypeUseCase.execute(SetChatTypeRequest(ChatType.Default))) {
                is SpecialistResponse.Success -> {
                    when (val response = chatInteractor.openChatUseCase.execute(ConnectChatRequest(resetPages = true))) {
                        is SpecialistResponse.Success -> Response.Success(Unit)
                        is SpecialistResponse.Error -> response.toProjectError()
                    }
                }
                is SpecialistResponse.Error -> typeResponse.toProjectError()
            }
        }
    }

    override suspend fun closeChat(): Response<Unit> {
        return connectionMutex.withLock {
            when (val response = chatInteractor.closeChatUseCase.execute(ConnectChatRequest(resetPages = false))) {
                is SpecialistResponse.Success -> Response.Success(Unit)
                is SpecialistResponse.Error -> response.toProjectError()
            }
        }
    }

    override suspend fun loadNextMessages(): Response<Unit> {
        return when (val response = chatInteractor.getNextMessagesUseCase.execute()) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    override suspend fun sendMessage(text: String): Response<Unit> {
        return when (val response = chatInteractor.writeMessageUseCase.execute(WriteMessageRequest(text))) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    override suspend fun sendImage(file: SupportChatPickedFile): Response<Unit> {
        return when (val response = chatInteractor.attachImageUseCase.execute(UploadChatFileRequest(file.path))) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    override suspend fun sendDocument(file: SupportChatPickedFile): Response<Unit> {
        return when (val response = chatInteractor.attachDocumentUseCase.execute(UploadChatFileRequest(file.path))) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    override suspend fun downloadFile(url: String): Response<ByteArray> {
        return when (val response = filesInteractor.downloadFileUseCase.execute(DownloadFileRequest(url))) {
            is SpecialistResponse.Success -> Response.Success(response.value)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    override suspend fun markMessagesAsRead(ids: List<Long>): Response<Unit> {
        return when (val response = chatInteractor.markMessagesAsReadUseCase.execute(MarkMessagesAsReadRequest(ids))) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    override suspend fun startWriting(): Response<Unit> {
        return when (val response = chatInteractor.startWritingUseCase.execute()) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    override suspend fun stopWriting(): Response<Unit> {
        return when (val response = chatInteractor.stopWritingUseCase.execute()) {
            is SpecialistResponse.Success -> Response.Success(Unit)
            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    private suspend fun mapChatEvent(event: ChatEvent): SupportChatEvent? {
        val myEmail = resolveCurrentUserEmail()
        return when (event) {
            is ChatEvent.Connection.Connected -> SupportChatEvent.ConnectionChanged(true)
            is ChatEvent.Connection.Disconnected -> SupportChatEvent.ConnectionChanged(false)
            is ChatEvent.Chat.Writing.Start -> SupportChatEvent.TypingChanged(
                authorEmail = event.userLogin,
                isTyping = true,
            )
            is ChatEvent.Chat.Writing.Stop -> SupportChatEvent.TypingChanged(
                authorEmail = event.userLogin,
                isTyping = false,
            )
            is ChatEvent.Chat.Message.Default -> SupportChatEvent.MessageReceived(event.toSupportChatMessage(myEmail))
            is ChatEvent.Chat.MessagesPage -> SupportChatEvent.HistoryPageReceived(
                messages = event.messages.map { it.toSupportChatMessage(myEmail) },
            )
            is ChatEvent.Chat.MarkMessageAsRead -> SupportChatEvent.MessagesRead(ids = event.messageIds)
            is ChatEvent.ChatRoom.SubscribeSuccess -> null
            else -> {
                logger.d { "Ignored unsupported chat event: $event" }
                null
            }
        }
    }

    private suspend fun resolveCurrentUserEmail(): String? {
        val cached = currentUserEmail
        if (cached != null) return cached

        return when (val response = userInteractor.getUserUseCase.execute()) {
            is SpecialistResponse.Success -> {
                currentUserEmail = response.value.login
                response.value.login
            }
            is SpecialistResponse.Error -> {
                logger.d(response.exception) { "Failed to resolve current user email for chat" }
                null
            }
        }
    }

    private fun ChatEvent.Chat.Message.toSupportChatMessage(myEmail: String?): SupportChatMessage {
        val content = when (this) {
            is ChatEvent.Chat.Message.Default -> messageContent
            else -> null
        }

        val attachment = when (content) {
            is ChatMessageContent.Image -> SupportChatAttachment.Image(
                id = content.id,
                name = content.name,
                url = content.url,
            )
            is ChatMessageContent.Document -> SupportChatAttachment.Document(
                id = content.id,
                name = content.name,
                url = content.url,
            )
            else -> null
        }

        val text = when (content) {
            is ChatMessageContent.Text -> content.text
            else -> null
        }

        val localTime = time.toInstant(TimeZone.UTC).toLocalDateTime(TimeZone.currentSystemDefault())

        return SupportChatMessage(
            id = id.toString(),
            serverId = id,
            text = text,
            attachment = attachment,
            time = localTime,
            displayTime = formatTime(localTime),
            isMine = myEmail != null && sender.login == myEmail,
            authorEmail = sender.login,
            status = if (wasRead) SupportChatMessageStatus.Read else SupportChatMessageStatus.Sent,
        )
    }

    private fun formatTime(time: LocalDateTime): String {
        val hour = time.hour.toString().padStart(2, '0')
        val minute = time.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    private fun <T> SpecialistResponse.Error.toProjectError(): Response<T> {
        return Response.Error(
            exception = exception,
            message = message ?: exception.message,
        )
    }

}
