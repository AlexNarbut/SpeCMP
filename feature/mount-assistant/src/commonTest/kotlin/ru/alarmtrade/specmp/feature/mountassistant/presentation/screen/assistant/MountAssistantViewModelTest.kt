package ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.common.AppException
import ru.alarmtrade.specmp.core.common.PlatformError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.FixedClock
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testAlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.service.SettingsWrite
import ru.alarmtrade.specmp.feature.mountassistant.data.service.AssistantJournalImpl
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantProductNotFoundException
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantSettingValue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTransport
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantWriteException
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AutostartStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BYPASS_VARIANT_SETTING_ID
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BlockReason
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CanFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CloneState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleRole
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultContent
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultFailure
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultRequest
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SettingsSourceKind
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SnapshotChange
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StepTitle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TodayPassport
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.WriteFailure
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.WriteStatus
import ru.alarmtrade.specmp.feature.mountassistant.presentation.navigation.AssistantNavigationTarget
import ru.alarmtrade.specmp.feature.mountassistant.testing.FakeAssistantAutostartRepository
import ru.alarmtrade.specmp.feature.mountassistant.testing.FakeAssistantDeviceRepository
import ru.alarmtrade.specmp.feature.mountassistant.testing.FakeAssistantFirmwareRepository
import ru.alarmtrade.specmp.feature.mountassistant.testing.FakeAssistantSettingsRepository
import ru.alarmtrade.specmp.feature.mountassistant.testing.FakeAssistantStateRepository
import ru.alarmtrade.specmp.feature.mountassistant.testing.FakeSessionController
import ru.alarmtrade.specmp.feature.mountassistant.testing.FakeSettingsWrites
import ru.alarmtrade.specmp.feature.mountassistant.testing.switchSetting
import ru.alarmtrade.specmp.feature.mountassistant.testing.testCheck
import ru.alarmtrade.specmp.feature.mountassistant.testing.testFacts
import ru.alarmtrade.specmp.feature.mountassistant.testing.testProduct
import ru.alarmtrade.specmp.feature.mountassistant.testing.timeSetting

