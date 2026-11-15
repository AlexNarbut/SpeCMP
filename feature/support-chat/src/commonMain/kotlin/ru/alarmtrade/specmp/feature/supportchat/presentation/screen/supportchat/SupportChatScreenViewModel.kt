package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.core.common.onError
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
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.isResendable
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.toAttachment
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.toUiAttachment
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentFiles
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpener
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver

/**
 * Сессию VM держит всю свою жизнь; на каждом `Ready` перечитывает первую страницу истории и отправляет очередь.
 * Отправки (новые, ретраи, очередь) идут строго по одной под `executeWithProceedLock`.
 */
class SupportChatScreenViewModel(
    private val repository: SupportChatRepository,
    private val dispatchers: AppDispatchers,
    fileSaver: SupportChatFileSaver,
    private val attachmentFiles: SupportChatAttachmentFiles,
    fileOpener: SupportChatFileOpener,
    private val outbox: SupportChatOutbox,
    private val clock: Clock = Clock.System,
) : BaseViewModel<SupportChatScreenState, SupportChatEffect>(SupportChatScreenState()) {

    private val transfers = SupportChatAttachmentTransfers(repository, fileSaver, fileOpener, dispatchers)

    // Очередь восстанавливается первой (это самые новые сообщения, история при своей загрузке встаёт перед ними),
    // затем сессия.
    override suspend fun IntentScope.onStart() {
        restoreOutbox()
        startSession()
    }

    fun onImagePicked(file: SupportChatPickedFile) {
        intent { addPendingAttachment(file.toUiAttachment(SupportChatUiAttachment.Kind.Image)) }
    }

    fun onDocumentPicked(file: SupportChatPickedFile) {
        intent { addPendingAttachment(file.toUiAttachment(SupportChatUiAttachment.Kind.Document)) }
    }

    fun onAttachmentRejected(tooLarge: Boolean) {
        intent { postSideEffect(SupportChatEffect.AttachmentRejected(tooLarge)) }
    }

    private suspend fun addPendingAttachment(attachment: SupportChatUiAttachment) = subIntent {
        reduce { copy(pendingAttachments = (pendingAttachments + attachment).toPersistentList()) }
    }

    fun onRemoveAttachment(id: String) {
        intent {
            val removed = currentState.pendingAttachments.firstOrNull { it.id == id } ?: return@intent
            reduce { copy(pendingAttachments = pendingAttachments.filterNot { it.id == id }.toPersistentList()) }
            removed.localPath?.let { path -> discardFile(path) }
        }
    }

    fun onSend(text: String) {
        intent {
            // Офлайн-ввод запрещён продуктово; UI выключает композер, VM — страховка.
            if (!currentState.isInputEnabled) return@intent
            val trimmed = text.trim()
            val attachments = currentState.pendingAttachments
            if (trimmed.isEmpty() && attachments.isEmpty()) return@intent

            // Контент сообщения эксклюзивен (SDK не поддерживает подпись к вложению):
            // каждое вложение и текст — отдельные сообщения.
            val timestamp = clock.now()
            val outgoing = buildList {
                attachments.forEach { add(optimisticMessage(timestamp, attachment = it.toAttachment())) }
                if (trimmed.isNotEmpty()) add(optimisticMessage(timestamp, text = trimmed))
            }

            reduce { withTimeline(timeline.withOptimistic(outgoing)).copy(pendingAttachments = persistentListOf()) }

            executeWithProceedLock(needSkipAnother = false) {
                outgoing.forEach { send(it.id) }
            }
        }
    }

    fun onRetry(messageId: String) {
        intent {
            // Атомарный переход Failed → Sending: второй тап (или action снекбара
            // параллельно с тапом по пузырю) перехода не получит и ничего не отправит.
            if (!tryTransition(messageId, from = RETRYABLE, to = SupportChatMessageStatus.Sending)) return@intent
            executeWithProceedLock(needSkipAnother = false) { send(messageId) }
        }
    }

    fun onDeleteFailed(messageId: String) {
        intent {
            val message = currentState.timeline.find(messageId) ?: return@intent
            if (!message.isMine || message.serverId != null || message.status != SupportChatMessageStatus.Failed) {
                return@intent
            }
            updateTimeline { it.without(messageId) }
            forget(message)
        }
    }

    /** Сообщение уже в `Sending`; вызывать под `executeWithProceedLock`. */
    private suspend fun send(messageId: String) = subIntent {
        val message = currentState.timeline.find(messageId) ?: return@subIntent
        if (message.status != SupportChatMessageStatus.Sending) return@subIntent

        if (!currentState.isInputEnabled) {
            park(message)
            return@subIntent
        }

        // Запись очереди — до отправки: Success от SDK не подтверждает доставку, а
        // процесс может умереть посреди загрузки. Удалит запись только серверное эхо.
        updateOutbox { enqueue(message.copy(status = SupportChatMessageStatus.Pending)) }

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
                updateOutbox { markSent(messageId) }
            }

            is Response.Error -> {
                logger.d(response.exception) { "Failed to send message" }
                if (response.exception.appError == ApiError.NoConnection || !currentState.isInputEnabled) {
                    // Отправку оборвала потеря связи — это не ошибка пользователя:
                    // сообщение ждёт переподключения в очереди.
                    park(message)
                } else {
                    fail(message)
                }
            }
        }
    }

    private suspend fun park(message: SupportChatMessage) = subIntent {
        updateStatus(message.id, SupportChatMessageStatus.Pending)
        updateOutbox { enqueue(message.copy(status = SupportChatMessageStatus.Pending)) }
    }

    private suspend fun fail(message: SupportChatMessage) = subIntent {
        updateStatus(message.id, SupportChatMessageStatus.Failed)
        updateOutbox { enqueue(message.copy(status = SupportChatMessageStatus.Failed)) }
        postSideEffect(SupportChatEffect.MessageSendFailed(message.id, canRetry = message.isResendable()))
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
        intent {
            executeWithProceedLock(needSkipAnother = false) {
                val pending = currentState.messages
                    .filter { it.isMine && it.status == SupportChatMessageStatus.Pending }
                    .map { it.id }
                for (id in pending) {
                    if (!currentState.isInputEnabled) break
                    if (tryTransition(id, from = PENDING, to = SupportChatMessageStatus.Sending)) {
                        send(id)
                    }
                }
            }
        }
    }

    private suspend fun forget(message: SupportChatMessage) {
        updateOutbox { remove(message.id) }
        message.attachment?.localPath?.let { discardFile(it) }
    }

    private suspend fun discardFile(path: String) {
        withContext(dispatchers.io) { attachmentFiles.discard(path) }
    }

    // Сессия живёт, пока VM её собирает (уход с экрана отпускает заявку в SDK); в фоне заявка держится —
    // SDK сам ждёт сеть и переподключается. Поток завершается только ошибкой (сессию не удалось начать).
    private suspend fun startSession() = subIntent {
        reduce { copy(isSessionActive = true) }
        launchReplacing(SESSION_KEY) {
            try {
                repository.session()
                    .flowOn(dispatchers.io)
                    .collect { step ->
                        when (step) {
                            is Response.Success -> handleEvent(step.value)
                            is Response.Error -> handleSessionUnavailable(step.exception)
                        }
                    }
            } finally {
                reduce { copy(isSessionActive = false) }
            }
        }
    }

    private suspend fun handleEvent(event: SupportChatEvent) = subIntent {
        when (event) {
            is SupportChatEvent.Connection -> handleConnection(event.state)

            is SupportChatEvent.TypingChanged -> {
                if (!event.isMine) handleTypingChanged(event.authorEmail, event.isTyping)
            }

            is SupportChatEvent.MessageReceived -> handleMessageReceived(event.message)

            is SupportChatEvent.MessagesRead -> updateTimeline { it.withRead(event.ids) }
        }
    }

    private suspend fun handleConnection(state: SupportChatConnectionState) = subIntent {
        reduce { withConnection(state) }
        when (state) {
            // Пока соединения не было, могли прийти сообщения; очередь — после страницы.
            SupportChatConnectionState.Ready -> loadFirstHistoryPage()

            SupportChatConnectionState.Reconnecting,
            SupportChatConnectionState.WaitingForNetwork,
            SupportChatConnectionState.AuthFailed,
            -> {
                logger.d { "Chat connection lost: $state" }
                requeueUnconfirmed()
            }

            SupportChatConnectionState.Connecting -> Unit
        }
    }

    private suspend fun handleSessionUnavailable(error: Throwable) = subIntent {
        logger.d(error) { "Chat session unavailable" }
        reduce { withSessionUnavailable() }
    }

    // Отправленные без эха при обрыве считаем недоставленными: до ping-таймаута SDK отвечает Success и в мёртвый сокет.
    // После переподключения уйдут повторно (at-least-once).
    private suspend fun requeueUnconfirmed() = subIntent {
        val unconfirmed = currentState.messages.filter {
            it.isMine && it.serverId == null && it.status == SupportChatMessageStatus.Sent
        }
        val requeued = unconfirmed.filter { tryTransition(it.id, from = SENT, to = SupportChatMessageStatus.Pending) }
        if (requeued.isEmpty()) return@subIntent
        intent(dispatchers.io) {
            requeued.forEach { message ->
                outbox.enqueue(message.copy(status = SupportChatMessageStatus.Pending)).onError(::logOutboxFailure)
            }
        }
    }

    private suspend fun handleTypingChanged(authorEmail: String?, isTyping: Boolean) = subIntent {
        reduce { copy(typingAuthor = if (isTyping) authorEmail else null) }
        if (!isTyping || authorEmail == null) {
            cancelLaunched(TYPING_KEY)
            return@subIntent
        }
        launchReplacing(TYPING_KEY) {
            delay(TYPING_TIMEOUT)
            reduce { if (typingAuthor == authorEmail) copy(typingAuthor = null) else this }
        }
    }

    private suspend fun handleMessageReceived(message: SupportChatMessage) = subIntent {
        val confirmed = updateTimelineConfirming(transform = { it.withIncoming(message) })
        confirmed.forEach { forget(it) }
    }

    /** Возвращает оптимистичные сообщения, которые заменило серверное (эхо или страница истории). */
    private suspend fun updateTimelineConfirming(
        transform: (SupportChatTimeline) -> SupportChatTimeline,
        extra: SupportChatScreenState.() -> SupportChatScreenState = { this },
    ): List<SupportChatMessage> = subIntent {
        var confirmed = emptyList<SupportChatMessage>()
        reduce {
            val after = transform(timeline)
            confirmed = timeline.messages.filter { old -> old.serverId == null && after.find(old.id) == null }
            withTimeline(after).extra()
        }
        return@subIntent confirmed
    }

    fun onLoadMore() {
        intent { loadMore() }
    }

    fun onHistoryRetry() {
        intent {
            val current = currentState
            when {
                !current.isSessionActive -> {
                    reduce { copy(connection = SupportChatConnection.Connecting, history = LCEState.Loading) }
                    startSession()
                }

                !current.isInitialHistoryLoaded -> if (current.isInputEnabled) loadFirstHistoryPage()

                else -> {
                    reduce { copy(isHistoryPagingFailed = false) }
                    loadMore()
                }
            }
        }
    }

    private suspend fun loadMore() = subIntent {
        val current = currentState
        if (!current.isInputEnabled || !current.isInitialHistoryLoaded || !current.hasMoreHistory) return@subIntent
        if (current.isLoadingHistory) return@subIntent
        reduce { copy(isLoadingMore = true, isLoadingHistory = true) }
        launchReplacing(HISTORY_KEY) { loadHistoryPage(current.historyCursor) }
    }

    // Самая новая страница — на каждом `Ready`: заменяет текущую загрузку.
    private suspend fun loadFirstHistoryPage() = subIntent {
        reduce { withFirstPageLoading() }
        launchReplacing(HISTORY_KEY) { loadHistoryPage(cursor = null) }
    }

    private suspend fun loadHistoryPage(cursor: SupportChatHistoryCursor?) = subIntent {
        val isFirstPage = cursor == null
        when (val response = withContext(dispatchers.io) { repository.loadHistoryPage(cursor) }) {
            is Response.Success -> {
                val page = response.value
                val confirmed = updateTimelineConfirming(
                    transform = { it.withHistoryPage(page.messages) },
                    extra = { withHistoryPage(page, isFirstPage) },
                )
                confirmed.forEach { forget(it) }
            }

            is Response.Error -> {
                logger.d(response.exception) { "Failed to load history page" }
                var hadContent = false
                reduce {
                    hadContent = history is LCEState.Content
                    withHistoryError(response.exception)
                }
                if (hadContent) postSideEffect(SupportChatEffect.HistoryLoadFailed)
            }
        }
        // Очередь — после самой новой страницы: дошедшие сообщения уже подтвердила история.
        if (isFirstPage) retryPendingMessages()
    }

    fun onMessagesVisible(messageIds: List<String>) {
        intent {
            // Батч: пачки видимых при скролле id — одним сетевым вызовом с debounce.
            val idSet = messageIds.toHashSet()
            val known = currentState.readIds
            val eligibleServerIds = currentState.messages.mapNotNull { message ->
                message.serverId?.takeIf {
                    message.id in idSet &&
                        !message.isMine &&
                        message.status != SupportChatMessageStatus.Read &&
                        !known.isKnown(it)
                }
            }
            if (eligibleServerIds.isEmpty()) return@intent

            // Не перезапускаем таймер при непрерывном скролле: flush не реже одного раза за READ_FLUSH_DELAY.
            var scheduleFlush = false
            reduce {
                scheduleFlush = !readIds.isFlushScheduled
                copy(readIds = readIds.withPending(eligibleServerIds))
            }
            if (!scheduleFlush) return@intent
            try {
                delay(READ_FLUSH_DELAY)
                flushReadIds()
            } finally {
                reduce { copy(readIds = readIds.copy(isFlushScheduled = false)) }
            }
        }
    }

    private suspend fun flushReadIds() = subIntent {
        // Отмечаем как «в работе» сразу (одним переходом), чтобы не отправить дубль; при ошибке
        // возвращаем в буфер — уйдут со следующим батчем.
        var ids = emptySet<Long>()
        reduce {
            ids = readIds.pending
            copy(readIds = readIds.pendingTaken())
        }
        if (ids.isEmpty()) return@subIntent
        val response = withContext(dispatchers.io) { repository.markMessagesAsRead(ids.toList()) }
        if (response is Response.Error) {
            logger.d(response.exception) { "Failed to mark messages as read" }
            reduce { copy(readIds = readIds.returned(ids)) }
        }
    }

    fun onDownloadAttachment(attachment: SupportChatUiAttachment) {
        if (attachment.url == null) return
        withFileSlot(attachment.id) { transfers.download(attachment) }
    }

    fun onOpenAttachment(attachment: SupportChatUiAttachment) {
        if (attachment.localPath == null && attachment.url == null) return
        withFileSlot(attachment.id) { transfers.open(attachment) }
    }

    /**
     * Одна файловая операция за раз: слот захватывается атомарно в том же reduce, что и запись (повторный тап по
     * тому же или другому вложению не пройдёт), освобождается в finally.
     */
    private fun withFileSlot(attachmentId: String, block: suspend () -> SupportChatEffect?) {
        intent {
            var acquired = false
            reduce {
                acquired = downloadingAttachmentId == null
                if (acquired) copy(downloadingAttachmentId = attachmentId) else this
            }
            if (!acquired) return@intent
            try {
                block()?.let(::postSideEffect)
            } finally {
                reduce { copy(downloadingAttachmentId = null) }
            }
        }
    }

    private suspend fun restoreOutbox() = subIntent {
        val restored = when (val response = withContext(dispatchers.io) { outbox.load() }) {
            is Response.Success -> response.value

            is Response.Error -> {
                logOutboxFailure(response.exception)
                return@subIntent
            }
        }
        if (restored.isEmpty()) return@subIntent
        updateTimeline { it.withOptimistic(restored) }
    }

    private suspend fun updateOutbox(change: suspend SupportChatOutbox.() -> Response<Unit>) {
        withContext(dispatchers.io) { outbox.change() }.onError(::logOutboxFailure)
    }

    private fun logOutboxFailure(e: Throwable) {
        logger.w(e) { "Support chat outbox write failed" }
    }

    private suspend fun updateTimeline(transform: (SupportChatTimeline) -> SupportChatTimeline) = subIntent {
        reduce { withTimeline(transform(timeline)) }
    }

    private suspend fun updateStatus(id: String, status: SupportChatMessageStatus) = subIntent {
        updateTimeline { it.withStatus(id, status) }
    }

    private suspend fun tryTransition(
        id: String,
        from: Set<SupportChatMessageStatus>,
        to: SupportChatMessageStatus,
    ): Boolean = subIntent {
        var transitioned = false
        reduce {
            val next = timeline.tryTransition(id, from, to)
            transitioned = next != null
            if (next == null) this else withTimeline(next)
        }
        return@subIntent transitioned
    }

    private companion object {
        const val SESSION_KEY = "session"
        const val HISTORY_KEY = "history"
        const val TYPING_KEY = "typing"
        val TYPING_TIMEOUT = 5.seconds
        val READ_FLUSH_DELAY = 300.milliseconds
        val RETRYABLE = setOf(SupportChatMessageStatus.Failed)
        val PENDING = setOf(SupportChatMessageStatus.Pending)
        val SENT = setOf(SupportChatMessageStatus.Sent)
    }
}
