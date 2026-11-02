package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass

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
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.BypassLearningRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntryResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.pin.ServicePinDialogState
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel.BaseDeviceViewModel

/**
 * Экран «Бесключевой обход»: вход в режим программирования ([ProgramModeEntry], PIN по BLE) → шаги
 * [BypassLearningRepository.learn] → итог. Обрыв связи на шагах — работа отменяется ([launchOnDevice]), экран
 * возвращается на старт с сообщением «Процедура прервана». Повтор — тот же путь заново.
 */
class BypassLearningViewModel(
    deviceSession: DeviceSession,
    private val repository: BypassLearningRepository,
    private val programMode: ProgramModeEntry,
    pinCooldown: ServicePinCooldown,
    private val dispatchers: AppDispatchers
) : BaseDeviceViewModel(deviceSession) {

    private val _state = MutableStateFlow(BypassState())

    private var learning: Job? = null

    // Остаток блокировки PIN, пока диалог PIN на экране (целые секунды).
    private val pinLock: Flow<Duration?> =
        pinCooldown.lockedFor(_state.map { state -> state.deviceId.takeIf { state.dialog is BypassDialog.Pin } })

    val state: StateFlow<BypassState> = combine(_state, pinLock) { state, lock ->
        val dialog = state.dialog
        if (dialog is BypassDialog.Pin) {
            state.copy(dialog = dialog.copy(pin = dialog.pin.copy(lockedFor = lock)))
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

    private fun onDeviceReady(device: AlarmDevice) {
        _state.update {
            it.copy(
                deviceName = device.systemName,
                deviceId = device.systemId,
                usb = deviceSession.transport.value != ConnectionTransport.BLE,
                canCode = device.can?.code,
                cloneAvailable = device.features?.contains(DeviceFeature.CLONE) == true
            )
        }
    }

    private fun onConnectionLost() {
        _state.update { state ->
            if (state.phase is BypassPhase.Running) {
                state.copy(
                    phase = BypassPhase.Start(interrupted = true),
                    dialog = null
                )
            } else {
                state
            }
        }
    }

    /** «Начать обучение» / «Повторить»: вход в режим программирования без PIN (SDK пробует сохранённый или 1111). */
    fun onStartClick() {
        if (_state.value.phase is BypassPhase.Running) return
        start(pin = null)
    }

    /** «Продолжить» в диалоге PIN; значение живёт только в вызове — в state и логи не попадает. */
    fun onPinSubmit(pin: String) {
        if (_state.value.dialog !is BypassDialog.Pin) return
        val servicePin = ServicePin.parseOrNull(pin) ?: return
        start(servicePin)
    }

    fun onDialogDismiss() {
        _state.update { state ->
            val sending = (state.dialog as? BypassDialog.Pin)?.pin?.sending == true
            if (sending) state else state.copy(dialog = null)
        }
    }

    /** «Назад»: во время обучения — «Прервать?», иначе выход. Возвращает, можно ли уйти сразу. */
    fun onBackRequest(): Boolean {
        if (_state.value.phase !is BypassPhase.Running) return true
        _state.update { it.copy(dialog = BypassDialog.Exit) }
        return false
    }

    /** «Да» в «Прервать?»: обучение отменяется. */
    fun onExitConfirm() {
        learning?.cancel()
        _state.update { it.copy(phase = BypassPhase.Start(), dialog = null) }
    }

    private fun start(pin: ServicePin?) {
        val systemId = _state.value.deviceId ?: return
        learning?.cancel()
        learning = launchOnDevice {
            val dialog = _state.value.dialog as? BypassDialog.Pin
            _state.update {
                if (dialog != null) {
                    it.copy(dialog = BypassDialog.Pin(dialog.pin.copy(sending = true, wrong = false)))
                } else {
                    it.copy(phase = BypassPhase.Running(), dialog = null)
                }
            }
            when (withContext(dispatchers.io) { programMode.enter(systemId, pin) }) {
                ProgramModeEntryResult.ENTERED -> learn()

                ProgramModeEntryResult.PIN_LOCKED -> showPin(dialog, wrong = false)

                // Неверный — только введённый PIN; молчаливая попытка SDK (сохранённый или 1111) — просто запрос PIN.
                ProgramModeEntryResult.WRONG_PIN -> showPin(dialog, wrong = pin != null)

                ProgramModeEntryResult.FAILED -> finishIfConnected(BypassResult.Error(BypassError.START_FAILED))
            }
        }
    }

    private fun showPin(previous: BypassDialog.Pin?, wrong: Boolean) {
        val count = (previous?.pin?.wrongCount ?: 0) + if (wrong) 1 else 0
        _state.update {
            it.copy(
                phase = BypassPhase.Start(),
                dialog = BypassDialog.Pin(ServicePinDialogState(wrong = wrong, wrongCount = count))
            )
        }
    }

    private suspend fun learn() {
        _state.update { it.copy(phase = BypassPhase.Running(), dialog = null) }
        repository.learn().flowOn(dispatchers.io).collect { event ->
            when (event) {
                is BypassEvent.Step -> _state.update { it.copy(phase = BypassPhase.Running(event.step)) }
                is BypassEvent.Finished -> _state.update { it.copy(phase = BypassPhase.Finished(event.result)) }
                BypassEvent.Failure -> finishIfConnected(BypassResult.Error(BypassError.START_FAILED))
            }
        }
    }

    // Сбой при живой связи — ошибка; при обрыве экран уже вернулся на старт (onConnectionLost).
    private fun finishIfConnected(result: BypassResult) {
        if (currentDevice == null) return
        _state.update { it.copy(phase = BypassPhase.Finished(result), dialog = null) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
