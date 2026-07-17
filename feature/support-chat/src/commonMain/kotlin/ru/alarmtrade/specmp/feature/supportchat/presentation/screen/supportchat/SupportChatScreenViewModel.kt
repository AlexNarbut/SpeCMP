package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.yield
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPendingAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatFileSaver
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatEffect
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class SupportChatScreenViewModel(
    private val repository: SupportChatRepository,
    private val dispatchers: AppDispatchers,
) : BaseViewModel() {

    private val _state = MutableStateFlow(SupportChatScreenState())
    val state: StateFlow<SupportChatScreenState> = _state.asStateFlow()

    private val _effect = sideEffectFlow<SupportChatEffect>()
    val effect: SharedFlow<SupportChatEffect> = _effect.asSharedFlow()

    private var typingTimeoutJob: Job? = null
    private var eventCollectionJob: Job? = null
    private var reconnectJob: Job? = null
    private var pendingRetryJob: Job? = null
    private var pendingRetryRequested = false
    private var sessionJob: Job? = null
    private val markedAsReadIds = mutableSetOf<Long>()

    // ViewModel живёт в app-level ViewModelStore (у nav3 нет per-entry owner),
    // поэтому сессия чата привязана к экрану явно: каждый вход начинает чистую
    // сессию, чтобы данные прошлого аккаунта/открытия не протекали в новую.
    fun onScreenOpened() {
        sessionJob?.cancel()
        reconnectJob?.cancel()
        pendingRetryRequested = false
        pendingRetryJob?.cancel()
        typingTimeoutJob?.cancel()

        _state.update { SupportChatScreenState() }
        markedAsReadIds.clear()

        startEventCollection()
        sessionJob = safeLaunch {
            // Give the hot event flow collector a chance to subscribe before the SDK
            // synchronously emits the first MessagesPage from loadNextMessages().
            yield()
            connectAndLoadInitialHistory()
        }
    }

    fun onScreenDisposed() {
        reconnectJob?.cancel()
        pendingRetryRequested = false
        pendingRetryJob?.cancel()
        typingTimeoutJob?.cancel()
        eventCollectionJob?.cancel()
        sessionJob?.cancel()
        sessionJob = safeLaunch {
            withContext(dispatchers.io) { repository.closeChat() }
        }
    }

    fun onDraftChange(value: String) {
        _state.update { it.copy(draftText = value) }
    }

    // Тип вложения определяется источником выбора, а не содержимым файла:
    // картинка, выбранная как документ, должна уйти документом.
    fun onImagePicked(file: SupportChatPickedFile) {
        addPendingAttachment(
            SupportChatPendingAttachment.Image(
                id = file.id,
                name = file.name,
                localPath = file.path,
                mimeType = file.mimeType,
                sizeBytes = file.sizeBytes,
            )
        )
    }

    fun onDocumentPicked(file: SupportChatPickedFile) {
        addPendingAttachment(
            SupportChatPendingAttachment.Document(
                id = file.id,
                name = file.name,
                localPath = file.path,
                mimeType = file.mimeType,
                sizeBytes = file.sizeBytes,
            )
        )
    }

    private fun addPendingAttachment(attachment: SupportChatPendingAttachment) {
        _state.update {
            it.copy(
                pendingAttachments = it.pendingAttachments + attachment,
            )
        }
    }

    fun onRemoveAttachment(id: String) {
        _state.update { current ->
            current.copy(pendingAttachments = current.pendingAttachments.filter { it.id != id })
        }
    }

    // Скачивание доступно только для входящих вложений (от поддержки) — см. UI.
    fun onDownloadAttachment(attachment: SupportChatAttachment, saver: SupportChatFileSaver) {
        val url = when (attachment) {
            is SupportChatAttachment.Image -> attachment.url
            is SupportChatAttachment.Document -> attachment.url
        } ?: return
        if (_state.value.downloadingAttachmentId != null) return

        _state.update { it.copy(downloadingAttachmentId = attachment.id) }
        safeLaunch {
            when (val response = withContext(dispatchers.io) { repository.downloadFile(url) }) {
                is Response.Success -> {
                    val savedPath = withContext(dispatchers.io) {
                        saver.saveToDownloads(attachment.name, response.value)
                    }
                    if (savedPath != null) {
                        _effect.tryEmit(SupportChatEffect.AttachmentDownloaded(savedPath))
                    } else {
                        _effect.tryEmit(SupportChatEffect.AttachmentDownloadFailed)
                    }
                }
                is Response.Error -> {
                    logger.d(response.exception) { "Failed to download attachment" }
                    _effect.tryEmit(SupportChatEffect.AttachmentDownloadFailed)
                }
            }
            _state.update { it.copy(downloadingAttachmentId = null) }
        }
    }

    fun onSend() {
        val text = _state.value.draftText.trim()
        val attachments = _state.value.pendingAttachments
        if (text.isEmpty() && attachments.isEmpty()) return

        val timestamp = now()
        val outgoingMessages = buildList {
            attachments.forEach { attachment ->
                add(createOptimisticMessage(timestamp = timestamp, attachment = attachment))
            }
            if (text.isNotEmpty()) {
                add(createOptimisticMessage(timestamp = timestamp, text = text))
            }
        }

        _state.update {
            it.copy(
                messages = it.messages + outgoingMessages,
                draftText = "",
                pendingAttachments = emptyList(),
            )
        }

        safeLaunch {
            outgoingMessages.forEach { message ->
                sendOutgoingMessage(
                    clientMessageId = message.id,
                    text = message.text.orEmpty(),
                    attachment = message.attachment?.toPending(),
                )
            }
        }
    }

    fun onRetry(messageId: String) {
        val message = _state.value.messages.find { it.id == messageId && it.isMine } ?: return
        safeLaunch {
            _state.update { current ->
                current.copy(
                    messages = current.messages.map {
                        if (it.id == messageId) it.copy(status = SupportChatMessageStatus.Sending) else it
                    },
                )
            }
            sendOutgoingMessage(
                clientMessageId = messageId,
                text = message.text ?: "",
                attachment = message.attachment?.toPending(),
            )
        }
    }

    fun onLoadMore() {
        val current = _state.value
        if (current.isLoadingHistory || !current.hasMoreHistory) return
        safeLaunch {
            _state.update { it.copy(isLoadingHistory = true) }
            val response = withContext(dispatchers.io) { repository.loadNextMessages() }
            if (response is Response.Error) {
                _state.update { it.copy(isLoadingHistory = false) }
                _effect.tryEmit(SupportChatEffect.HistoryLoadFailed)
            }
        }
    }

    fun onConnectionRetry() {
        launchReconnect(immediate = true)
    }

    fun onMessageVisible(messageId: String) {
        val message = _state.value.messages.find { it.id == messageId } ?: return
        val serverId = message.serverId ?: return
        if (message.isMine || message.status == SupportChatMessageStatus.Read || serverId in markedAsReadIds) return

        markedAsReadIds += serverId
        safeLaunch {
            val response = withContext(dispatchers.io) { repository.markMessagesAsRead(listOf(serverId)) }
            if (response is Response.Error) {
                markedAsReadIds -= serverId
            }
        }
    }

    private fun startEventCollection() {
        eventCollectionJob?.cancel()
        eventCollectionJob = safeLaunch {
            repository.observeEvents()
                .collect { response ->
                    when (response) {
                        is Response.Success -> handleEvent(response.value)
                        is Response.Error -> logger.d(response.exception) { "Chat event error" }
                    }
                }
        }
    }

    private suspend fun handleEvent(event: SupportChatEvent) {
        when (event) {
            is SupportChatEvent.ConnectionChanged -> handleConnectionChanged(event.isConnected)
            is SupportChatEvent.TypingChanged -> handleTypingChanged(event.authorEmail, event.isTyping)
            is SupportChatEvent.MessageReceived -> handleMessageReceived(event.message)
            is SupportChatEvent.HistoryPageReceived -> handleHistoryPageReceived(event.messages)
            is SupportChatEvent.MessagesRead -> handleMessagesRead(event.ids)
        }
    }

    private fun handleConnectionChanged(isConnected: Boolean) {
        _state.update { it.copy(isOffline = !isConnected) }
        if (isConnected) {
            retryPendingMessages()
        } else {
            launchReconnect(immediate = false)
        }
    }

    private fun handleTypingChanged(authorEmail: String?, isTyping: Boolean) {
        _state.update { it.copy(typingAuthor = if (isTyping) authorEmail else null) }
        typingTimeoutJob?.cancel()
        if (isTyping && authorEmail != null) {
            typingTimeoutJob = safeLaunch {
                delay(TYPING_TIMEOUT)
                _state.update { current ->
                    if (current.typingAuthor == authorEmail) current.copy(typingAuthor = null) else current
                }
            }
        }
    }

    private fun handleMessageReceived(message: SupportChatMessage) {
        _state.update { current ->
            val index = when {
                message.serverId != null -> current.messages.indexOfFirst {
                    it.isMine && it.serverId == message.serverId
                }
                else -> -1
            }
            val updatedMessages = if (index != -1) {
                current.messages.toMutableList().apply { set(index, message) }
            } else {
                // Серверное эхо исходящего сообщения может прийти без client-id.
                // Пытаемся сопоставить по содержимому с последним оптимистичным сообщением.
                val echoIndex = if (message.isMine) {
                    current.messages.indexOfLast {
                        it.isMine && it.serverId == null && it.contentMatches(message)
                    }
                } else {
                    -1
                }
                if (echoIndex != -1) {
                    current.messages.toMutableList().apply { set(echoIndex, message) }
                } else {
                    current.messages + message
                }
            }
            current.copy(messages = updatedMessages)
        }
    }

    private fun handleHistoryPageReceived(messages: List<SupportChatMessage>) {
        _state.update { current ->
            val existingIds = current.messages.map { it.serverId ?: it.id }.toHashSet()
            val newMessages = messages.filter { (it.serverId ?: it.id) !in existingIds }
            current.copy(
                messages = newMessages + current.messages,
                isLoadingHistory = false,
                hasMoreHistory = newMessages.isNotEmpty(),
            )
        }
    }

    private fun handleMessagesRead(ids: List<Long>) {
        _state.update { current ->
            current.copy(
                messages = current.messages.map { message ->
                    if (message.serverId != null && message.serverId in ids) {
                        message.copy(status = SupportChatMessageStatus.Read)
                    } else {
                        message
                    }
                },
            )
        }
    }

    private suspend fun sendOutgoingMessage(
        clientMessageId: String,
        text: String,
        attachment: SupportChatPendingAttachment?,
    ) {
        if (_state.value.isOffline) {
            _state.update { current ->
                current.copy(
                    messages = current.messages.map {
                        if (it.id == clientMessageId) it.copy(status = SupportChatMessageStatus.Pending) else it
                    },
                )
            }
            return
        }

        val response = when {
            text.isNotBlank() -> withContext(dispatchers.io) { repository.sendMessage(text) }
            attachment != null -> sendAttachment(attachment)
            else -> Response.Success(Unit)
        }

        when (response) {
            is Response.Success -> {
                _state.update { current ->
                    current.copy(
                        messages = current.messages.map {
                            if (it.id == clientMessageId) it.copy(status = SupportChatMessageStatus.Sent) else it
                        },
                    )
                }
            }
            is Response.Error -> {
                logger.d(response.exception) { "Failed to send message" }
                _state.update { current ->
                    current.copy(
                        messages = current.messages.map {
                            if (it.id == clientMessageId) it.copy(status = SupportChatMessageStatus.Failed) else it
                        },
                    )
                }
                _effect.tryEmit(SupportChatEffect.MessageSendFailed(clientMessageId))
            }
        }
    }

    private suspend fun sendAttachment(attachment: SupportChatPendingAttachment): Response<Unit> {
        val file = SupportChatPickedFile(
            id = attachment.id,
            name = attachment.name,
            path = attachment.localPath,
            mimeType = attachment.mimeType,
            sizeBytes = attachment.sizeBytes,
        )
        return withContext(dispatchers.io) {
            when (attachment) {
                is SupportChatPendingAttachment.Image -> repository.sendImage(file)
                is SupportChatPendingAttachment.Document -> repository.sendDocument(file)
            }
        }
    }

    private fun retryPendingMessages() {
        pendingRetryRequested = true
        if (pendingRetryJob?.isActive == true) return

        pendingRetryJob = safeLaunch {
            while (pendingRetryRequested) {
                pendingRetryRequested = false
                retryPendingMessagesSequentially()
            }
        }
    }

    private suspend fun retryPendingMessagesSequentially() {
        val pending = _state.value.messages.filter { it.status == SupportChatMessageStatus.Pending && it.isMine }
        pending.forEach { message ->
            if (_state.value.isOffline) return

            _state.update { current ->
                current.copy(
                    messages = current.messages.map {
                        if (it.id == message.id) it.copy(status = SupportChatMessageStatus.Sending) else it
                    },
                )
            }
            sendOutgoingMessage(
                clientMessageId = message.id,
                text = message.text ?: "",
                attachment = message.attachment?.toPending(),
            )
        }
    }

    private fun SupportChatPendingAttachment.toAttachment(): SupportChatAttachment {
        return when (this) {
            is SupportChatPendingAttachment.Image -> SupportChatAttachment.Image(
                id = id,
                name = name,
                url = "",
                localPath = localPath,
            )
            is SupportChatPendingAttachment.Document -> SupportChatAttachment.Document(
                id = id,
                name = name,
                url = null,
                localPath = localPath,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
            )
        }
    }

    private fun SupportChatMessage.contentMatches(other: SupportChatMessage): Boolean {
        return text == other.text && attachment?.name == other.attachment?.name
    }

    private fun SupportChatAttachment.toPending(): SupportChatPendingAttachment? {
        return when (this) {
            is SupportChatAttachment.Image -> localPath?.let {
                SupportChatPendingAttachment.Image(id = id, name = name, localPath = it)
            }
            is SupportChatAttachment.Document -> localPath?.let {
                SupportChatPendingAttachment.Document(
                    id = id,
                    name = name,
                    localPath = it,
                    mimeType = mimeType,
                    sizeBytes = sizeBytes,
                )
            }
        }
    }

    private suspend fun connectAndLoadInitialHistory() {
        when (withContext(dispatchers.io) { repository.openChat() }) {
            is Response.Success -> loadHistoryPage()
            is Response.Error -> {
                _state.update {
                    it.copy(
                        isLoadingHistory = false,
                        isOffline = true,
                    )
                }
                launchReconnect(immediate = false)
            }
        }
    }

    private suspend fun loadHistoryPage() {
        _state.update { it.copy(isLoadingHistory = true) }
        val response = withContext(dispatchers.io) { repository.loadNextMessages() }
        if (response is Response.Error) {
            _state.update {
                it.copy(isLoadingHistory = false)
            }
            _effect.tryEmit(SupportChatEffect.HistoryLoadFailed)
        }
    }

    private fun launchReconnect(immediate: Boolean) {
        reconnectJob?.cancel()
        reconnectJob = safeLaunch {
            var shouldDelay = !immediate
            while (_state.value.isOffline) {
                if (shouldDelay) delay(RECONNECT_DELAY)
                shouldDelay = true

                when (withContext(dispatchers.io) { repository.openChat() }) {
                    is Response.Success -> {
                        _state.update { it.copy(isOffline = false) }
                        if (_state.value.messages.isEmpty() &&
                            _state.value.hasMoreHistory &&
                            !_state.value.isLoadingHistory
                        ) {
                            loadHistoryPage()
                        }
                        retryPendingMessages()
                        return@safeLaunch
                    }
                    is Response.Error -> _state.update { it.copy(isOffline = true) }
                }
            }
        }
    }

    private fun createOptimisticMessage(
        timestamp: LocalDateTime,
        text: String? = null,
        attachment: SupportChatPendingAttachment? = null,
    ): SupportChatMessage {
        return SupportChatMessage(
            id = Random.nextLong().toString(),
            serverId = null,
            text = text,
            attachment = attachment?.toAttachment(),
            time = timestamp,
            displayTime = formatTime(timestamp),
            isMine = true,
            authorEmail = null,
            status = SupportChatMessageStatus.Sending,
        )
    }

    private fun now(): LocalDateTime {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    private fun formatTime(time: LocalDateTime): String {
        val hour = time.hour.toString().padStart(2, '0')
        val minute = time.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    private companion object {
        val TYPING_TIMEOUT = 5.seconds
        val RECONNECT_DELAY = 3.seconds
    }
}