@OptIn(ExperimentalCoroutinesApi::class)
class MountAssistantViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val sessionRepository = FakeDeviceSessionRepository(testAlarmDevice)
    private val journal = AssistantJournalImpl()
    private val devices = FakeAssistantDeviceRepository()
    private val firmware = FakeAssistantFirmwareRepository()
    private val autostart = FakeAssistantAutostartRepository()
    private val vehicleState = FakeAssistantStateRepository()
    private val settings = FakeAssistantSettingsRepository()
    private val sessions = FakeSessionController()
    private val settingsWrites = FakeSettingsWrites()
    private val time = TestTimeSource()
    private val cooldown = ServicePinCooldown(time)
    private val clock = FixedClock(Instant.fromEpochSeconds(1_700_000_000))
    private val viewModels = mutableListOf<MountAssistantViewModel>()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun test(block: suspend TestScope.() -> Unit): TestResult = runTest(dispatcher) {
        try {
            block()
        } finally {
            viewModels.forEach { it.viewModelScope.cancel() }
        }
    }

    private fun TestScope.viewModel(): Pair<MountAssistantViewModel, MutableList<MountAssistantEffect>> {
        val session = testDeviceSession(sessionRepository, dispatcher = dispatcher, transport = ConnectionTransport.BLE)
        val vm = MountAssistantViewModel(
            deviceSession = session,
            journal = journal,
            devices = devices,
            firmware = firmware,
            autostart = autostart,
            vehicleState = vehicleState,
            settings = settings,
            sessions = sessions,
            settingsWrites = settingsWrites,
            pinCooldown = cooldown,
            dispatchers = testDispatchers(dispatcher),
            clock = clock,
        ).also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        val effects = mutableListOf<MountAssistantEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm to effects
    }

    private fun entries() = journal.state.value.entries

    @Test
    fun `start greets, binds the block and reads autostart quietly`() = test {
        val (vm, _) = viewModel()
        assertEquals(listOf<JournalEntry>(JournalEntry.Greeting(1)), entries())
        assertEquals(testAlarmDevice.systemId, journal.state.value.systemId)
        assertEquals(1, autostart.calls)
        assertEquals((autostart.status as Response.Success).value, journal.state.value.progress.autostart)
        assertEquals(testFacts, vm.state.value.facts)
    }

    @Test
    fun `firmware check adds a step and a card, then opens and pulses the topic`() = test {
        val (vm, _) = viewModel()
        vm.onActionClick(AssistantAction.FIRMWARE_CHECK)
        runCurrent()

        val step = assertIs<JournalEntry.Step>(entries()[1])
        assertEquals(StepTitle.Action(AssistantAction.FIRMWARE_CHECK), step.title)
        assertEquals(clock.instant, step.at)
        val card = assertIs<JournalEntry.Result>(entries()[2])
        assertEquals(ResultContent.FirmwareData(testCheck, testFacts.can), card.content)
        assertEquals(testCheck, journal.state.value.progress.firmware)
        assertEquals(DockLevel.Topic(AssistantTopic.FIRMWARE), vm.state.value.level)
        assertEquals(AssistantTopic.FIRMWARE, vm.state.value.pulse?.topic)
    }

    @Test
    fun `error card retries in place`() = test {
        firmware.results += Response.Error(AppException(PlatformError.DeviceDisconnected, IllegalStateException()))
        val (vm, _) = viewModel()
        vm.onActionClick(AssistantAction.FIRMWARE_CHECK)
        runCurrent()
        val card = assertIs<JournalEntry.Result>(entries().last())
        assertEquals(ResultContent.Error(ResultFailure.Common(PlatformError.DeviceDisconnected)), card.content)

        vm.onRetry(card.id)
        runCurrent()
        assertIs<ResultContent.FirmwareData>((entries().last() as JournalEntry.Result).content)
        assertEquals(3, entries().size)
    }

    @Test
    fun `lost link turns a loading card into a link error`() = test {
        val gate = CompletableDeferred<Unit>()
        firmware.gate = gate
        val (vm, _) = viewModel()
        vm.onActionClick(AssistantAction.FIRMWARE_CHECK)
        runCurrent()
        assertEquals(ResultContent.Loading, (entries().last() as JournalEntry.Result).content)

        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        assertEquals(
            ResultContent.Error(ResultFailure.LinkLost),
            (entries().last() as JournalEntry.Result).content,
        )
    }

    @Test
    fun `blocked action leaves a notice and returns to topics`() = test {
        devices.facts.value = testFacts.copy(can = CanFacts(factory = true, code = null, version = null))
        val (vm, _) = viewModel()
        vm.onTopicClick(AssistantTopic.SETTINGS)
        vm.onActionClick(AssistantAction.SETTINGS_ALL)
        runCurrent()
        assertEquals(JournalEntry.Notice(2, BlockReason.FACTORY_CAN), entries().last())
        assertEquals(DockLevel.Home, vm.state.value.level)
        assertEquals(0, autostart.calls)
    }

    @Test
    fun `hidden action does nothing`() = test {
        val (vm, _) = viewModel()
        vm.onActionClick(AssistantAction.FIRMWARE_UPDATE)
        runCurrent()
        assertEquals(1, entries().size)
    }

    @Test
    fun `topic with one action runs it, topic with several opens its level`() = test {
        val (vm, effects) = viewModel()
        vm.onTopicClick(AssistantTopic.STATE)
        runCurrent()
        assertEquals(DockLevel.Topic(AssistantTopic.STATE), vm.state.value.level)

        vm.onTopicBack()
        vm.onTopicClick(AssistantTopic.SETTINGS)
        assertEquals(DockLevel.Topic(AssistantTopic.SETTINGS), vm.state.value.level)
        vm.onActionClick(AssistantAction.SETTINGS_FIND)
        assertEquals(listOf<MountAssistantEffect>(MountAssistantEffect.FocusSearch), effects)
    }

    @Test
    fun `search without settings opens the matched topic`() = test {
        val (vm, _) = viewModel()
        vm.onQueryChange("обход")
        vm.onSearchSubmit(AssistantTopic.AUTOSTART)
        assertEquals(DockLevel.Topic(AssistantTopic.AUTOSTART), vm.state.value.level)
        assertEquals("", vm.state.value.query)
    }

    @Test
    fun `reconnect of the same block keeps the journal, a new screen rebuilds from it`() = test {
        val (vm, _) = viewModel()
        vm.onActionClick(AssistantAction.STATE)
        runCurrent()
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        sessionRepository.link.value = DeviceLinkState.READY
        runCurrent()
        assertEquals(3, entries().size)

        val (second, _) = viewModel()
        assertEquals(entries(), second.state.value.journal.entries)
        assertTrue(entries().first() is JournalEntry.Greeting)
        assertEquals(ResultRequest.State, (entries().last() as JournalEntry.Result).request)
    }

    @Test
    fun `dock collapses on scroll up only without a query, header remembers the choice`() = test {
        val (vm, _) = viewModel()
        vm.onFeedScrolledUp()
        assertTrue(vm.state.value.dockCollapsed)
        vm.onDockToggle()
        vm.onQueryChange("сир")
        vm.onFeedScrolledUp()
        assertEquals(false, vm.state.value.dockCollapsed)
        vm.onHeaderToggle(collapsed = true)
        assertEquals(HeaderMode.OPEN, vm.state.value.header)
    }

    @Test
    fun `handoff writes a step, remembers the block and navigates`() = test {
        val (vm, effects) = viewModel()
        vm.onActionClick(AssistantAction.BYPASS_RUN)
        assertEquals(StepTitle.Action(AssistantAction.BYPASS_RUN), (entries().last() as JournalEntry.Step).title)
        val pending = assertNotNull(journal.state.value.pendingHandoff)
        assertEquals(AssistantAction.BYPASS_RUN, pending.action)
        assertEquals(false, pending.before.autostart?.bypassLearned)
        assertEquals(
            listOf<MountAssistantEffect>(MountAssistantEffect.Navigate(AssistantNavigationTarget.BypassLearning)),
            effects,
        )
    }

    @Test
    fun `archive without a check reads the modules and opens the only one`() = test {
        val (vm, effects) = viewModel()
        vm.onActionClick(AssistantAction.FIRMWARE_ARCHIVE)
        runCurrent()

        assertEquals(1, firmware.calls)
        assertIs<ResultContent.FirmwareData>((entries().last() as JournalEntry.Result).content)
        assertEquals(AssistantAction.FIRMWARE_ARCHIVE, journal.state.value.pendingHandoff?.action)
        val target = AssistantNavigationTarget.FirmwareArchive(1, FirmwareModuleRole.MAIN, "3.56")
        assertEquals(listOf<MountAssistantEffect>(MountAssistantEffect.Navigate(target)), effects)
    }

    @Test
    fun `archive with several modules shows them, a module button opens its archive`() = test {
        val bluetooth =
            FirmwareModuleCheck(2, FirmwareModuleRole.BLUETOOTH, "BT", "1.14", "1.14", updateAvailable = false)
        firmware.results += Response.Success(FirmwareCheck(testCheck.modules + bluetooth))
        val (vm, effects) = viewModel()
        vm.onActionClick(AssistantAction.FIRMWARE_CHECK)
        runCurrent()

        vm.onActionClick(AssistantAction.FIRMWARE_ARCHIVE)
        runCurrent()
        assertEquals(1, firmware.calls)
        assertIs<ResultContent.FirmwareData>((entries().last() as JournalEntry.Result).content)
        assertNull(journal.state.value.pendingHandoff)
        assertTrue(effects.isEmpty())

        vm.onModuleArchiveClick(2)
        assertEquals(StepTitle.Action(AssistantAction.FIRMWARE_ARCHIVE), (entries().last() as JournalEntry.Step).title)
        val target = AssistantNavigationTarget.FirmwareArchive(2, FirmwareModuleRole.BLUETOOTH, "1.14")
        assertEquals(listOf<MountAssistantEffect>(MountAssistantEffect.Navigate(target)), effects)
    }

    @Test
    fun `autostart status carries the bypass variant setting found by its id`() = test {
        val variant = switchSetting.copy(id = BYPASS_VARIANT_SETTING_ID, name = "Bypass variant")
        settings.found = listOf(variant)
        val (vm, _) = viewModel()
        vm.onActionClick(AssistantAction.AUTOSTART_STATUS)
        runCurrent()
        val withVariant = (entries().last() as JournalEntry.Result).content as ResultContent.AutostartData
        assertEquals(variant, withVariant.variant)

        settings.found = emptyList()
        vm.onActionClick(AssistantAction.AUTOSTART_STATUS)
        runCurrent()
        val without = (entries().last() as JournalEntry.Result).content as ResultContent.AutostartData
        assertNull(without.variant)
    }

    @Test
    fun `settings written in the editor are reported on return`() = test {
        val (vm, _) = viewModel()
        vm.onActionClick(AssistantAction.SETTINGS_ALL)

        settingsWrites.record(SettingsWrite(SettingsSource.Device, listOf("Siren", "Turbo timer")))
        settingsWrites.record(SettingsWrite(SettingsSource.Defaults("DX4G"), listOf("Not the block")))
        vm.onScreenResumed()
        runCurrent()

        val change = (entries().last() as JournalEntry.Return).changes?.single()
        assertEquals(SnapshotChange.Settings(before = 0, after = 2, names = listOf("Siren", "Turbo timer")), change)
        assertEquals(2, journal.state.value.progress.settingsChanged)
    }

    @Test
    fun `module button flashes the latest version, an up-to-date module is reflashed`() = test {
        val current =
            FirmwareModuleCheck(2, FirmwareModuleRole.BLUETOOTH, "BT", "1.14", "1.14", updateAvailable = false)
        firmware.results += Response.Success(FirmwareCheck(testCheck.modules + current))
        val (vm, effects) = viewModel()
        vm.onActionClick(AssistantAction.FIRMWARE_CHECK)
        runCurrent()

        vm.onModuleFlashClick(1)
        assertEquals(StepTitle.Action(AssistantAction.FIRMWARE_UPDATE), (entries().last() as JournalEntry.Step).title)
        assertEquals(AssistantAction.FIRMWARE_UPDATE, journal.state.value.pendingHandoff?.action)
        vm.onModuleFlashClick(2)
        assertEquals(StepTitle.Reflash, (entries().last() as JournalEntry.Step).title)

        val targets = listOf(1L, 2L).map { MountAssistantEffect.Navigate(AssistantNavigationTarget.FirmwareFlash(it)) }
        assertEquals<List<MountAssistantEffect>>(targets, effects)
    }

    @Test
    fun `return keeps the topic level the screen was opened from`() = test {
        val (vm, _) = viewModel()
        vm.onTopicClick(AssistantTopic.SETTINGS)
        vm.onActionClick(AssistantAction.SETTINGS_ALL)

        vm.onScreenResumed()
        runCurrent()

        assertNull(journal.state.value.pendingHandoff)
        assertEquals(DockLevel.Topic(AssistantTopic.SETTINGS), vm.state.value.level)
    }

    @Test
    fun `return compares the block before and after`() = test {
        val (vm, _) = viewModel()
        vm.onActionClick(AssistantAction.BYPASS_RUN)
        autostart.status = Response.Success(AutostartStatus(true, CloneState.REQUIRED, 3))

        vm.onScreenResumed()
        runCurrent()

        val card = assertIs<JournalEntry.Return>(entries().last())
        assertEquals(listOf<SnapshotChange>(SnapshotChange.Bypass(before = false, after = true)), card.changes)
        assertNull(journal.state.value.pendingHandoff)
        // Пульт остаётся на уровне, с которого ушли; тема результата только мигает.
        assertEquals(DockLevel.Home, vm.state.value.level)
        assertEquals(AssistantTopic.AUTOSTART, vm.state.value.pulse?.topic)
        vm.onScreenResumed()
        assertEquals(card, entries().last())
    }

    @Test
    fun `return without changes says the block did not change, quiet screens add nothing`() = test {
        val (vm, _) = viewModel()
        vm.onActionClick(AssistantAction.SETTINGS_ALL)
        vm.onScreenResumed()
        runCurrent()
        assertEquals(emptyList(), assertIs<JournalEntry.Return>(entries().last()).changes)

        vm.onActionClick(AssistantAction.SUPPORT)
        val size = entries().size
        vm.onScreenResumed()
        runCurrent()
        assertEquals(size, entries().size)
        assertNull(journal.state.value.pendingHandoff)
    }

    @Test
    fun `a new screen after the bootloader finishes the return from the journal`() = test {
        val (vm, _) = viewModel()
        vm.onActionClick(AssistantAction.FIRMWARE_CHECK)
        runCurrent()
        vm.onActionClick(AssistantAction.FIRMWARE_UPDATE)
        viewModels.forEach { it.viewModelScope.cancel() }
        devices.facts.value = testFacts.copy(firmwareVersion = "3.57")
        firmware.results +=
            Response.Success(testCheck.copy(modules = testCheck.modules.map { it.copy(updateAvailable = false) }))

        val (fresh, _) = viewModel()
        runCurrent()

        val card = assertIs<JournalEntry.Return>(entries().last())
        assertEquals(AssistantAction.FIRMWARE_UPDATE, card.action)
        assertEquals(listOf<SnapshotChange>(SnapshotChange.Firmware("3.56", "3.57")), card.changes)
        assertEquals(false, journal.state.value.progress.firmware?.updateAvailable)
        assertEquals(DockLevel.Home, fresh.state.value.level)
        assertTrue(entries().first() is JournalEntry.Greeting)
    }

    @Test
    fun `resume without a pending screen does nothing`() = test {
        val (vm, _) = viewModel()
        vm.onScreenResumed()
        runCurrent()
        assertEquals(1, entries().size)
    }

    private fun TestScope.searchedCard(vm: MountAssistantViewModel): JournalEntry.Result {
        settings.found = listOf(switchSetting, timeSetting)
        vm.onQueryChange("автозапуск")
        advanceTimeBy(400)
        runCurrent()
        vm.onSearchSubmit(null)
        runCurrent()
        return entries().last() as JournalEntry.Result
    }

    private fun TestScope.pendingConfirm(vm: MountAssistantViewModel): JournalEntry.Confirm {
        val card = searchedCard(vm)
        vm.onEditStart(card.id, switchSetting.id)
        vm.onDraftChange(card.id, AssistantSettingValue.Switch(true))
        vm.onEditNext(card.id)
        runCurrent()
        return entries().last() as JournalEntry.Confirm
    }

    @Test
    fun `live search shows settings in the dock, a pick adds a card open for editing`() = test {
        settings.found = listOf(switchSetting)
        val (vm, _) = viewModel()
        vm.onQueryChange("ав")
        advanceTimeBy(400)
        runCurrent()
        assertEquals(emptyList(), settings.queries)

        vm.onQueryChange("автозапуск")
        advanceTimeBy(400)
        runCurrent()
        assertEquals(listOf(switchSetting), vm.state.value.liveSettings)

        vm.onSettingPick(switchSetting.id)
        runCurrent()
        assertEquals(
            StepTitle.Setting(switchSetting.name),
            (entries()[entries().lastIndex - 1] as JournalEntry.Step).title,
        )
        val data = assertIs<ResultContent.SettingsData>((entries().last() as JournalEntry.Result).content)
        assertEquals(switchSetting.id, data.editing)
        assertEquals(switchSetting.value, data.draft)
        assertEquals("", vm.state.value.query)
    }

    @Test
    fun `search submit puts the found settings into the feed`() = test {
        val (vm, _) = viewModel()
        val card = searchedCard(vm)
        assertEquals(ResultRequest.Settings("автозапуск"), card.request)
        assertEquals(listOf(switchSetting, timeSetting), (card.content as ResultContent.SettingsData).found)
        assertEquals(DockLevel.Topic(AssistantTopic.SETTINGS), vm.state.value.level)
    }

    @Test
    fun `edit and next ask to confirm with other unsaved changes`() = test {
        settings.others = 2
        val (vm, _) = viewModel()
        val confirm = pendingConfirm(vm)
        assertEquals(WriteStatus.PENDING, confirm.status)
        assertEquals(AssistantSettingValue.Switch(false), confirm.from)
        assertEquals(AssistantSettingValue.Switch(true), confirm.to)
        assertEquals(2, confirm.otherUnsaved)
        assertEquals(
            null,
            (entries()[entries().lastIndex - 1] as JournalEntry.Result).content.let {
                (it as ResultContent.SettingsData).editing
            },
        )
    }

    @Test
    fun `write saves to the block and counts the change`() = test {
        val (vm, _) = viewModel()
        val confirm = pendingConfirm(vm)
        vm.onWriteConfirm(confirm.id)
        runCurrent()
        assertEquals(WriteStatus.DONE, (entries().last() as JournalEntry.Confirm).status)
        assertEquals(
            listOf<Pair<Int, AssistantSettingValue>>(11 to AssistantSettingValue.Switch(true)),
            settings.writes,
        )
        assertEquals(1, journal.state.value.progress.settingsChanged)
        assertEquals(AssistantTopic.SETTINGS, vm.state.value.pulse?.topic)
    }

    @Test
    fun `wrong pin opens the pin dialog, the entered pin is saved and the session written`() = test {
        settings.writeResults += Response.Error(AssistantWriteException(WriteFailure.WRONG_PIN))
        settings.saveResults += Response.Error(AssistantWriteException(WriteFailure.WRONG_PIN))
        val (vm, _) = viewModel()
        val confirm = pendingConfirm(vm)
        vm.onWriteConfirm(confirm.id)
        runCurrent()
        assertEquals(WriteStatus.PIN, (entries().last() as JournalEntry.Confirm).status)
        assertEquals(PinPrompt(confirm.id, testAlarmDevice.systemId), vm.state.value.pin)

        vm.onPinSubmit("1234")
        runCurrent()
        assertEquals(1, vm.state.value.pin?.wrongCount)
        assertEquals(1, sessionRepository.pins.size)

        time += 20.seconds
        vm.onPinSubmit("4321")
        runCurrent()
        assertEquals(WriteStatus.DONE, (entries().last() as JournalEntry.Confirm).status)
        assertNull(vm.state.value.pin)
        assertEquals(2, settings.saves)
    }

    @Test
    fun `pin dismissed returns to waiting, cancel then reverts the value in the session`() = test {
        settings.writeResults += Response.Error(AssistantWriteException(WriteFailure.WRONG_PIN))
        val (vm, _) = viewModel()
        val confirm = pendingConfirm(vm)
        vm.onWriteConfirm(confirm.id)
        runCurrent()
        vm.onPinDismiss()
        val waiting = entries().last() as JournalEntry.Confirm
        assertEquals(WriteStatus.PENDING, waiting.status)
        assertTrue(waiting.pinSkipped)

        vm.onWriteCancel(confirm.id)
        runCurrent()
        assertEquals(WriteStatus.CANCELLED, (entries().last() as JournalEntry.Confirm).status)
        assertEquals(
            listOf<Pair<Int, AssistantSettingValue>>(11 to AssistantSettingValue.Switch(false)),
            settings.reverts,
        )
    }

    @Test
    fun `write error retries with save only, a plain cancel does not touch the session`() = test {
        settings.writeResults += Response.Error(AssistantWriteException(WriteFailure.FAILED))
        val (vm, _) = viewModel()
        val confirm = pendingConfirm(vm)
        vm.onWriteConfirm(confirm.id)
        runCurrent()
        val failed = entries().last() as JournalEntry.Confirm
        assertEquals(WriteStatus.ERROR, failed.status)
        assertEquals(ResultFailure.Failed, failed.failure)

        vm.onWriteConfirm(confirm.id)
        runCurrent()
        assertEquals(1, settings.writes.size)
        assertEquals(1, settings.saves)
        assertEquals(WriteStatus.DONE, (entries().last() as JournalEntry.Confirm).status)

        val second = pendingConfirm(vm)
        vm.onWriteCancel(second.id)
        runCurrent()
        assertEquals(emptyList(), settings.reverts)
    }

    @Test
    fun `open in settings and source pick hand off to the editor`() = test {
        val (vm, effects) = viewModel()
        vm.onOpenInSettings(switchSetting.id)
        assertEquals(StepTitle.OpenInSettings, (entries().last() as JournalEntry.Step).title)
        assertEquals(AssistantAction.SETTINGS_ALL, journal.state.value.pendingHandoff?.action)
        assertEquals(
            MountAssistantEffect.Navigate(AssistantNavigationTarget.SettingsEditor(focusSettingId = switchSetting.id)),
            effects.last(),
        )
        journal.endHandoff()

        vm.onActionClick(AssistantAction.SETTINGS_LOAD)
        val picker = assertIs<JournalEntry.SourcePicker>(entries().last())
        vm.onSourcePick(picker.id, SettingsSourceKind.FILE)
        assertEquals(SettingsSourceKind.FILE, (entries().last() as JournalEntry.SourcePicker).used)
        assertEquals(AssistantAction.SETTINGS_LOAD, journal.state.value.pendingHandoff?.action)
        assertEquals(
            MountAssistantEffect.Navigate(AssistantNavigationTarget.SettingsEditor(openLoad = true)),
            effects.last(),
        )
    }

    @Test
    fun `cloud source is not picked offline`() = test {
        devices.facts.value = testFacts.copy(online = false)
        val (vm, effects) = viewModel()
        vm.onActionClick(AssistantAction.SETTINGS_LOAD)
        val picker = entries().last()
        vm.onSourcePick(picker.id, SettingsSourceKind.CLOUD)
        assertEquals(null, (entries().last() as JournalEntry.SourcePicker).used)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `leaving the screen discards the device settings session`() = test {
        val (vm, _) = viewModel()
        val store = ViewModelStore()
        store.put("assistant", vm)
        store.clear()
        assertEquals(listOf<SettingsSource>(SettingsSource.Device), sessions.discarded)
    }

    @Test
    fun `product info shows the connected block product, details open the full screen`() = test {
        val (vm, effects) = viewModel()
        vm.onActionClick(AssistantAction.PRODUCT_INFO)
        runCurrent()

        val card = assertIs<JournalEntry.Result>(entries().last())
        assertEquals(ResultRequest.Product, card.request)
        assertEquals(ResultContent.ProductData(testProduct), card.content)
        assertEquals(listOf(testFacts.systemId), devices.productRequests)
        assertEquals(DockLevel.Topic(AssistantTopic.DEVICES), vm.state.value.level)

        vm.onProductOpen(testProduct.id.toString())
        runCurrent()
        assertEquals(
            MountAssistantEffect.Navigate(AssistantNavigationTarget.ProductInfo("77", testFacts.systemId)),
            effects.last(),
        )
    }

    @Test
    fun `unknown product is a neutral card, not an error`() = test {
        devices.product = Response.Error(AssistantProductNotFoundException())
        val (vm, _) = viewModel()
        vm.onActionClick(AssistantAction.PRODUCT_INFO)
        runCurrent()

        assertEquals(
            ResultContent.ProductNotFound(testFacts.systemId),
            (entries().last() as JournalEntry.Result).content,
        )
    }

    @Test
    fun `a number in the search opens the product screen instead of the settings search`() = test {
        val (vm, effects) = viewModel()
        vm.onQueryChange("4123 700107131")
        vm.onSearchSubmit(topic = null)
        runCurrent()

        assertEquals(StepTitle.ProductNumber("4123700107131"), (entries().last() as JournalEntry.Step).title)
        assertEquals("", vm.state.value.query)
        assertEquals(
            MountAssistantEffect.Navigate(AssistantNavigationTarget.ProductInfo("4123700107131", testFacts.systemId)),
            effects.last(),
        )
    }

    @Test
    fun `without the account feature a number is an ordinary query`() = test {
        devices.facts.value = testFacts.copy(productSearch = false)
        val (vm, effects) = viewModel()
        vm.onQueryChange("107131")
        vm.onSearchSubmit(topic = null)
        runCurrent()

        assertTrue(effects.none { it is MountAssistantEffect.Navigate })
        assertTrue(entries().none { (it as? JournalEntry.Step)?.title is StepTitle.ProductNumber })
    }

    private val routeDone = testFacts.copy(
        transport = AssistantTransport.USB,
        capabilities = emptySet(),
        can = null,
    )

    @Test
    fun `today passport of the unit is read on start`() = test {
        devices.passport = Response.Success(TodayPassport("p1", "20261119-1", issued = true))
        viewModel()

        assertEquals(listOf(testAlarmDevice.systemId), devices.passportRequests)
        assertEquals(TodayPassport("p1", "20261119-1", issued = true), journal.state.value.progress.passport)
    }

    @Test
    fun `finished route offers the passport with one feed card`() = test {
        devices.facts.value = routeDone
        val (vm, _) = viewModel()
        vm.onQueryChange("датчик")
        vm.onQueryChange("")
        runCurrent()

        assertEquals(1, entries().count { it is JournalEntry.Passport })
        assertEquals(null, entries().filterIsInstance<JournalEntry.Passport>().single().passport)
    }

    @Test
    fun `passport is not offered when one was issued today`() = test {
        devices.facts.value = routeDone
        devices.passport = Response.Success(TodayPassport("p1", "20261119-1", issued = true))
        viewModel()

        assertTrue(entries().none { it is JournalEntry.Passport })
    }

    @Test
    fun `passport action reads the unit, issued passport is opened`() = test {
        val (vm, effects) = viewModel()
        vm.onActionClick(AssistantAction.MOUNT_PASSPORT)
        runCurrent()
        assertEquals(MountAssistantEffect.Navigate(AssistantNavigationTarget.MountPassport(null)), effects.last())

        devices.passport = Response.Success(TodayPassport("p1", "20261119-1", issued = true))
        vm.onScreenResumed()
        runCurrent()
        vm.onActionClick(AssistantAction.MOUNT_PASSPORT)
        runCurrent()

        assertEquals(MountAssistantEffect.Navigate(AssistantNavigationTarget.MountPassport("p1")), effects.last())
    }

    @Test
    fun `return after issuing shows the issued card without a return card`() = test {
        devices.facts.value = routeDone
        val (vm, _) = viewModel()
        vm.onActionClick(AssistantAction.MOUNT_PASSPORT)
        runCurrent()

        devices.passport = Response.Success(TodayPassport("p1", "20261119-1", issued = true))
        vm.onScreenResumed()
        runCurrent()

        val card = entries().filterIsInstance<JournalEntry.Passport>().single()
        assertEquals("20261119-1", card.passport?.number)
        assertTrue(entries().none { it is JournalEntry.Return })
        assertEquals(TodayPassport("p1", "20261119-1", issued = true), journal.state.value.progress.passport)
    }
}
