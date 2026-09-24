package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryPage
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
    // Application-scoped скоуп: закрытие чат-сессии из onCleared должно пережить
    // viewModelScope (уже отменён на момент onCleared).
    private val appScope: CoroutineScope,
) : BaseViewModel() {

    private val _state = MutableStateFlow(SupportChatScreenState())
    val state: StateFlow<SupportChatScreenState> = _state.asStateFlow()

    private val _effect = sideEffectFlow<SupportChatEffect>()
    val effect: SharedFlow<SupportChatEffect> = _effect.asSharedFlow()

    // Таймаут индикатора набора. Пишется только из коллектора событий
    // (единственный последовательный контекст), поэтому гонок нет: раньше поле
    // писалось и из io-коллектора, и с main — отсюда росла потерянная отмена
    // и «повисший» индикатор.
    private var typingTimeoutJob: Job? = null
    private val markedAsReadIds = mutableSetOf<Long>()

    init {
        // Порядок обязателен: сначала подписка на hot flow событий, затем openChat —
        // SDK публикует MessagesPage без replay прямо во время loadNextMessages().
        startEventCollection()
        safeLaunch {
            // Дать коллектору событий шанс подписаться до синхронного emit'а истории.
            yield()
            connectAndLoadInitialHistory()
        }
    }

    // public для тестов: platform вызывает onCleared через ViewModelStore,
    // unit-тесты — напрямую.
    public override fun onCleared() {
        appScope.launch {
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
                history = LCEState.Content(it.messages + outgoingMessages),
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
            updateMessageStatus(messageId, SupportChatMessageStatus.Sending)
            sendOutgoingMessage(
                clientMessageId = messageId,
                text = message.text ?: "",
                attachment = message.attachment?.toPending(),
            )
        }
    }

    fun onLoadMore() {
        val current = _state.value
        if (current.isLoadingMore || !current.hasMoreHistory || current.history !is LCEState.Content) return
        safeLaunch {
            _state.update { it.copy(isLoadingMore = true) }
            when (val response = withContext(dispatchers.io) { repository.loadNextMessages() }) {
                is Response.Success -> _state.update { mergeHistoryPage(it, response.value) }
                is Response.Error -> {
                    // Лента живая: ошибка пагинации — тост, ленту не затираем.
                    logger.d(response.exception) { "Failed to load next history page" }
                    _state.update { it.copy(isLoadingMore = false) }
                    _effect.tryEmit(SupportChatEffect.HistoryLoadFailed)
                }
            }
        }
    }

    fun onHistoryRetry() {
        safeLaunch { loadInitialHistory() }
    }

    fun onConnectionRetry() {
        // Повторный клик во время выполнения пропускаем: попытка одна.
        safeLaunch {
            executeWithProceedLock {
                when (withContext(dispatchers.io) { repository.openChat() }) {
                    is Response.Success -> {
                        _state.update { it.copy(isOffline = false) }
                        if (_state.value.history !is LCEState.Content &&
                            _state.value.hasMoreHistory
                        ) {
                            loadInitialHistory()
                        }
                        retryPendingMessages()
                    }
                    is Response.Error -> _state.update { it.copy(isOffline = true) }
                }
            }
        }
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
        safeLaunch(dispatchers.io) {
            repository.observeEvents()
                .collect { response ->
                    when (response) {
                        is Response.Success -> handleEvent(response.value)
                        is Response.Error -> handleEventError(response.exception)
                    }
                }
        }
    }

    private suspend fun handleEvent(event: SupportChatEvent) {
        when (event) {
            is SupportChatEvent.ConnectionChanged -> handleConnectionChanged(event.isConnected)
            is SupportChatEvent.TypingChanged -> handleTypingChanged(event.authorEmail, event.isTyping)
            is SupportChatEvent.MessageReceived -> handleMessageReceived(event.message)
            is SupportChatEvent.MessagesRead -> handleMessagesRead(event.ids)
        }
    }

    private fun handleEventError(error: Throwable) {
        // Одноразовая реакция сознательная: на iOS обрыв сокета приходит потоком
        // ошибок ~кГц (SDK мапит onFailure в State.Error), повторные вызовы
        // closeChat только дёргают SDK. Не «чинить» без пересмотра обоснования.
        if (_state.value.isOffline) return
        logger.d(error) { "Chat event error, switching to offline" }
        _state.update { it.copy(isOffline = true) }
        safeLaunch(dispatchers.io) { repository.closeChat() }
    }

    private fun handleConnectionChanged(isConnected: Boolean) {
        _state.update { it.copy(isOffline = !isConnected) }
        if (isConnected) {
            retryPendingMessages()
        } else {
            safeLaunch {
                withContext(dispatchers.io) { repository.closeChat() }
            }
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
            val messages = current.messages
            val index = when {
                message.serverId != null -> messages.indexOfFirst {
                    it.isMine && it.serverId == message.serverId
                }
                else -> -1
            }
            val updatedMessages = if (index != -1) {
                messages.toMutableList().apply { set(index, message) }
            } else {
                val echoIndex = if (message.isMine) {
                    messages.indexOfEcho(message)
                } else {
                    -1
                }
                if (echoIndex != -1) {
                    messages.toMutableList().apply { set(echoIndex, message) }
                } else {
                    // Первое сообщение снимает скелетон: history становится Content,
                    // и полноэкранная ошибка/скелетон затирки ленты не дождутся.
                    messages + message
                }
            }
            current.copy(history = LCEState.Content(updatedMessages))
        }
    }

    private fun List<SupportChatMessage>.indexOfEcho(message: SupportChatMessage): Int {
        indexOfLast { it.isMine && it.serverId == null && it.contentMatches(message) }
            .takeIf { it != -1 }?.let { return it }
        return indexOfFirst { it.isMine && it.serverId == null && it.shapeMatches(message) }
    }

    private fun mergeHistoryPage(
        current: SupportChatScreenState,
        page: SupportChatHistoryPage,
    ): SupportChatScreenState {
        val existingIds = current.messages.map { it.serverId ?: it.id }.toHashSet()
        val newMessages = page.messages.filter { (it.serverId ?: it.id) !in existingIds }
        return current.copy(
            history = LCEState.Content(newMessages + current.messages),
            isLoadingMore = false,
            hasMoreHistory = page.hasMore,
        )
    }

    private fun handleMessagesRead(ids: List<Long>) {
        _state.update { current ->
            current.copy(
                history = LCEState.Content(
                    current.messages.map { message ->
                        if (message.serverId != null && message.serverId in ids) {
                            message.copy(status = SupportChatMessageStatus.Read)
                        } else {
                            message
                        }
                    }
                ),
            )
        }
    }

    private suspend fun sendOutgoingMessage(
        clientMessageId: String,
        text: String,
        attachment: SupportChatPendingAttachment?,
    ) {
        if (_state.value.isOffline) {
            updateMessageStatus(clientMessageId, SupportChatMessageStatus.Pending)
            return
        }

        val response = when {
            text.isNotBlank() -> withContext(dispatchers.io) { repository.sendMessage(text) }
            attachment != null -> sendAttachment(attachment)
            else -> Response.Success(Unit)
        }

        when (response) {
            is Response.Success -> updateMessageStatus(clientMessageId, SupportChatMessageStatus.Sent)
            is Response.Error -> {
                logger.d(response.exception) { "Failed to send message" }
                updateMessageStatus(clientMessageId, SupportChatMessageStatus.Failed)
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
        // Сериализация через общий proceed-lock: вызовы, пришедшие во время
        // выполнения, дождутся конца и выполнятся следом — повторный прогон
        // по пустой очереди дёшев. Самодельный флаг-барьер не нужен.
        safeLaunch {
            executeWithProceedLock(needSkipAnother = false) {
                retryPendingMessagesSequentially()
            }
        }
    }

    private suspend fun retryPendingMessagesSequentially() {
        val pending = _state.value.messages.filter { it.status == SupportChatMessageStatus.Pending && it.isMine }
        pending.forEach { message ->
            if (_state.value.isOffline) return

            updateMessageStatus(message.id, SupportChatMessageStatus.Sending)
            sendOutgoingMessage(
                clientMessageId = message.id,
                text = message.text ?: "",
                attachment = message.attachment?.toPending(),
            )
        }
    }

    private fun updateMessageStatus(id: String, status: SupportChatMessageStatus) {
        updateMessages { messages -> messages.map { if (it.id == id) it.copy(status = status) else it } }
    }

    private fun updateMessages(transform: (List<SupportChatMessage>) -> List<SupportChatMessage>) {
        _state.update { current -> current.copy(history = LCEState.Content(transform(current.messages))) }
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

    private fun SupportChatMessage.shapeMatches(other: SupportChatMessage): Boolean {
        if (text.isNullOrBlank() != other.text.isNullOrBlank()) return false
        val a = attachment ?: return other.attachment == null
        val b = other.attachment ?: return false
        return (a is SupportChatAttachment.Image && b is SupportChatAttachment.Image) ||
            (a is SupportChatAttachment.Document && b is SupportChatAttachment.Document)
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
            is Response.Success -> loadInitialHistory()
            is Response.Error -> {
                // Сессия не открыта: баннер offline поверх скелетона,
                // повтор — кнопкой баннера (onConnectionRetry).
                _state.update { it.copy(isOffline = true) }
            }
        }
    }

    private suspend fun loadInitialHistory() {
        _state.update { it.copy(history = LCEState.Loading()) }
        when (val response = withContext(dispatchers.io) { repository.loadNextMessages() }) {
            is Response.Success -> _state.update { mergeHistoryPage(it, response.value) }
            is Response.Error -> {
                logger.d(response.exception) { "Failed to load initial history" }
                // Полноэкранная ошибка с кнопкой «Повторить» (onHistoryRetry).
                _state.update { it.copy(history = LCEState.Error(error = "")) }
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
    }
}
