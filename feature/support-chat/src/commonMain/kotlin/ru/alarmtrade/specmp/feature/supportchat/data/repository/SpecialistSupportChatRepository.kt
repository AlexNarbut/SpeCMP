package ru.alarmtrade.specmp.feature.supportchat.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.TimeZone
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.asResponse
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.account.AccountRepository
import ru.alarmtrade.specmp.feature.supportchat.data.mapper.SupportChatMessageMapper
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryPage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specialist.domain.chat.ChatEvent
import ru.alarmtrade.specialist.domain.chat.ChatType
import ru.alarmtrade.specialist.interactors.chat.ChatInteractor
import ru.alarmtrade.specialist.interactors.chat.requests.ConnectChatRequest
import ru.alarmtrade.specialist.interactors.chat.requests.MarkMessagesAsReadRequest
import ru.alarmtrade.specialist.interactors.chat.requests.SetChatTypeRequest
import ru.alarmtrade.specialist.interactors.chat.requests.UploadChatFileRequest
import ru.alarmtrade.specialist.interactors.chat.requests.WriteMessageRequest
import ru.alarmtrade.specialist.interactors.files.RemoteFilesInteractor
import ru.alarmtrade.specialist.interactors.files.requests.DownloadFileRequest
import kotlin.concurrent.Volatile

