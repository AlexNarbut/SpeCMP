package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone

import androidx.lifecycle.viewModelScope
import kotlin.time.Duration
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.CloneRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.pin.ServicePinDialogState
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel.BaseDeviceViewModel

/**
 * Экран «Процедура Clone»: попытки → подтверждение → [CloneRepository.run] по этапам → итог.
 *
 * Обрыв связи на процессе: работа отменяется ([launchOnDevice]), экран возвращается на старт с причиной (D2): до
 * вычисления на сервере попытка точно цела, позже — могла списаться, счётчик перечитывается. Неверный PIN (BLE) — из
 * потока SDK: диалог PIN, повтор с введённым PIN. На этапах записи выход запрещён.
 */
class CloneViewModel(
    deviceSession: DeviceSession,
    private val repository: CloneRepository,
    private val pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers,
    fromBypass: Boolean
) : BaseDeviceViewModel(deviceSession) {

    private val _state = MutableStateFlow(CloneState(fromBypass = fromBypass))

    private var procedure: Job? = null

    // Остаток блокировки PIN, пока диалог PIN на экране (целые секунды).
    private val pinLock: Flow<Duration?> =
        pinCooldown.lockedFor(_state.map { state -> state.deviceId.takeIf { state.dialog is CloneDialog.Pin } })

    val state: StateFlow<CloneState> = combine(_state, pinLock) { state, lock ->
        val dialog = state.dialog
        if (dialog is CloneDialog.Pin) {
            state.copy(
                dialog = dialog.copy(pin = dialog.pin.copy(lockedFor = lock))
            )
        } else {
            state
        }
    }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), _state.value)

    init {
        onEachDeviceReady(::onDeviceReady)
        onDeviceLost(::onConnectionLost)
    }

    private suspend fun onDeviceReady(device: AlarmDevice) {
        _state.update {
            it.copy(
                deviceName = device.systemName,
                deviceId = device.systemId,
                usb = deviceSession.transport.value != ConnectionTransport.BLE
            )
        }
        refreshAttempts()
    }

    private suspend fun refreshAttempts() {
        val attempts = withContext(dispatchers.io) { repository.attempts() }
        _state.update { it.copy(attempts = attempts?.let(CloneAttempts::Known) ?: CloneAttempts.Unknown) }
    }

    private fun onConnectionLost() {
        _state.update { state ->
            val running = state.phase as? ClonePhase.Running ?: return@update state
            val interruption = if (running.stage.ordinal < CloneStage.CALCULATING.ordinal) {
                CloneInterruption.ATTEMPT_KEPT
            } else {
                CloneInterruption.CHECK_ATTEMPTS
            }
            state.copy(phase = ClonePhase.Start(interruption), dialog = null)
        }
    }

    /** «Начать Clone» / «Повторить»: сначала подтверждение с числом попыток. */
    fun onStartClick() {
        _state.update {
            if (it.canStart &&
                it.phase !is ClonePhase.Running
            ) {
                it.copy(dialog = CloneDialog.Confirm)
            } else {
                it
            }
        }
    }

    fun onConfirm() {
        _state.update { it.copy(dialog = null) }
        run()
    }

    /** «Продолжить» в диалоге PIN; значение живёт только в вызове — в state и логи не попадает. */
    fun onPinSubmit(pin: String) {
        val dialog = _state.value.dialog as? CloneDialog.Pin ?: return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        val systemId = _state.value.deviceId ?: return
        if (pinCooldown.remainingNow(systemId) > Duration.ZERO) return
        _state.update { it.copy(dialog = CloneDialog.Pin(dialog.pin.copy(sending = true, wrong = false))) }
        launchOnDevice {
            when (withContext(dispatchers.io) { deviceSession.setServicePin(servicePin) }) {
                is Response.Error -> _state.update { it.copy(dialog = CloneDialog.Pin(dialog.pin)) }

                is Response.Success -> {
                    _state.update { it.copy(dialog = null) }
                    run(pinTries = dialog.pin.wrongCount)
                }
            }
        }
    }

    fun onDialogDismiss() {
        _state.update { state ->
            val sending = (state.dialog as? CloneDialog.Pin)?.pin?.sending == true
            if (sending) state else state.copy(dialog = null)
        }
    }

    /**
     * «Назад»: на процессе — «Прервать?» (на этапах записи — ничего), иначе выход. Возвращает, можно ли уйти сразу.
     */
    fun onBackRequest(): Boolean {
        val state = _state.value
        if (state.phase !is ClonePhase.Running) return true
        if (!state.locked) _state.update { it.copy(dialog = CloneDialog.Exit) }
        return false
    }

    /** «Да» в «Прервать?»: процедура отменяется (если запись ещё не началась). */
    fun onExitConfirm() {
        if (_state.value.locked) return
        procedure?.cancel()
        _state.update { it.copy(phase = ClonePhase.Start(), dialog = null) }
    }

    /** [pinTries] — сколько раз PIN уже запрашивали в этой попытке (0 — PIN не вводили). */
    private fun run(pinTries: Int = 0) {
        procedure?.cancel()
        procedure = launchOnDevice {
            _state.update { it.copy(phase = ClonePhase.Running(CloneStage.PREPARING)) }
            repository.run().flowOn(dispatchers.io).collect { event -> onEvent(event, pinTries) }
        }
    }

    private suspend fun onEvent(event: CloneEvent, pinTries: Int) {
        when (event) {
            is CloneEvent.Stage -> _state.update { it.copy(phase = ClonePhase.Running(event.stage, event.inQueue)) }

            is CloneEvent.Finished -> finish(event.result)

            CloneEvent.WrongPin -> {
                _state.value.deviceId?.let(pinCooldown::start)
                // Неверный — только введённый PIN; молчаливая попытка SDK (сохранённый или 1111) — просто запрос PIN.
                _state.update {
                    it.copy(
                        phase = ClonePhase.Start(),
                        dialog = CloneDialog.Pin(
                            ServicePinDialogState(wrong = pinTries > 0, wrongCount = pinTries + 1)
                        )
                    )
                }
            }

            // Сбой при живой связи — ошибка; при обрыве экран уже вернулся на старт (onConnectionLost).
            CloneEvent.Failure -> if (currentDevice != null) finish(CloneResult.Failed(CloneFailure.UNKNOWN))
        }
    }

    private suspend fun finish(result: CloneResult) {
        _state.update { it.copy(phase = ClonePhase.Finished(result)) }
        refreshAttempts()
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
