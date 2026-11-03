package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import androidx.lifecycle.viewModelScope
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatConnectionState
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryCursor
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatConnection
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatConnectionBanner
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatEffect
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpenResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaveResult

@OptIn(ExperimentalCoroutinesApi::class)
class SupportChatScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSupportChatRepository
    private lateinit var saver: FakeSupportChatFileSaver
    private lateinit var outbox: FakeSupportChatOutbox
    private lateinit var attachmentFiles: FakeSupportChatAttachmentFiles
    private lateinit var fileOpener: FakeSupportChatFileOpener
    private lateinit var dispatchers: AppDispatchers

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSupportChatRepository()
        saver = FakeSupportChatFileSaver()
        outbox = FakeSupportChatOutbox()
        attachmentFiles = FakeSupportChatAttachmentFiles()
        fileOpener = FakeSupportChatFileOpener()
        dispatchers = AppDispatchers(
            main = testDispatcher,
            io = testDispatcher,
            default = testDispatcher
        )
    }

    // region События и история

    @Test
    fun `events are collected and messages are formed`() = runChatTest {
        val viewModel = createViewModel()

        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 1, text = "Hello")))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
        assertEquals("Hello", viewModel.state.value.messages.first().text)
        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.first().status)
    }

    @Test
    fun `initial history emitted during load is collected`() = runChatTest {
        repository.historyPageOnLoad = listOf(incomingMessage(id = 11, text = "From history"))

        val viewModel = createViewModel()

        assertEquals(listOf("From history"), viewModel.state.value.messages.map { it.text })
        assertTrue(viewModel.state.value.history is LCEState.Content)
        assertTrue(viewModel.state.value.isInitialHistoryLoaded)
    }

    @Test
    fun `descending server page is normalized to ascending order in state`() = runChatTest {
        // SDK отдаёт страницу истории newest-first; лента — по возрастанию времени.
        repository.historyPageOnLoad = listOf(
            incomingMessageAt(id = 3, minute = 12, text = "Third"),
            incomingMessageAt(id = 2, minute = 9, text = "Second"),
            incomingMessageAt(id = 1, minute = 5, text = "First")
        )
        val viewModel = createViewModel()

        assertEquals(listOf("First", "Second", "Third"), viewModel.state.value.messages.map { it.text })
    }

    @Test
    fun `live message is placed after older history page`() = runChatTest {
        repository.historyPageOnLoad = listOf(
            incomingMessageAt(id = 2, minute = 9, text = "Second"),
            incomingMessageAt(id = 1, minute = 5, text = "First")
        )
        val viewModel = createViewModel()

        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessageAt(id = 3, minute = 12, text = "Live")))
        advanceUntilIdle()

        assertEquals(listOf("First", "Second", "Live"), viewModel.state.value.messages.map { it.text })
    }

    @Test
    fun `repeated live delivery of a known incoming message does not duplicate it`() = runChatTest {
        // Дубликат по serverId дал бы дубликат ключа LazyColumn → краш (U-1).
        repository.historyPageOnLoad = listOf(incomingMessage(id = 5, text = "Known"))
        val viewModel = createViewModel()

        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 5, text = "Known")))
        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 5, text = "Known")))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
    }

    @Test
    fun `message status updates to Read when MessagesRead event arrives`() = runChatTest {
        val viewModel = createViewModel()
        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 42, text = "Read me")))
        advanceUntilIdle()

        repository.emitEvent(SupportChatEvent.MessagesRead(listOf(42)))
        advanceUntilIdle()

        assertEquals(SupportChatMessageStatus.Read, viewModel.state.value.messages.first().status)
    }

    @Test
    fun `visible incoming message is marked as read`() = runChatTest {
        val viewModel = createViewModel()
        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 7, text = "Visible")))
        advanceUntilIdle()

        viewModel.onMessagesVisible(listOf("7"))
        advanceUntilIdle()

        assertEquals(SupportChatMessageStatus.Read, viewModel.state.value.messages.first().status)
        assertEquals(listOf(listOf(7L)), repository.markedAsRead)
    }

    @Test
    fun `visible incoming messages are marked as read in one batch call`() = runChatTest {
        val viewModel = createViewModel()
        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 7, text = "First")))
        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 8, text = "Second")))
        advanceUntilIdle()

        viewModel.onMessagesVisible(listOf("7", "8"))
        advanceUntilIdle()

        assertEquals(listOf(listOf(7L, 8L)), repository.markedAsRead)
    }

    @Test
    fun `typing indicator toggles by Writing events`() = runChatTest {
        val viewModel = createViewModel()

        repository.emitEvent(SupportChatEvent.TypingChanged("agent@example.com", true, isMine = false))
        runCurrent()
        assertEquals("agent@example.com", viewModel.state.value.typingAuthor)

        repository.emitEvent(SupportChatEvent.TypingChanged("agent@example.com", false, isMine = false))
        runCurrent()
        assertNull(viewModel.state.value.typingAuthor)
    }

    @Test
    fun `own typing event is ignored`() = runChatTest {
        val viewModel = createViewModel()

        repository.emitEvent(SupportChatEvent.TypingChanged("me@example.com", true, isMine = true))
        runCurrent()

        assertNull(viewModel.state.value.typingAuthor)
    }

    // endregion

    // region Первичная загрузка и пагинация

    @Test
    fun `first load error shows fullscreen error state and retry recovers`() = runChatTest {
        repository.historyPageOnLoad = null
        repository.loadHistoryError = Exception("Network error")
        val viewModel = createViewModel()

        assertTrue(viewModel.state.value.history is LCEState.Error)

        repository.historyPageOnLoad = listOf(incomingMessage(id = 5, text = "Recovered"))
        viewModel.onHistoryRetry()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.history is LCEState.Content)
        assertEquals(listOf("Recovered"), viewModel.state.value.messages.map { it.text })
    }

    @Test
    fun `live message replaces fullscreen error with content`() = runChatTest {
        repository.historyPageOnLoad = null
        repository.loadHistoryError = Exception("timeout")
        val viewModel = createViewModel()
        assertTrue(viewModel.state.value.history is LCEState.Error)

        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 9, text = "Live")))
        advanceUntilIdle()

        assertEquals(listOf("Live"), viewModel.state.value.messages.map { it.text })
    }

    @Test
    fun `initial load error keeps restored outbox on screen and in storage`() = runChatTest {
        // P-1: ошибка первой загрузки не должна затирать ленту и outbox.
        val failed = ownOptimistic(text = "Unsent", status = SupportChatMessageStatus.Failed)
        outbox.enqueue(failed)
        repository.historyPageOnLoad = null
        repository.loadHistoryError = Exception("timeout")

        val viewModel = createViewModel(advance = false)
        val effects = collectEffects(viewModel)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.history is LCEState.Content)
        assertEquals(listOf("Unsent"), viewModel.state.value.messages.map { it.text })
        assertTrue(effects.any { it is SupportChatEffect.HistoryLoadFailed })

        // Последующие события не перезаписывают очередь снимком.
        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 3, text = "Live")))
        advanceUntilIdle()

        assertEquals(setOf("Unsent", "Live"), viewModel.state.value.messages.map { it.text }.toSet())
        assertEquals(listOf(failed.id), outbox.entries.keys.toList())
    }

    @Test
    fun `load more during initial load is skipped and not reported as error`() = runChatTest {
        // P-2: восстановленная лента короткая — скролл просит страницу, пока
        // первичная загрузка ещё идёт.
        outbox.enqueue(ownOptimistic(text = "Unsent", status = SupportChatMessageStatus.Failed))
        repository.historyPageOnLoad = listOf(incomingMessage(id = 1, text = "Old"))
        repository.loadDelayMillis = 1_000

        val viewModel = createViewModel(advance = false)
        val effects = collectEffects(viewModel)
        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(1, repository.loadHistoryCallCount)
        assertTrue(effects.none { it is SupportChatEffect.HistoryLoadFailed })
        assertTrue(viewModel.state.value.isInitialHistoryLoaded)
        assertEquals(setOf("Unsent", "Old"), viewModel.state.value.messages.map { it.text }.toSet())
    }

    @Test
    fun `loadMore failure emits history load failed effect and pauses paging`() = runChatTest {
        repository.historyPageOnLoad = listOf(incomingMessage(id = 1, text = "Old"))
        val viewModel = createViewModel()
        repository.historyPageOnLoad = null
        val effects = collectEffects(viewModel)

        repository.loadHistoryError = Exception("Network error")
        viewModel.onLoadMore()
        advanceUntilIdle()

        assertTrue(effects.any { it is SupportChatEffect.HistoryLoadFailed })
        assertFalse(viewModel.state.value.isLoadingMore)
        assertTrue(viewModel.state.value.isHistoryPagingFailed)
        assertEquals(listOf("Old"), viewModel.state.value.messages.map { it.text })

        repository.historyPageOnLoad = listOf(incomingMessage(id = 2, text = "Older"))
        viewModel.onLoadMore()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isHistoryPagingFailed)
    }

    @Test
    fun `empty history page sets hasMoreHistory to false`() = runChatTest {
        val viewModel = createViewModel()

        assertEquals(false, viewModel.state.value.hasMoreHistory)
        assertTrue(viewModel.state.value.history is LCEState.Content)
    }

    @Test
    fun `history page with messages keeps hasMoreHistory true`() = runChatTest {
        repository.historyPageOnLoad = listOf(incomingMessage(id = 1, text = "Old"))
        val viewModel = createViewModel()

        assertEquals(true, viewModel.state.value.hasMoreHistory)
        assertEquals(1, viewModel.state.value.messages.size)
    }

    @Test
    fun `duplicate history messages are ignored`() = runChatTest {
        val page = listOf(incomingMessage(id = 1, text = "Old"))
        repository.historyPageOnLoad = page
        val viewModel = createViewModel()

        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
    }

    @Test
    fun `loadMore passes the cursor of the previous page`() = runChatTest {
        repository.historyPageOnLoad = listOf(incomingMessageAt(id = 2, minute = 10, text = "Newest"))
        val viewModel = createViewModel()
        val firstCursor = viewModel.state.value.historyCursor

        repository.historyPageOnLoad = listOf(incomingMessageAt(id = 1, minute = 5, text = "Older"))
        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(listOf(null, firstCursor), repository.historyCursors)
        assertEquals(listOf("Older", "Newest"), viewModel.state.value.messages.map { it.text })
    }

    @Test
    fun `loadMore is no-op when hasMoreHistory is false`() = runChatTest {
        val viewModel = createViewModel()
        val callsAfterInit = repository.loadHistoryCallCount

        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(callsAfterInit, repository.loadHistoryCallCount)
    }

    // endregion

    // region Отправка

    @Test
    fun `send adds optimistic message and marks it Sent`() = runChatTest {
        val viewModel = createViewModel()

        viewModel.onSend("Test message")
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.first().status)
        assertEquals(listOf("Test message"), repository.sentMessages)
    }

    @Test
    fun `send is ignored while offline`() = runChatTest {
        val viewModel = createViewModel()
        repository.disconnect()
        runCurrent()

        viewModel.onSend("Offline")
        runCurrent()

        assertTrue(viewModel.state.value.messages.isEmpty())
        assertTrue(repository.sentMessages.isEmpty())
    }

    @Test
    fun `send is ignored until chat room is subscribed`() = runChatTest {
        // До Ready (подписка комнаты) ввод закрыт.
        repository.autoConnect = false
        val viewModel = createViewModel(advance = false)
        runCurrent()
        assertEquals(SupportChatConnection.Connecting, viewModel.state.value.connection)

        viewModel.onSend("Too early")
        runCurrent()
        assertTrue(repository.sentMessages.isEmpty())

        repository.connect()
        runCurrent()
        assertEquals(SupportChatConnection.Online, viewModel.state.value.connection)
    }

    @Test
    fun `history loads only after chat room is subscribed`() = runChatTest {
        repository.autoConnect = false
        repository.historyPageOnLoad = listOf(incomingMessage(id = 15, text = "Loaded after subscribe"))
        val viewModel = createViewModel(advance = false)
        runCurrent()
        assertEquals(0, repository.loadHistoryCallCount)

        repository.connect()
        advanceUntilIdle()

        assertEquals(listOf("Loaded after subscribe"), viewModel.state.value.messages.map { it.text })
    }

    @Test
    fun `interrupted send parks message as Pending and resends after reconnect`() = runChatTest {
        val viewModel = createViewModel()
        repository.disconnectDuringNextSend = true

        viewModel.onSend("Queued")
        runCurrent()

        val parked = viewModel.state.value.messages.single()
        assertEquals(SupportChatMessageStatus.Pending, parked.status)
        assertEquals(SupportChatConnection.Offline, viewModel.state.value.connection)
        assertTrue(repository.sentMessages.isEmpty())
        assertEquals(SupportChatMessageStatus.Pending, outbox.entries.getValue(parked.id).status)

        // SDK переподключился → Ready → свежая страница → очередь.
        repository.connect()
        advanceUntilIdle()

        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.single().status)
        assertEquals(listOf("Queued"), repository.sentMessages)
        // Success SDK — не доставка: запись ждёт серверного эха.
        assertEquals(SupportChatMessageStatus.Sent, outbox.entries.getValue(parked.id).status)
    }

    @Test
    fun `reconnect reloads the newest history page and keeps the session`() = runChatTest {
        repository.historyPageOnLoad = listOf(incomingMessage(id = 1, text = "Old"))
        val viewModel = createViewModel()
        assertEquals(listOf<SupportChatHistoryCursor?>(null), repository.historyCursors)

        repository.disconnect()
        runCurrent()
        repository.historyPageOnLoad = listOf(incomingMessage(id = 2, text = "While offline"))
        repository.connect()
        advanceUntilIdle()

        assertEquals(listOf<SupportChatHistoryCursor?>(null, null), repository.historyCursors)
        assertEquals(setOf("Old", "While offline"), viewModel.state.value.messages.map { it.text }.toSet())
        assertEquals(1, repository.sessionCount)
        assertEquals(0, repository.releasedSessionCount)
    }

    @Test
    fun `lost connection shows offline banner and keeps the session`() = runChatTest {
        val viewModel = createViewModel()

        repository.disconnect()
        runCurrent()

        assertEquals(SupportChatConnection.Offline, viewModel.state.value.connection)
        assertEquals(SupportChatConnectionBanner.Reconnecting, viewModel.state.value.connectionBanner)
        assertFalse(viewModel.state.value.isInputEnabled)
        // Переподключается SDK: заявка на соединение не отпускается.
        assertEquals(0, repository.releasedSessionCount)
    }

    @Test
    fun `waiting for network and reconnecting show the same banner`() = runChatTest {
        val viewModel = createViewModel(advance = false)
        assertEquals(SupportChatConnectionBanner.Connecting, viewModel.state.value.connectionBanner)
        advanceUntilIdle()
        assertNull(viewModel.state.value.connectionBanner)

        val banners = mutableListOf<SupportChatConnectionBanner?>()
        listOf(
            SupportChatConnectionState.Reconnecting,
            SupportChatConnectionState.WaitingForNetwork,
            SupportChatConnectionState.Reconnecting
        ).forEach { state ->
            repository.disconnect(state)
            runCurrent()
            banners += viewModel.state.value.connectionBanner
        }
        assertEquals(List<SupportChatConnectionBanner?>(3) { SupportChatConnectionBanner.Reconnecting }, banners)

        repository.connect()
        runCurrent()
        assertNull(viewModel.state.value.connectionBanner)
    }

    @Test
    fun `reconnect does not reset already loaded history paging`() = runChatTest {
        repository.historyPageOnLoad = listOf(incomingMessage(id = 2, text = "Newest"))
        val viewModel = createViewModel()
        repository.historyPageOnLoad = listOf(incomingMessage(id = 1, text = "Older"))
        viewModel.onLoadMore()
        advanceUntilIdle()
        val cursorBeforeReconnect = viewModel.state.value.historyCursor

        repository.disconnect()
        runCurrent()
        repository.connect()
        advanceUntilIdle()

        assertEquals(cursorBeforeReconnect, viewModel.state.value.historyCursor)
        assertTrue(viewModel.state.value.hasMoreHistory)
    }

    @Test
    fun `auth failure shows auth banner until the connection is ready again`() = runChatTest {
        val viewModel = createViewModel()

        repository.disconnect(SupportChatConnectionState.AuthFailed)
        runCurrent()

        assertEquals(SupportChatConnectionBanner.AuthFailed, viewModel.state.value.connectionBanner)
        assertFalse(viewModel.state.value.isInputEnabled)

        // После повторного входа SDK подключается сам.
        repository.connect()
        runCurrent()
        assertNull(viewModel.state.value.connectionBanner)
    }

    @Test
    fun `unavailable session shows fullscreen error and retry starts a new session`() = runChatTest {
        repository.sessionUnavailable = true
        val viewModel = createViewModel()

        assertTrue(viewModel.state.value.history is LCEState.Error)
        assertEquals(1, repository.sessionCount)

        repository.sessionUnavailable = false
        repository.historyPageOnLoad = listOf(incomingMessage(id = 1, text = "Recovered"))
        viewModel.onHistoryRetry()
        advanceUntilIdle()

        assertEquals(2, repository.sessionCount)
        assertEquals(SupportChatConnection.Online, viewModel.state.value.connection)
        assertEquals(listOf("Recovered"), viewModel.state.value.messages.map { it.text })
    }

    @Test
    fun `pending messages retry sequentially in queue order`() = runChatTest {
        val viewModel = createViewModel()
        val queuedTexts = listOf("First", "Second", "Third")
        repository.disconnectDuringNextSend = true
        queuedTexts.forEach { viewModel.onSend(it) }
        runCurrent()
        assertTrue(viewModel.state.value.messages.all { it.status == SupportChatMessageStatus.Pending })

        repository.outgoingDelayMillis = 1_000
        repository.connect()
        runCurrent()
        assertEquals(1, repository.maxConcurrentOutgoingCalls)

        advanceUntilIdle()
        assertEquals(queuedTexts, repository.sentMessages)
        assertEquals(1, repository.maxConcurrentOutgoingCalls)
        assertTrue(viewModel.state.value.messages.all { it.status == SupportChatMessageStatus.Sent })
    }

    @Test
    fun `connection retry sends queued attachment`() = runChatTest {
        val viewModel = createViewModel()
        repository.disconnectDuringNextSend = true
        viewModel.onImagePicked(pickedImage(id = "queued-image", name = "mount.png"))
        viewModel.onSend("")
        runCurrent()
        assertEquals(SupportChatMessageStatus.Pending, viewModel.state.value.messages.single().status)

        repository.connect()
        advanceUntilIdle()

        assertEquals(listOf("queued-image"), repository.sentImages.map { it.id })
        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.single().status)
    }

    @Test
    fun `failed send marks message as Failed and emits retry effect`() = runChatTest {
        repository.sendMessageResult = Response.Error(Exception("Network error"))
        val viewModel = createViewModel()
        val effects = collectEffects(viewModel)

        viewModel.onSend("Fail me")
        advanceUntilIdle()

        val failedMessage = viewModel.state.value.messages.single()
        assertEquals(SupportChatMessageStatus.Failed, failedMessage.status)
        val effect = effects.filterIsInstance<SupportChatEffect.MessageSendFailed>().single()
        assertEquals(failedMessage.id, effect.messageId)
        assertTrue(effect.canRetry)
        assertEquals(SupportChatMessageStatus.Failed, outbox.entries.getValue(failedMessage.id).status)
    }

    @Test
    fun `double retry sends the message only once`() = runChatTest {
        // P-4: второй тап (или action снекбара) не должен отправить дубль.
        repository.sendMessageResult = Response.Error(Exception("Network error"))
        val viewModel = createViewModel()
        viewModel.onSend("Retry me")
        advanceUntilIdle()
        val id = viewModel.state.value.messages.single().id

        repository.sendMessageResult = Response.Success(Unit)
        viewModel.onRetry(id)
        viewModel.onRetry(id)
        advanceUntilIdle()

        assertEquals(listOf("Retry me", "Retry me"), repository.sentMessages)
        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.single().status)
        assertEquals(SupportChatMessageStatus.Sent, outbox.entries.getValue(id).status)
    }

    @Test
    fun `failed message can be deleted with its local copy`() = runChatTest {
        repository.sendImageResult = Response.Error(Exception("Upload failed"))
        val viewModel = createViewModel()
        viewModel.onImagePicked(pickedImage(id = "img", name = "a.png"))
        viewModel.onSend("")
        advanceUntilIdle()
        val failed = viewModel.state.value.messages.single()
        assertEquals(SupportChatMessageStatus.Failed, failed.status)

        viewModel.onDeleteFailed(failed.id)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.messages.isEmpty())
        assertTrue(outbox.entries.isEmpty())
        assertEquals(listOf("/tmp/a.png"), attachmentFiles.discarded)
    }

    @Test
    fun `server echo with renamed attachment merges into optimistic image and discards local copy`() = runChatTest {
        val viewModel = createViewModel()
        viewModel.onImagePicked(pickedImage(id = "img-1", name = "local-name.png"))
        viewModel.onSend("")
        runCurrent()

        repository.emitEvent(
            SupportChatEvent.MessageReceived(ownImageEcho(id = 100, attachmentName = "server-renamed.png"))
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
        assertEquals(100L, viewModel.state.value.messages.single().serverId)
        assertEquals(listOf("/tmp/local-name.png"), attachmentFiles.discarded)
    }

    @Test
    fun `server echoes for two pending images merge in FIFO order`() = runChatTest {
        val viewModel = createViewModel()
        viewModel.onImagePicked(pickedImage(id = "img-a", name = "a.png"))
        viewModel.onSend("")
        runCurrent()
        viewModel.onImagePicked(pickedImage(id = "img-b", name = "b.png"))
        viewModel.onSend("")
        runCurrent()

        repository.emitEvent(SupportChatEvent.MessageReceived(ownImageEcho(id = 101, attachmentName = "renamed-a.png")))
        runCurrent()
        repository.emitEvent(SupportChatEvent.MessageReceived(ownImageEcho(id = 102, attachmentName = "renamed-b.png")))
        runCurrent()

        assertEquals(listOf(101L, 102L), viewModel.state.value.messages.map { it.serverId })
    }

    @Test
    fun `server echoes for two identical texts merge in FIFO order`() = runChatTest {
        val viewModel = createViewModel()
        viewModel.onSend("Same")
        runCurrent()
        viewModel.onSend("Same")
        runCurrent()

        repository.emitEvent(SupportChatEvent.MessageReceived(ownTextEcho(id = 201, text = "Same")))
        runCurrent()
        repository.emitEvent(SupportChatEvent.MessageReceived(ownTextEcho(id = 202, text = "Same")))
        runCurrent()

        assertEquals(listOf(201L, 202L), viewModel.state.value.messages.map { it.serverId })
    }

    @Test
    fun `picked image is added to pending attachments and sent`() = runChatTest {
        val viewModel = createViewModel()

        viewModel.onImagePicked(pickedImage(id = "img-1", name = "screenshot.png"))
        advanceUntilIdle()
        assertEquals(SupportChatUiAttachment.Kind.Image, viewModel.state.value.pendingAttachments.single().kind)

        viewModel.onSend("")
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.messages.size)
        assertTrue(viewModel.state.value.pendingAttachments.isEmpty())
        assertEquals(listOf("img-1"), repository.sentImages.map { it.id })
    }

    @Test
    fun `removed composer attachment discards its local copy`() = runChatTest {
        val viewModel = createViewModel()
        viewModel.onImagePicked(pickedImage(id = "img-1", name = "drop.png"))

        viewModel.onRemoveAttachment("img-1")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.pendingAttachments.isEmpty())
        assertEquals(listOf("/tmp/drop.png"), attachmentFiles.discarded)
    }

    @Test
    fun `text and all attachments are sent as separate messages`() = runChatTest {
        val viewModel = createViewModel()
        viewModel.onImagePicked(pickedImage(id = "img-1", name = "install.png"))
        viewModel.onDocumentPicked(
            SupportChatPickedFile(
                id = "doc-1",
                name = "scheme.pdf",
                path = "/tmp/scheme.pdf",
                mimeType = "application/pdf",
                sizeBytes = 2_048
            )
        )
        viewModel.onSend("See attachments")
        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.messages.size)
        assertEquals(listOf("img-1"), repository.sentImages.map { it.id })
        assertEquals(listOf("doc-1"), repository.sentDocuments.map { it.id })
        assertEquals(listOf("See attachments"), repository.sentMessages)
    }

    @Test
    fun `image file picked as document stays document`() = runChatTest {
        val viewModel = createViewModel()

        viewModel.onDocumentPicked(pickedImage(id = "img-as-doc", name = "screenshot.png"))
        advanceUntilIdle()
        assertEquals(SupportChatUiAttachment.Kind.Document, viewModel.state.value.pendingAttachments.first().kind)

        viewModel.onSend("")
        advanceUntilIdle()

        assertEquals(listOf("img-as-doc"), repository.sentDocuments.map { it.id })
        assertTrue(repository.sentImages.isEmpty())
    }

    @Test
    fun `rejected attachment emits effect`() = runChatTest {
        val viewModel = createViewModel()
        val effects = collectEffects(viewModel)

        viewModel.onAttachmentRejected(tooLarge = true)
        advanceUntilIdle()

        assertEquals(listOf<SupportChatEffect>(SupportChatEffect.AttachmentRejected(tooLarge = true)), effects)
    }

    @Test
    fun `sent message without echo is requeued on disconnect and resent after reconnect`() = runChatTest {
        // E-4: SDK отвечает Success и в мёртвый сокет — без эха сообщение не доставлено.
        val viewModel = createViewModel()
        viewModel.onSend("No echo")
        runCurrent()
        val message = viewModel.state.value.messages.single()
        assertEquals(SupportChatMessageStatus.Sent, message.status)
        assertEquals(SupportChatMessageStatus.Sent, outbox.entries.getValue(message.id).status)

        repository.disconnect()
        runCurrent()

        assertEquals(SupportChatMessageStatus.Pending, viewModel.state.value.messages.single().status)
        assertEquals(SupportChatMessageStatus.Pending, outbox.entries.getValue(message.id).status)

        repository.connect()
        advanceUntilIdle()

        assertEquals(listOf("No echo", "No echo"), repository.sentMessages)
    }

    @Test
    fun `server echo confirms delivery and clears the outbox entry`() = runChatTest {
        val viewModel = createViewModel()
        viewModel.onSend("Echoed")
        runCurrent()

        repository.emitEvent(SupportChatEvent.MessageReceived(ownTextEcho(id = 300, text = "Echoed")))
        advanceUntilIdle()

        assertTrue(outbox.entries.isEmpty())
        repository.disconnect()
        runCurrent()
        assertEquals(SupportChatMessageStatus.Sent, viewModel.state.value.messages.single().status)
    }

    @Test
    fun `restored message already present in history is not resent`() = runChatTest {
        // После рестарта «Sent без эха» восстанавливается как Pending; если оно
        // на самом деле дошло, страница истории его подтверждает — повтора нет.
        val restored = ownOptimistic(text = "Delivered", status = SupportChatMessageStatus.Pending)
        outbox.enqueue(restored)
        repository.historyPageOnLoad = listOf(
            ownTextEcho(id = 400, text = "Delivered").copy(time = restored.time)
        )

        val viewModel = createViewModel()

        assertTrue(repository.sentMessages.isEmpty())
        assertEquals(listOf(400L), viewModel.state.value.messages.map { it.serverId })
        assertTrue(outbox.entries.isEmpty())
    }

    // endregion

    // region Скачивание

    @Test
    fun `download attachment saves file and emits effect`() = runChatTest {
        val viewModel = createViewModel()
        val effects = collectEffects(viewModel)

        viewModel.onDownloadAttachment(documentUiAttachment())
        advanceUntilIdle()

        assertEquals(listOf("scheme.pdf"), saver.savedFiles.map { it.first })
        assertTrue(effects.any { it is SupportChatEffect.AttachmentDownloaded })
        assertNull(viewModel.state.value.downloadingAttachmentId)
    }

    @Test
    fun `double tap on the same attachment downloads it once`() = runChatTest {
        // P-7: guard захватывается атомарно — второй тап того же вложения не проходит.
        val viewModel = createViewModel()

        viewModel.onDownloadAttachment(documentUiAttachment())
        viewModel.onDownloadAttachment(documentUiAttachment())
        advanceUntilIdle()

        assertEquals(1, saver.savedFiles.size)
    }

    @Test
    fun `failed attachment download emits error effect`() = runChatTest {
        repository.downloadFileResult = Response.Error(Exception("No network"))
        val viewModel = createViewModel()
        val effects = collectEffects(viewModel)

        viewModel.onDownloadAttachment(documentUiAttachment())
        advanceUntilIdle()

        assertTrue(saver.savedFiles.isEmpty())
        assertTrue(effects.any { it is SupportChatEffect.AttachmentDownloadFailed })
        assertNull(viewModel.state.value.downloadingAttachmentId)
    }

    @Test
    fun `failed attachment save emits error effect`() = runChatTest {
        saver.saveResult = SupportChatFileSaveResult.Failed
        val viewModel = createViewModel()
        val effects = collectEffects(viewModel)

        viewModel.onDownloadAttachment(imageUiAttachment())
        advanceUntilIdle()

        assertTrue(effects.any { it is SupportChatEffect.AttachmentDownloadFailed })
    }

    @Test
    fun `cancelled attachment save emits nothing`() = runChatTest {
        saver.saveResult = SupportChatFileSaveResult.Cancelled
        val viewModel = createViewModel()
        val effects = collectEffects(viewModel)

        viewModel.onDownloadAttachment(imageUiAttachment())
        advanceUntilIdle()

        assertTrue(effects.isEmpty())
    }

    @Test
    fun `opening a document downloads it only on demand and opens it`() = runChatTest {
        val viewModel = createViewModel()
        val downloadsBefore = repository.downloadFileCallCount

        // Лента загружена — файлы не скачивались.
        assertEquals(0, downloadsBefore)

        viewModel.onOpenAttachment(documentUiAttachment())
        advanceUntilIdle()

        assertEquals(1, repository.downloadFileCallCount)
        assertEquals(listOf("scheme.pdf"), fileOpener.openedBytes)
        assertNull(viewModel.state.value.downloadingAttachmentId)
    }

    @Test
    fun `opening own unsent attachment uses local copy without network`() = runChatTest {
        val viewModel = createViewModel()

        viewModel.onOpenAttachment(documentUiAttachment().copy(url = null, localPath = "/files/scheme.pdf"))
        advanceUntilIdle()

        assertEquals(0, repository.downloadFileCallCount)
        assertEquals(listOf("/files/scheme.pdf"), fileOpener.openedLocal)
    }

    @Test
    fun `open failure reports no app or failed download`() = runChatTest {
        val viewModel = createViewModel()
        val effects = collectEffects(viewModel)

        fileOpener.result = SupportChatFileOpenResult.NoApp
        viewModel.onOpenAttachment(documentUiAttachment())
        advanceUntilIdle()

        repository.downloadFileResult = Response.Error(Exception("404"))
        viewModel.onOpenAttachment(documentUiAttachment())
        advanceUntilIdle()

        assertEquals(
            listOf<SupportChatEffect>(
                SupportChatEffect.AttachmentOpenFailed(noApp = true),
                SupportChatEffect.AttachmentOpenFailed(noApp = false)
            ),
            effects
        )
        assertEquals(1, fileOpener.openedBytes.size)
    }

    @Test
    fun `denied storage permission is reported separately`() = runChatTest {
        saver.saveResult = SupportChatFileSaveResult.PermissionDenied
        val viewModel = createViewModel()
        val effects = collectEffects(viewModel)

        viewModel.onDownloadAttachment(documentUiAttachment())
        advanceUntilIdle()

        assertEquals(listOf<SupportChatEffect>(SupportChatEffect.AttachmentSavePermissionDenied), effects)
    }

    @Test
    fun `open and download share one busy slot`() = runChatTest {
        val viewModel = createViewModel()

        viewModel.onOpenAttachment(documentUiAttachment())
        viewModel.onDownloadAttachment(documentUiAttachment())
        advanceUntilIdle()

        assertEquals(1, repository.downloadFileCallCount)
        assertTrue(saver.savedFiles.isEmpty())
    }

    // endregion

    // region Жизненный цикл и outbox

    @Test
    fun `parked message survives view model death and is sent right after reopening`() = runChatTest {
        val firstViewModel = createViewModel()
        repository.disconnectDuringNextSend = true
        firstViewModel.onSend("Survive me")
        runCurrent()
        assertEquals(1, outbox.entries.size)

        // Уход с экрана: отмена viewModelScope отпускает сессию.
        firstViewModel.viewModelScope.coroutineContext.cancelChildren()
        advanceUntilIdle()

        val secondViewModel = createViewModel()

        assertEquals(listOf("Survive me"), repository.sentMessages)
        assertEquals(SupportChatMessageStatus.Sent, secondViewModel.state.value.messages.single().status)
        assertEquals(listOf(SupportChatMessageStatus.Sent), outbox.entries.values.map { it.status })
    }

    @Test
    fun `cancelled view model scope releases the session`() = runChatTest {
        val viewModel = createViewModel()

        viewModel.viewModelScope.coroutineContext.cancelChildren()
        advanceUntilIdle()

        assertEquals(1, repository.releasedSessionCount)
        assertEquals(
            listOf(FakeSupportChatRepository.ACQUIRE_CALL, FakeSupportChatRepository.RELEASE_CALL),
            repository.connectionCalls
        )
    }

    @Test
    fun `new view model instance starts a clean session`() = runChatTest {
        repository.historyPageOnLoad = listOf(incomingMessage(id = 1, text = "Previous session"))
        val firstViewModel = createViewModel()
        assertEquals(1, firstViewModel.state.value.messages.size)

        firstViewModel.viewModelScope.coroutineContext.cancelChildren()
        advanceUntilIdle()

        repository.historyPageOnLoad = emptyList()
        val secondViewModel = createViewModel()

        assertTrue(secondViewModel.state.value.messages.isEmpty())
        assertEquals(
            listOf(
                FakeSupportChatRepository.ACQUIRE_CALL,
                FakeSupportChatRepository.RELEASE_CALL,
                FakeSupportChatRepository.ACQUIRE_CALL
            ),
            repository.connectionCalls
        )
    }

    @Test
    fun `cleared view model stops collecting events`() = runChatTest {
        val viewModel = createViewModel()
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        advanceUntilIdle()

        repository.emitEvent(SupportChatEvent.MessageReceived(incomingMessage(id = 1, text = "After clear")))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.messages.isEmpty())
    }

    // endregion

    // region Хелперы

    private val createdViewModels = mutableListOf<SupportChatScreenViewModel>()

    /**
     * `runTest`, который в конце отменяет скоупы созданных VM: сессия не завершается сама,
     * и без отмены `runTest` ждал бы её до таймаута вместо честного результата.
     */
    private fun runChatTest(block: suspend TestScope.() -> Unit): TestResult = runTest {
        try {
            block()
        } finally {
            createdViewModels.forEach { it.viewModelScope.coroutineContext.cancelChildren() }
        }
    }

    private fun TestScope.createViewModel(advance: Boolean = true): SupportChatScreenViewModel {
        val viewModel = SupportChatScreenViewModel(
            repository = repository,
            dispatchers = dispatchers,
            fileSaver = saver,
            attachmentFiles = attachmentFiles,
            fileOpener = fileOpener,
            outbox = outbox
        )
        createdViewModels += viewModel
        // Экран подписан на state — с этого стартует работа VM (launchOnFirstStart).
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        if (advance) advanceUntilIdle()
        return viewModel
    }

    private fun TestScope.collectEffects(viewModel: SupportChatScreenViewModel): List<SupportChatEffect> {
        val effects = mutableListOf<SupportChatEffect>()
        // Unconfined: advanceUntilIdle не выполняет задачи backgroundScope, поэтому
        // коллектор должен получать эффект сразу при tryEmit, без диспатча.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }
        return effects
    }

    private fun incomingMessage(id: Long, text: String): SupportChatMessage = SupportChatMessage(
        id = id.toString(),
        serverId = id,
        text = text,
        attachment = null,
        time = Clock.System.now(),
        isMine = false,
        authorEmail = "agent@example.com",
        status = SupportChatMessageStatus.Sent
    )

    private fun incomingMessageAt(id: Long, minute: Int, text: String): SupportChatMessage =
        incomingMessage(id = id, text = text).copy(
            time = LocalDateTime(2026, 1, 15, 10, minute).toInstant(TimeZone.UTC)
        )

    private fun ownOptimistic(text: String, status: SupportChatMessageStatus): SupportChatMessage = SupportChatMessage(
        id = "local-$text",
        serverId = null,
        text = text,
        attachment = null,
        time = Clock.System.now(),
        isMine = true,
        authorEmail = null,
        status = status
    )

    private fun ownTextEcho(id: Long, text: String): SupportChatMessage =
        incomingMessage(id = id, text = text).copy(isMine = true, authorEmail = "me@example.com")

    private fun ownImageEcho(id: Long, attachmentName: String): SupportChatMessage =
        incomingMessage(id = id, text = "").copy(
            text = null,
            attachment = SupportChatAttachment.Image(
                id = "srv-$id",
                name = attachmentName,
                url = "https://example.com/files/$id"
            ),
            isMine = true,
            authorEmail = "me@example.com"
        )

    private fun pickedImage(id: String, name: String): SupportChatPickedFile =
        SupportChatPickedFile(id = id, name = name, path = "/tmp/$name", mimeType = "image/png")

    private fun documentUiAttachment(): SupportChatUiAttachment = SupportChatUiAttachment(
        id = "doc-1",
        name = "scheme.pdf",
        kind = SupportChatUiAttachment.Kind.Document,
        url = "https://files/scheme.pdf",
        localPath = null,
        mimeType = "application/pdf",
        sizeBytes = 1024
    )

    private fun imageUiAttachment(): SupportChatUiAttachment = SupportChatUiAttachment(
        id = "img-1",
        name = "photo.jpg",
        kind = SupportChatUiAttachment.Kind.Image,
        url = "https://files/photo.jpg",
        localPath = null,
        mimeType = null,
        sizeBytes = null
    )

    // endregion
}
