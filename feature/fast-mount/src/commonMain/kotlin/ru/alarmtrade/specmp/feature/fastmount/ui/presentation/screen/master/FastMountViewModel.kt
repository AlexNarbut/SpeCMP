package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import kotlin.time.TimeSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isLinkDown
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.ProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.fastmount.domain.model.AutoTaskKind
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountChecklist
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTask
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTaskStatus
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState
import ru.alarmtrade.specmp.feature.fastmount.domain.model.fastMountPlan
import ru.alarmtrade.specmp.feature.fastmount.domain.model.isNewerFirmwareThan
import ru.alarmtrade.specmp.feature.fastmount.domain.repository.FastMountRepository
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.model.FastMountError

/**
 * Мастер быстрого монтажа (одна VM на мастер, живёт всю запись навигации).
 *
 * **Подготовка.** Блок фиксируется на первом подключении; другой блок останавливает мастер оверлеем, возврат нужного —
 * продолжение с того же места. Проверка блока: класс (сигнализация), доступ на сервере, заводской CAN, свежесть
 * прошивки. Дальше — автомобиль и конфигурация (экраны других фич через слоты) и сводка с планом шагов.
 *
 * **Шаги SDK.** Порядок ведёт SDK (`steps()`); VM вызывает «вперёд/назад» и держит то, чего нет в SDK: занятость,
 * ошибки шагов, итог обхода/Clone, ожидание перезагрузки блока ([RebootWait]) и статусы заданий проверки системы.
 * Поток вопросов SDK собирается ровно одним подписчиком — здесь, пока идёт проверка системы.
 */