class SpecialistSupportChatRepository(
    private val chatInteractor: ChatInteractor,
    private val accountRepository: AccountRepository,
    private val filesInteractor: RemoteFilesInteractor,
) : SupportChatRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    private val messageMapper = SupportChatMessageMapper(TimeZone.currentSystemDefault())

    // Сериализует open/close: переплетение closeChat прошлой сессии с openChat
    // новой закрыло бы свежее соединение.
    private val connectionMutex = Mutex()

    // Ожидающий вызов loadNextMessages. SDK отвечает на запрос страницы не
    // возвратом значения, а эмитом MessagesPage в общий поток событий —
    // репозиторий маршрутизирует её сюда, чтобы контракт loadNextMessages
    // имел гарантированный финал (см. интерфейс). Мьютекс защищает установку
    // и потребление: параллельный запрос страницы отклоняется.
    private val historyPageMutex = Mutex()
    private var historyPageAwaiter: CompletableDeferred<SupportChatHistoryPage>? = null

    // Identity текущей чат-сессии. Репозиторий не владеет identity аккаунта:
    // email резолвится из AccountRepository один раз на openChat (под мьютексом)
    // и живёт ровно столько, сколько живёт сессия. Запись — только из openChat
    // под connectionMutex, чтение — из коллектора событий; ссылка неизменяема,
    // поэтому @Volatile достаточно.
    @Volatile
    private var sessionEmail: String? = null

    override fun observeEvents(): Flow<Response<SupportChatEvent>> {
        return flow {
            // Единственная обёртка: SDK-flow может бросить мимо Response-контракта,
            // safeFlowRun конвертирует это в Response.Error (CancellationException
            // пробрасывается).
            emitAll(
                safeFlowRun {
                    chatInteractor.getChatEventsFlowUseCase.execute()
                        .mapNotNull { response ->
                            when (response) {
                                is SpecialistResponse.Success -> mapChatEvent(response.value)?.asResponse()
                                is SpecialistResponse.Error -> Response.Error(
                                    exception = response.exception,
                                    message = response.message ?: response.exception.message,
                                )
                            }
                        }
                }
            )
        }
    }

    override suspend fun openChat(): Response<Unit> {
        return connectionMutex.withLock {
            // Identity сессии перечитывается на каждое открытие: репозиторий — синглтон
            // и переживает смену аккаунта. Неуспех резолва — ошибка открытия чата
            // (offline-баннер), а не молчаливое вычисление isMine по чужому/пустому email.
            when (val profileResponse = accountRepository.currentProfile()) {
                is Response.Error -> {
                    sessionEmail = null
                    Response.Error(profileResponse.exception, profileResponse.message)
                }

                is Response.Success -> {
                    sessionEmail = profileResponse.value.email
                    chatInteractor.setChatTypeUseCase.execute(SetChatTypeRequest(ChatType.Default))
                        .toProject()
                        .mapIfSuccess {
                            chatInteractor.openChatUseCase.execute(ConnectChatRequest(resetPages = true)).toProject()
                        }
                        .also { response ->
                            if (response is Response.Error) sessionEmail = null
                        }
                }
            }
        }
    }

    override suspend fun closeChat(): Response<Unit> {
        return connectionMutex.withLock {
            sessionEmail = null
            chatInteractor.closeChatUseCase.execute(ConnectChatRequest(resetPages = false)).toProject()
        }
    }

    override suspend fun loadNextMessages(): Response<SupportChatHistoryPage> {
        val awaiter = CompletableDeferred<SupportChatHistoryPage>()
        val installed = historyPageMutex.withLock {
            if (historyPageAwaiter != null) {
                false
            } else {
                historyPageAwaiter = awaiter
                true
            }
        }
        if (!installed) {
            return Response.Error(IllegalStateException("History page request is already in progress"))
        }

        return try {
            when (val response = chatInteractor.getNextMessagesUseCase.execute()) {
                is SpecialistResponse.Error -> {
                    consumeHistoryPageAwaiter()
                    response.toProjectError()
                }

                is SpecialistResponse.Success -> {
                    // Гарантированный финал: если событие со страницей не пришло
                    // (обрыв соединения между запросом и ответом), вызов всё равно
                    // завершается — флаг загрузки не может залипнуть.
                    val page = withTimeoutOrNull(HISTORY_PAGE_TIMEOUT_MS) { awaiter.await() }
                    if (page == null) {
                        consumeHistoryPageAwaiter()
                        Response.Error(
                            IllegalStateException(
                                "History page was not emitted within $HISTORY_PAGE_TIMEOUT_MS ms"
                            )
                        )
                    } else {
                        Response.Success(page)
                    }
                }
            }
        } catch (e: CancellationException) {
            consumeHistoryPageAwaiter()
            throw e
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
        return when (val response = filesInteractor.downloadFileUseCase.execute(DownloadFileRequest(url))) {
            is SpecialistResponse.Success -> {
                // Файл целиком в памяти (ByteArray): без лимита документ на десятки
                // МБ даст OOM на слабом устройстве. Потоковая запись в файл —
                // перспектива, лимит — минимальная защита.
                if (response.value.size > MAX_DOWNLOAD_BYTES) {
                    logger.d { "Downloaded file exceeds limit: ${response.value.size} bytes" }
                    Response.Error(
                        IllegalStateException("Downloaded file exceeds $MAX_DOWNLOAD_BYTES bytes")
                    )
                } else {
                    Response.Success(response.value)
                }
            }

            is SpecialistResponse.Error -> response.toProjectError()
        }
    }

    override suspend fun markMessagesAsRead(ids: List<Long>): Response<Unit> {
        return chatInteractor.markMessagesAsReadUseCase.execute(MarkMessagesAsReadRequest(ids)).toProject()
    }

    override suspend fun release() {
        when (val response = closeChat()) {
            is Response.Error -> logger.d(response.exception) { "Failed to close chat on logout" }
            is Response.Success -> Unit
        }
    }

    private suspend fun mapChatEvent(event: ChatEvent): SupportChatEvent? {
        val myEmail = sessionEmail
        return when (event) {
            is ChatEvent.Connection.Connected -> SupportChatEvent.ConnectionChanged(true)
            is ChatEvent.Connection.Disconnected -> SupportChatEvent.ConnectionChanged(false)
            is ChatEvent.Chat.Writing.Start -> SupportChatEvent.TypingChanged(
                authorEmail = event.userLogin,
                isTyping = true,
                isMine = event.userLogin != null && event.userLogin == myEmail,
            )
            is ChatEvent.Chat.Writing.Stop -> SupportChatEvent.TypingChanged(
                authorEmail = event.userLogin,
                isTyping = false,
                isMine = event.userLogin != null && event.userLogin == myEmail,
            )
            is ChatEvent.Chat.Message.Default -> SupportChatEvent.MessageReceived(messageMapper.map(event, myEmail))
            is ChatEvent.Chat.MessagesPage -> {
                // Ответ на loadNextMessages: страница уходит ожидающему вызову,
                // в общий поток событий не эмитится.
                val page = SupportChatHistoryPage(
                    messages = event.messages.map { messageMapper.map(it, myEmail) },
                    hasMore = event.messages.isNotEmpty(),
                )
                val awaiter = consumeHistoryPageAwaiter()
                if (awaiter != null) {
                    awaiter.complete(page)
                } else {
                    logger.d { "History page arrived without a pending request — dropped" }
                }
                null
            }
            is ChatEvent.Chat.MarkMessageAsRead -> SupportChatEvent.MessagesRead(ids = event.messageIds)
            is ChatEvent.ChatRoom.SubscribeSuccess -> null
            else -> {
                logger.d { "Ignored unsupported chat event: $event" }
                null
            }
        }
    }

    private suspend fun consumeHistoryPageAwaiter(): CompletableDeferred<SupportChatHistoryPage>? {
        return historyPageMutex.withLock {
            historyPageAwaiter.also { historyPageAwaiter = null }
        }
    }

    private fun <T> SpecialistResponse<T>.toProject(): Response<T> {
        return when (this) {
            is SpecialistResponse.Success -> Response.Success(value)
            is SpecialistResponse.Error -> toProjectError()
        }
    }

    private fun <T> SpecialistResponse.Error.toProjectError(): Response<T> {
        return Response.Error(
            exception = exception,
            message = message ?: exception.message,
        )
    }

    private companion object {
        const val HISTORY_PAGE_TIMEOUT_MS = 15_000L
        const val MAX_DOWNLOAD_BYTES = 20L * 1024 * 1024
    }

}
