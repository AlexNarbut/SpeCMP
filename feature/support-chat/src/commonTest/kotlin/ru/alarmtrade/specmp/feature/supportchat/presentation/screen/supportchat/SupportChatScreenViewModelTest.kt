package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.supportchat.data.outbox.SupportChatOutboxEntry
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatEffect
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SupportChatScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSupportChatRepository
    private lateinit var saver: FakeSupportChatFileSaver
    private lateinit var outboxStore: FakeSupportChatOutboxStore
    private lateinit var dispatchers: AppDispatchers

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSupportChatRepository()
        saver = FakeSupportChatFileSaver()
        outboxStore = FakeSupportChatOutboxStore()
        dispatchers = AppDispatchers(
            main = testDispatcher,
            io = testDispatcher,
            default = testDispatcher,
        )
    }

    @Test
    fun `events are collected and messages are formed`() = runTest {
        val viewModel = createViewModel()
        val incoming = incomingMessage(id = 1, text = "Hello")

        repository.emitEvent(SupportChatEvent.MessageReceived(incoming))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
        assertEquals("Hello", viewModel.state.value.messages.first().text)
        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.first().status)
    }

    @Test
    fun `initial history emitted during load is collected`() = runTest {
        repository.historyPageOnLoad = listOf(incomingMessage(id = 11, text = "From history"))

        val viewModel = createViewModel()

        assertEquals(listOf("From history"), viewModel.state.value.messages.map { it.text })
        assertTrue(viewModel.state.value.history is LCEState.Content)
    }

    @Test
    fun `send adds optimistic message with Sending status`() = runTest {
        val viewModel = createViewModel()

        viewModel.onSend("Test message")
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
        assertEquals("Test message", viewModel.state.value.messages.first().text)
        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.first().status)
        assertEquals(listOf("Test message"), repository.sentMessages)
    }

    @Test
    fun `message status updates to Read when MessagesRead event arrives`() = runTest {
        val viewModel = createViewModel()
        val incoming = incomingMessage(id = 42, text = "Read me")

        repository.emitEvent(SupportChatEvent.MessageReceived(incoming))
        advanceUntilIdle()

        repository.emitEvent(SupportChatEvent.MessagesRead(listOf(42)))
        advanceUntilIdle()

        assertEquals(SupportChatMessageStatus.Read, viewModel.state.value.messages.first().status)
    }

    @Test
    fun `visible incoming message is marked as read`() = runTest {
        val viewModel = createViewModel()
        val incoming = incomingMessage(id = 7, text = "Visible")

        repository.emitEvent(SupportChatEvent.MessageReceived(incoming))
        advanceUntilIdle()

        viewModel.onMessagesVisible(listOf("7"))
        advanceUntilIdle()

        assertEquals(SupportChatMessageStatus.Read, viewModel.state.value.messages.first().status)
        assertEquals(listOf(listOf(7L)), repository.markedAsRead)
    }

    @Test
    fun `visible incoming messages are marked as read in one batch call`() = runTest {
        val viewModel = createViewModel()
        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 7, text = "First")))
        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 8, text = "Second")))
        advanceUntilIdle()

        // Два сообщения видимы в одном кадре скролла — один сетевой вызов (P-11).
        viewModel.onMessagesVisible(listOf("7", "8"))
        advanceUntilIdle()

        assertEquals(listOf(listOf(7L, 8L)), repository.markedAsRead)
    }

    @Test
    fun `offline queue puts message to Pending and retries on Connected`() = runTest {
        val viewModel = createViewModel()

        repository.emitEvent(SupportChatEvent.ConnectionChanged(false))
        runCurrent()

        viewModel.onSend("Queued")
        runCurrent()

        assertEquals(SupportChatMessageStatus.Pending, viewModel.state.value.messages.first().status)
        assertTrue(repository.sentMessages.isEmpty())

        repository.emitEvent(SupportChatEvent.ConnectionChanged(true))
        runCurrent()

        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.first().status)
        assertEquals(listOf("Queued"), repository.sentMessages)
    }

    @Test
    fun `connection retry reconnects and sends queued messages`() = runTest {
        val viewModel = createViewModel()
        val callsAfterInit = repository.openChatCallCount

        repository.emitEvent(SupportChatEvent.ConnectionChanged(false))
        runCurrent()
        viewModel.onSend("Queued")
        runCurrent()

        viewModel.onConnectionRetry()
        runCurrent()

        assertEquals(callsAfterInit + 1, repository.openChatCallCount)
        assertEquals(listOf("Queued"), repository.sentMessages)
        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.single().status)
        assertEquals(false, viewModel.state.value.isOffline)
    }

    @Test
    fun `connection lost tears down websocket and shows offline banner`() = runTest {
        val viewModel = createViewModel()
        val closeCallsAfterInit = repository.closeChatCallCount

        repository.emitEvent(SupportChatEvent.ConnectionChanged(false))
        runCurrent()

        // closeSocket обнуляет webSocket.value в SDK — это останавливает
        // рекурсивный цикл receiveMessage в iosMain, вешающий main-поток.
        assertEquals(true, viewModel.state.value.isOffline)
        assertEquals(closeCallsAfterInit + 1, repository.closeChatCallCount)
    }

    @Test
    fun `connection lost does not auto-reconnect without manual retry`() = runTest {
        val viewModel = createViewModel()
        val openCallsAfterInit = repository.openChatCallCount

        repository.emitEvent(SupportChatEvent.ConnectionChanged(false))
        advanceUntilIdle()

        // Никаких фоновых попыток: переподключение только кнопкой на баннере.
        assertEquals(openCallsAfterInit, repository.openChatCallCount)
        assertEquals(true, viewModel.state.value.isOffline)
    }

    @Test
    fun `ws error event switches to offline once and ignores error storm`() = runTest {
        val viewModel = createViewModel()
        val closeCallsAfterInit = repository.closeChatCallCount

        repository.emitError(Exception("socket lost"))
        runCurrent()

        assertEquals(true, viewModel.state.value.isOffline)
        assertEquals(closeCallsAfterInit + 1, repository.closeChatCallCount)

        // Шторм повторных ошибок (на iOS обрыв сокета шлёт их ~кГц) не должен
        // повторно дёргать SDK и состояние.
        repository.emitError(Exception("socket lost"))
        repository.emitError(Exception("socket lost"))
        runCurrent()

        assertEquals(closeCallsAfterInit + 1, repository.closeChatCallCount)
        assertEquals(true, viewModel.state.value.isOffline)
    }

    @Test
    fun `connected event during openChat does not cancel history loading`() = runTest {
        repository.openChatResult = Response.Error(Exception("Offline"))
        val viewModel = createViewModel()
        runCurrent()

        assertEquals(true, viewModel.state.value.isOffline)
        repository.openChatResult = Response.Success(Unit)
        repository.emitConnectedDuringNextOpenChat = true
        repository.historyPageOnLoad = listOf(incomingMessage(id = 15, text = "Loaded after reconnect"))

        viewModel.onConnectionRetry()
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.isOffline)
        assertEquals(listOf("Loaded after reconnect"), viewModel.state.value.messages.map { it.text })
    }

    @Test
    fun `pending messages retry sequentially in queue order`() = runTest {
        val viewModel = createViewModel()
        val queuedTexts = listOf("First", "Second", "Third")

        repository.emitEvent(SupportChatEvent.ConnectionChanged(false))
        runCurrent()
        queuedTexts.forEach { text ->
            viewModel.onSend(text)
        }
        runCurrent()
        assertTrue(viewModel.state.value.messages.all { it.status == SupportChatMessageStatus.Pending })

        repository.outgoingDelayMillis = 1_000
        repository.emitEvent(SupportChatEvent.ConnectionChanged(true))
        runCurrent()

        assertEquals(1, repository.maxConcurrentOutgoingCalls)
        advanceUntilIdle()
        assertEquals(queuedTexts, repository.sentMessages)
        assertEquals(1, repository.maxConcurrentOutgoingCalls)
        assertTrue(viewModel.state.value.messages.all { it.status == SupportChatMessageStatus.Sent })
    }

    @Test
    fun `connection retry sends queued attachment`() = runTest {
        val viewModel = createViewModel()
        val image = SupportChatPickedFile(
            id = "queued-image",
            name = "mount.png",
            path = "/tmp/mount.png",
            mimeType = "image/png",
        )

        repository.emitEvent(SupportChatEvent.ConnectionChanged(false))
        runCurrent()
        viewModel.onImagePicked(image)
        viewModel.onSend("")
        runCurrent()

        assertEquals(SupportChatMessageStatus.Pending, viewModel.state.value.messages.single().status)
        viewModel.onConnectionRetry()
        runCurrent()

        assertEquals(listOf("queued-image"), repository.sentImages.map { it.id })
        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.single().status)
    }

    @Test
    fun `server echo with renamed attachment merges into optimistic image instead of duplicating`() = runTest {
        val viewModel = createViewModel()
        viewModel.onImagePicked(
            SupportChatPickedFile(
                id = "img-1",
                name = "local-name.png",
                path = "/tmp/local-name.png",
                mimeType = "image/png",
            ),
        )
        viewModel.onSend("")
        runCurrent()
        assertEquals(1, viewModel.state.value.messages.size)

        repository.emitEvent(SupportChatEvent.MessageReceived(ownImageEcho(id = 100, attachmentName = "server-renamed.png")))
        runCurrent()

        assertEquals(1, viewModel.state.value.messages.size)
        assertEquals(100L, viewModel.state.value.messages.single().serverId)
    }

    @Test
    fun `server echoes for two pending images merge in FIFO order`() = runTest {
        val viewModel = createViewModel()
        viewModel.onImagePicked(
            SupportChatPickedFile(id = "img-a", name = "a.png", path = "/tmp/a.png", mimeType = "image/png"),
        )
        viewModel.onSend("")
        runCurrent()
        viewModel.onImagePicked(
            SupportChatPickedFile(id = "img-b", name = "b.png", path = "/tmp/b.png", mimeType = "image/png"),
        )
        viewModel.onSend("")
        runCurrent()
        assertEquals(2, viewModel.state.value.messages.size)

        repository.emitEvent(SupportChatEvent.MessageReceived(ownImageEcho(id = 101, attachmentName = "renamed-a.png")))
        runCurrent()
        repository.emitEvent(SupportChatEvent.MessageReceived(ownImageEcho(id = 102, attachmentName = "renamed-b.png")))
        runCurrent()

        assertEquals(2, viewModel.state.value.messages.size)
        assertEquals(listOf(101L, 102L), viewModel.state.value.messages.map { it.serverId })
    }

    @Test
    fun `typing indicator toggles by Writing events`() = runTest {
        val viewModel = createViewModel()

        repository.emitEvent(SupportChatEvent.TypingChanged("agent@example.com", true, isMine = false))
        runCurrent()

        assertEquals("agent@example.com", viewModel.state.value.typingAuthor)

        repository.emitEvent(SupportChatEvent.TypingChanged("agent@example.com", false, isMine = false))
        runCurrent()

        assertNull(viewModel.state.value.typingAuthor)
    }

    @Test
    fun `own typing event is ignored`() = runTest {
        val viewModel = createViewModel()

        repository.emitEvent(SupportChatEvent.TypingChanged("me@example.com", true, isMine = true))
        runCurrent()

        assertNull(viewModel.state.value.typingAuthor)
    }

    @Test
    fun `server echoes for two identical texts merge in FIFO order`() = runTest {
        val viewModel = createViewModel()
        viewModel.onSend("Same")
        runCurrent()
        viewModel.onSend("Same")
        runCurrent()
        assertEquals(2, viewModel.state.value.messages.size)

        repository.emitEvent(
            SupportChatEvent.MessageReceived(
                ownTextEcho(id = 201, text = "Same"),
            ),
        )
        runCurrent()
        repository.emitEvent(
            SupportChatEvent.MessageReceived(
                ownTextEcho(id = 202, text = "Same"),
            ),
        )
        runCurrent()

        assertEquals(2, viewModel.state.value.messages.size)
        assertEquals(listOf(201L, 202L), viewModel.state.value.messages.map { it.serverId })
    }

    @Test
    fun `failed send marks message as Failed and emits retry effect`() = runTest {
        repository.sendMessageResult = Response.Error(Exception("Network error"))
        val viewModel = createViewModel()

        val effects = mutableListOf<SupportChatEffect>()
        val collectJob = launch { viewModel.effect.collect { effects.add(it) } }
        runCurrent()

        viewModel.onSend("Fail me")
        advanceUntilIdle()

        val failedMessage = viewModel.state.value.messages.first()
        assertEquals(SupportChatMessageStatus.Failed, failedMessage.status)
        val effect = effects.filterIsInstance<SupportChatEffect.MessageSendFailed>().single()
        assertEquals(failedMessage.id, effect.messageId)
        collectJob.cancel()
    }

    @Test
    fun `picked image is added to pending attachments and sent`() = runTest {
        val viewModel = createViewModel()
        val file = SupportChatPickedFile(
            id = "img-1",
            name = "screenshot.png",
            path = "/tmp/screenshot.png",
            mimeType = "image/png",
        )

        viewModel.onImagePicked(file)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.pendingAttachments.size)
        assertEquals(SupportChatUiAttachment.Kind.Image, viewModel.state.value.pendingAttachments.first().kind)

        viewModel.onSend("")
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
        assertEquals(0, viewModel.state.value.pendingAttachments.size)
        assertEquals(1, repository.sentImages.size)
        assertEquals("img-1", repository.sentImages.first().id)
    }

    @Test
    fun `text and all attachments are sent as separate messages`() = runTest {
        val viewModel = createViewModel()
        val image = SupportChatPickedFile(
            id = "img-1",
            name = "install.png",
            path = "/tmp/install.png",
            mimeType = "image/png",
            sizeBytes = 512,
        )
        val document = SupportChatPickedFile(
            id = "doc-1",
            name = "scheme.pdf",
            path = "/tmp/scheme.pdf",
            mimeType = "application/pdf",
            sizeBytes = 2_048,
        )

        viewModel.onImagePicked(image)
        viewModel.onDocumentPicked(document)
        viewModel.onSend("See attachments")
        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.messages.size)
        assertEquals(2, viewModel.state.value.messages.count { it.attachment != null })
        assertEquals(1, viewModel.state.value.messages.count { it.text == "See attachments" })
        assertTrue(viewModel.state.value.pendingAttachments.isEmpty())
        assertEquals(listOf("img-1"), repository.sentImages.map { it.id })
        assertEquals(listOf("doc-1"), repository.sentDocuments.map { it.id })
        assertEquals(listOf("See attachments"), repository.sentMessages)
    }

    @Test
    fun `loadMore failure emits history load failed effect`() = runTest {
        repository.historyPageOnLoad = listOf(incomingMessage(id = 1, text = "Old"))
        val viewModel = createViewModel()
        repository.historyPageOnLoad = null

        val effects = mutableListOf<SupportChatEffect>()
        val collectJob = launch { viewModel.effect.collect { effects.add(it) } }
        runCurrent()

        repository.loadNextMessagesResult = Response.Error(Exception("Network error"))
        viewModel.onLoadMore()
        advanceUntilIdle()

        assertTrue(effects.any { it is SupportChatEffect.HistoryLoadFailed })
        assertEquals(false, viewModel.state.value.isLoadingMore)
        // Лента остаётся на экране: ошибка пагинации не затирает Content.
        assertTrue(viewModel.state.value.history is LCEState.Content)
        assertEquals(listOf("Old"), viewModel.state.value.messages.map { it.text })
        collectJob.cancel()
    }

    @Test
    fun `first load error shows fullscreen error state and retry recovers`() = runTest {
        repository.historyPageOnLoad = null
        repository.loadNextMessagesResult = Response.Error(Exception("Network error"))
        val viewModel = createViewModel()

        assertTrue(viewModel.state.value.history is LCEState.Error)

        repository.historyPageOnLoad = listOf(incomingMessage(id = 5, text = "Recovered"))
        viewModel.onHistoryRetry()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.history is LCEState.Content)
        assertEquals(listOf("Recovered"), viewModel.state.value.messages.map { it.text })
    }

    @Test
    fun `live message replaces fullscreen error with content`() = runTest {
        // Первая страница «зависла» (не эмитится), но сокет уже жив.
        repository.historyPageOnLoad = null
        repository.loadNextMessagesResult = Response.Error(Exception("timeout"))
        val viewModel = createViewModel()
        assertTrue(viewModel.state.value.history is LCEState.Error)

        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 9, text = "Live")))
        advanceUntilIdle()

        assertEquals(listOf("Live"), viewModel.state.value.messages.map { it.text })
    }

    @Test
    fun `empty history page sets hasMoreHistory to false`() = runTest {
        // По умолчанию фейк отдаёт пустую первую страницу.
        val viewModel = createViewModel()

        assertEquals(false, viewModel.state.value.hasMoreHistory)
        assertTrue(viewModel.state.value.history is LCEState.Content)
    }

    @Test
    fun `history page with messages keeps hasMoreHistory true`() = runTest {
        repository.historyPageOnLoad = listOf(incomingMessage(id = 1, text = "Old"))
        val viewModel = createViewModel()

        assertEquals(true, viewModel.state.value.hasMoreHistory)
        assertEquals(1, viewModel.state.value.messages.size)
    }

    @Test
    fun `duplicate history messages are ignored`() = runTest {
        val page = listOf(incomingMessage(id = 1, text = "Old"))
        repository.historyPageOnLoad = page
        val viewModel = createViewModel()
        assertEquals(1, viewModel.state.value.messages.size)

        // Вторая такая же страница (сбой пагинации): дедупликация по id.
        repository.historyPageOnLoad = page
        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
    }

    @Test
    fun `loadMore is no-op when hasMoreHistory is false`() = runTest {
        val viewModel = createViewModel()
        val callsAfterInit = repository.loadNextMessagesCallCount

        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(callsAfterInit, repository.loadNextMessagesCallCount)
    }

    @Test
    fun `image file picked as document stays document`() = runTest {
        val viewModel = createViewModel()
        val file = SupportChatPickedFile(
            id = "img-as-doc",
            name = "screenshot.png",
            path = "/tmp/screenshot.png",
            mimeType = "image/png",
        )

        viewModel.onDocumentPicked(file)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.pendingAttachments.first().kind == SupportChatUiAttachment.Kind.Document)

        viewModel.onSend("")
        advanceUntilIdle()

        assertEquals(listOf("img-as-doc"), repository.sentDocuments.map { it.id })
        assertTrue(repository.sentImages.isEmpty())
    }

    @Test
    fun `download attachment saves file and emits effect`() = runTest {
        val viewModel = createViewModel()
        val attachment = documentUiAttachment()

        val effects = mutableListOf<SupportChatEffect>()
        val collectJob = launch { viewModel.effect.collect { effects.add(it) } }
        runCurrent()

        viewModel.onDownloadAttachment(attachment)
        advanceUntilIdle()

        assertEquals(listOf("scheme.pdf"), saver.savedFiles.map { it.first })
        assertTrue(effects.any { it is SupportChatEffect.AttachmentDownloaded })
        assertNull(viewModel.state.value.downloadingAttachmentId)
        collectJob.cancel()
    }

    @Test
    fun `failed attachment download emits error effect`() = runTest {
        repository.downloadFileResult = Response.Error(Exception("No network"))
        val viewModel = createViewModel()
        val attachment = documentUiAttachment()

        val effects = mutableListOf<SupportChatEffect>()
        val collectJob = launch { viewModel.effect.collect { effects.add(it) } }
        runCurrent()

        viewModel.onDownloadAttachment(attachment)
        advanceUntilIdle()

        assertTrue(saver.savedFiles.isEmpty())
        assertTrue(effects.any { it is SupportChatEffect.AttachmentDownloadFailed })
        assertNull(viewModel.state.value.downloadingAttachmentId)
        collectJob.cancel()
    }

    @Test
    fun `failed attachment save emits error effect`() = runTest {
        saver.saveResult = ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaveResult.Failed
        val viewModel = createViewModel()
        val attachment = imageUiAttachment()

        val effects = mutableListOf<SupportChatEffect>()
        val collectJob = launch { viewModel.effect.collect { effects.add(it) } }
        runCurrent()

        viewModel.onDownloadAttachment(attachment)
        advanceUntilIdle()

        assertTrue(effects.any { it is SupportChatEffect.AttachmentDownloadFailed })
        collectJob.cancel()
    }

    @Test
    fun `cancelled attachment save emits nothing`() = runTest {
        saver.saveResult = ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaveResult.Cancelled
        val viewModel = createViewModel()
        val attachment = imageUiAttachment()

        val effects = mutableListOf<SupportChatEffect>()
        val collectJob = launch { viewModel.effect.collect { effects.add(it) } }
        runCurrent()

        viewModel.onDownloadAttachment(attachment)
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
        collectJob.cancel()
    }

    private fun ownTextEcho(id: Long, text: String): SupportChatMessage {
        return SupportChatMessage(
            id = id.toString(),
            serverId = id,
            text = text,
            attachment = null,
            time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isMine = true,
            authorEmail = "me@example.com",
            status = SupportChatMessageStatus.Sent,
        )
    }

    private fun documentUiAttachment(): SupportChatUiAttachment {
        return SupportChatUiAttachment(
            id = "doc-1",
            name = "scheme.pdf",
            kind = SupportChatUiAttachment.Kind.Document,
            url = "https://files/scheme.pdf",
            localPath = null,
            mimeType = "application/pdf",
            sizeBytes = 1024,
        )
    }

    private fun imageUiAttachment(): SupportChatUiAttachment {
        return SupportChatUiAttachment(
            id = "img-1",
            name = "photo.jpg",
            kind = SupportChatUiAttachment.Kind.Image,
            url = "https://files/photo.jpg",
            localPath = null,
            mimeType = null,
            sizeBytes = null,
        )
    }

    @Test
    fun `offline message is persisted to outbox and restored in new view model`() = runTest {
        val firstViewModel = createViewModel()
        repository.emitEvent(SupportChatEvent.ConnectionChanged(false))
        runCurrent()
        firstViewModel.onSend("Survive me")
        runCurrent()

        assertEquals(1, outboxStore.entries.size)
        assertEquals(SupportChatOutboxEntry.Status.Pending, outboxStore.entries.single().status)

        // «Смерть» VM: новый инстанс восстанавливает очередь из outbox
        // (события эмитим только при живом коллекторе — SharedFlow replay=0
        // не доставляет то, что было эмитнуто до подписки).
        firstViewModel.onCleared()
        firstViewModel.viewModelScope.coroutineContext.cancelChildren()
        advanceUntilIdle()

        val secondViewModel = createViewModel()
        val restored = secondViewModel.state.value.messages.single { it.text == "Survive me" }
        assertEquals(SupportChatMessageStatus.Pending, restored.status)

        // Очередь уходит по сети при восстановлении соединения.
        repository.emitEvent(SupportChatEvent.ConnectionChanged(true))
        advanceUntilIdle()

        assertEquals(listOf("Survive me"), repository.sentMessages)
        assertTrue(outboxStore.entries.isEmpty())
    }

    @Test
    fun `failed message stays in outbox with Failed status`() = runTest {
        val firstViewModel = createViewModel()
        repository.emitEvent(SupportChatEvent.ConnectionChanged(false))
        runCurrent()
        firstViewModel.onSend("Fail later")
        runCurrent()
        assertEquals(1, outboxStore.entries.size)

        repository.sendMessageResult = Response.Error(Exception("Network error"))
        repository.emitEvent(SupportChatEvent.ConnectionChanged(true))
        advanceUntilIdle()

        assertEquals(SupportChatOutboxEntry.Status.Failed, outboxStore.entries.single().status)
    }

    @Test
    fun `cleared view model closes chat via app scope`() = runTest {
        val viewModel = createViewModel()

        viewModel.onCleared()
        advanceUntilIdle()

        assertEquals(1, repository.closeChatCallCount)
    }

    @Test
    fun `new view model instance starts a clean session`() = runTest {
        repository.historyPageOnLoad = listOf(incomingMessage(id = 1, text = "Previous session"))
        val firstViewModel = createViewModel()
        assertEquals(1, firstViewModel.state.value.messages.size)

        // Переход dispose→open — это новый per-entry инстанс VM (декоратор
        // rememberViewModelStoreNavEntryDecorator): чистый state, новая сессия.
        firstViewModel.onCleared()
        firstViewModel.viewModelScope.coroutineContext.cancelChildren()
        advanceUntilIdle()

        repository.historyPageOnLoad = emptyList()
        val secondViewModel = createViewModel()

        assertTrue(secondViewModel.state.value.messages.isEmpty())
        advanceUntilIdle()

        assertEquals(1, repository.closeChatCallCount)
        assertEquals(2, repository.openChatCallCount)
        assertEquals(
            listOf(FakeSupportChatRepository.OPEN_CALL, FakeSupportChatRepository.CLOSE_CALL, FakeSupportChatRepository.OPEN_CALL),
            repository.connectionCalls,
        )
        assertTrue(secondViewModel.state.value.messages.isEmpty())
    }

    @Test
    fun `cleared view model stops collecting events`() = runTest {
        val viewModel = createViewModel()
        viewModel.onCleared()
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        advanceUntilIdle()

        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 1, text = "After clear")))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.messages.isEmpty())
    }

    private fun TestScope.createViewModel(): SupportChatScreenViewModel {
        val viewModel = SupportChatScreenViewModel(
            repository = repository,
            dispatchers = dispatchers,
            appScope = CoroutineScope(testDispatcher),
            fileSaver = saver,
            outboxStore = outboxStore,
        )
        advanceUntilIdle()
        return viewModel
    }

    private fun incomingMessage(id: Long, text: String): SupportChatMessage {
        return SupportChatMessage(
            id = id.toString(),
            serverId = id,
            text = text,
            attachment = null,
            time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isMine = false,
            authorEmail = "agent@example.com",
            status = SupportChatMessageStatus.Sent,
        )
    }

    private fun ownImageEcho(id: Long, attachmentName: String): SupportChatMessage {
        return SupportChatMessage(
            id = id.toString(),
            serverId = id,
            text = null,
            attachment = SupportChatAttachment.Image(
                id = "srv-$id",
                name = attachmentName,
                url = "https://example.com/files/$id",
            ),
            time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isMine = true,
            authorEmail = "me@example.com",
            status = SupportChatMessageStatus.Sent,
        )
    }
}