class FastMountViewModel(
    deviceSession: DeviceSession,
    private val repository: FastMountRepository,
    private val programMode: ProgramModeRepository,
    private val dispatchers: AppDispatchers
) : BaseDeviceViewModel(deviceSession) {

    private val _state = MutableStateFlow(FastMountState())
    val state: StateFlow<FastMountState> = _state.asStateFlow()

    private val _effect: MutableSharedFlow<FastMountEffect> = sideEffectFlow()
    val effect: SharedFlow<FastMountEffect> = _effect.asSharedFlow()

    private var stepsJob: Job? = null
    private var questionsJob: Job? = null
    private var rebootJob: Job? = null
    private var mapJob: Job? = null
    private var mapBytes: ByteArray? = null
    private var mountStart: TimeSource.Monotonic.ValueTimeMark? = null
    private var writeRequested = false

    init {
        onEachDeviceReady(::onDeviceReady)
    }

    // region Блок

    private suspend fun onDeviceReady(device: AlarmDevice) {
        val fixed = _state.value.fixed ?: FixedDevice(
            systemId = device.systemId,
            systemName = device.systemName,
            title = device.title,
            transport = deviceSession.transport.value
        ).also { fixed -> _state.update { it.copy(fixed = fixed) } }
        if (device.systemId != fixed.systemId) {
            logger.d { "Another unit connected: ${device.systemId}, expected ${fixed.systemId}" }
            _state.update { it.copy(wrongDevice = device.title) }
            return
        }
        _state.update { it.copy(wrongDevice = null, unit = device.toUnitInfo()) }
        val current = _state.value
        // Проверяем на экране проверки блока; в мастере и на следующих экранах переподключение ничего не меняет.
        if (current.screen == WizardScreen.CHECK && current.check !is UnitCheck.Unsupported) checkUnit(device)
    }

    fun onRetryCheck() {
        val device = currentDevice ?: return
        launchOnDevice { checkUnit(device) }
    }

    private suspend fun checkUnit(device: AlarmDevice) {
        _state.update { it.copy(check = UnitCheck.Checking) }
        val check = runCheck(device)
        logger.d { "Unit ${device.systemName} check: $check" }
        _state.update { it.copy(check = check) }
    }

    private suspend fun runCheck(device: AlarmDevice): UnitCheck {
        // Без модели в базе класс не определить (DeviceSession уже пробовал синхронизировать базу).
        if (!device.modelInDatabase) return UnitCheck.Unsupported(UnsupportedReason.NOT_IN_DATABASE)
        val deviceClass = when (val response = io { repository.deviceClass(device.systemName) }) {
            is Response.Success -> response.value
            is Response.Error -> return UnitCheck.Failed(FastMountError.from(response.exception))
        }
        when (deviceClass) {
            MountDeviceClass.ALARM -> Unit
            MountDeviceClass.MICROBOT -> return UnitCheck.Unsupported(UnsupportedReason.MICROBOT)
            MountDeviceClass.OTHER -> return UnitCheck.Unsupported(UnsupportedReason.NOT_ALARM)
        }
        val access = when (
            val response = io { repository.verify(device.systemId, device.systemName, device.firmwareVersion) }
        ) {
            is Response.Success -> response.value
            is Response.Error -> return UnitCheck.Failed(FastMountError.from(response.exception))
        }
        if (!access.granted) return UnitCheck.Done(UnitVerdict.NoAccess(access.message))
        if (device.can?.isFactory == true) return UnitCheck.Done(UnitVerdict.FactoryCan)
        // Свежесть прошивки — только рекомендация: не узнали — не мешаем монтажу.
        val latest = (io { repository.latestFirmware(device.systemName) } as? Response.Success)?.value
        return if (latest != null && latest.isNewerFirmwareThan(device.firmwareVersion)) {
            UnitCheck.Done(UnitVerdict.Outdated(latest))
        } else {
            UnitCheck.Done(UnitVerdict.Ready)
        }
    }

    fun onOpenFirmwareUpdate() {
        _state.update { it.copy(screen = WizardScreen.FIRMWARE) }
    }

    /** Вернулись с экрана прошивки — блок мог обновиться: проверяем заново. */
    fun onFirmwareBack() {
        _state.update { it.copy(screen = WizardScreen.CHECK) }
        onRetryCheck()
    }

    // endregion

    // region Подготовка

    fun onCheckNext() {
        val check = _state.value.check
        if (check !is UnitCheck.Done || check.verdict.blocking) return
        _state.update { it.copy(screen = WizardScreen.VEHICLE) }
    }

    fun onCarPicked(car: MountCar) {
        _state.update { it.copy(car = car, screen = WizardScreen.CONFIGURATION) }
    }

    fun onConfigurationPicked(id: Int) {
        _state.update { it.copy(screen = WizardScreen.SUMMARY) }
        loadConfiguration(id)
    }

    fun onRetrySummary() {
        (_state.value.summary as? SummaryLoad.Failed)?.let { loadConfiguration(it.configurationId) }
    }

    private fun loadConfiguration(id: Int) {
        _state.update { it.copy(summary = SummaryLoad.Loading) }
        safeLaunch {
            when (val response = io { repository.configuration(id) }) {
                is Response.Success -> applyConfiguration(response.value)

                is Response.Error -> _state.update {
                    it.copy(summary = SummaryLoad.Failed(id, FastMountError.from(response.exception)))
                }
            }
        }
    }

    private fun applyConfiguration(configuration: FastMountConfiguration) {
        val transport = _state.value.fixed?.transport ?: ConnectionTransport.BLE
        val plan = fastMountPlan(configuration, transport)
        _state.update {
            it.copy(
                configuration = configuration,
                plan = plan,
                summary = SummaryLoad.Ready,
                master = it.master.copy(
                    mountMap = if (configuration.mountingMap == null) MountMapState.None else MountMapState.Downloading
                )
            )
        }
        // Карту монтажа качаем заранее: к шагу «Монтаж» она обычно уже готова.
        downloadMap()
    }

    /** «Назад» мастера (системная кнопка и стрелка в шапке) — по таблице правил макета. */
    fun onBack() {
        val current = _state.value
        when (current.screen) {
            WizardScreen.CHECK -> _effect.tryEmit(FastMountEffect.ChooseAnother)
            WizardScreen.FIRMWARE -> onFirmwareBack()
            WizardScreen.VEHICLE -> _state.update { it.copy(screen = WizardScreen.CHECK) }
            WizardScreen.CONFIGURATION -> _state.update { it.copy(screen = WizardScreen.VEHICLE) }
            WizardScreen.SUMMARY -> _state.update { it.copy(screen = WizardScreen.CONFIGURATION) }
            WizardScreen.MASTER -> onMasterBack(current.master)
        }
    }

    private fun onMasterBack(master: MasterState) {
        when (master.screen) {
            MasterScreen.STARTING -> if (!master.starting) {
                _state.update { it.copy(screen = WizardScreen.SUMMARY, master = it.master.copy(startError = null)) }
            }

            MasterScreen.WRITING -> if (!master.busy) showDialog(WizardDialog.EXIT)

            MasterScreen.MOUNT -> showDialog(WizardDialog.EXIT)

            MasterScreen.BYPASS, MasterScreen.CLONE ->
                if (!master.busy) {
                    showDialog(if (master.cloneNeeded == true) WizardDialog.TO_MOUNT_CLONE else WizardDialog.TO_MOUNT)
                }

            MasterScreen.SLAVE, MasterScreen.FEEDBACK, MasterScreen.SYSTEM_CHECK -> previous()

            // Перезагрузка — замок; на «Готово» назад нет.
            MasterScreen.REBOOTING, MasterScreen.DONE -> Unit
        }
    }

    fun onChooseAnother() {
        _effect.tryEmit(FastMountEffect.ChooseAnother)
    }

    fun onOpenAdvancedMount() {
        _effect.tryEmit(FastMountEffect.AdvancedMount)
    }

    fun onExit() {
        _effect.tryEmit(FastMountEffect.Exit)
    }

    // endregion

    // region Диалоги

    private fun showDialog(dialog: WizardDialog) {
        _state.update { it.copy(dialog = dialog) }
    }

    fun onDialogDismiss() {
        _state.update { it.copy(dialog = null) }
    }

    fun onDialogConfirm() {
        val dialog = _state.value.dialog ?: return
        _state.update { it.copy(dialog = null) }
        when (dialog) {
            WizardDialog.EXIT -> exitMount()

            WizardDialog.TO_MOUNT, WizardDialog.TO_MOUNT_CLONE -> {
                _state.update {
                    it.copy(master = it.master.copy(stepAttempt = it.master.stepAttempt + 1, cloneNeeded = null))
                }
                previous()
            }

            WizardDialog.MOUNT_DONE -> nextWithReboot(FastMountStep.MOUNT)

            WizardDialog.SLAVE_DONE -> next()
        }
    }

    /** Выход из незавершённого монтажа: блок — из режима программирования (BLE, по возможности), затем на главную. */
    private fun exitMount() {
        safeLaunch {
            if (_state.value.fixed?.transport == ConnectionTransport.BLE && currentDevice != null) {
                withTimeoutOrNull(EXIT_TIMEOUT_MILLIS) { io { programMode.exit() } }
            }
            _effect.tryEmit(FastMountEffect.Exit)
        }
    }

    // endregion

    // region Шаги SDK

    fun onStart() {
        val configuration = _state.value.configuration ?: return
        if (_state.value.master.starting) return
        _state.update {
            it.copy(screen = WizardScreen.MASTER, master = it.master.copy(starting = true, startError = null))
        }
        safeLaunch {
            when (val response = io { repository.start(configuration.id) }) {
                is Response.Success -> {
                    mountStart = TimeSource.Monotonic.markNow()
                    writeRequested = false
                    _state.update { it.copy(master = it.master.copy(starting = false)) }
                    observeSteps()
                }

                is Response.Error -> _state.update {
                    it.copy(
                        master = it.master.copy(
                            starting = false,
                            startError = FastMountError.from(response.exception)
                        )
                    )
                }
            }
        }
    }

    private fun observeSteps() {
        stepsJob?.cancel()
        stepsJob = safeLaunch {
            repository.steps().collect { response ->
                when (response) {
                    is Response.Success -> onStep(response.value)
                    is Response.Error -> logger.w(response.exception) { "Fast mount state error" }
                }
            }
        }
    }

    private fun onStep(step: FastMountStep) {
        val previous = _state.value.master.step
        if (step == previous) return
        logger.d { "Fast mount step: $previous -> $step" }
        _state.update {
            it.copy(
                master = it.master.copy(
                    step = step,
                    error = null,
                    toast = if (previous == FastMountStep.WRITING_SETTINGS && step != FastMountStep.WRITING_SETTINGS) {
                        WizardToast.SETTINGS_WRITTEN
                    } else {
                        it.master.toast
                    },
                    cloneNeeded = if (step == FastMountStep.CLONE) true else it.master.cloneNeeded,
                    mountMinutes = mountStart?.elapsedNow()?.inWholeMinutes?.toInt() ?: 0
                )
            )
        }
        if (step == FastMountStep.SYSTEM_CHECK) observeQuestions() else stopQuestions()
        // Запись настроек начинается сама: подтверждение было на сводке.
        if (step == FastMountStep.WRITING_SETTINGS && !writeRequested) {
            writeRequested = true
            onWriteRetry()
        }
    }

    fun onWriteRetry() {
        val master = _state.value.master
        if (master.busy || master.step != FastMountStep.WRITING_SETTINGS) return
        _state.update { it.copy(master = it.master.copy(busy = true, error = null)) }
        launchOnDevice { finishCall(io { repository.next() }) }
            ?: _state.update { it.copy(master = it.master.copy(busy = false)) }
    }

    fun onMountDone() = showDialog(WizardDialog.MOUNT_DONE)

    /** «Далее» на итоге обхода (успех или «нужен Clone») — к следующему шагу через перезагрузку блока. */
    fun onBypassNext(cloneRequired: Boolean) {
        _state.update { it.copy(master = it.master.copy(cloneNeeded = cloneRequired)) }
        nextWithReboot(FastMountStep.KEY_BYPASS)
    }

    fun onCloneNext() = nextWithReboot(FastMountStep.CLONE)

    /** Экран обхода/Clone вернул «Назад» (не во время процесса) — «Вернуться к монтажу?». */
    fun onStepScreenBack() = onMasterBack(_state.value.master)

    fun onSlaveDoneChange(done: Boolean) {
        _state.update { it.copy(master = it.master.copy(slaveDone = done)) }
    }

    fun onSlaveNext() {
        if (_state.value.master.slaveDone) showDialog(WizardDialog.SLAVE_DONE)
    }

    fun onRetryStep() {
        when (_state.value.master.screen) {
            MasterScreen.WRITING -> onWriteRetry()
            MasterScreen.FEEDBACK -> onFeedbackSend()
            else -> _state.update { it.copy(master = it.master.copy(error = null)) }
        }
    }

    private fun next() {
        if (_state.value.master.busy) return
        _state.update { it.copy(master = it.master.copy(busy = true, error = null)) }
        safeLaunch { finishCall(io { repository.next() }) }
    }

    private fun previous() {
        if (_state.value.master.busy) return
        _state.update { it.copy(master = it.master.copy(busy = true, error = null)) }
        safeLaunch { finishCall(io { repository.previous() }) }
    }

    private fun finishCall(response: Response<Unit>) {
        _state.update {
            it.copy(
                master = it.master.copy(
                    busy = false,
                    error = (response as? Response.Error)?.let { error -> FastMountError.from(error.exception) }
                )
            )
        }
        if (response is Response.Error) logger.w(response.exception) { "Fast mount transition failed" }
    }

    /**
     * Шаг, после которого SDK перезагружает блок (выход из режима программирования): монтаж, обход, Clone.
     * По BLE экран «Перезагрузка блока…» держится, пока блок не вернётся. Связь не оборвалась за [REBOOT_GRACE_MILLIS]
     * после перехода SDK — блок не перезагружался, идём дальше. Не вернулся за тайм-аут сессии (60 с) — обёртка
     * показывает потерю связи, мастер ждёт возврата блока.
     */
    private fun nextWithReboot(from: FastMountStep) {
        val master = _state.value.master
        if (master.busy) return
        val ble = _state.value.fixed?.transport == ConnectionTransport.BLE
        if (!ble) {
            next()
            return
        }
        deviceSession.expectReboot(DeviceReboot.Restart)
        _state.update {
            it.copy(
                master = it.master.copy(
                    busy = true,
                    error = null,
                    reboot = RebootWait(from)
                )
            )
        }
        rebootJob?.cancel()
        rebootJob = safeLaunch {
            val response = io { repository.next() }
            finishCall(response)
            if (response is Response.Error) {
                deviceSession.cancelReboot()
                _state.update { it.copy(master = it.master.copy(reboot = null)) }
                return@safeLaunch
            }
            awaitReboot()
            _state.update { it.copy(master = it.master.copy(reboot = null)) }
        }
    }

    private suspend fun awaitReboot() {
        // SDK уходит в Waiting* и сам переходит к следующему шагу.
        _state.first { it.master.step != FastMountStep.REBOOTING && it.master.step != it.master.reboot?.from }
        val dropped = withTimeoutOrNull(REBOOT_GRACE_MILLIS) {
            deviceSession.state.first { it.isLinkDown }
        } != null
        if (!dropped) {
            deviceSession.cancelReboot()
            return
        }
        deviceSession.state.map { it is DeviceSessionState.Ready }.first { it }
    }

    // endregion

    // region Карта монтажа

    fun onMapRetry() = downloadMap()

    fun onMapOpen() {
        val bytes = mapBytes ?: return
        val name = _state.value.configuration?.mountingMap?.name?.takeIf { it.isNotBlank() } ?: DEFAULT_MAP_NAME
        _effect.tryEmit(FastMountEffect.OpenFile(name.withPdfExtension(), bytes))
    }

    fun onFileOpenResult(opened: Boolean?) {
        val toast = when (opened) {
            true -> null
            false -> WizardToast.FILE_NO_APP
            null -> WizardToast.FILE_FAILED
        } ?: return
        _state.update { it.copy(master = it.master.copy(toast = toast)) }
    }

    fun onToastShown() {
        _state.update { it.copy(master = it.master.copy(toast = null)) }
    }

    private fun downloadMap() {
        val map = _state.value.configuration?.mountingMap ?: return
        if (mapJob?.isActive == true) return
        _state.update { it.copy(master = it.master.copy(mountMap = MountMapState.Downloading)) }
        mapJob = safeLaunch {
            val state = when (val response = io { repository.download(map.url) }) {
                is Response.Success -> {
                    mapBytes = response.value
                    MountMapState.Ready(response.value.size)
                }

                is Response.Error -> {
                    logger.w(response.exception) { "Mounting map download failed" }
                    MountMapState.Failed
                }
            }
            _state.update { it.copy(master = it.master.copy(mountMap = state)) }
        }
    }

    // endregion

    // region Проверка системы

    private fun observeQuestions() {
        if (questionsJob?.isActive == true) return
        rebuildChecklist()
        questionsJob = safeLaunch {
            repository.questions().collect { response ->
                when (response) {
                    is Response.Success -> onQuestions(response.value)
                    is Response.Error -> logger.w(response.exception) { "Questions error" }
                }
            }
        }
    }

    private fun stopQuestions() {
        questionsJob?.cancel()
        questionsJob = null
    }

    /** Курсор SDK сдвинулся: всё, что до него и не закрыто нами, ответил SDK по телеметрии — «пройдено». */
    private fun onQuestions(questions: QuestionsState) {
        val all = _state.value.configuration?.questions.orEmpty()
        val currentIndex = if (questions.completed) {
            all.size
        } else {
            all.indexOfFirst { it.id == questions.current?.id }.takeIf { it >= 0 } ?: all.size
        }
        _state.update { state ->
            val statuses = state.master.statuses.toMutableMap()
            all.take(currentIndex).forEach { question ->
                if (statuses[question.id] == null) statuses[question.id] = MountTaskStatus.PASSED
            }
            state.copy(
                master = state.master.copy(
                    statuses = statuses,
                    currentTask = all.getOrNull(currentIndex)?.id
                )
            )
        }
        rebuildChecklist()
    }

    private fun rebuildChecklist() {
        val all = _state.value.configuration?.questions.orEmpty()
        _state.update { state ->
            val master = state.master
            val tasks = all.map { question ->
                MountTask(
                    id = question.id,
                    text = question.task.ifBlank { question.expected },
                    status = master.statuses[question.id] ?: MountTaskStatus.PENDING,
                    active = question.id == master.currentTask && master.statuses[question.id] == null,
                    auto = if (question.automatic) AutoTaskKind.of(question.id) else null,
                    options = if (question.automatic) emptyList() else question.answers.map { it.text }
                )
            }
            state.copy(master = master.copy(checklist = MountChecklist(tasks)))
        }
    }

    fun onTaskAnswer(taskId: Int, option: Int) {
        val question = _state.value.configuration?.questions?.firstOrNull { it.id == taskId } ?: return
        val answer = question.answers.getOrNull(option) ?: return
        if (_state.value.master.currentTask != taskId) return
        markTask(taskId, if (answer.passed) MountTaskStatus.PASSED else MountTaskStatus.FAILED)
        safeLaunch { (io { repository.answer(answer) } as? Response.Error)?.let { rollbackTask(taskId, it) } }
    }

    fun onTaskSkip(taskId: Int) {
        if (_state.value.master.currentTask != taskId) return
        markTask(taskId, MountTaskStatus.SKIPPED)
        safeLaunch { (io { repository.skip() } as? Response.Error)?.let { rollbackTask(taskId, it) } }
    }

    private fun markTask(taskId: Int, status: MountTaskStatus) {
        _state.update { it.copy(master = it.master.copy(statuses = it.master.statuses + (taskId to status))) }
        rebuildChecklist()
    }

    private fun rollbackTask(taskId: Int, error: Response.Error) {
        logger.w(error.exception) { "Answer failed" }
        _state.update { it.copy(master = it.master.copy(statuses = it.master.statuses - taskId)) }
        rebuildChecklist()
    }

    /** «Далее» проверки системы (в т.ч. «Завершить» с незакрытыми): оставшиеся пропускаем по курсору SDK. */
    fun onSystemCheckNext() {
        if (_state.value.master.busy) return
        val pending = _state.value.master.checklist.tasks.filter { it.status == MountTaskStatus.PENDING }
        _state.update { it.copy(master = it.master.copy(busy = true, error = null)) }
        pending.forEach { markTask(it.id, MountTaskStatus.SKIPPED) }
        safeLaunch {
            repeat(pending.size) { io { repository.skip() } }
            finishCall(io { repository.next() })
        }
    }

    fun onSystemCheckBack() = previous()

    // endregion

    // region Отзыв

    fun onRatingChange(rating: Int) {
        if (_state.value.master.busy) return
        _state.update { it.copy(master = it.master.copy(rating = rating)) }
    }

    fun onFeedbackChange(text: String) {
        if (_state.value.master.busy) return
        _state.update { it.copy(master = it.master.copy(feedback = text)) }
    }

    fun onFeedbackSend() {
        val master = _state.value.master
        if (master.busy || master.rating == 0 || master.feedback.isBlank()) return
        _state.update { it.copy(master = it.master.copy(busy = true, error = null)) }
        safeLaunch {
            when (val response = io { repository.feedback(master.feedback.trim(), master.rating) }) {
                is Response.Success -> finishCall(io { repository.next() })
                is Response.Error -> finishCall(response)
            }
        }
    }

    // endregion

    override fun onCleared() {
        stepsJob?.cancel()
        questionsJob?.cancel()
        super.onCleared()
    }

    private suspend fun <T> io(block: suspend () -> T): T = withContext(dispatchers.io) { block() }

    private companion object {
        const val EXIT_TIMEOUT_MILLIS = 5_000L
        const val REBOOT_GRACE_MILLIS = 10_000L
        const val DEFAULT_MAP_NAME = "mounting-map"
    }
}

private fun String.withPdfExtension(): String = if (endsWith(".pdf", ignoreCase = true)) this else "$this.pdf"

private fun AlarmDevice.toUnitInfo(): UnitInfo = UnitInfo(
    firmware = displayFirmwareVersion,
    canCode = can?.code,
    canVersion = can?.firmwareVersion,
    canFactory = can?.isFactory == true
)
