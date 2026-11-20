package ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen

import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.speech.HandsFreeSettings
import ru.alarmtrade.specmp.core.speech.Speaker
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.ProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntryResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.follow
import ru.alarmtrade.specmp.feature.device.state.domain.model.CommandReply
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.repository.DeviceStateRepository

/** Опрос — в `repeatOnSubscription`: экран в фоне блок не опрашивает; потеря связи отменяет опрос. */
@OptIn(ExperimentalCoroutinesApi::class)
class CheckStateViewModel(
    deviceSession: DeviceSession,
    private val repository: DeviceStateRepository,
    private val programMode: ProgramModeEntry,
    private val programModeRepository: ProgramModeRepository,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
    private val speaker: Speaker,
    private val handsFree: HandsFreeSettings,
    private val fastMount: Boolean,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) : BaseDeviceViewModel<CheckStateState, CheckStateEffect>(
    deviceSession,
    CheckStateState(fastMount = fastMount, commands = commandsFor(null, fastMount)),
) {

    override suspend fun IntentScope.onStart() {
        onEachDeviceReady(::onDeviceReady)
        onDeviceLost(::onConnectionLost)
        intent { repeatOnSubscription { poll() } }
        select { lastSnapshot }
            .flatMapLatest { mark -> if (mark == null) flowOf(null) else staleSecondsSince(mark) }
            .distinctUntilChanged()
            .collectIntoState { stale ->
                copy(staleSeconds = stale.takeIf { vehicle != null && currentDevice != null })
            }
        pinCooldown.lockedFor(select { deviceId.takeIf { dialog is CheckStateDialog.Pin } })
            .collectIntoState { lock -> withPinLock(lock) }
        // В быстром монтаже говорит мастер (чек-лист) — один голос.
        if (!fastMount) intent { repeatOnSubscription { speakZones() } }
    }

    override fun onCleared() {
        if (!fastMount) speaker.stop()
        super.onCleared()
    }

    private suspend fun speakZones() = speaker.follow(
        enabled = handsFree.enabled,
        states = select { vehicle?.zoneValues() }.filterNotNull().settled(ZONE_SPEECH_WINDOW),
        phrase = ::zonesPhrase,
    )

    private suspend fun poll() = subIntent {
        connectedDevice
            .flatMapLatest { device ->
                if (device == null) {
                    emptyFlow()
                } else {
                    repository.states().flowOn(dispatchers.io)
                        .onStart { reduce { copy(lastSnapshot = timeSource.markNow()) } }
                }
            }
            .collect { onResponse(it) }
    }

    private fun staleSecondsSince(mark: TimeMark): Flow<Int?> = flow {
        while (currentCoroutineContext().isActive) {
            val elapsed = mark.elapsedNow()
            emit(elapsed.inWholeSeconds.toInt().takeIf { elapsed >= STALE_AFTER })
            delay(TICK)
        }
    }

    private suspend fun onDeviceReady(device: AlarmDevice) = subIntent {
        reduce { copy(deviceName = device.systemName, deviceId = device.systemId) }
    }

    // Работа с блоком уже отменена (launchOnDevice); кнопки в отправке — «Нет связи», PIN закрыт.
    private suspend fun onConnectionLost() = subIntent {
        reduce {
            val lost = pending?.let { it to CommandStatus.Finished(CommandOutcome.NO_LINK, resultSeq + 1) }
            copy(
                pending = null,
                statuses = if (lost != null) statuses + lost else statuses,
                resultSeq = lost?.second?.seq ?: resultSeq,
                staleSeconds = null,
                dialog = dialog.takeUnless { it is CheckStateDialog.Pin },
            )
        }
    }

    private suspend fun onResponse(response: Response<VehicleState>) = subIntent {
        // Ошибка одного опроса не обрывает экран: остаётся последний снимок, через порог — «Нет данных N с».
        if (response !is Response.Success) return@subIntent
        val vehicle = response.value
        reduce {
            val previous = this.vehicle
            copy(
                vehicle = vehicle,
                lastSnapshot = timeSource.markNow(),
                tick = tick + 1,
                changedZones = if (previous == null) emptySet() else changedZones(previous, vehicle),
                guardChanged = previous != null && previous.security != vehicle.security,
                commands = commandsFor(vehicle, fastMount),
            )
        }
        if (fastMount && vehicle.programMode) exitProgramModeOnce()
    }

    // Быстрый монтаж: блок в режиме программирования не охраняет — выводим его оттуда один раз.
    private suspend fun exitProgramModeOnce() = subIntent {
        if (currentState.programExiting) return@subIntent
        reduce { copy(programExiting = true) }
        launchOnDevice {
            withContext(dispatchers.io) {
                programModeRepository.exit().onError { logger.w(it) { "Program mode exit failed" } }
            }
        }
    }

    fun onCommand(command: StateCommand) {
        intent {
            val state = currentState
            if (state.pending != null || state.loading) return@intent
            if (state.commands.any { it.command == command && it.danger }) {
                reduce { copy(dialog = CheckStateDialog.Confirm(command)) }
            } else {
                send(command)
            }
        }
    }

    fun onConfirm() {
        intent {
            val dialog = currentState.dialog as? CheckStateDialog.Confirm ?: return@intent
            reduce { copy(dialog = null) }
            send(dialog.command)
        }
    }

    /** «Повторить» в снэкбаре — без повторного подтверждения: его уже дали. */
    fun onRetry(command: StateCommand) {
        if (currentState.pending != null) return
        send(command)
    }

    fun onPinSubmit(pin: String) {
        val dialog = currentState.dialog as? CheckStateDialog.Pin ?: return
        // PIN уже отправляется — повторный тап не запускает второй вход в режим программирования.
        if (dialog.pin.sending || currentState.pending != null) return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        enterProgramMode(servicePin)
    }

    fun onDialogDismiss() {
        intent {
            reduce {
                val sending = (dialog as? CheckStateDialog.Pin)?.pin?.sending == true
                if (sending) this else copy(dialog = null)
            }
        }
    }

    fun onBack() {
        intent {
            val vehicle = currentState.vehicle
            val dialog = when {
                fastMount -> CheckStateDialog.FastBack

                vehicle != null && (vehicle.serviceMode || vehicle.programMode) ->
                    CheckStateDialog.Exit(service = vehicle.serviceMode, program = vehicle.programMode)

                else -> null
            }
            if (dialog == null) {
                postSideEffect(CheckStateEffect.Close)
            } else {
                reduce { copy(dialog = dialog) }
            }
        }
    }

    /** Уходим, даже если блок не ответил. */
    fun onExitTurnOff() {
        intent {
            val dialog = currentState.dialog as? CheckStateDialog.Exit ?: return@intent
            reduce { copy(dialog = null) }
            val job = launchOnDevice {
                // Уходим и когда связь оборвалась посреди команд (работу с блоком отменяет обрыв).
                try {
                    withContext(dispatchers.io) {
                        if (dialog.service) {
                            withTimeoutOrNull(COMMAND_TIMEOUT) {
                                repository.send(StateCommand.SERVICE_MODE_OFF)
                                    .onError { logger.w(it) { "Service mode was not turned off on exit" } }
                            }
                        }
                        if (dialog.program) {
                            withTimeoutOrNull(COMMAND_TIMEOUT) {
                                programModeRepository.exit()
                                    .onError { logger.w(it) { "Program mode was not turned off on exit" } }
                            }
                        }
                    }
                } finally {
                    postSideEffect(CheckStateEffect.Close)
                }
            }
            if (job == null) postSideEffect(CheckStateEffect.Close)
        }
    }

    fun onNext(notDone: Int, total: Int) {
        intent {
            if (notDone > 0) {
                reduce { copy(dialog = CheckStateDialog.FastFinish(notDone, total)) }
            } else {
                postSideEffect(CheckStateEffect.Next)
            }
        }
    }

    fun onFinishConfirm() {
        intent {
            reduce { copy(dialog = null) }
            postSideEffect(CheckStateEffect.Next)
        }
    }

    private fun send(command: StateCommand) {
        when (command) {
            StateCommand.PROGRAM_MODE_ON -> enterProgramMode(pin = null)
            StateCommand.PROGRAM_MODE_OFF -> execute(command) { programModeRepository.exit().toReply() }
            else -> execute(command) { repository.send(command) }
        }
    }

    // Команда с итогом на кнопке и в снэкбаре. Обрыв связи во время отправки — onConnectionLost.
    private fun execute(command: StateCommand, block: suspend () -> Response<CommandReply>) {
        val job = launchOnDevice {
            reduce { copy(pending = command, statuses = statuses + (command to CommandStatus.Sending)) }
            val reply = withTimeoutOrNull(COMMAND_TIMEOUT) { withContext(dispatchers.io) { block() } }
            finish(command, reply.toOutcome())
        }
        if (job == null) finish(command, CommandOutcome.NO_LINK)
    }

    private fun enterProgramMode(pin: ServicePin?) {
        val systemId = currentState.deviceId ?: return
        val command = StateCommand.PROGRAM_MODE_ON
        val job = launchOnDevice {
            val dialog = currentState.dialog as? CheckStateDialog.Pin
            reduce {
                copy(
                    pending = command,
                    statuses = statuses + (command to CommandStatus.Sending),
                    dialog = dialog?.let { pinDialog -> pinDialog.copy(pin = pinDialog.pin.copy(sending = true)) },
                )
            }
            when (withContext(dispatchers.io) { programMode.enter(systemId, pin) }) {
                ProgramModeEntryResult.ENTERED -> finish(command, CommandOutcome.DONE)

                ProgramModeEntryResult.PIN_LOCKED -> showPin(dialog, wrong = false)

                // Неверный — только введённый PIN; молчаливая попытка SDK (сохранённый или 1111) — просто запрос PIN.
                ProgramModeEntryResult.WRONG_PIN -> showPin(dialog, wrong = pin != null)

                ProgramModeEntryResult.FAILED -> finish(command, CommandOutcome.NO_REPLY)
            }
        }
        if (job == null) finish(command, CommandOutcome.NO_LINK)
    }

    private suspend fun showPin(previous: CheckStateDialog.Pin?, wrong: Boolean) = subIntent {
        val count = (previous?.pin?.wrongCount ?: 0) + if (wrong) 1 else 0
        reduce {
            // Отсчёт блокировки приходит отдельным потоком по блоку — новый диалог не должен его потерять.
            val lockedFor = (dialog as? CheckStateDialog.Pin)?.pin?.lockedFor
            copy(
                pending = null,
                statuses = statuses - StateCommand.PROGRAM_MODE_ON,
                dialog = CheckStateDialog.Pin(
                    ServicePinDialogState(wrong = wrong, wrongCount = count, lockedFor = lockedFor),
                ),
            )
        }
    }

    private fun finish(command: StateCommand, outcome: CommandOutcome) {
        intent {
            val status = CommandStatus.Finished(outcome, currentState.resultSeq + 1)
            reduce {
                copy(
                    pending = null,
                    statuses = statuses + (command to status),
                    resultSeq = status.seq,
                    dialog = dialog.takeUnless { it is CheckStateDialog.Pin },
                )
            }
            postSideEffect(CheckStateEffect.CommandFinished(command, outcome))
            // Итог на кнопке — несколько секунд, потом кнопка возвращается в покой (если итог не сменился новым).
            delay(RESULT_VISIBLE)
            reduce { if (statuses[command] == status) copy(statuses = statuses - command) else this }
        }
    }

    private fun Response<CommandReply>?.toOutcome(): CommandOutcome = when (this) {
        null -> CommandOutcome.NO_REPLY
        is Response.Success -> if (value == CommandReply.DONE) CommandOutcome.DONE else CommandOutcome.REJECTED
        is Response.Error -> if (currentDevice == null) CommandOutcome.NO_LINK else CommandOutcome.NO_REPLY
    }

    private fun Response<Unit>.toReply(): Response<CommandReply> = when (this) {
        is Response.Success -> Response.Success(CommandReply.DONE)
        is Response.Error -> this
    }

    private companion object {
        val STALE_AFTER = 10.seconds
        val TICK = 1.seconds
        val COMMAND_TIMEOUT = 15.seconds
        val RESULT_VISIBLE = 3.seconds

        // Изменения зон в пределах окна звучат одной фразой; открыли и сразу закрыли — молчим.
        val ZONE_SPEECH_WINDOW = 2.seconds
    }
}

private fun changedZones(previous: VehicleState, current: VehicleState): Set<Zone> =
    Zone.entries.filterTo(mutableSetOf()) { previous.zone(it) != current.zone(it) }
