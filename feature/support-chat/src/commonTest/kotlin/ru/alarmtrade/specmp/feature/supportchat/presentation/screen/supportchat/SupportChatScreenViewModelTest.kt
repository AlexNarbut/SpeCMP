package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlinx.coroutines.Dispatchers
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
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPendingAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatPickedFile
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
    private lateinit var dispatchers: AppDispatchers

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSupportChatRepository()
        dispatchers = AppDispatchers(
            main = testDispatcher,
            io = testDispatcher,
            default = testDispatcher,
        )
    }

    @Test
    fun `events are collected and messages are formed`() = runTest {
        val viewModel = createViewModelAndStart()
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

        val viewModel = createViewModelAndStart()

        assertEquals(listOf("From history"), viewModel.state.value.messages.map { it.text })
        assertEquals(false, viewModel.state.value.isLoadingHistory)
    }

    @Test
    fun `send adds optimistic message with Sending status`() = runTest {
        val viewModel = createViewModelAndStart()

        viewModel.onDraftChange("Test message")
        viewModel.onSend()
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
        assertEquals("Test message", viewModel.state.value.messages.first().text)
        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.first().status)
        assertEquals(listOf("Test message"), repository.sentMessages)
    }

    @Test
    fun `message status updates to Read when MessagesRead event arrives`() = runTest {
        val viewModel = createViewModelAndStart()
        val incoming = incomingMessage(id = 42, text = "Read me")

        repository.emitEvent(SupportChatEvent.MessageReceived(incoming))
        advanceUntilIdle()

        repository.emitEvent(SupportChatEvent.MessagesRead(listOf(42)))
        advanceUntilIdle()

        assertEquals(SupportChatMessageStatus.Read, viewModel.state.value.messages.first().status)
    }

    @Test
    fun `visible incoming message is marked as read`() = runTest {
        val viewModel = createViewModelAndStart()
        val incoming = incomingMessage(id = 7, text = "Visible")

        repository.emitEvent(SupportChatEvent.MessageReceived(incoming))
        advanceUntilIdle()

        viewModel.onMessageVisible("7")
        advanceUntilIdle()

        assertEquals(SupportChatMessageStatus.Read, viewModel.state.value.messages.first().status)
        assertEquals(listOf(listOf(7L)), repository.markedAsRead)
    }

    @Test
    fun `offline queue puts message to Pending and retries on Connected`() = runTest {
        val viewModel = createViewModelAndStart()

        repository.emitEvent(SupportChatEvent.ConnectionChanged(false))
        runCurrent()

        viewModel.onDraftChange("Queued")
        viewModel.onSend()
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
        val viewModel = createViewModelAndStart()
        val callsAfterInit = repository.openChatCallCount

        repository.emitEvent(SupportChatEvent.ConnectionChanged(false))
        runCurrent()
        viewModel.onDraftChange("Queued")
        viewModel.onSend()
        runCurrent()

        viewModel.onConnectionRetry()
        runCurrent()

        assertEquals(callsAfterInit + 1, repository.openChatCallCount)
        assertEquals(listOf("Queued"), repository.sentMessages)
        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.single().status)
        assertEquals(false, viewModel.state.value.isOffline)
    }

    @Test
    fun `connected event during openChat does not cancel history loading`() = runTest {
        repository.openChatResult = Response.Error(Exception("Offline"))
        val viewModel = SupportChatScreenViewModel(
            repository = repository,
            dispatchers = dispatchers,
        )
        viewModel.onScreenOpened()
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
        val viewModel = createViewModelAndStart()
        val queuedTexts = listOf("First", "Second", "Third")

        repository.emitEvent(SupportChatEvent.ConnectionChanged(false))
        runCurrent()
        queuedTexts.forEach { text ->
            viewModel.onDraftChange(text)
            viewModel.onSend()
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
        val viewModel = createViewModelAndStart()
        val image = SupportChatPickedFile(
            id = "queued-image",
            name = "mount.png",
            path = "/tmp/mount.png",
            mimeType = "image/png",
        )

        repository.emitEvent(SupportChatEvent.ConnectionChanged(false))
        runCurrent()
        viewModel.onImagePicked(image)
        viewModel.onSend()
        runCurrent()

        assertEquals(SupportChatMessageStatus.Pending, viewModel.state.value.messages.single().status)
        viewModel.onConnectionRetry()
        runCurrent()

        assertEquals(listOf("queued-image"), repository.sentImages.map { it.id })
        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.single().status)
    }

    @Test
    fun `typing indicator toggles by Writing events`() = runTest {
        val viewModel = createViewModelAndStart()

        repository.emitEvent(SupportChatEvent.TypingChanged("agent@example.com", true))
        runCurrent()

        assertEquals("agent@example.com", viewModel.state.value.typingAuthor)

        repository.emitEvent(SupportChatEvent.TypingChanged("agent@example.com", false))
        runCurrent()

        assertNull(viewModel.state.value.typingAuthor)
    }

    @Test
    fun `failed send marks message as Failed and emits retry effect`() = runTest {
        repository.sendMessageResult = Response.Error(Exception("Network error"))
        val viewModel = createViewModelAndStart()

        val effects = mutableListOf<SupportChatEffect>()
        val collectJob = launch { viewModel.effect.collect { effects.add(it) } }
        runCurrent()

        viewModel.onDraftChange("Fail me")
        viewModel.onSend()
        advanceUntilIdle()

        val failedMessage = viewModel.state.value.messages.first()
        assertEquals(SupportChatMessageStatus.Failed, failedMessage.status)
        val effect = effects.filterIsInstance<SupportChatEffect.MessageSendFailed>().single()
        assertEquals(failedMessage.id, effect.messageId)
        collectJob.cancel()
    }

    @Test
    fun `picked image is added to pending attachments and sent`() = runTest {
        val viewModel = createViewModelAndStart()
        val file = SupportChatPickedFile(
            id = "img-1",
            name = "screenshot.png",
            path = "/tmp/screenshot.png",
            mimeType = "image/png",
        )

        viewModel.onImagePicked(file)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.pendingAttachments.size)
        assertTrue(viewModel.state.value.pendingAttachments.first() is SupportChatPendingAttachment.Image)

        viewModel.onSend()
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
        assertEquals(0, viewModel.state.value.pendingAttachments.size)
        assertEquals(1, repository.sentImages.size)
        assertEquals("img-1", repository.sentImages.first().id)
    }

    @Test
    fun `text and all attachments are sent as separate messages`() = runTest {
        val viewModel = createViewModelAndStart()
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
        viewModel.onDraftChange("See attachments")
        viewModel.onSend()
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
        val viewModel = createViewModelAndStart()

        repository.emitEvent(SupportChatEvent.HistoryPageReceived(listOf(incomingMessage(id = 1, text = "Old"))))
        advanceUntilIdle()

        val effects = mutableListOf<SupportChatEffect>()
        val collectJob = launch { viewModel.effect.collect { effects.add(it) } }
        runCurrent()

        repository.loadNextMessagesResult = Response.Error(Exception("Network error"))
        viewModel.onLoadMore()
        advanceUntilIdle()

        assertTrue(effects.any { it is SupportChatEffect.HistoryLoadFailed })
        assertEquals(false, viewModel.state.value.isLoadingHistory)
        collectJob.cancel()
    }

    @Test
    fun `empty history page sets hasMoreHistory to false`() = runTest {
        val viewModel = createViewModelAndStart()

        repository.emitEvent(SupportChatEvent.HistoryPageReceived(emptyList()))
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.hasMoreHistory)
        assertEquals(false, viewModel.state.value.isLoadingHistory)
    }

    @Test
    fun `history page with messages keeps hasMoreHistory true`() = runTest {
        val viewModel = createViewModelAndStart()
        val page = listOf(incomingMessage(id = 1, text = "Old"))

        repository.emitEvent(SupportChatEvent.HistoryPageReceived(page))
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.hasMoreHistory)
        assertEquals(1, viewModel.state.value.messages.size)
    }

    @Test
    fun `duplicate history messages are ignored`() = runTest {
        val viewModel = createViewModelAndStart()
        val page = listOf(incomingMessage(id = 1, text = "Old"))

        repository.emitEvent(SupportChatEvent.HistoryPageReceived(page))
        advanceUntilIdle()

        repository.emitEvent(SupportChatEvent.HistoryPageReceived(page))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
        assertEquals(false, viewModel.state.value.hasMoreHistory)
    }

    @Test
    fun `loadMore is no-op when hasMoreHistory is false`() = runTest {
        val viewModel = createViewModelAndStart()
        val callsAfterInit = repository.loadNextMessagesCallCount

        repository.emitEvent(SupportChatEvent.HistoryPageReceived(emptyList()))
        advanceUntilIdle()

        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(callsAfterInit, repository.loadNextMessagesCallCount)
    }

    @Test
    fun `image file picked as document stays document`() = runTest {
        val viewModel = createViewModelAndStart()
        val file = SupportChatPickedFile(
            id = "img-as-doc",
            name = "screenshot.png",
            path = "/tmp/screenshot.png",
            mimeType = "image/png",
        )

        viewModel.onDocumentPicked(file)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.pendingAttachments.first() is SupportChatPendingAttachment.Document)

        viewModel.onSend()
        advanceUntilIdle()

        assertEquals(listOf("img-as-doc"), repository.sentDocuments.map { it.id })
        assertTrue(repository.sentImages.isEmpty())
    }

    @Test
    fun `download attachment saves file and emits effect`() = runTest {
        val viewModel = createViewModelAndStart()
        val saver = FakeSupportChatFileSaver()
        val attachment = SupportChatAttachment.Document(
            id = "doc-1",
            name = "scheme.pdf",
            url = "https://files/scheme.pdf",
        )

        val effects = mutableListOf<SupportChatEffect>()
        val collectJob = launch { viewModel.effect.collect { effects.add(it) } }
        runCurrent()

        viewModel.onDownloadAttachment(attachment, saver)
        advanceUntilIdle()

        assertEquals(listOf("scheme.pdf"), saver.savedFiles.map { it.first })
        assertTrue(effects.any { it is SupportChatEffect.AttachmentDownloaded })
        assertNull(viewModel.state.value.downloadingAttachmentId)
        collectJob.cancel()
    }

    @Test
    fun `failed attachment download emits error effect`() = runTest {
        repository.downloadFileResult = Response.Error(Exception("No network"))
        val viewModel = createViewModelAndStart()
        val saver = FakeSupportChatFileSaver()
        val attachment = SupportChatAttachment.Document(
            id = "doc-1",
            name = "scheme.pdf",
            url = "https://files/scheme.pdf",
        )

        val effects = mutableListOf<SupportChatEffect>()
        val collectJob = launch { viewModel.effect.collect { effects.add(it) } }
        runCurrent()

        viewModel.onDownloadAttachment(attachment, saver)
        advanceUntilIdle()

        assertTrue(saver.savedFiles.isEmpty())
        assertTrue(effects.any { it is SupportChatEffect.AttachmentDownloadFailed })
        assertNull(viewModel.state.value.downloadingAttachmentId)
        collectJob.cancel()
    }

    @Test
    fun `failed attachment save emits error effect`() = runTest {
        val viewModel = createViewModelAndStart()
        val saver = FakeSupportChatFileSaver().apply { saveResult = null }
        val attachment = SupportChatAttachment.Image(
            id = "img-1",
            name = "photo.jpg",
            url = "https://files/photo.jpg",
        )

        val effects = mutableListOf<SupportChatEffect>()
        val collectJob = launch { viewModel.effect.collect { effects.add(it) } }
        runCurrent()

        viewModel.onDownloadAttachment(attachment, saver)
        advanceUntilIdle()

        assertTrue(effects.any { it is SupportChatEffect.AttachmentDownloadFailed })
        collectJob.cancel()
    }

    @Test
    fun `screen disposal closes chat`() = runTest {
        val viewModel = createViewModelAndStart()

        viewModel.onScreenDisposed()
        advanceUntilIdle()

        assertEquals(1, repository.closeChatCallCount)
    }

    @Test
    fun `reopen after dispose resets state and restarts session`() = runTest {
        repository.historyPageOnLoad = listOf(incomingMessage(id = 1, text = "Previous session"))
        val viewModel = createViewModelAndStart()
        assertEquals(1, viewModel.state.value.messages.size)

        viewModel.onScreenDisposed()
        advanceUntilIdle()

        repository.historyPageOnLoad = emptyList()
        viewModel.onScreenOpened()

        // Сброс синхронный: данные прошлой сессии не видны ни на кадр новой.
        assertTrue(viewModel.state.value.messages.isEmpty())

        advanceUntilIdle()

        assertEquals(1, repository.closeChatCallCount)
        assertEquals(2, repository.openChatCallCount)
        assertEquals(
            listOf(FakeSupportChatRepository.OPEN_CALL, FakeSupportChatRepository.CLOSE_CALL, FakeSupportChatRepository.OPEN_CALL),
            repository.connectionCalls,
        )
        assertTrue(viewModel.state.value.messages.isEmpty())
    }

    @Test
    fun `immediate reopen cancels pending close of previous session`() = runTest {
        val viewModel = createViewModelAndStart()

        viewModel.onScreenDisposed()
        viewModel.onScreenOpened()
        advanceUntilIdle()

        // Быстрый возврат на экран отменяет ещё не начавшийся closeChat:
        // новая сессия переиспользует соединение через openChat.
        assertEquals(0, repository.closeChatCallCount)
        assertEquals(2, repository.openChatCallCount)
    }

    @Test
    fun `events are not collected after dispose and resume after reopen`() = runTest {
        val viewModel = createViewModelAndStart()

        viewModel.onScreenDisposed()
        advanceUntilIdle()

        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 1, text = "Between sessions")))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.messages.isEmpty())

        viewModel.onScreenOpened()
        advanceUntilIdle()

        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 2, text = "New session")))
        advanceUntilIdle()

        assertEquals(listOf("New session"), viewModel.state.value.messages.map { it.text })
    }

    private fun TestScope.createViewModelAndStart(): SupportChatScreenViewModel {
        val viewModel = SupportChatScreenViewModel(
            repository = repository,
            dispatchers = dispatchers,
        )
        viewModel.onScreenOpened()
        advanceUntilIdle()
        return viewModel
    }

    private fun incomingMessage(id: Long, text: String): SupportChatMessage {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return SupportChatMessage(
            id = id.toString(),
            serverId = id,
            text = text,
            attachment = null,
            time = now,
            displayTime = formatTime(now),
            isMine = false,
            authorEmail = "agent@example.com",
            status = SupportChatMessageStatus.Sent,
        )
    }

    private fun formatTime(time: LocalDateTime): String {
        val hour = time.hour.toString().padStart(2, '0')
        val minute = time.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    private val SupportChatMessage.isRead: Boolean
        get() = status == SupportChatMessageStatus.Read
}
