package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.di.createDeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountFile
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTaskStatus
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState
import ru.alarmtrade.specmp.feature.fastmount.domain.model.UnitAccess
import ru.alarmtrade.specmp.feature.fastmount.ui.FakeConnectionRepository
import ru.alarmtrade.specmp.feature.fastmount.ui.FakeFastMountRepository
import ru.alarmtrade.specmp.feature.fastmount.ui.FakeProgramModeRepository
import ru.alarmtrade.specmp.feature.fastmount.ui.FakeSessionRepository
import ru.alarmtrade.specmp.feature.fastmount.ui.question
import ru.alarmtrade.specmp.feature.fastmount.ui.testConfiguration
import ru.alarmtrade.specmp.feature.fastmount.ui.testDevice

@OptIn(ExperimentalCoroutinesApi::class)
class FastMountViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sessionRepository = FakeSessionRepository()
    private val repository = FakeFastMountRepository()
    private val programMode = FakeProgramModeRepository()
    private val viewModels = mutableListOf<FastMountViewModel>()
    private val stateCollectors = mutableListOf<Job>()
    private lateinit var session: DeviceSession

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

    // USB по умолчанию: без цикла переподключения BLE — связь в тесте переключает сам тест.
    private fun TestScope.viewModel(transport: ConnectionTransport = ConnectionTransport.USB): FastMountViewModel {
        val dispatchers = AppDispatchers(dispatcher, dispatcher, dispatcher)
        session =
            createDeviceSession(
                sessionRepository,
                FakeConnectionRepository(),
                dispatchers,
                backgroundScope.coroutineContext,
            ).also {
                it.start()
            }
        session.attach(transport)
        val vm = FastMountViewModel(session, repository, programMode, dispatchers).also { viewModels += it }
        // Экран подписан на state — с этого стартует работа VM (onStart).
        stateCollectors += backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()
        return vm
    }

    /** Экран ушёл (нет подписчика state дольше тайм-аута), затем вернулся. */
    private fun TestScope.leaveScreen() {
        stateCollectors.forEach { it.cancel() }
        stateCollectors.clear()
        advanceTimeBy(10_000)
        runCurrent()
    }

    private fun TestScope.returnToScreen(vm: FastMountViewModel) {
        stateCollectors += backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()
    }

    private fun TestScope.effects(vm: FastMountViewModel): List<FastMountEffect> {
        val effects = mutableListOf<FastMountEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        return effects
    }

    private fun TestScope.reconnect(systemId: Long, device: AlarmDevice = testDevice(systemId)) {
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        sessionRepository.device = device
        sessionRepository.link.value = DeviceLinkState.READY
        runCurrent()
    }

    /** Состояние, которое видит экран: `state` — `stateIn`, интент доходит до него после диспетчеризации. */
    private fun TestScope.state(vm: FastMountViewModel): FastMountState {
        runCurrent()
        return vm.state.value
    }

    private val car = MountCar(id = 1, brand = "Volkswagen", model = "Polo", years = "2020", canCode = "0213")

    /** Проверка блока → автомобиль → конфигурация → сводка → старт: мастер на записи настроек. */
    private fun TestScope.started(transport: ConnectionTransport = ConnectionTransport.USB): FastMountViewModel {
        val vm = viewModel(transport)
        vm.onCheckNext()
        vm.onCarPicked(car)
        vm.onConfigurationPicked(42)
        runCurrent()
        vm.onStartMount()
        runCurrent()
        return vm
    }

    @Test
    fun `alarm unit is fixed and ready`() = test {
        val vm = viewModel()
        assertEquals(UnitCheck.Done(UnitVerdict.Ready), state(vm).check)
        assertEquals(77, state(vm).fixed?.systemId)
        assertEquals("3.57", state(vm).unit?.firmware)
    }

    @Test
    fun `microbot is not supported`() = test {
        repository.deviceClass = Response.Success(MountDeviceClass.MICROBOT)
        assertEquals(UnitCheck.Unsupported(UnsupportedReason.MICROBOT), state(viewModel()).check)
    }

    @Test
    fun `unit without model in database is not supported without a server request`() = test {
        sessionRepository.device = testDevice(modelInDatabase = false)
        val vm = viewModel()
        assertEquals(UnitCheck.Unsupported(UnsupportedReason.NOT_IN_DATABASE), state(vm).check)
        assertEquals(0, repository.classRequests)
    }

    @Test
    fun `failed check can be retried`() = test {
        repository.deviceClass = Response.Error(IllegalStateException())
        val vm = viewModel()
        assertIs<UnitCheck.Failed>(state(vm).check)

        repository.deviceClass = Response.Success(MountDeviceClass.ALARM)
        vm.onRetryCheck()
        runCurrent()
        assertEquals(UnitCheck.Done(UnitVerdict.Ready), state(vm).check)
    }

    @Test
    fun `no access and factory CAN block the wizard`() = test {
        repository.access = Response.Success(UnitAccess(granted = false, message = "Чужой блок"))
        val vm = viewModel()
        assertEquals(UnitCheck.Done(UnitVerdict.NoAccess("Чужой блок")), state(vm).check)
        vm.onCheckNext()
        assertEquals(WizardScreen.CHECK, state(vm).screen)

        repository.access = Response.Success(UnitAccess(granted = true, message = ""))
        // На экране проверки переподключение проверяет блок заново (например, после обновления прошивки).
        reconnect(77, testDevice(can = CanInfo("1.0", "0000", isFactory = true)))
        assertEquals(UnitCheck.Done(UnitVerdict.FactoryCan), state(vm).check)
    }

    @Test
    fun `outdated firmware is only a recommendation`() = test {
        repository.latest = Response.Success("3.60")
        val vm = viewModel()
        assertEquals(UnitCheck.Done(UnitVerdict.Outdated("3.60")), state(vm).check)
        vm.onCheckNext()
        assertEquals(WizardScreen.VEHICLE, state(vm).screen)
    }

    @Test
    fun `another unit after reconnect stops the wizard until the fixed one returns`() = test {
        val vm = viewModel()
        reconnect(systemId = 99)
        assertEquals(testDevice(99).systemName + "  99", state(vm).wrongDevice)
        assertEquals(77, state(vm).fixed?.systemId)

        reconnect(systemId = 77)
        assertNull(state(vm).wrongDevice)
        assertEquals(UnitCheck.Done(UnitVerdict.Ready), state(vm).check)
    }

    @Test
    fun `preparation builds the plan and starts writing settings`() = test {
        val vm = started()
        val state = state(vm)
        assertEquals(WizardScreen.MASTER, state.screen)
        // USB: без Slave и проверки системы; Clone — по необходимости.
        assertEquals(
            listOf(
                FastMountStep.WRITING_SETTINGS,
                FastMountStep.MOUNT,
                FastMountStep.KEY_BYPASS,
                FastMountStep.CLONE,
                FastMountStep.FEEDBACK,
            ),
            state.plan?.steps?.map { it.step },
        )
        // Запись начинается сама.
        assertEquals(1, repository.nextCalls)
    }

    @Test
    fun `summary retry reloads the configuration that failed`() = test {
        val vm = viewModel()
        vm.onCheckNext()
        vm.onCarPicked(car)
        vm.onConfigurationPicked(42)
        runCurrent()
        // Другая конфигурация не загрузилась — повтор берёт её id, а не уже загруженной.
        repository.configuration = Response.Error(IllegalStateException())
        vm.onBack()
        vm.onConfigurationPicked(7)
        runCurrent()
        assertEquals(7, assertIs<SummaryLoad.Failed>(state(vm).summary).configurationId)

        repository.configuration = Response.Success(testConfiguration())
        vm.onRetrySummary()
        runCurrent()
        assertEquals(listOf(42, 7, 7), repository.configurationRequests)
        assertEquals(SummaryLoad.Ready, state(vm).summary)
    }

    @Test
    fun `start error returns to summary on back`() = test {
        repository.startResponse = Response.Error(IllegalStateException())
        val vm = started()
        assertEquals(MasterScreen.STARTING, state(vm).master.screen)
        assertIs<Any>(state(vm).master.startError)
        vm.onBack()
        assertEquals(WizardScreen.SUMMARY, state(vm).screen)
    }

    @Test
    fun `usb route goes through mount, bypass, clone and feedback to finish`() = test {
        repository.onNext = { step ->
            when (step) {
                FastMountStep.WRITING_SETTINGS -> FastMountStep.MOUNT
                FastMountStep.MOUNT -> FastMountStep.KEY_BYPASS
                FastMountStep.KEY_BYPASS -> FastMountStep.CLONE
                FastMountStep.CLONE -> FastMountStep.FEEDBACK
                FastMountStep.FEEDBACK -> FastMountStep.FINISH
                else -> step
            }
        }
        val vm = started()
        runCurrent()
        assertEquals(MasterScreen.MOUNT, state(vm).master.screen)
        assertEquals(WizardToast.SETTINGS_WRITTEN, state(vm).master.toast)

        vm.onMountDone()
        assertEquals(WizardDialog.MOUNT_DONE, state(vm).dialog)
        vm.onDialogConfirm()
        runCurrent()
        assertEquals(MasterScreen.BYPASS, state(vm).master.screen)

        vm.onBypassNext(cloneRequired = true)
        runCurrent()
        assertEquals(true, state(vm).master.cloneNeeded)
        assertEquals(MasterScreen.CLONE, state(vm).master.screen)

        vm.onCloneNext()
        runCurrent()
        assertEquals(MasterScreen.FEEDBACK, state(vm).master.screen)

        vm.onFeedbackSend()
        assertEquals(null, repository.feedback)
        vm.onRatingChange(5)
        vm.onFeedbackChange("Всё по карте")
        vm.onFeedbackSend()
        runCurrent()
        assertEquals("Всё по карте" to 5, repository.feedback)
        assertEquals(MasterScreen.DONE, state(vm).master.screen)
    }

    @Test
    fun `back from mount asks to exit and leaves program mode on ble`() = test {
        repository.onNext = { if (it == FastMountStep.WRITING_SETTINGS) FastMountStep.MOUNT else it }
        val vm = started(ConnectionTransport.BLE)
        runCurrent()
        val effects = mutableListOf<FastMountEffect>()
        backgroundScope.launch { vm.sideEffect.toList(effects) }

        vm.onBack()
        assertEquals(WizardDialog.EXIT, state(vm).dialog)
        vm.onDialogConfirm()
        runCurrent()
        assertEquals(1, programMode.exits)
        assertEquals(listOf<FastMountEffect>(FastMountEffect.Exit), effects)
    }

    @Test
    fun `back to mount from bypass restarts the bypass step`() = test {
        repository.onNext = { if (it == FastMountStep.WRITING_SETTINGS) FastMountStep.KEY_BYPASS else it }
        repository.onPrevious = { FastMountStep.MOUNT }
        val vm = started()
        runCurrent()
        assertEquals(MasterScreen.BYPASS, state(vm).master.screen)

        vm.onStepScreenBack()
        assertEquals(WizardDialog.TO_MOUNT, state(vm).dialog)
        vm.onDialogConfirm()
        runCurrent()
        assertEquals(MasterScreen.MOUNT, state(vm).master.screen)
        assertEquals(1, state(vm).master.stepAttempt)
    }

    @Test
    fun `ble reboot waits for the unit to come back`() = test {
        repository.onNext = { step ->
            when (step) {
                FastMountStep.WRITING_SETTINGS -> FastMountStep.MOUNT
                FastMountStep.MOUNT -> FastMountStep.REBOOTING
                else -> step
            }
        }
        val vm = started(ConnectionTransport.BLE)
        runCurrent()
        vm.onMountDone()
        vm.onDialogConfirm()
        runCurrent()
        assertEquals(MasterScreen.REBOOTING, state(vm).master.screen)
        assertEquals(DeviceReboot.Restart, session.reboot.value)

        // Блок перезагрузился: SDK перешёл к обходу, связь оборвалась.
        repository.step.value = FastMountStep.KEY_BYPASS
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        assertEquals(MasterScreen.REBOOTING, state(vm).master.screen)

        sessionRepository.link.value = DeviceLinkState.READY
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(MasterScreen.BYPASS, state(vm).master.screen)
        assertNull(session.reboot.value)
    }

    @Test
    fun `system check marks auto answers, answers manual ones and skips the rest on next`() = test {
        repository.onNext = { step ->
            when (step) {
                FastMountStep.WRITING_SETTINGS -> FastMountStep.SYSTEM_CHECK
                FastMountStep.SYSTEM_CHECK -> FastMountStep.FEEDBACK
                else -> step
            }
        }
        val vm = started(ConnectionTransport.BLE)
        repository.questionsState.value = QuestionsState(current = question(5, automatic = true), completed = false)
        runCurrent()
        val first = state(vm).master.checklist
        assertEquals(listOf(5, 100, 101), first.tasks.map { it.id })
        assertTrue(first.tasks[0].active)

        // SDK засчитал авто-задание по телеметрии — курсор на ручном.
        repository.questionsState.value = QuestionsState(current = question(100), completed = false)
        runCurrent()
        assertEquals(MountTaskStatus.PASSED, state(vm).master.checklist.tasks[0].status)

        vm.onTaskAnswer(100, option = 1)
        runCurrent()
        assertEquals(MountTaskStatus.FAILED, state(vm).master.checklist.tasks[1].status)
        assertEquals(2, repository.answers.single().value)

        repository.questionsState.value = QuestionsState(current = question(101), completed = false)
        runCurrent()
        vm.onSystemCheckNext()
        runCurrent()
        assertEquals(MountTaskStatus.SKIPPED, state(vm).master.checklist.tasks[2].status)
        assertEquals(1, repository.skips)
        assertEquals(MasterScreen.FEEDBACK, state(vm).master.screen)
        assertEquals(1, state(vm).master.checklist.skipped)
    }

    @Test
    fun `repeated taps during an sdk call are skipped`() = test {
        repository.onNext = { if (it == FastMountStep.WRITING_SETTINGS) FastMountStep.SYSTEM_CHECK else it }
        repository.onPrevious = { FastMountStep.SLAVE }
        val vm = started(ConnectionTransport.BLE)
        repository.questionsState.value = QuestionsState(current = question(100), completed = false)
        runCurrent()
        repository.callMillis = 1_000

        // Ответ ещё отправляется — второй тап того же ответа его не дублирует.
        vm.onTaskAnswer(100, option = 0)
        vm.onTaskAnswer(100, option = 0)
        runCurrent()
        assertEquals(1, repository.answers.size)
        advanceTimeBy(1_001)

        // «Назад» во время перехода — один вызов SDK, «занято» снимается после ответа.
        vm.onSystemCheckBack()
        vm.onSystemCheckBack()
        runCurrent()
        assertTrue(state(vm).master.busy)
        advanceTimeBy(1_001)
        assertEquals(1, repository.previousCalls)
        assertEquals(false, state(vm).master.busy)
    }

    @Test
    fun `sdk steps and system check questions keep going while nobody watches the screen`() = test {
        repository.onNext = { if (it == FastMountStep.WRITING_SETTINGS) FastMountStep.SYSTEM_CHECK else it }
        val vm = started(ConnectionTransport.BLE)
        repository.questionsState.value = QuestionsState(current = question(5, automatic = true), completed = false)
        runCurrent()
        leaveScreen()

        // Автоответы SDK считаются в подписчике вопросов — он жив и без экрана.
        assertEquals(1, repository.questionsState.subscriptionCount.value)
        repository.questionsState.value = QuestionsState(current = question(100), completed = false)
        runCurrent()
        repository.step.value = FastMountStep.FEEDBACK
        runCurrent()
        // Проверка системы позади — вопросы больше не слушаем.
        assertEquals(0, repository.questionsState.subscriptionCount.value)

        returnToScreen(vm)
        assertEquals(MasterScreen.FEEDBACK, state(vm).master.screen)
        assertEquals(MountTaskStatus.PASSED, state(vm).master.checklist.tasks[0].status)
    }

    @Test
    fun `mounting map is downloaded ahead, retried after a failure and opened as pdf`() = test {
        repository.configuration = Response.Success(testConfiguration(mountingMap = MountFile("Карта", "url")))
        repository.downloadResponse = Response.Error(IllegalStateException())
        val vm = viewModel()
        val effects = effects(vm)
        vm.onCheckNext()
        vm.onCarPicked(car)
        vm.onConfigurationPicked(42)
        runCurrent()
        assertEquals(MountMapState.Failed, state(vm).master.mountMap)

        repository.downloadResponse = Response.Success(ByteArray(3))
        vm.onMapRetry()
        runCurrent()
        assertEquals(3, assertIs<MountMapState.Ready>(state(vm).master.mountMap).sizeBytes)

        vm.onMapOpen()
        runCurrent()
        assertEquals("Карта.pdf", assertIs<FastMountEffect.OpenFile>(effects.single()).name)
        vm.onFileOpenResult(false)
        assertEquals(WizardToast.FILE_NO_APP, state(vm).master.toast)
        vm.onToastShown()
        assertNull(state(vm).master.toast)
    }

    @Test
    fun `failed answer returns the task to the cursor`() = test {
        repository.onNext = { if (it == FastMountStep.WRITING_SETTINGS) FastMountStep.SYSTEM_CHECK else it }
        val vm = started(ConnectionTransport.BLE)
        repository.questionsState.value = QuestionsState(current = question(100), completed = false)
        runCurrent()
        repository.answerResponse = Response.Error(IllegalStateException())

        vm.onTaskAnswer(100, option = 0)
        runCurrent()
        val task = state(vm).master.checklist.tasks[1]
        assertEquals(MountTaskStatus.PENDING, task.status)
        assertTrue(task.active)
    }

    @Test
    fun `back on check offers another unit and return from firmware checks the unit again`() = test {
        val vm = viewModel()
        val effects = effects(vm)
        vm.onBack()
        runCurrent()
        assertEquals(listOf<FastMountEffect>(FastMountEffect.ChooseAnother), effects)

        vm.onOpenFirmwareUpdate()
        assertEquals(WizardScreen.FIRMWARE, state(vm).screen)
        repository.latest = Response.Success("3.60")
        vm.onBack()
        runCurrent()
        assertEquals(WizardScreen.CHECK, state(vm).screen)
        assertEquals(UnitCheck.Done(UnitVerdict.Outdated("3.60")), state(vm).check)
        assertEquals(2, repository.classRequests)
    }

    @Test
    fun `reconnect after the check does not check the unit again`() = test {
        val vm = started()
        reconnect(systemId = 77)
        assertEquals(1, repository.classRequests)
        assertEquals(WizardScreen.MASTER, state(vm).screen)
    }
}
