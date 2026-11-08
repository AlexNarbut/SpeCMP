package ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen

import androidx.lifecycle.viewModelScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.ProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntryResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.pin.ServicePinDialogState
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel.LegacyBaseDeviceViewModel
import ru.alarmtrade.specmp.feature.device.state.domain.model.CommandReply
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.repository.DeviceStateRepository

/**
 * Экран «Проверка состояния»: живое состояние блока (снимок раз в 3 с, только BLE) и команды.
 *
 * Опрос блока идёт, пока экран подписан на [state] и блок на связи: поток [DeviceStateRepository.states] вплетён в
 * цепочку state (`WhileSubscribed`) — экран в фоне или под другим экраном блок не опрашивает; потеря связи
 * (`connectedDevice` → `null`) отменяет опрос, возврат блока запускает его заново.
 *
 * @param fastMount экран — шаг быстрого монтажа: без режима программирования (блок в нём выводится автоматически),
 *   «Назад» и «Далее» — с подтверждением.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CheckStateViewModel(
    deviceSession: DeviceSession,
    private val repository: DeviceStateRepository,
    private val programMode: ProgramModeEntry,
    private val programModeRepository: ProgramModeRepository,
    pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
    private val fastMount: Boolean,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) : LegacyBaseDeviceViewModel(deviceSession) {

    private val _state =
        MutableStateFlow(CheckStateState(fastMount = fastMount, commands = commandsFor(null, fastMount)))

    // Время последнего снимка (или начала опроса): от него считается «Нет данных N с».
    private val lastSnapshot = MutableStateFlow<TimeMark?>(null)

    // Номер итога команды: одинаковые итоги подряд различимы — таймер старого не снимет новый.
    private val resultIds = MutableStateFlow(0)

    private val polling: Flow<Unit> = connectedDevice
        .flatMapLatest { device ->
            if (device == null) {
                emptyFlow()
            } else {
                repository.states()
                    .flowOn(dispatchers.io)
                    .onStart { lastSnapshot.update { timeSource.markNow() } }
                    .onEach(::onResponse)
                    .map { }
            }
        }
        .onStart { emit(Unit) }

    private val staleSeconds: Flow<Int?> = lastSnapshot
        .flatMapLatest { mark ->
            if (mark == null) {
                flowOf<Int?>(null)
            } else {
                flow<Int?> {
                    while (true) {
                        val elapsed = mark.elapsedNow()
                        emit(elapsed.inWholeSeconds.toInt().takeIf { elapsed >= STALE_AFTER })
                        delay(TICK)
                    }
                }
            }
        }
        .distinctUntilChanged()

    private val pinLock: Flow<Duration?> =
        pinCooldown.lockedFor(_state.map { state -> state.deviceId.takeIf { state.dialog is CheckStateDialog.Pin } })

    val state: StateFlow<CheckStateState> = combine(_state, staleSeconds, pinLock, polling) { state, stale, lock, _ ->
        val dialog = state.dialog
        val withLock = (dialog as? CheckStateDialog.Pin)?.let { it.copy(pin = it.pin.copy(lockedFor = lock)) }
        state.copy(
            staleSeconds = stale.takeIf { state.vehicle != null && currentDevice != null },
            dialog = withLock ?: dialog,
        )
    }
        .launchOnFirstStart {
            onEachDeviceReady(::onDeviceReady)
            onDeviceLost(::onConnectionLost)
        }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), _state.value)

    private val _effect = sideEffectFlow<CheckStateEffect>()
    val effect: SharedFlow<CheckStateEffect> = _effect.asSharedFlow()

    private fun onDeviceReady(device: AlarmDevice) {
        _state.update { it.copy(deviceName = device.systemName, deviceId = device.systemId) }
    }

    // Работа с блоком уже отменена (launchOnDevice); кнопки в отправке — «Нет связи», PIN закрыт.
    private fun onConnectionLost() {
        val lost = _state.value.pending?.let { it to finished(CommandOutcome.NO_LINK) }
        _state.update { state ->
            state.copy(
                pending = null,
                statuses = if (lost != null && state.pending == lost.first) state.statuses + lost else state.statuses,
                dialog = state.dialog.takeUnless { it is CheckStateDialog.Pin },
            )
        }
    }

    private fun onResponse(response: Response<VehicleState>) {
        // Ошибка одного опроса не обрывает экран: остаётся последний снимок, через порог — «Нет данных N с».
        if (response !is Response.Success) return
        val vehicle = response.value
        lastSnapshot.update { timeSource.markNow() }
        _state.update { state ->
            val previous = state.vehicle
            state.copy(
                vehicle = vehicle,
                tick = state.tick + 1,
                changedZones = if (previous == null) emptySet() else changedZones(previous, vehicle),
                guardChanged = previous != null && previous.security != vehicle.security,
                commands = commandsFor(vehicle, fastMount),
            )
        }
        if (fastMount && vehicle.programMode) exitProgramModeOnce()
    }

    // Быстрый монтаж: блок в режиме программирования не охраняет — выводим один раз (старое приложение делало так же).
    private fun exitProgramModeOnce() {
        if (_state.value.programExiting) return
        _state.update { it.copy(programExiting = true) }
        launchOnDevice { withContext(dispatchers.io) { programModeRepository.exit() } }
    }

    /** Нажатие (удержание) кнопки команды: опасные — через лист подтверждения. */
    fun onCommand(command: StateCommand) {
        val state = _state.value
        if (state.pending != null || state.loading) return
        if (state.commands.any { it.command == command && it.danger }) {
            _state.update { it.copy(dialog = CheckStateDialog.Confirm(command)) }
        } else {
            send(command)
        }
    }

    /** Удержание 2 с в листе подтверждения. */
    fun onConfirm() {
        val dialog = _state.value.dialog as? CheckStateDialog.Confirm ?: return
        _state.update { it.copy(dialog = null) }
        send(dialog.command)
    }

    /** «Повторить» в снэкбаре — без повторного подтверждения: его уже дали. */
    fun onRetry(command: StateCommand) {
        if (_state.value.pending != null) return
        send(command)
    }

    fun onPinSubmit(pin: String) {
        val dialog = _state.value.dialog as? CheckStateDialog.Pin ?: return
        // PIN уже отправляется — повторный тап не запускает второй вход в режим программирования.
        if (dialog.pin.sending || _state.value.pending != null) return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        enterProgramMode(servicePin)
    }

    fun onDialogDismiss() {
        _state.update { state ->
            val sending = (state.dialog as? CheckStateDialog.Pin)?.pin?.sending == true
            if (sending) state else state.copy(dialog = null)
        }
    }

    /**
     * «Назад». Быстрый монтаж — «Вернуться на предыдущий шаг?»; расширенный — предупреждение, если блок оставлен в
     * режиме ТО или программирования. Возвращает, можно ли уйти сразу.
     */
    fun onBackRequest(): Boolean {
        val vehicle = _state.value.vehicle
        val dialog = when {
            fastMount -> CheckStateDialog.FastBack

            vehicle != null && (vehicle.serviceMode || vehicle.programMode) ->
                CheckStateDialog.Exit(service = vehicle.serviceMode, program = vehicle.programMode)

            else -> null
        }
        _state.update { it.copy(dialog = dialog ?: it.dialog) }
        return dialog == null
    }

    /** «Выключить и выйти»: выключаем режимы и уходим, даже если блок не ответил. */
    fun onExitTurnOff() {
        val dialog = _state.value.dialog as? CheckStateDialog.Exit ?: return
        _state.update { it.copy(dialog = null) }
        val job = launchOnDevice {
            // Уходим и когда связь оборвалась посреди команд (работу с блоком отменяет обрыв).
            try {
                withContext(dispatchers.io) {
                    if (dialog.service) {
                        withTimeoutOrNull(COMMAND_TIMEOUT) {
                            repository.send(StateCommand.SERVICE_MODE_OFF)
                        }
                    }
                    if (dialog.program) withTimeoutOrNull(COMMAND_TIMEOUT) { programModeRepository.exit() }
                }
            } finally {
                _effect.tryEmit(CheckStateEffect.Close)
            }
        }
        if (job == null) _effect.tryEmit(CheckStateEffect.Close)
    }

    /** Быстрый монтаж, «Далее»: [notDone] заданий чек-листа не закрыто из [total]. */
    fun onNext(notDone: Int, total: Int) {
        if (notDone > 0) {
            _state.update { it.copy(dialog = CheckStateDialog.FastFinish(notDone, total)) }
        } else {
            _effect.tryEmit(CheckStateEffect.Next)
        }
    }

    fun onFinishConfirm() {
        _state.update { it.copy(dialog = null) }
        _effect.tryEmit(CheckStateEffect.Next)
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
            _state.update { it.copy(pending = command, statuses = it.statuses + (command to CommandStatus.Sending)) }
            val reply = withTimeoutOrNull(COMMAND_TIMEOUT) { withContext(dispatchers.io) { block() } }
            finish(command, reply.toOutcome())
        }
        if (job == null) finish(command, CommandOutcome.NO_LINK)
    }

    private fun enterProgramMode(pin: ServicePin?) {
        val systemId = _state.value.deviceId ?: return
        val command = StateCommand.PROGRAM_MODE_ON
        val job = launchOnDevice {
            val dialog = _state.value.dialog as? CheckStateDialog.Pin
            _state.update {
                it.copy(
                    pending = command,
                    statuses = it.statuses + (command to CommandStatus.Sending),
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

    private fun showPin(previous: CheckStateDialog.Pin?, wrong: Boolean) {
        val count = (previous?.pin?.wrongCount ?: 0) + if (wrong) 1 else 0
        _state.update {
            it.copy(
                pending = null,
                statuses = it.statuses - StateCommand.PROGRAM_MODE_ON,
                dialog = CheckStateDialog.Pin(ServicePinDialogState(wrong = wrong, wrongCount = count)),
            )
        }
    }

    private fun finish(command: StateCommand, outcome: CommandOutcome) {
        val status = finished(outcome)
        _state.update {
            it.copy(
                pending = null,
                statuses = it.statuses + (command to status),
                dialog = it.dialog.takeUnless { dialog -> dialog is CheckStateDialog.Pin },
            )
        }
        _effect.tryEmit(CheckStateEffect.CommandFinished(command, outcome))
        // Итог на кнопке — несколько секунд, потом кнопка возвращается в покой (если итог не сменился новым).
        safeLaunch {
            delay(RESULT_VISIBLE)
            _state.update { state ->
                if (state.statuses[command] == status) state.copy(statuses = state.statuses - command) else state
            }
        }
    }

    private fun finished(outcome: CommandOutcome) = CommandStatus.Finished(outcome, resultIds.updateAndGet { it + 1 })

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
        const val STOP_TIMEOUT_MILLIS = 5_000L
        val STALE_AFTER = 10.seconds
        val TICK = 1.seconds
        val COMMAND_TIMEOUT = 15.seconds
        val RESULT_VISIBLE = 3.seconds
    }
}

private fun changedZones(previous: VehicleState, current: VehicleState): Set<Zone> =
    Zone.entries.filterTo(mutableSetOf()) { previous.zone(it) != current.zone(it) }
