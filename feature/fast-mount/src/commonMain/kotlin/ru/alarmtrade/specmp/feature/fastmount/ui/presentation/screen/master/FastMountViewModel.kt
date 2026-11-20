package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import kotlin.time.TimeSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isLinkDown
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.ProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountFile
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountTaskStatus
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionsState
import ru.alarmtrade.specmp.feature.fastmount.domain.model.fastMountPlan
import ru.alarmtrade.specmp.feature.fastmount.domain.model.isNewerFirmwareThan
import ru.alarmtrade.specmp.feature.fastmount.domain.repository.FastMountPassportRepository
import ru.alarmtrade.specmp.feature.fastmount.domain.repository.FastMountRepository
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.model.FastMountError

/** Поток вопросов SDK собирается ровно одним подписчиком — здесь, пока идёт проверка системы. */
class FastMountViewModel(
    deviceSession: DeviceSession,
    private val repository: FastMountRepository,
    private val passports: FastMountPassportRepository,
    private val programMode: ProgramModeRepository,
    private val dispatchers: AppDispatchers,
) : BaseDeviceViewModel<FastMountState, FastMountEffect>(deviceSession, FastMountState()) {

    override suspend fun IntentScope.onStart() {
        onEachDeviceReady(::onDeviceReady)
    }

    private suspend fun onDeviceReady(device: AlarmDevice) = subIntent {
        val fixed = currentState.fixed ?: FixedDevice(
            systemId = device.systemId,
            systemName = device.systemName,
            title = device.title,
            transport = deviceSession.transport.value,
        ).also { fixed -> reduce { copy(fixed = fixed) } }
        if (device.systemId != fixed.systemId) {
            logger.d { "Another unit connected: ${device.systemId}, expected ${fixed.systemId}" }
            reduce { copy(wrongDevice = device.title) }
            return@subIntent
        }
        reduce { copy(wrongDevice = null, unit = device.toUnitInfo()) }
        val current = currentState
        // Проверяем на экране проверки блока; в мастере и на следующих экранах переподключение ничего не меняет.
        if (current.screen == WizardScreen.CHECK && current.check !is UnitCheck.Unsupported) checkUnit(device)
    }

    fun onRetryCheck() {
        val device = currentDevice ?: return
        launchOnDevice(UNIT_CHECK) { checkUnit(device) }
    }

    private suspend fun checkUnit(device: AlarmDevice) = subIntent {
        reduce { copy(check = UnitCheck.Checking) }
        val check = runCheck(device)
        logger.d { "Unit ${device.systemName} check: $check" }
        reduce { copy(check = check) }
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
        // Единая CAN-прошивка — последняя версия под CAN-код блока (заводские настройки отсечены выше).
        val can = device.can?.takeIf { it.code.isNotBlank() }
        val latest = (io { repository.latestFirmware(device.systemName, can?.code, can?.version) } as? Response.Success)
            ?.value
        return if (latest != null && latest.isNewerFirmwareThan(device.firmwareVersion)) {
            UnitCheck.Done(UnitVerdict.Outdated(latest))
        } else {
            UnitCheck.Done(UnitVerdict.Ready)
        }
    }

    fun onOpenFirmwareUpdate() {
        intent { reduce { copy(screen = WizardScreen.FIRMWARE) } }
    }

    fun onFirmwareBack() {
        intent {
            reduce { copy(screen = WizardScreen.CHECK) }
            onRetryCheck()
        }
    }

    fun onCheckNext() {
        intent {
            val check = currentState.check
            if (check !is UnitCheck.Done || check.verdict.blocking) return@intent
            reduce { copy(screen = WizardScreen.VEHICLE) }
        }
    }

    fun onCarPicked(car: MountCar) {
        intent { reduce { copy(car = car, screen = WizardScreen.CONFIGURATION) } }
    }

    fun onConfigurationPicked(id: Int) {
        intent {
            reduce { copy(screen = WizardScreen.SUMMARY) }
            loadConfiguration(id)
        }
    }

    fun onRetrySummary() {
        (currentState.summary as? SummaryLoad.Failed)?.let { loadConfiguration(it.configurationId) }
    }

    private fun loadConfiguration(id: Int) {
        intent {
            reduce { copy(summary = SummaryLoad.Loading) }
            when (val response = io { repository.configuration(id) }) {
                is Response.Success -> applyConfiguration(response.value)

                is Response.Error -> reduce {
                    copy(summary = SummaryLoad.Failed(id, FastMountError.from(response.exception)))
                }
            }
        }
    }

    private suspend fun applyConfiguration(configuration: FastMountConfiguration) = subIntent {
        val plan = fastMountPlan(configuration, currentState.fixed?.transport ?: ConnectionTransport.BLE)
        val map = configuration.mountingMap
        reduce {
            copy(
                configuration = configuration,
                plan = plan,
                summary = SummaryLoad.Ready,
                master = master.copy(mountMap = if (map == null) MountMapState.None else MountMapState.Downloading),
            )
        }
        // Карту монтажа качаем заранее: к шагу «Монтаж» она обычно уже готова.
        if (map == null) cancelLaunched(MAP) else downloadMap(map)
    }

    fun onBack() {
        intent {
            val current = currentState
            when (current.screen) {
                WizardScreen.CHECK -> postSideEffect(FastMountEffect.ChooseAnother)
                WizardScreen.FIRMWARE -> onFirmwareBack()
                WizardScreen.VEHICLE -> reduce { copy(screen = WizardScreen.CHECK) }
                WizardScreen.CONFIGURATION -> reduce { copy(screen = WizardScreen.VEHICLE) }
                WizardScreen.SUMMARY -> reduce { copy(screen = WizardScreen.CONFIGURATION) }
                WizardScreen.MASTER -> onMasterBack(current.master)
            }
        }
    }

    private suspend fun onMasterBack(master: MasterState) = subIntent {
        when (master.screen) {
            MasterScreen.STARTING -> if (!master.starting) {
                reduce { copy(screen = WizardScreen.SUMMARY, master = this.master.copy(startError = null)) }
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
        intent { postSideEffect(FastMountEffect.ChooseAnother) }
    }

    fun onOpenAdvancedMount() {
        intent { postSideEffect(FastMountEffect.AdvancedMount) }
    }

    /** С финала без паспорта итог мастера остаётся заготовкой: паспорт можно дооформить позже. */
    fun onExit() {
        intent {
            val master = currentState.master
            val result = master.result
            if (master.screen != MasterScreen.DONE || master.passport.passportId != null || result == null) {
                postSideEffect(FastMountEffect.Exit)
                return@intent
            }
            executeWithProceedLock {
                withTimeoutOrNull(BLANK_TIMEOUT_MILLIS) { io { passports.createBlank(result) } }
                    ?.onError { logger.w(it) { "Mount passport blank was not saved" } }
                postSideEffect(FastMountEffect.Exit)
            }
        }
    }

    private suspend fun showDialog(dialog: WizardDialog) = subIntent {
        reduce { copy(dialog = dialog) }
    }

    fun onDialogDismiss() {
        intent { reduce { copy(dialog = null) } }
    }

    fun onDialogConfirm() {
        intent {
            val dialog = currentState.dialog ?: return@intent
            reduce { copy(dialog = null) }
            when (dialog) {
                WizardDialog.EXIT -> exitMount()

                WizardDialog.TO_MOUNT, WizardDialog.TO_MOUNT_CLONE -> {
                    reduce { copy(master = master.copy(stepAttempt = master.stepAttempt + 1, cloneNeeded = null)) }
                    previous()
                }

                WizardDialog.MOUNT_DONE -> nextWithReboot(FastMountStep.MOUNT)

                WizardDialog.SLAVE_DONE -> next()
            }
        }
    }

    private fun exitMount() {
        intent {
            if (currentState.fixed?.transport == ConnectionTransport.BLE && currentDevice != null) {
                withTimeoutOrNull(EXIT_TIMEOUT_MILLIS) {
                    io { programMode.exit() }.onError { logger.w(it) { "Program mode exit failed" } }
                }
            }
            postSideEffect(FastMountEffect.Exit)
        }
    }

    fun onStartMount() {
        val configuration = currentState.configuration ?: return
        intent { executeWithProceedLock { start(configuration.id) } }
    }

    private suspend fun start(configurationId: Int) = subIntent {
        reduce { copy(screen = WizardScreen.MASTER, master = master.copy(starting = true, startError = null)) }
        when (val response = io { repository.start(configurationId) }) {
            is Response.Success -> {
                reduce { copy(master = master.copy(starting = false, startedAt = TimeSource.Monotonic.markNow())) }
                launchReplacing(STEPS) { collectSteps() }
            }

            is Response.Error -> reduce {
                copy(master = master.copy(starting = false, startError = FastMountError.from(response.exception)))
            }
        }
    }

    private suspend fun collectSteps() = subIntent {
        repository.steps().flowOn(dispatchers.io).collect { response ->
            when (response) {
                is Response.Success -> onStep(response.value)
                is Response.Error -> logger.w(response.exception) { "Fast mount state error" }
            }
        }
    }

    private suspend fun onStep(step: FastMountStep) = subIntent {
        val previous = currentState.master.step
        if (step == previous) return@subIntent
        logger.d { "Fast mount step: $previous -> $step" }
        val systemCheck = step == FastMountStep.SYSTEM_CHECK
        reduce {
            copy(master = master.withStep(step))
                .let { if (systemCheck) it.withChecklist() else it }
                .let {
                    if (step ==
                        FastMountStep.FINISH
                    ) {
                        it.copy(master = it.master.copy(result = it.fastMountResult()))
                    } else {
                        it
                    }
                }
        }
        if (systemCheck) launchReplacing(QUESTIONS) { collectQuestions() } else cancelLaunched(QUESTIONS)
        // Запись настроек начинается сама — на первом шаге после старта: подтверждение было на сводке.
        if (previous == null && step == FastMountStep.WRITING_SETTINGS) onWriteRetry()
    }

    fun onWriteRetry() {
        if (currentState.master.step != FastMountStep.WRITING_SETTINGS) return
        launchOnDevice { transition { io { repository.next() } } }
    }

    fun onMountDone() {
        intent { showDialog(WizardDialog.MOUNT_DONE) }
    }

    fun onBypassNext(cloneRequired: Boolean) {
        intent {
            reduce { copy(master = master.copy(cloneNeeded = cloneRequired)) }
            nextWithReboot(FastMountStep.KEY_BYPASS)
        }
    }

    fun onCloneNext() {
        nextWithReboot(FastMountStep.CLONE)
    }

    fun onStepScreenBack() {
        intent { onMasterBack(currentState.master) }
    }

    fun onSlaveDoneChange(done: Boolean) {
        intent { reduce { copy(master = master.copy(slaveDone = done)) } }
    }

    fun onSlaveNext() {
        intent { if (currentState.master.slaveDone) showDialog(WizardDialog.SLAVE_DONE) }
    }

    fun onRetryStep() {
        intent {
            when (currentState.master.screen) {
                MasterScreen.WRITING -> onWriteRetry()
                MasterScreen.FEEDBACK -> onFeedbackSend()
                else -> reduce { copy(master = master.copy(error = null)) }
            }
        }
    }

    private fun next() {
        intent { transition { io { repository.next() } } }
    }

    private fun previous() {
        intent { transition { io { repository.previous() } } }
    }

    /** Переход SDK под общим замком VM: один за раз, повторные нажатия во время перехода пропускаются. */
    private suspend fun transition(call: suspend () -> Response<Unit>) = subIntent {
        executeWithProceedLock { runTransition(call) }
    }

    private suspend fun runTransition(call: suspend () -> Response<Unit>): Boolean = subIntent {
        reduce { copy(master = master.copy(busy = true, error = null)) }
        var response: Response<Unit>? = null
        try {
            response = call()
        } finally {
            finishCall(response)
        }
        return@subIntent response is Response.Success
    }

    private suspend fun finishCall(response: Response<Unit>?) = subIntent {
        val error = response as? Response.Error
        reduce { copy(master = master.copy(busy = false, error = error?.let { FastMountError.from(it.exception) })) }
        if (error != null) logger.w(error.exception) { "Fast mount transition failed" }
    }

    /**
     * Связь не оборвалась за [REBOOT_GRACE_MILLIS] после перехода SDK — блок не перезагружался, идём дальше. Не
     * вернулся за тайм-аут сессии — обёртка показывает потерю связи, мастер ждёт возврата блока.
     */
    private fun nextWithReboot(from: FastMountStep) {
        if (currentState.fixed?.transport != ConnectionTransport.BLE) {
            next()
            return
        }
        // Замок держится до возврата блока: во время перезагрузки других переходов нет.
        intent { executeWithProceedLock { rebootTransition(from) } }
    }

    private suspend fun rebootTransition(from: FastMountStep) = subIntent {
        deviceSession.expectReboot(DeviceReboot.Restart)
        reduce { copy(master = master.copy(reboot = RebootWait(from))) }
        try {
            if (runTransition { io { repository.next() } }) {
                awaitReboot()
            } else {
                deviceSession.cancelReboot()
            }
        } finally {
            reduce { copy(master = master.copy(reboot = null)) }
        }
    }

    private suspend fun awaitReboot() {
        // SDK уходит в Waiting* и сам переходит к следующему шагу.
        select { master }.first { it.step != FastMountStep.REBOOTING && it.step != it.reboot?.from }
        val dropped = withTimeoutOrNull(REBOOT_GRACE_MILLIS) {
            deviceSession.state.first { it.isLinkDown }
        } != null
        if (!dropped) {
            deviceSession.cancelReboot()
            return
        }
        deviceSession.state.map { it is DeviceSessionState.Ready }.first { it }
    }

    fun onMapRetry() {
        intent {
            val map = currentState.configuration?.mountingMap ?: return@intent
            if (currentState.master.mountMap == MountMapState.Downloading) return@intent
            reduce { copy(master = master.copy(mountMap = MountMapState.Downloading)) }
            downloadMap(map)
        }
    }

    fun onMapOpen() {
        intent {
            val bytes = (currentState.master.mountMap as? MountMapState.Ready)?.bytes ?: return@intent
            val name = currentState.configuration?.mountingMap?.name?.takeIf { it.isNotBlank() } ?: DEFAULT_MAP_NAME
            postSideEffect(FastMountEffect.OpenFile(name.withPdfExtension(), bytes))
        }
    }

    fun onFileOpenResult(opened: Boolean?) {
        intent {
            val toast = when (opened) {
                true -> null
                false -> WizardToast.FILE_NO_APP
                null -> WizardToast.FILE_FAILED
            } ?: return@intent
            reduce { copy(master = master.copy(toast = toast)) }
        }
    }

    fun onToastShown() {
        intent { reduce { copy(master = master.copy(toast = null)) } }
    }

    // Новая конфигурация заменяет загрузку карты прежней.
    private fun downloadMap(map: MountFile) {
        launchReplacing(MAP) {
            val mountMap = when (val response = io { repository.download(map.url) }) {
                is Response.Success -> MountMapState.Ready(response.value)

                is Response.Error -> {
                    logger.w(response.exception) { "Mounting map download failed" }
                    MountMapState.Failed
                }
            }
            reduce { copy(master = master.copy(mountMap = mountMap)) }
        }
    }

    private suspend fun collectQuestions() = subIntent {
        repository.questions().flowOn(dispatchers.io).collect { response ->
            when (response) {
                is Response.Success -> onQuestions(response.value)
                is Response.Error -> logger.w(response.exception) { "Questions error" }
            }
        }
    }

    private suspend fun onQuestions(questions: QuestionsState) = subIntent {
        reduce { withQuestions(questions) }
    }

    fun onTaskAnswer(taskId: Int, option: Int) {
        val question = currentState.configuration?.questions?.firstOrNull { it.id == taskId } ?: return
        val answer = question.answers.getOrNull(option) ?: return
        val status = if (answer.passed) MountTaskStatus.PASSED else MountTaskStatus.FAILED
        closeTask(taskId, status) { repository.answer(answer) }
    }

    fun onTaskSkip(taskId: Int) {
        closeTask(taskId, MountTaskStatus.SKIPPED) { repository.skip() }
    }

    /** Закрыть открытое задание (курсор SDK на нём, ещё без статуса): двойное нажатие не отправит ответ дважды. */
    private fun closeTask(taskId: Int, status: MountTaskStatus, call: suspend () -> Response<Unit>) {
        intent {
            executeWithProceedLock {
                val master = currentState.master
                if (master.currentTask != taskId || master.statuses[taskId] != null) return@executeWithProceedLock
                reduce { withTaskStatus(taskId, status) }
                (io { call() } as? Response.Error)?.let { error ->
                    logger.w(error.exception) { "Answer failed" }
                    reduce { withTaskStatus(taskId, null) }
                }
            }
        }
    }

    fun onSystemCheckNext() {
        intent {
            transition {
                val pending = currentState.master.checklist.tasks.filter { it.status == MountTaskStatus.PENDING }
                reduce { pending.fold(this) { state, task -> state.withTaskStatus(task.id, MountTaskStatus.SKIPPED) } }
                repeat(pending.size) {
                    io { repository.skip() }.onError { e -> logger.w(e) { "Checklist task skip failed" } }
                }
                io { repository.next() }
            }
        }
    }

    fun onSystemCheckBack() {
        previous()
    }

    fun onRatingChange(rating: Int) {
        intent {
            if (currentState.master.busy) return@intent
            reduce { copy(master = master.copy(rating = rating)) }
        }
    }

    fun onFeedbackChange(text: String) {
        intent {
            if (currentState.master.busy) return@intent
            reduce { copy(master = master.copy(feedback = text)) }
        }
    }

    fun onFeedbackSend() {
        intent {
            val master = currentState.master
            if (master.rating == 0 || master.feedback.isBlank()) return@intent
            transition {
                when (val response = io { repository.feedback(master.feedback.trim(), master.rating) }) {
                    is Response.Success -> io { repository.next() }
                    is Response.Error -> response
                }
            }
        }
    }

    /** «Оформить паспорт»: считывание блока; паспорт уже считан — его предотчёт. */
    fun onPassportStart() {
        intent {
            val flow = currentState.master.passport
            val next = if (flow.passportId != null) {
                flow.copy(screen = PassportScreen.EDITOR)
            } else {
                flow.copy(screen = PassportScreen.READING, attempt = flow.attempt + 1)
            }
            reduce { copy(master = master.copy(passport = next)) }
        }
    }

    fun onPassportRead(passportId: String) {
        intent {
            reduce {
                copy(
                    master = master.copy(
                        passport = master.passport.copy(screen = PassportScreen.EDITOR, passportId = passportId),
                    ),
                )
            }
            launchReplacing(PASSPORT) {
                passports.observe(passportId).flowOn(dispatchers.io).collect { response ->
                    if (response is Response.Success) {
                        reduce {
                            copy(master = master.copy(passport = master.passport.copy(passport = response.value)))
                        }
                    }
                }
            }
        }
    }

    fun onPassportCancel() {
        closePassport()
    }

    fun onPassportClose() {
        closePassport()
    }

    private fun closePassport() {
        intent { reduce { copy(master = master.copy(passport = master.passport.copy(screen = null))) } }
    }

    private suspend fun <T> io(block: suspend () -> T): T = withContext(dispatchers.io) { block() }

    private companion object {
        const val UNIT_CHECK = "unitCheck"
        const val STEPS = "steps"
        const val QUESTIONS = "questions"
        const val MAP = "map"
        const val PASSPORT = "passport"
        const val BLANK_TIMEOUT_MILLIS = 3_000L
        const val EXIT_TIMEOUT_MILLIS = 5_000L
        const val REBOOT_GRACE_MILLIS = 10_000L
        const val DEFAULT_MAP_NAME = "mounting-map"
    }
}
