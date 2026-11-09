package ru.alarmtrade.specmp.feature.supportchat.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.chat.ChatConnectionState
import ru.alarmtrade.specialist.domain.chat.ChatCursor
import ru.alarmtrade.specialist.domain.chat.ChatEvent
import ru.alarmtrade.specialist.domain.chat.ChatMessageContent
import ru.alarmtrade.specialist.domain.chat.ChatMessageSender
import ru.alarmtrade.specialist.domain.chat.ChatMessagesPage
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.account.domain.model.AccountProfile
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatConnectionState
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryCursor
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatHistoryPage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatUnavailableException

class SpecialistSupportChatRepositoryTest {
    private val dataSource = FakeSupportChatDataSource()
    private val account = FakeAccountRepository(Response.Success(AccountProfile(name = "Я", email = MY_EMAIL)))
    private val repository = SpecialistSupportChatRepository(dataSource, account)

    @Test
    fun `session starts connecting and maps connection states`() = runTest {
        dataSource.session = flowOf(
            SpecialistResponse.Success(ChatConnectionState.Idle),
            SpecialistResponse.Success(ChatConnectionState.Ready),
            SpecialistResponse.Error.Network(NetworkIsNotAvailableException()),
            SpecialistResponse.Success(ChatConnectionState.Reconnecting(attempt = 1, cause = null)),
            SpecialistResponse.Success(ChatConnectionState.WaitingForNetwork),
            SpecialistResponse.Success(ChatConnectionState.AuthFailed),
        )

        val states = repository.session().toList().map {
            assertIs<SupportChatEvent.Connection>(assertIs<Response.Success<SupportChatEvent>>(it).value).state
        }

        assertEquals(
            listOf(
                SupportChatConnectionState.Connecting,
                SupportChatConnectionState.Connecting,
                SupportChatConnectionState.Ready,
                SupportChatConnectionState.Reconnecting,
                SupportChatConnectionState.WaitingForNetwork,
                SupportChatConnectionState.AuthFailed,
            ),
            states,
        )
    }

    @Test
    fun `session maps chat events against the profile email`() = runTest {
        dataSource.events = flowOf(
            SpecialistResponse.Success(message(id = 7, senderLogin = "agent@example.com")),
            SpecialistResponse.Error.Network(NetworkIsNotAvailableException()),
            SpecialistResponse.Success(ChatEvent.Chat.MarkMessageAsRead(listOf(7L, 8L))),
            SpecialistResponse.Success(ChatEvent.Chat.Writing.Start(MY_EMAIL)),
            SpecialistResponse.Success(ChatEvent.Chat.Writing.Stop("agent@example.com")),
        )

        val events = repository.session().toList()
            .map { assertIs<Response.Success<SupportChatEvent>>(it).value }
            .filterNot { it is SupportChatEvent.Connection }

        val received = assertIs<SupportChatEvent.MessageReceived>(events[0]).message
        assertEquals(7L, received.serverId)
        assertEquals(false, received.isMine)
        assertEquals(SupportChatEvent.MessagesRead(listOf(7L, 8L)), events[1])
        assertEquals(SupportChatEvent.TypingChanged(MY_EMAIL, isTyping = true, isMine = true), events[2])
        assertEquals(
            SupportChatEvent.TypingChanged("agent@example.com", isTyping = false, isMine = false),
            events[3],
        )
        assertEquals(4, events.size)
    }

    @Test
    fun `session without a profile ends unavailable and does not connect`() = runTest {
        val failure = IllegalStateException("no token")
        account.profile = Response.Error(failure)

        val values = repository.session().toList()

        assertEquals(
            Response.Success(SupportChatEvent.Connection(SupportChatConnectionState.Connecting)),
            values.first(),
        )
        val error = assertIs<SupportChatUnavailableException>(assertIs<Response.Error>(values.last()).exception)
        assertSame(failure, error.cause)
        assertEquals(0, dataSource.sessionSubscriptions)
    }

    @Test
    fun `history page maps messages and passes the cursor`() = runTest {
        dataSource.page = SpecialistResponse.Success(
            ChatMessagesPage(
                messages = listOf(message(id = 1, senderLogin = MY_EMAIL), message(id = 2, senderLogin = "agent@x")),
                hasMore = true,
                total = 40,
                nextCursor = ChatCursor.fromToken(2),
            ),
        )

        val page = assertIs<Response.Success<SupportChatHistoryPage>>(
            repository.loadHistoryPage(SupportChatHistoryCursor(1)),
        ).value

        assertEquals(listOf(1L, 2L), page.messages.map { it.serverId })
        assertEquals(listOf(true, false), page.messages.map { it.isMine })
        assertEquals(SupportChatHistoryCursor(2), page.nextCursor)
        assertEquals(1, dataSource.pageRequests.single().cursor)
        assertEquals(20, dataSource.pageRequests.single().pageSize)
    }

    @Test
    fun `first history page has no cursor and last page has no next cursor`() = runTest {
        val page = assertIs<Response.Success<SupportChatHistoryPage>>(repository.loadHistoryPage(null)).value

        assertNull(dataSource.pageRequests.single().cursor)
        assertNull(page.nextCursor)
    }

    @Test
    fun `history errors come from the profile or the SDK`() = runTest {
        dataSource.page = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())
        val sdkError = assertIs<Response.Error>(repository.loadHistoryPage(null))
        assertEquals(ApiError.NoConnection, sdkError.exception.appError)

        val failure = IllegalStateException("no token")
        account.profile = Response.Error(failure)
        assertSame(failure, assertIs<Response.Error>(repository.loadHistoryPage(null)).exception)
        assertEquals(1, dataSource.pageRequests.size)
    }

    @Test
    fun `messages and attachments go to their SDK calls`() = runTest {
        val file = SupportChatPickedFile(id = "f", name = "a.png", path = "/tmp/a.png", mimeType = "image/png")

        assertEquals(Response.Success(Unit), repository.sendMessage("Привет"))
        assertEquals(Response.Success(Unit), repository.sendImage(file))
        assertEquals(Response.Success(Unit), repository.sendDocument(file))
        assertEquals(Response.Success(Unit), repository.markMessagesAsRead(listOf(3L, 4L)))

        assertEquals(listOf("text:Привет", "image:/tmp/a.png", "document:/tmp/a.png"), dataSource.sent)
        assertEquals(listOf(listOf(3L, 4L)), dataSource.readRequests)
    }

    @Test
    fun `failed send keeps the SDK error`() = runTest {
        dataSource.sendResult = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        assertEquals(ApiError.NoConnection, assertIs<Response.Error>(repository.sendMessage("x")).exception.appError)
    }

    @Test
    fun `download returns the file bytes or the error`() = runTest {
        dataSource.download = SpecialistResponse.Success(byteArrayOf(1, 2, 3))
        val bytes = assertIs<Response.Success<ByteArray>>(repository.downloadFile("https://files/a.png")).value
        assertEquals(listOf<Byte>(1, 2, 3), bytes.toList())

        dataSource.download = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())
        assertIs<Response.Error>(repository.downloadFile("https://files/a.png"))
    }

    private fun message(id: Long, senderLogin: String) = ChatEvent.Chat.Message.Default(
        id = id,
        time = LocalDateTime(2026, 1, 15, 10, 0),
        wasRead = false,
        sender = ChatMessageSender(userId = 1, login = senderLogin),
        messageContent = ChatMessageContent.Text("text $id"),
    )

    private companion object {
        const val MY_EMAIL = "me@example.com"
    }
}
