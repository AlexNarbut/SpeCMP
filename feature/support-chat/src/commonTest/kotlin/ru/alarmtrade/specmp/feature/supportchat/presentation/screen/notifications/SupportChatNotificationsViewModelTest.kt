package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.notifications

import androidx.lifecycle.ViewModelStore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatNotifier
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.FakeSupportChatRepository

class SupportChatNotificationsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeSupportChatRepository()

    private val notifier = object : SupportChatNotifier {
        var denied = false
        var started = 0
        var disposed = 0
        var settingsOpened = 0
        val shown = mutableListOf<String?>()

        override fun start() {
            started++
        }

        override fun show(text: String?) {
            shown += text
        }

        override fun isDenied(): Boolean = denied

        override fun openSettings() {
            settingsOpened++
        }

        override fun dispose() {
            disposed++
        }
    }

    private fun TestScope.viewModel() =
        SupportChatNotificationsViewModel(repository, notifier, testDispatchers(dispatcher))
            .also { vm -> backgroundScope.launch { vm.state.collect {} } }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `starts the notifier and keeps one chat session`() = runTest(dispatcher) {
        val vm = viewModel()

        assertEquals(1, notifier.started)
        assertEquals(1, repository.sessionCount)
        assertFalse(vm.state.value.askToEnable)
    }

    @Test
    fun `shows only messages from support, attachment without text as null`() = runTest(dispatcher) {
        viewModel()

        repository.emitEvent(SupportChatEvent.MessageReceived(message("1", text = "Здравствуйте", mine = false)))
        repository.emitEvent(SupportChatEvent.MessageReceived(message("2", text = "моё", mine = true)))
        repository.emitEvent(SupportChatEvent.MessageReceived(message("3", text = " ", mine = false)))

        assertEquals(listOf("Здравствуйте", null), notifier.shown)
    }

    @Test
    fun `denied notifications ask to open settings`() = runTest(dispatcher) {
        notifier.denied = true
        val vm = viewModel()
        assertTrue(vm.state.value.askToEnable)

        vm.onOpenSettings()

        assertEquals(1, notifier.settingsOpened)
        assertFalse(vm.state.value.askToEnable)
    }

    @Test
    fun `not now hides the prompt without opening settings`() = runTest(dispatcher) {
        notifier.denied = true
        val vm = viewModel()

        vm.onDismiss()

        assertFalse(vm.state.value.askToEnable)
        assertEquals(0, notifier.settingsOpened)
    }

    @Test
    fun `leaving the authorized zone releases the notifier and the session`() = runTest(dispatcher) {
        val store = ViewModelStore()
        store.put("notifications", viewModel())

        store.clear()

        assertEquals(1, notifier.disposed)
        assertEquals(1, repository.releasedSessionCount)
    }

    private fun message(id: String, text: String?, mine: Boolean) = SupportChatMessage(
        id = id,
        serverId = id.toLong(),
        text = text,
        attachment = null,
        time = Instant.fromEpochSeconds(0),
        isMine = mine,
        authorEmail = null,
        status = SupportChatMessageStatus.Sent,
    )
}
