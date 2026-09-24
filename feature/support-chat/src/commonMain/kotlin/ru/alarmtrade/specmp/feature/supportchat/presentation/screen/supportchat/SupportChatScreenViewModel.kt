package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.supportchat.data.outbox.SupportChatOutboxEntry
import ru.alarmtrade.specmp.feature.supportchat.data.outbox.SupportChatOutboxStore
import ru.alarmtrade.specmp.feature.supportchat.data.outbox.toOutboxEntries
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryPage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatEffect
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaveResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SupportChatScreenViewModel(
    private val repository: SupportChatRepository,
    private val dispatchers: AppDispatchers,
    // Application-scoped скоуп: закрытие чат-сессии из onCleared должно пережить
    // viewModelScope (уже отменён на момент onCleared).
    private val appScope: CoroutineScope,
    // Платформенный сейвер из composeApp: зависимость конструктора, передаётся
    // через parametersOf при создании VM (нужен composition context на Android).
    private val fileSaver: SupportChatFileSaver,
    // Персистентная очередь неотправленных: переживает смерть VM и пересоздание
    // процесса; очищается при логауте (зарегистрирован как AuthCloseable).
    private val outboxStore: SupportChatOutboxStore,
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

    // Оба только main-confined: пишутся из интентов экрана (main) и сброса
    // таймаута в viewModelScope.
    private val markedAsReadIds = mutableSetOf<Long>()
    private val pendingReadIds = mutableSetOf<Long>()
    private var readFlushJob: Job? = null

    // Клиентские id оптимистичных сообщений: метка времени + счётчик инстанса VM.
    // Контракт идентичности — в KDoc SupportChatMessage; уникальность держится
    // в пределах инстанса VM (пер-ентри скоуп не даёт двум VM пересечься).
    private var clientMessageIdSequence = 0L

    init {
        // Очередь восстанавливается первой: это самые новые сообщения,
        // история при своей загрузке встаёт перед ними.
        restoreOutbox()
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

    // Тип вложения определяется источником выбора, а не содержимым файла:
    // картинка, выбранная как документ, должна уйти документом.
    fun onImagePicked(file: SupportChatPickedFile) {
        addPendingAttachment(file.toUiAttachment(SupportChatUiAttachment.Kind.Image))
    }

    fun onDocumentPicked(file: SupportChatPickedFile) {
        addPendingAttachment(file.toUiAttachment(SupportChatUiAttachment.Kind.Document))
    }

    private fun addPendingAttachment(attachment: SupportChatUiAttachment) {
        _state.update {
            it.copy(pendingAttachments = (it.pendingAttachments + attachment).toPersistentList())
        }
    }

    fun onRemoveAttachment(id: String) {
        _state.update { current ->
            current.copy(pendingAttachments = current.pendingAttachments.filterNot { it.id == id }.toPersistentList())
        }
    }

    // Скачивание доступно только для входящих вложений (от поддержки) — см. UI.
    fun onDownloadAttachment(attachment: SupportChatUiAttachment) {
        val url = attachment.url ?: return

        // Атомарный guard: двойное нажатие не пройдёт (раньше проверка и запись
        // были разнесены, оба нажатия проходили, второе молча игнорировалось).
        _state.update { current ->
            if (current.downloadingAttachmentId != null) {
                current
            } else {
                current.copy(downloadingAttachmentId = attachment.id)
            }
        }
        if (_state.value.downloadingAttachmentId != attachment.id) return

        safeLaunch {
            when (val response = withContext(dispatchers.io) { repository.downloadFile(url) }) {
                is Response.Success -> {
                    when (val result = withContext(dispatchers.io) {
                        fileSaver.saveToDownloads(attachment.name, response.value)
                    }) {
                        is SupportChatFileSaveResult.Saved ->
                            _effect.tryEmit(SupportChatEffect.AttachmentDownloaded(result.path))
                        // Пользователь сам отменил системный диалог — не ошибка.
                        SupportChatFileSaveResult.Cancelled -> Unit
                        SupportChatFileSaveResult.Failed ->
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

    fun onSend(text: String) {
        val trimmed = text.trim()
        val attachments = _state.value.pendingAttachments
        if (trimmed.isEmpty() && attachments.isEmpty()) return

        val timestamp = now()
        val outgoingMessages = buildList {
            attachments.forEach { attachment ->
                add(createOptimisticMessage(timestamp = timestamp, attachment = attachment))
            }
            if (trimmed.isNotEmpty()) {
                add(createOptimisticMessage(timestamp = timestamp, text = trimmed))
            }
        }

        _state.update {
            it.copy(
                history = LCEState.Content((it.messages + outgoingMessages).toPersistentList()),
                pendingAttachments = persistentListOf(),
            )
        }

        safeLaunch {
            outgoingMessages.forEach { message ->
                sendOutgoingMessage(
                    clientMessageId = message.id,
                    text = message.text.orEmpty(),
                    attachment = message.attachment,
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
                attachment = message.attachment,
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

    fun onMessagesVisible(messageIds: List<String>) {
        // Батчинг (P-11): скролл истории отдаёт пачки видимых id — помечаем
        // прочитанными одним сетевым вызовом с debounce, а не запросом на
        // каждое сообщение.
        val eligibleServerIds = mutableListOf<Long>()
        val idSet = messageIds.toHashSet()
        _state.value.messages.forEach { message ->
            val serverId = message.serverId ?: return@forEach
            if (message.id in idSet &&
                !message.isMine &&
                message.status != SupportChatMessageStatus.Read &&
                serverId !in markedAsReadIds &&
                serverId !in pendingReadIds
            ) {
                eligibleServerIds += serverId
            }
        }
        if (eligibleServerIds.isEmpty()) return

        pendingReadIds += eligibleServerIds
        // Не перезапускаем таймер при непрерывном скролле: flush не реже
        // одного раза за READ_FLUSH_DELAY.
        if (readFlushJob?.isActive == true) return
        readFlushJob = safeLaunch {
            delay(READ_FLUSH_DELAY)
            flushReadIds()
        }
    }

    private suspend fun flushReadIds() {
        val ids = pendingReadIds.toList()
        if (ids.isEmpty()) return

        // Отмечаем как «в работе» сразу, чтобы не отправить дубль; при ошибке
        // возвращаем в буфер — следующая видимость доотправит.
        markedAsReadIds += ids
        pendingReadIds.clear()
        val response = withContext(dispatchers.io) { repository.markMessagesAsRead(ids) }
        if (response is Response.Error) {
            logger.d(response.exception) { "Failed to mark messages as read" }
            markedAsReadIds -= ids.toSet()
            pendingReadIds += ids
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
            is SupportChatEvent.TypingChanged -> {
                // Своё событие набора (SDK эхит его обратно) не рисуем.
                if (!event.isMine) {
                    handleTypingChanged(event.authorEmail, event.isTyping)
                }
            }
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
            current.copy(history = LCEState.Content(updatedMessages.toPersistentList()))
        }
        // Серверное эхо заменило оптимистичное сообщение: его запись
        // покидает очередь (снимок берёт только serverId == null).
        persistOutbox()
    }

    private fun List<SupportChatMessage>.indexOfEcho(message: SupportChatMessage): Int {
        // Обе ветки — FIFO: порядок подтверждений сервера соответствует
        // порядку отправки (P-9).
        indexOfFirst { it.isMine && it.serverId == null && it.contentMatches(message) }
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
            history = LCEState.Content((newMessages + current.messages).toPersistentList()),
            isLoadingMore = false,
            hasMoreHistory = page.hasMore,
        )
    }

    private fun handleMessagesRead(ids: List<Long>) {
        updateMessages { messages ->
            messages.map { message ->
                if (message.serverId != null && message.serverId in ids) {
                    message.copy(status = SupportChatMessageStatus.Read)
                } else {
                    message
                }
            }
        }
    }

    private suspend fun sendOutgoingMessage(
        clientMessageId: String,
        text: String,
        attachment: SupportChatAttachment?,
    ) {
        if (_state.value.isOffline) {
            updateMessageStatus(clientMessageId, SupportChatMessageStatus.Pending)
            return
        }

        // Контент сообщения эксклюзивен (SDK не поддерживает подпись к вложению),
        // поэтому каждая ветка здесь — либо текст, либо вложение. «Нечего
        // отправлять» — ошибка контракта, а не молчаливый Success: иначе
        // сообщение получит статус Sent, ничего не отправив (P-7).
        val response = when {
            text.isNotBlank() && attachment != null -> unsupportedMessageContent()
            text.isNotBlank() -> withContext(dispatchers.io) { repository.sendMessage(text) }
            attachment?.localPath != null -> sendAttachment(attachment)
            attachment != null -> missingLocalFile()
            else -> nothingToSend()
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

    private fun unsupportedMessageContent(): Response.Error {
        logger.d { "Message with both text and attachment is not supported by SDK" }
        return Response.Error(IllegalStateException("Message cannot carry text and attachment together"))
    }

    private fun missingLocalFile(): Response.Error {
        logger.d { "Attachment local file is missing, cannot resend" }
        return Response.Error(IllegalStateException("Attachment local file is missing"))
    }

    private fun nothingToSend(): Response.Error {
        logger.d { "Message has neither text nor attachment" }
        return Response.Error(IllegalStateException("Nothing to send"))
    }

    private suspend fun sendAttachment(attachment: SupportChatAttachment): Response<Unit> {
        val localPath = attachment.localPath ?: return missingLocalFile()
        val file = SupportChatPickedFile(
            id = attachment.id,
            name = attachment.name,
            path = localPath,
            mimeType = (attachment as? SupportChatAttachment.Document)?.mimeType,
            sizeBytes = (attachment as? SupportChatAttachment.Document)?.sizeBytes,
        )
        return withContext(dispatchers.io) {
            when (attachment) {
                is SupportChatAttachment.Image -> repository.sendImage(file)
                is SupportChatAttachment.Document -> repository.sendDocument(file)
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
                attachment = message.attachment,
            )
        }
    }

    private fun updateMessageStatus(id: String, status: SupportChatMessageStatus) {
        updateMessages { messages -> messages.map { if (it.id == id) it.copy(status = status) else it } }
        // Любой переход статуса меняет снимок очереди: Sent/Read удаляют
        // запись, Pending/Failed — сохраняют.
        persistOutbox()
    }

    private fun updateMessages(transform: (List<SupportChatMessage>) -> List<SupportChatMessage>) {
        _state.update { current ->
            current.copy(history = LCEState.Content(transform(current.messages).toPersistentList()))
        }
    }

    private fun restoreOutbox() {
        val restored = outboxStore.load().map { it.toMessage() }
        if (restored.isEmpty()) return
        _state.update { current ->
            current.copy(history = LCEState.Content((current.messages + restored).toPersistentList()))
        }
    }

    private fun persistOutbox() {
        outboxStore.save(_state.value.messages.toOutboxEntries())
    }

    private fun SupportChatOutboxEntry.toMessage(): SupportChatMessage {
        val domainAttachment = attachment?.let { stored ->
            when (stored.kind) {
                SupportChatOutboxEntry.Kind.Image -> SupportChatAttachment.Image(
                    id = stored.id,
                    name = stored.name,
                    localPath = stored.localPath,
                )
                SupportChatOutboxEntry.Kind.Document -> SupportChatAttachment.Document(
                    id = stored.id,
                    name = stored.name,
                    localPath = stored.localPath,
                    mimeType = stored.mimeType,
                    sizeBytes = stored.sizeBytes,
                )
            }
        }
        return SupportChatMessage(
            id = clientId,
            serverId = null,
            text = text,
            attachment = domainAttachment,
            time = Instant.fromEpochMilliseconds(timeMillis).toLocalDateTime(TimeZone.currentSystemDefault()),
            isMine = true,
            authorEmail = null,
            status = when (status) {
                SupportChatOutboxEntry.Status.Pending -> SupportChatMessageStatus.Pending
                SupportChatOutboxEntry.Status.Failed -> SupportChatMessageStatus.Failed
            },
        )
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
        // В Content не сбрасываем: лента (в т.ч. восстановленная из outbox)
        // остаётся на экране, а сброс на Loading стёр бы её до merge.
        _state.update { current ->
            if (current.history is LCEState.Content) {
                current
            } else {
                current.copy(history = LCEState.Loading())
            }
        }
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
        attachment: SupportChatUiAttachment? = null,
    ): SupportChatMessage {
        return SupportChatMessage(
            id = nextClientMessageId(),
            serverId = null,
            text = text,
            attachment = attachment?.toDomain(),
            time = timestamp,
            isMine = true,
            authorEmail = null,
            status = SupportChatMessageStatus.Sending,
        )
    }

    private fun nextClientMessageId(): String {
        return "local-${Clock.System.now().toEpochMilliseconds()}-${clientMessageIdSequence++}"
    }

    private fun now(): LocalDateTime {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    private fun SupportChatUiAttachment.toDomain(): SupportChatAttachment {
        return when (kind) {
            SupportChatUiAttachment.Kind.Image -> SupportChatAttachment.Image(
                id = id,
                name = name,
                localPath = localPath,
            )
            SupportChatUiAttachment.Kind.Document -> SupportChatAttachment.Document(
                id = id,
                name = name,
                localPath = localPath,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
            )
        }
    }

    private fun SupportChatPickedFile.toUiAttachment(kind: SupportChatUiAttachment.Kind): SupportChatUiAttachment {
        return SupportChatUiAttachment(
            id = id,
            name = name,
            kind = kind,
            url = null,
            localPath = path,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
        )
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

    private companion object {
        val TYPING_TIMEOUT = 5.seconds
        val READ_FLUSH_DELAY = 300.milliseconds
    }

}
