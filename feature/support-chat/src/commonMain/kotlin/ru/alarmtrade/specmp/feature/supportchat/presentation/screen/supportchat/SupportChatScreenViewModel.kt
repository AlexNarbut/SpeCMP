package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatConnectionState
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryCursor
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatOutbox
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specmp.feature.supportchat.domain.timeline.SupportChatTimeline
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatConnection
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatEffect
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatError
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.isResendable
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.toDomain
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.toUiAttachment
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentFiles
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpener
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver

/**
 * ViewModel экрана чата. Соединением владеет SDK: VM держит сессию, пока жива (уход с экрана
 * отпускает её), отображает состояние соединения и на каждом `Ready` перечитывает первую
 * страницу истории и отправляет очередь. Инварианты ленты держит [SupportChatTimeline],
 * очередь — [SupportChatOutbox].
 *
 * Конкурентность:
 * - события сессии собираются на main (поток репозитория — на io), лента и флаги — только
 *   `_state.update {}`;
 * - загрузка истории — одна за раз ([historyJob], main-confined);
 * - отправка — [sendMutex]: новые, ретраи и очередь уходят строго по одной;
 * - outbox — явными переходами, собственный мьютекс реализации.
 */
class SupportChatScreenViewModel(
    private val repository: SupportChatRepository,
    private val dispatchers: AppDispatchers,
    // Платформенные порты из composeApp: зависимости конструктора, передаются
    // через parametersOf при создании VM.
    fileSaver: SupportChatFileSaver,
    private val attachmentFiles: SupportChatAttachmentFiles,
    fileOpener: SupportChatFileOpener,
    private val outbox: SupportChatOutbox
) : BaseViewModel() {

    private val _state = MutableStateFlow(SupportChatScreenState())
    val state: StateFlow<SupportChatScreenState> = _state.asStateFlow()

    private val _effect = sideEffectFlow<SupportChatEffect>()
    val effect: SharedFlow<SupportChatEffect> = _effect.asSharedFlow()

    private val sendMutex = Mutex()

    private val transfers = SupportChatAttachmentTransfers(repository, fileSaver, fileOpener, dispatchers)

    // Пишется только из коллектора событий (единственный последовательный контекст).
    private var typingTimeoutJob: Job? = null

    // Main-confined: сбор сессии и текущая загрузка страницы истории.
    private var sessionJob: Job? = null
    private var historyJob: Job? = null

    // Main-confined: интенты экрана и flush в viewModelScope.
    private val markedAsReadIds = mutableSetOf<Long>()
    private val pendingReadIds = mutableSetOf<Long>()
    private var readFlushJob: Job? = null

    init {
        sessionJob = safeLaunch {
            // Очередь восстанавливается первой: это самые новые сообщения,
            // история при своей загрузке встаёт перед ними.
            restoreOutbox()
            collectSession()
        }
    }

    // region Вложения композера

    fun onImagePicked(file: SupportChatPickedFile) {
        addPendingAttachment(file.toUiAttachment(SupportChatUiAttachment.Kind.Image))
    }

    fun onDocumentPicked(file: SupportChatPickedFile) {
        addPendingAttachment(file.toUiAttachment(SupportChatUiAttachment.Kind.Document))
    }

    fun onAttachmentRejected(tooLarge: Boolean) {
        _effect.tryEmit(SupportChatEffect.AttachmentRejected(tooLarge))
    }

    private fun addPendingAttachment(attachment: SupportChatUiAttachment) {
        _state.update {
            it.copy(pendingAttachments = (it.pendingAttachments + attachment).toPersistentList())
        }
    }

    fun onRemoveAttachment(id: String) {
        val removed = _state.value.pendingAttachments.firstOrNull { it.id == id } ?: return
        _state.update { current ->
            current.copy(pendingAttachments = current.pendingAttachments.filterNot { it.id == id }.toPersistentList())
        }
        removed.localPath?.let { path -> safeLaunch { discardFile(path) } }
    }

    // endregion

    // region Отправка

    fun onSend(text: String) {
        // Офлайн-ввод запрещён продуктово; UI выключает композер, VM — страховка.
        if (!_state.value.isInputEnabled) return
        val trimmed = text.trim()
        val attachments = _state.value.pendingAttachments
        if (trimmed.isEmpty() && attachments.isEmpty()) return

        // Контент сообщения эксклюзивен (SDK не поддерживает подпись к вложению):
        // каждое вложение и текст — отдельные сообщения.
        val timestamp = Clock.System.now()
        val outgoing = buildList {
            attachments.forEach { add(optimisticMessage(timestamp, attachment = it.toDomain())) }
            if (trimmed.isNotEmpty()) add(optimisticMessage(timestamp, text = trimmed))
        }

        _state.update {
            it.copy(
                history = LCEState.Content(it.timeline.withOptimistic(outgoing)),
                pendingAttachments = persistentListOf()
            )
        }

        safeLaunch {
            sendMutex.withLock {
                outgoing.forEach { send(it.id) }
            }
        }
    }

    fun onRetry(messageId: String) {
        // Атомарный переход Failed → Sending: второй тап (или action снекбара
        // параллельно с тапом по пузырю) перехода не получит и ничего не отправит.
        if (!tryTransition(messageId, from = RETRYABLE, to = SupportChatMessageStatus.Sending)) return
        safeLaunch {
            sendMutex.withLock { send(messageId) }
        }
    }

    fun onDeleteFailed(messageId: String) {
        val message = _state.value.timeline.find(messageId) ?: return
        if (!message.isMine || message.serverId != null || message.status != SupportChatMessageStatus.Failed) return
        updateTimeline { it.without(messageId) }
        safeLaunch { forget(message) }
    }

    /**
     * Отправка одного сообщения, уже переведённого в `Sending`. Вызывать под [sendMutex].
     */
    private suspend fun send(messageId: String) {
        val message = _state.value.timeline.find(messageId) ?: return
        if (message.status != SupportChatMessageStatus.Sending) return

        if (!_state.value.isInputEnabled) {
            park(message)
            return
        }

        // Запись очереди — до отправки: Success от SDK не подтверждает доставку, а
        // процесс может умереть посреди загрузки. Удалит запись только серверное эхо.
        withContext(dispatchers.io) { outbox.enqueue(message.copy(status = SupportChatMessageStatus.Pending)) }

        val response = when {
            !message.isResendable() -> missingContent(message)
            message.attachment != null -> sendAttachment(message.attachment)
            else -> withContext(dispatchers.io) { repository.sendMessage(message.text.orEmpty()) }
        }

        when (response) {
            is Response.Success -> {
                // «Ушло в сокет», а не «доставлено»: запись остаётся до эха. Если эхо
                // успело раньше, markSent не воскресит удалённую запись.
                updateStatus(messageId, SupportChatMessageStatus.Sent)
                withContext(dispatchers.io) { outbox.markSent(messageId) }
            }

            is Response.Error -> {
                logger.d(response.exception) { "Failed to send message" }
                if (response.exception.appError == ApiError.NoConnection || !_state.value.isInputEnabled) {
                    // Отправку оборвала потеря связи — это не ошибка пользователя:
                    // сообщение ждёт переподключения в очереди.
                    park(message)
                } else {
                    fail(message)
                }
            }
        }
    }

    private suspend fun park(message: SupportChatMessage) {
        updateStatus(message.id, SupportChatMessageStatus.Pending)
        withContext(dispatchers.io) { outbox.enqueue(message.copy(status = SupportChatMessageStatus.Pending)) }
    }

    private suspend fun fail(message: SupportChatMessage) {
        updateStatus(message.id, SupportChatMessageStatus.Failed)
        withContext(dispatchers.io) { outbox.enqueue(message.copy(status = SupportChatMessageStatus.Failed)) }
        _effect.tryEmit(SupportChatEffect.MessageSendFailed(message.id, canRetry = message.isResendable()))
    }

    private fun missingContent(message: SupportChatMessage): Response.Error {
        // Вложение без локального файла (копию вычистили) или пустой текст —
        // отправить нечего. Это не Success: сообщение уходит в Failed без Retry.
        logger.d { "Message ${message.id} has nothing to send" }
        return Response.Error(IllegalStateException("Message has nothing to send"))
    }

    private suspend fun sendAttachment(attachment: SupportChatAttachment): Response<Unit> {
        val localPath = attachment.localPath
            ?: return Response.Error(IllegalStateException("Attachment local file is missing"))
        val file = SupportChatPickedFile(
            id = attachment.id,
            name = attachment.name,
            path = localPath,
            mimeType = (attachment as? SupportChatAttachment.Document)?.mimeType,
            sizeBytes = (attachment as? SupportChatAttachment.Document)?.sizeBytes
        )
        return withContext(dispatchers.io) {
            when (attachment) {
                is SupportChatAttachment.Image -> repository.sendImage(file)
                is SupportChatAttachment.Document -> repository.sendDocument(file)
            }
        }
    }

    private fun retryPendingMessages() {
        safeLaunch {
            sendMutex.withLock {
                val pending = _state.value.messages
                    .filter { it.isMine && it.status == SupportChatMessageStatus.Pending }
                    .map { it.id }
                for (id in pending) {
                    if (!_state.value.isInputEnabled) break
                    if (tryTransition(id, from = PENDING, to = SupportChatMessageStatus.Sending)) {
                        send(id)
                    }
                }
            }
        }
    }

    // Сообщение покинуло ленту (эхо заменило оптимистичное, пользователь удалил):
    // запись очереди и локальная копия вложения больше не нужны.
    private suspend fun forget(message: SupportChatMessage) {
        withContext(dispatchers.io) { outbox.remove(message.id) }
        message.attachment?.localPath?.let { discardFile(it) }
    }

    private suspend fun discardFile(path: String) {
        withContext(dispatchers.io) { attachmentFiles.discard(path) }
    }

    // endregion

    // region Соединение

    /**
     * Сессия живёт, пока VM её собирает: отмена `viewModelScope` при уходе с экрана отпускает
     * соединение в SDK. В фоне заявка держится — SDK сам ждёт сеть и переподключается.
     */
    private suspend fun collectSession() {
        repository.session()
            .flowOn(dispatchers.io)
            .collect { handleEvent(it) }
    }

    private suspend fun handleEvent(event: SupportChatEvent) {
        when (event) {
            is SupportChatEvent.Connection -> handleConnection(event.state)

            is SupportChatEvent.TypingChanged -> {
                // Своё событие набора (SDK эхит его обратно) не рисуем.
                if (!event.isMine) handleTypingChanged(event.authorEmail, event.isTyping)
            }

            is SupportChatEvent.MessageReceived -> handleMessageReceived(event.message)

            is SupportChatEvent.MessagesRead -> updateTimeline { it.withRead(event.ids) }
        }
    }

    /** UI-состояние соединения — прямое отображение состояния SDK. */
    private fun handleConnection(state: SupportChatConnectionState) {
        when (state) {
            SupportChatConnectionState.Connecting ->
                _state.update { it.copy(connection = SupportChatConnection.Connecting) }

            SupportChatConnectionState.Ready -> {
                _state.update {
                    it.copy(
                        connection = SupportChatConnection.Online,
                        hasConnectionIssue = false,
                        isChatAuthFailed = false
                    )
                }
                // Пока соединения не было, могли прийти сообщения; очередь — после страницы.
                loadFirstHistoryPage()
            }

            SupportChatConnectionState.Reconnecting,
            SupportChatConnectionState.WaitingForNetwork,
            SupportChatConnectionState.AuthFailed -> {
                logger.d { "Chat connection lost: $state" }
                _state.update {
                    it.copy(
                        connection = SupportChatConnection.Offline,
                        hasConnectionIssue = true,
                        isChatAuthFailed = state == SupportChatConnectionState.AuthFailed
                    )
                }
                requeueUnconfirmed()
            }

            SupportChatConnectionState.Unavailable -> {
                logger.d { "Chat session unavailable" }
                _state.update {
                    it.copy(
                        connection = SupportChatConnection.Offline,
                        hasConnectionIssue = true,
                        // Показывать нечего — полноэкранная ошибка с «Повторить» (перезапуск сессии).
                        history = if (it.history is LCEState.Content) {
                            it.history
                        } else {
                            LCEState.Error(
                                SupportChatError.HistoryUnavailable
                            )
                        }
                    )
                }
            }
        }
    }

    // Отправленные в сокет, но без эха, при обрыве считаем недоставленными: до ping-таймаута
    // SDK отвечает Success и в мёртвый сокет. После переподключения они уйдут повторно
    // (at-least-once: дубль возможен, если эхо потерялось, а сообщение дошло).
    private fun requeueUnconfirmed() {
        val unconfirmed = _state.value.messages.filter {
            it.isMine && it.serverId == null && it.status == SupportChatMessageStatus.Sent
        }
        val requeued = unconfirmed.filter { tryTransition(it.id, from = SENT, to = SupportChatMessageStatus.Pending) }
        if (requeued.isEmpty()) return
        safeLaunch(dispatchers.io) {
            requeued.forEach { outbox.enqueue(it.copy(status = SupportChatMessageStatus.Pending)) }
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

    private suspend fun handleMessageReceived(message: SupportChatMessage) {
        val confirmed = updateTimelineConfirming(transform = { it.withIncoming(message) })
        confirmed.forEach { forget(it) }
    }

    /**
     * Применяет [transform] к ленте и возвращает оптимистичные сообщения, которые
     * исчезли из неё — их заменило серверное (эхо или страница истории): запись
     * очереди и локальная копия вложения больше не нужны.
     */
    private fun updateTimelineConfirming(
        transform: (SupportChatTimeline) -> SupportChatTimeline,
        extra: (SupportChatScreenState) -> SupportChatScreenState = { it }
    ): List<SupportChatMessage> {
        var confirmed = emptyList<SupportChatMessage>()
        _state.update { current ->
            val before = current.timeline
            val after = transform(before)
            confirmed = before.messages.filter { old -> old.serverId == null && after.find(old.id) == null }
            extra(current.copy(history = LCEState.Content(after)))
        }
        return confirmed
    }

    // endregion

    // region История

    fun onLoadMore() {
        val current = _state.value
        if (!current.isInputEnabled || !current.isInitialHistoryLoaded || !current.hasMoreHistory) return
        if (historyJob?.isActive == true) return
        _state.update { it.copy(isLoadingMore = true) }
        historyJob = safeLaunch { loadHistoryPage(current.historyCursor) }
    }

    fun onHistoryRetry() {
        when {
            // Сессию не удалось начать — начать заново.
            sessionJob?.isActive != true -> {
                _state.update { it.copy(connection = SupportChatConnection.Connecting, history = LCEState.Loading) }
                sessionJob = safeLaunch { collectSession() }
            }

            !_state.value.isInitialHistoryLoaded -> if (_state.value.isInputEnabled) loadFirstHistoryPage()

            else -> {
                _state.update { it.copy(isHistoryPagingFailed = false) }
                onLoadMore()
            }
        }
    }

    /**
     * Самая новая страница — на каждом `Ready`: пока соединения не было, могли прийти
     * сообщения. Перезапускает текущую загрузку. Пока первая страница не получена —
     * первичная загрузка (скелетон / полноэкранная ошибка), дальше — тихое обновление.
     */
    private fun loadFirstHistoryPage() {
        historyJob?.cancel()
        _state.update { current ->
            current.copy(
                isLoadingMore = false,
                // Лента (восстановленный outbox, live-сообщение) остаётся на экране.
                history = if (!current.isInitialHistoryLoaded && current.history !is LCEState.Content) {
                    LCEState.Loading
                } else {
                    current.history
                }
            )
        }
        historyJob = safeLaunch { loadHistoryPage(cursor = null) }
    }

    private suspend fun loadHistoryPage(cursor: SupportChatHistoryCursor?) {
        val isFirstPage = cursor == null
        when (val response = withContext(dispatchers.io) { repository.loadHistoryPage(cursor) }) {
            is Response.Success -> {
                val page = response.value
                val confirmed = updateTimelineConfirming(
                    transform = { it.withHistoryPage(page.messages) },
                    extra = { current ->
                        // Повторная первая страница (после переподключения) пагинацию не сбрасывает:
                        // курсор — номер страницы, новые сообщения сдвигают её только к дублям.
                        val resetPaging = !isFirstPage || !current.isInitialHistoryLoaded
                        current.copy(
                            isInitialHistoryLoaded = true,
                            isLoadingMore = false,
                            isHistoryPagingFailed = false,
                            historyCursor = if (resetPaging) page.nextCursor else current.historyCursor,
                            hasMoreHistory = if (resetPaging) page.nextCursor != null else current.hasMoreHistory
                        )
                    }
                )
                confirmed.forEach { forget(it) }
            }

            is Response.Error -> {
                logger.d(response.exception) { "Failed to load history page" }
                var hadContent = false
                _state.update { current ->
                    hadContent = current.history is LCEState.Content
                    current.copy(
                        isLoadingMore = false,
                        isHistoryPagingFailed = true,
                        // Показывать нечего — полноэкранная ошибка с «Повторить».
                        history = if (hadContent) {
                            current.history
                        } else {
                            LCEState.Error(
                                SupportChatError.from(response.exception)
                            )
                        }
                    )
                }
                if (hadContent) _effect.tryEmit(SupportChatEffect.HistoryLoadFailed)
            }
        }
        // Очередь — после самой новой страницы: дошедшие сообщения уже подтвердила история.
        if (isFirstPage) retryPendingMessages()
    }

    // endregion

    // region Прочтение

    fun onMessagesVisible(messageIds: List<String>) {
        // Батчинг: скролл истории отдаёт пачки видимых id — помечаем прочитанными
        // одним сетевым вызовом с debounce, а не запросом на каждое сообщение.
        val idSet = messageIds.toHashSet()
        val eligibleServerIds = _state.value.messages.mapNotNull { message ->
            val serverId = message.serverId
            serverId?.takeIf {
                message.id in idSet &&
                    !message.isMine &&
                    message.status != SupportChatMessageStatus.Read &&
                    serverId !in markedAsReadIds &&
                    serverId !in pendingReadIds
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

    // endregion

    // region Скачивание

    fun onDownloadAttachment(attachment: SupportChatUiAttachment) {
        if (attachment.url == null) return
        withFileSlot(attachment.id) { transfers.download(attachment) }
    }

    /** Открыть вложение во внешнем приложении (индикатор — тот же слот, что у скачивания). */
    fun onOpenAttachment(attachment: SupportChatUiAttachment) {
        if (attachment.localPath == null && attachment.url == null) return
        withFileSlot(attachment.id) { transfers.open(attachment) }
    }

    /**
     * Одна файловая операция за раз: слот захватывается атомарно в том же update, что и запись (повторный тап по
     * тому же или другому вложению не пройдёт), освобождается в finally.
     */
    private fun withFileSlot(attachmentId: String, block: suspend () -> SupportChatEffect?) {
        val previous = _state.getAndUpdate {
            if (it.downloadingAttachmentId == null) it.copy(downloadingAttachmentId = attachmentId) else it
        }
        if (previous.downloadingAttachmentId != null) return
        safeLaunch {
            try {
                block()?.let(_effect::tryEmit)
            } finally {
                _state.update { it.copy(downloadingAttachmentId = null) }
            }
        }
    }

    // endregion

    // region Внутреннее

    private suspend fun restoreOutbox() {
        val restored = withContext(dispatchers.io) { outbox.load() }
        if (restored.isEmpty()) return
        updateTimeline { it.withOptimistic(restored) }
    }

    private fun updateTimeline(transform: (SupportChatTimeline) -> SupportChatTimeline) {
        _state.update { current -> current.copy(history = LCEState.Content(transform(current.timeline))) }
    }

    private fun updateStatus(id: String, status: SupportChatMessageStatus) {
        updateTimeline { it.withStatus(id, status) }
    }

    private fun tryTransition(id: String, from: Set<SupportChatMessageStatus>, to: SupportChatMessageStatus): Boolean {
        var transitioned = false
        _state.update { current ->
            val next = current.timeline.tryTransition(id, from, to)
            transitioned = next != null
            if (next == null) current else current.copy(history = LCEState.Content(next))
        }
        return transitioned
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun optimisticMessage(
        timestamp: Instant,
        text: String? = null,
        attachment: SupportChatAttachment? = null
    ): SupportChatMessage = SupportChatMessage(
        // Глобально уникален: переживает пересоздание VM и процесса (ключ outbox).
        id = "$LOCAL_ID_PREFIX${Uuid.random()}",
        serverId = null,
        text = text,
        attachment = attachment,
        time = timestamp,
        isMine = true,
        authorEmail = null,
        status = SupportChatMessageStatus.Sending
    )

    // endregion

    private companion object {
        val TYPING_TIMEOUT = 5.seconds
        val READ_FLUSH_DELAY = 300.milliseconds
        val RETRYABLE = setOf(SupportChatMessageStatus.Failed)
        val PENDING = setOf(SupportChatMessageStatus.Pending)
        val SENT = setOf(SupportChatMessageStatus.Sent)
        const val LOCAL_ID_PREFIX = "local-"
    }
}
